package com.khoben.samples.studyar.ImageProcessing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ObjectPool<T> {
    protected long expirationTime;

    protected Map<T, Long> unlocked;

    public ObjectPool() {
        expirationTime = 30000; // 30 sec
        unlocked = new ConcurrentHashMap<>();
    }

    protected abstract T create(T l);

    public abstract boolean validate(T l);

    public synchronized T checkOut(T l) {
        long now = System.currentTimeMillis();
        T t;
        if (unlocked.size() > 0) {
            for (T t1 : unlocked.keySet()) {
                t = t1;
                if ((now - unlocked.get(t)) > expirationTime) {
                    unlocked.remove(t);
                    t = null;
                } else {
                    if (validate(l)) {
                        return t;
                    } else {
                        t = null;
                    }
                }
            }
        }

        t = create(l);
        unlocked.put(t, now);
        return t;
    }
}