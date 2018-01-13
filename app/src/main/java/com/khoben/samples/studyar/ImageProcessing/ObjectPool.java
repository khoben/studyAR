package com.khoben.samples.studyar.ImageProcessing;

import android.util.Log;

import com.khoben.samples.studyar.Lesson;

import java.util.Enumeration;
import java.util.Hashtable;

public abstract class ObjectPool<T> {
    protected long expirationTime;

    protected Hashtable<T, Long> locked, unlocked;

    public ObjectPool() {
        expirationTime = 30000; // 30 sec
        locked = new Hashtable<>();
        unlocked = new Hashtable<>();
    }

    protected abstract T create(Lesson l);

    public abstract boolean validate(Lesson l);

    public synchronized T checkOut(Lesson l) {
        long now = System.currentTimeMillis();
        T t;
        if (unlocked.size() > 0) {
            Enumeration<T> e = unlocked.keys();
            while (e.hasMoreElements()) {
                t = e.nextElement();
                if ((now - unlocked.get(t)) > expirationTime) {
                    unlocked.remove(t);
                    t = null;
                } else {
                    if (validate(l)) {
                        return (t);
                    } else {
                        t = null;
                    }
                }
            }
        }

        t = create(l);
        unlocked.put(t, now);
        return (t);
    }
}