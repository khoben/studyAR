package com.khoben.samples.studyar.AR;


import android.util.Log;

import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.StorageType;


public class ARUtils {
    private final static String TAG = "ARUtils";

    public static void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, (target1, status) -> Log.i(TAG, String.format("load target (%b): %s (%d)", status, target1.name(), target1.runtimeID())));
    }

    public static void loadFromJsonFile(ImageTracker tracker, String path, String targetname) {
        ImageTarget target = new ImageTarget();
        target.setup(path, StorageType.Assets, targetname);
        tracker.loadTarget(target, (target1, status) -> Log.i(TAG, String.format("load target (%b): %s (%d)", status, target1.name(), target1.runtimeID())));
    }

    public static void loadAllFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, (target1, status) -> Log.i(TAG, String.format("load target (%b): %s (%d)", status, target1.name(), target1.runtimeID())));
        }
    }
}
