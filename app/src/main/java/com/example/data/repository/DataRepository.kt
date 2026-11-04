package com.example.data.repository

import android.content.Context
import com.example.data.model.CourseProgress
import com.example.data.model.RecitationProgress
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * DataRepository for handling student recitation progress, course progress,
 * and curriculum milestones in Cloud Firestore.
 *
 * Implements zero-trust security invariants:
 * - Direct fail-fast accessor for authenticated caller
 * - Subcollection / User scoping (users/{studentId}/recitations and users/{studentId}/courses)
 * - Structured JSON error logging via handleFirestoreError
 * - ServerTimestamp and null-safe deserialization
 */
class DataRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val auth = Firebase.auth

    /**
     * Resolves the current authenticated user's ID or throws an explicit IllegalStateException.
     */
    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً للوصول إلى قاعدة بيانات التسميع.")
    }

    // =========================================================================
    // 1. Recitation Progress Operations (قراءة وكتابة سجلات التسميع)
    // =========================================================================

    /**
     * Observes real-time recitation progress entries for a given student (or current student if omitted).
     */
    fun observeStudentRecitations(targetStudentId: String? = null): Flow<List<RecitationProgress>> = flow {
        val uid = targetStudentId ?: requireUserId()
        val path = "users/$uid/recitations"

        emitAll(
            db.collection("users")
                .document(uid)
                .collection("recitations")
                .orderBy("recordedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(RecitationProgress::class.java)
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    /**
     * Writes/adds a new recitation progress record for a student.
     */
    suspend fun addRecitationProgress(
        progress: RecitationProgress,
        targetStudentId: String? = null
    ): String {
        val uid = targetStudentId ?: requireUserId()
        val path = "users/$uid/recitations"
        val collectionRef = db.collection("users").document(uid).collection("recitations")

        val payload = hashMapOf<String, Any?>(
            "studentId" to uid,
            "studentName" to progress.studentName,
            "studentCode" to progress.studentCode,
            "surahNumber" to progress.surahNumber,
            "surahName" to progress.surahName,
            "startAyah" to progress.startAyah,
            "endAyah" to progress.endAyah,
            "recitationType" to progress.recitationType,
            "rating" to progress.rating,
            "riwayah" to progress.riwayah,
            "teacherId" to (progress.teacherId.ifBlank { auth.currentUser?.uid ?: "" }),
            "teacherName" to progress.teacherName,
            "notes" to progress.notes,
            "sessionDurationMinutes" to progress.sessionDurationMinutes,
            "isPassed" to progress.isPassed,
            "recordedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            val docRef = collectionRef.add(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    /**
     * Updates an existing recitation progress record.
     */
    suspend fun updateRecitationProgress(
        recitationId: String,
        progress: RecitationProgress,
        targetStudentId: String? = null
    ) {
        val uid = targetStudentId ?: requireUserId()
        require(recitationId.isNotBlank()) { "معرف سجل التسميع لا يمكن أن يكون فارغاً" }
        val docRef = db.collection("users").document(uid).collection("recitations").document(recitationId)

        val updates = hashMapOf<String, Any?>(
            "surahNumber" to progress.surahNumber,
            "surahName" to progress.surahName,
            "startAyah" to progress.startAyah,
            "endAyah" to progress.endAyah,
            "recitationType" to progress.recitationType,
            "rating" to progress.rating,
            "riwayah" to progress.riwayah,
            "notes" to progress.notes,
            "isPassed" to progress.isPassed,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // =========================================================================
    // 2. Course & Program Progress Operations (متابعة البرامج والدورات)
    // =========================================================================

    /**
     * Observes courses and program tracks enrolled by a student in real-time.
     */
    fun observeStudentCourses(targetStudentId: String? = null): Flow<List<CourseProgress>> = flow {
        val uid = targetStudentId ?: requireUserId()
        val path = "users/$uid/courses"

        emitAll(
            db.collection("users")
                .document(uid)
                .collection("courses")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CourseProgress::class.java)
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    /**
     * Saves or updates a student's milestone progress within a course.
     */
    suspend fun saveCourseProgress(
        course: CourseProgress,
        targetStudentId: String? = null
    ) {
        val uid = targetStudentId ?: requireUserId()
        val courseId = course.programId.ifBlank { "course_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("courses").document(courseId)

        val payload = hashMapOf<String, Any?>(
            "studentId" to uid,
            "programId" to course.programId,
            "programTitle" to course.programTitle,
            "completedHalaqat" to course.completedHalaqat,
            "totalHalaqat" to course.totalHalaqat,
            "memorizedJuzCount" to course.memorizedJuzCount,
            "currentSurah" to course.currentSurah,
            "status" to course.status,
            "sanadEligible" to course.sanadEligible,
            "lastSessionAt" to FieldValue.serverTimestamp(),
            "enrolledAt" to (course.enrolledAt ?: FieldValue.serverTimestamp())
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
