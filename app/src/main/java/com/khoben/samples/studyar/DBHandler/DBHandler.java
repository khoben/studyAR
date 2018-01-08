package com.khoben.samples.studyar.DBHandler;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class DBHandler {
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
