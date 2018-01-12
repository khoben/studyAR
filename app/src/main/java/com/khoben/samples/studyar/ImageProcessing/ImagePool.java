package com.khoben.samples.studyar.ImageProcessing;


import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.AR.Render.TextureHelper;
import com.khoben.samples.studyar.DatabaseHelper.FirebaseHelper;
import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;

import java.util.Enumeration;

public class ImagePool extends ObjectPool<Pair<Lesson, Bitmap>> {

    private ImageProcessing imageProcessing;

    public ImagePool(MainActivity mainActivity){
        super();
        imageProcessing = new ImageProcessing(mainActivity);
    }


    @Override
    protected Pair<Lesson, Bitmap> create(Lesson l) {
        return new Pair<>(l,imageProcessing.generateBitmap(l));
    }

    @Override
    public boolean validate(Lesson l) {
        Enumeration e = unlocked.keys();
        while (e.hasMoreElements()){
            Pair<Lesson, Bitmap> l1 = (Pair<Lesson, Bitmap>) e.nextElement();
            if (l1.first.getAud().equals(l.getAud())){
                return true;
            }
        }
        return false;
    }
}
