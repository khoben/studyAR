package com.khoben.samples.studyar.ImageProcessing;

import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;

import java.util.Enumeration;


public class ImagePool extends ObjectPool<Lesson> {

    public static final String TAG = "ImagePool";

    private ImageProcessing imageProcessing;

    public ImagePool(MainActivity mainActivity){
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
        Enumeration e = unlocked.keys();
        while (e.hasMoreElements()){
            Lesson l1 = (Lesson) e.nextElement();
            if (l1.getAud().equals(l.getAud())){
                return true;
            }
        }
        return false;
    }

    public Lesson findExistingLesson(String aud){
        Enumeration e = unlocked.keys();
        while (e.hasMoreElements()){
            Lesson l1 = (Lesson) e.nextElement();
            if (l1.getAud().equals(aud)){
                return l1;
            }
        }
        return null;
    }
}
