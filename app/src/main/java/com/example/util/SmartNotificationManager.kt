package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/**
 * مدير الإشعارات الذكية لمقرأة الإمام الجزري:
 * يرسل تنبيهات فورية حقيقية إلى شريط إشعارات الجوال (Android Notification Panel):
 * 1. تنبيهات حلقات التسميع والإقراء المباشرة.
 * 2. إشعارات قبول واعتماد استمارة الطالب الجديد.
 * 3. إشعارات الإدارة بالطلبات والتسجيلات الجديدة.
 * 4. تذكير الورد القرآني اليومي وأذكار التلاوة.
 * 5. الإعلانات العامة والتنبيهات العاجلة من المشرف العام.
 */
object SmartNotificationManager {

    const val TAG = "SmartNotification"

    // قنوات الإشعارات (Notification Channels)
    const val CHANNEL_ID_HALAQAT = "maqraah_halaqat_channel"
    const val CHANNEL_ID_ALERTS = "maqraah_alerts_channel"
    const val CHANNEL_ID_WIRD = "maqraah_wird_channel"
    const val CHANNEL_ID_ADMIN = "maqraah_admin_channel"

    private var channelsCreated = false

    /**
     * إنشاء قنوات التنبيه بنظام أندرويد (تتطلب أندرويد 8+)
     */
    fun initChannels(context: Context) {
        if (channelsCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // 1. قناة حلقات التسميع المباشرة
            val halaqatChannel = NotificationChannel(
                CHANNEL_ID_HALAQAT,
                "حلقات التسميع والإقراء المباشرة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات فورية عند بدء جلسات التسميع المباشرة ودخول المقرئ للحلقة"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // 2. قناة تنبيهات المقرأة والطلاب
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "تنبيهات المقرأة واعتماد الاستمارات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات قبول تسجيل الطلاب وشحن الدقائق والإعلانات العامة"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // 3. قناة الورد القرآني اليومي
            val wirdChannel = NotificationChannel(
                CHANNEL_ID_WIRD,
                "الورد القرآني وتذكير الحفظ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير يومي بمراجعة وتثبيت المحفوظ وتلاوة القرآن الكريم"
                enableVibration(true)
                setShowBadge(true)
            }

            // 4. قناة إشعارات الإدارة العامة
            val adminChannel = NotificationChannel(
                CHANNEL_ID_ADMIN,
                "إشعارات الإدارة والمشرف العام",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات فورية للمشرف العام عند تسجيل طالب جديد أو إرسال إشعار تحويل"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(halaqatChannel, alertsChannel, wirdChannel, adminChannel)
            )
            channelsCreated = true
            Log.d(TAG, "Notification channels successfully initialized.")
        }
    }

    /**
     * التحقق من توفر إذن الإشعارات (مطلوب لأندرويد 13 فما فوق)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * إرسال إشعار عام إلى لوحة إشعارات النظام (System Notification Panel)
     */
    fun sendNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        subText: String? = "مقرأة الإمام الجزري"
    ) {
        initChannels(context)

        // التحقق من الصلاحية في أندرويد 13+
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted, skipping notification $notificationId")
            return
        }

        try {
            // نية فتح التطبيق عند النقر على الإشعار من لوحة الإشعارات
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("from_notification", true)
                putExtra("notification_id", notificationId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setSubText(subText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Notification $notificationId posted to notification panel.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification to tray", e)
        }
    }

    // =========================================================================
    // أحداث الإشعارات الذكية التلقائية والتجريبية
    // =========================================================================

    /**
     * إشعار للطالب عند تسجيل استمارته بنجاح
     */
    fun notifyStudentRegistered(context: Context, studentName: String, programName: String) {
        sendNotification(
            context = context,
            notificationId = 101,
            channelId = CHANNEL_ID_ALERTS,
            title = "أهلاً بك في مقرأة الإمام الجزري 📖",
            message = "مرحباً يا $studentName، تم استلام استمارة تسجيلك في ($programName) بنجاح وجاري ربطك بالحلقة."
        )
    }

    /**
     * إشعار للمشرف العام عند تسجيل طالب جديد في المقرأة
     */
    fun notifyAdminNewStudent(context: Context, studentName: String, programName: String) {
        sendNotification(
            context = context,
            notificationId = 201,
            channelId = CHANNEL_ID_ADMIN,
            title = "تسجيل طالب جديد في المقرأة 👤",
            message = "تم تسجيل الطالب ($studentName) في مسار ($programName). انقر لمراجعة واعتماد الطلب في لوحة الإدارة.",
            subText = "لوحة تحكم الإدارة"
        )
    }

    /**
     * إشعار عند بدء حلقة تسميع مباشرة (Live Halaqah)
     */
    fun notifyHalaqahLive(context: Context, halaqahTitle: String, teacherName: String) {
        sendNotification(
            context = context,
            notificationId = 301,
            channelId = CHANNEL_ID_HALAQAT,
            title = "بدأت الآن: $halaqahTitle 🎙️",
            message = "جلسة التسميع والإقراء مباشرة الآن بإشراف $teacherName. انقر للدخول إلى الحلقة."
        )
    }

    /**
     * إشعار تذكير الورد القرآني اليومي
     */
    fun notifyDailyWirdReminder(context: Context, wirdName: String = "تلاوة ومراجعة سورة البقرة وتثبيت المحفوظ") {
        sendNotification(
            context = context,
            notificationId = 401,
            channelId = CHANNEL_ID_WIRD,
            title = "حان موعد وردك القرآني اليومي 🕌",
            message = "«خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ» - لا تنسَ نصيبك من $wirdName اليوم."
        )
    }

    /**
     * إشعار الإعلانات العامة
     */
    fun notifyAnnouncement(context: Context, title: String, content: String) {
        sendNotification(
            context = context,
            notificationId = (1000..9999).random(),
            channelId = CHANNEL_ID_ALERTS,
            title = "إعلان جديد: $title 📢",
            message = content
        )
    }

    /**
     * زر الفحص الفوري: إرسال إشعار تجريبي في لوحة الإشعارات
     */
    fun sendTestNotification(context: Context, label: String = "إشعار تجريبي ناجح") {
        sendNotification(
            context = context,
            notificationId = (5000..9000).random(),
            channelId = CHANNEL_ID_ALERTS,
            title = "مقرأة الإمام الجزري - تنبيه شريط الإشعارات 🔔",
            message = "الإشعارات الذكية تعمل بنجاح على جوالك الآن! ستصلك تنبيهات الحلقات المباشرة والتسجيلات وورد التلاوة أولاً بأول."
        )
    }
}
