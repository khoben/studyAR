package com.khoben.samples.studyar.DatabaseHelper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseHelper {
    static public DatabaseReference timetableReference;
    static public DatabaseReference titleReference;
    static public FirebaseDatabase firebaseDatabase;

    public static void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);

        titleReference = firebaseDatabase.getReference().child("title");
        timetableReference = firebaseDatabase.getReference().child("timetable");

        titleReference.keepSynced(true);
        timetableReference.keepSynced(true);
    }
}
