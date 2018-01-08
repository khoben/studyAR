package com.khoben.samples.studyar.TimetableManager;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.DBHandler.DBHandler;
import com.khoben.samples.studyar.Lesson;

/**
 * Created by extless on 07.01.2018.
 */

public class TimetableManager {
    private static Lesson lesson;
    private final String TAG = "TimetableManager";

    public TimetableManager() {
        lesson = null;
    }


    public Lesson getLessonByAud(String aud) {
        DBHandler.timetableReference.child(aud).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lesson = dataSnapshot.getValue(Lesson.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage().toString());
            }
        });

        return lesson;
    }
}
