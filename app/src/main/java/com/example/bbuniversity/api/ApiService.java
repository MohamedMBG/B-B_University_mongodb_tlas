package com.example.bbuniversity.api;

import com.example.bbuniversity.models.Abscence;
import com.example.bbuniversity.models.ClassInfo;
import com.example.bbuniversity.models.Classe;
import com.example.bbuniversity.models.Complaint;
import com.example.bbuniversity.models.DashboardResponse;
import com.example.bbuniversity.models.Etudiant;
import com.example.bbuniversity.models.Matiere;
import com.example.bbuniversity.models.Note;
import com.example.bbuniversity.models.Professeur;
import com.example.bbuniversity.models.TimetableResponse;
import com.example.bbuniversity.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/classes/{classId}/timetables")
    Call<Map<String, Object>> getTimetable(@Path("classId") String classId);



    @GET("api/matieres")
    Call<List<Matiere>> getMatieres();

    @GET("api/students/{studentId}/complaints")
    Call<List<Complaint>> getComplaints(@Path("studentId") String studentId);

    @POST("api/users")
    Call<Void> createUser(@Body Map<String, Object> userData);

    @PUT("api/users/{id}")
    Call<Void> updateUser(@Path("id") String id, @Body Map<String, Object> updates);

    //classes et matieres
    @POST("api/classes")
    Call<Void> createClass(@Body Map<String, Object> classData);

    @POST("api/matieres")
    Call<Void> createMatiere(@Body Map<String, Object> matiereData);

    //admin dashboard

    @GET("api/admin/dashboard")
    Call<DashboardResponse> getAdminDashboard();

    @POST("api/notes")
    Call<Note> createOrUpdateNote(@Body Note note);

    @GET("api/students/{studentId}/notes")
    Call<List<Note>> getNotesByStudent(@Path("studentId") String studentId);


    @GET("api/users/{id}")
    Call<User> getUser(@Path("id") String id);


    @GET("api/students/{studentId}/notes")
    Call<List<Note>> getStudentSubjects(@Path("studentId") String studentId);

    @POST("api/complaints")
    Call<Complaint> createComplaint(@Body Complaint complaint);

    @GET("api/students")
    Call<List<Etudiant>> getStudents();

    @GET("api/classes/{classId}/students")
    Call<List<Etudiant>> getStudentsByClass(@Path("classId") String classId);

    @GET("api/users")
    Call<List<Professeur>> getProfessors(@Query("role") String role);

    @GET("api/students/{studentId}/absences")
    Call<List<Abscence>> getStudentAbsences(@Path("studentId") String studentId);

    // Justifier une absence + ajuster la note côté serveur
    @POST("api/students/{studentId}/absences/{absenceId}/justify")
    Call<Void> justifyAbsence(
            @Path("studentId") String studentId,
            @Path("absenceId") String absenceId
    );

    @GET("api/students/{studentId}/absences")
    Call<List<Abscence>> getAbsences(@Path("studentId") String studentUserId);

    @GET("api/users/{id}")
    Call<Professeur> getProfessorById(@Path("id") String id);

    @PUT("api/complaints/{id}/accept")
    Call<Void> acceptComplaint(
            @Path("id") String complaintId,
            @Body Map<String, Object> body   // { "modifiedGrade": 15.5 }
    );

    @PUT("api/complaints/{id}/reject")
    Call<Void> rejectComplaint(
            @Path("id") String complaintId,
            @Body Map<String, Object> body   // { "response": "..." }
    );

    @GET("api/complaints")
    Call<List<Complaint>> getComplaintsForTeacher(@Query("teacherId") String teacherId);

    @POST("api/students/{studentId}/absences")
    Call<Abscence> createAbsence(
            @Path("studentId") String studentUserId,
            @Body Abscence absence
    );

    @POST("api/students/{studentId}/absences")
    Call<Abscence> addAbsence(
            @Path("studentId") String studentId,
            @Body Abscence absence
    );

    // Sauvegarder / écraser l'emploi du temps d'une classe
    @POST("api/classes/{classId}/timetables")
    Call<Void> saveTimetable(
            @Path("classId") String classId,
            @Body Map<String, Object> body
    );

    // ➕ AJOUTER POUR LES CLASSES (liste depuis Mongo)
    @GET("api/classes")
    Call<List<Map<String, Object>>> getClasses();


    @GET("api/classes")
    Call<List<Classe>> getClassesMongo();

    @GET("api/classes/{classId}/timetables")
    Call<TimetableResponse> getTimetablee(@Path("classId") String classId);



}
