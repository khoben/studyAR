package com.khoben.samples.studyar.ImageProcessing;

import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;

import java.util.Enumeration;
import java.util.Iterator;


public class ImagePool extends ObjectPool<Lesson> {

    public static final String TAG = "ImagePool";

    private ImageProcessing imageProcessing;

    public ImagePool(MainActivity mainActivity) {
        super();
        imageProcessing = new ImageProcessing(mainActivity);
    }


    @Override
    protected Lesson create(Lesson l) {
        l.setBitmap(imageProcessing.generateBitmap(l));
        return l;
    }

    @Override
    public boolean validate(Lesson l) {
        for (Lesson l1 : unlocked.keySet()) {
            if (l1.getAud().equals(l.getAud())) {
                return true;
            }
        }
        return false;
    }

    public Lesson findExistingLesson(String aud) {
        for (Lesson l1 : unlocked.keySet()) {
            if (l1.getAud().equals(aud)) {
                return l1;
            }
        }
        return null;
    }
}
