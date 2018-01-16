package com.khoben.samples.studyar.ImageProcessing;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;
import com.khoben.samples.studyar.Rubberstamp.RubberStamp;
import com.khoben.samples.studyar.Rubberstamp.RubberStampConfig;
import com.khoben.samples.studyar.Rubberstamp.RubberStampPosition;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ImageProcessing {
    private static final String TAG = "ImageProcessing";

    private Bitmap mainBitmap;
    private Bitmap teacherProfileBitmap;
    private MainActivity mainActivity;
    private RubberStampConfig config;
    private RubberStamp rubberStamp;

    private final static String boldFontPath = "fonts/Roboto-Bold.ttf";
    private final static String regularFontPath = "fonts/Roboto-Regular.ttf";
    private final static String italicFontPath = "fonts/Roboto-Italic.ttf";
    private final static String lightItalicFontPath = "fonts/Roboto-LightItalic.ttf";
    private final int fontSize = 100;

    private final int marginLeft = 30;
    private final int marginTop = 30;

    private final int scaleBitmapFactor = 4;

    public ImageProcessing() {
    }

    public ImageProcessing(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        rubberStamp = new RubberStamp(mainActivity);
    }

    public int getStringWidth(String s, String fontPath) {
        AssetManager assetManager = mainActivity.getAssets();
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Typeface typeface = Typeface.createFromAsset(assetManager, fontPath);
        paint.setTypeface(typeface);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        Rect result = new Rect();
        paint.getTextBounds(s, 0, s.length(), result);
        return result.width();
    }

    public Bitmap generateBitmap(Lesson lesson) {

        if (lesson == null) {
            Log.w(TAG, "Lesson is NULL");
            return null;
        }

        Log.i(TAG, lesson.toString());

        Map<String, String> lessonClassStringFields = new ConcurrentSkipListMap<>();

        lessonClassStringFields.put(String.format("Аудитория №%s", lesson.getAud()), boldFontPath);
        lessonClassStringFields.put(lesson.getSubject(), regularFontPath);
        lessonClassStringFields.put(lesson.getFio(), regularFontPath);
        lessonClassStringFields.put(lesson.getDegree(), lightItalicFontPath);

        int maxWidth = 0;
        int cur;
        for (String string : lessonClassStringFields.keySet()) {
            cur = getStringWidth(string, lessonClassStringFields.get(string));
            if (cur > maxWidth)
                maxWidth = cur;
        }

        teacherProfileBitmap = getBitmapFromAsset(mainActivity, String.format("teachers/%s.jpg", lesson.getFio().split(" ")[0]));

        if (teacherProfileBitmap == null)
            return null;

        teacherProfileBitmap = Bitmap.createScaledBitmap(teacherProfileBitmap, (teacherProfileBitmap.getWidth() * scaleBitmapFactor),
                (teacherProfileBitmap.getHeight() * scaleBitmapFactor), true);

        int amountLines = lessonClassStringFields.size();

        mainBitmap = Bitmap.createBitmap(maxWidth + teacherProfileBitmap.getWidth() + marginLeft * 4,
                fontSize * amountLines + (amountLines + 2) * marginTop, Bitmap.Config.ARGB_8888);

        mainBitmap.eraseColor(Color.WHITE);

        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(lessonClassStringFields)
                .rubberStamp(teacherProfileBitmap)
                .rubberStampPosition(RubberStampPosition.CUSTOM, teacherProfileBitmap.getWidth() + marginLeft, fontSize)
                .margin(marginLeft, marginTop)
                .textColor(Color.BLACK)
                .textSize(fontSize)
                .textFont(regularFontPath)
                .build();

        mainBitmap = rubberStamp.addStamp(config);

        return mainBitmap;
    }


    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            Log.e(TAG, "Не удалось загрузить изображение");
        }

        return bitmap;
    }
}
