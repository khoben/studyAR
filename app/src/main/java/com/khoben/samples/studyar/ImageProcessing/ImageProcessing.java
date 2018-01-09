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

import com.khoben.samples.studyar.AR.MyAR;
import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;
import com.vinaygaba.rubberstamp.RubberStamp;
import com.vinaygaba.rubberstamp.RubberStampConfig;
import com.vinaygaba.rubberstamp.RubberStampPosition;

import java.io.IOException;
import java.io.InputStream;

public class ImageProcessing {
    private Bitmap mainBitmap;
    private Bitmap teacherProfileBitmap;
    private MainActivity mainActivity;
    private RubberStampConfig config;
    private RubberStamp rubberStamp;

    private final static String boldFontPath = "fonts/Roboto-Bold.ttf";
    private final static String regularFontPath = "fonts/Roboto-Regular.ttf";
    private final int fontSize = 100;

    private final String TAG = "ImageProcessing";
    private final int marginLeft = 30;

    public ImageProcessing() {
    }

    public ImageProcessing(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

        //TODO: add id

        if (lesson == null) {
            Log.i(TAG, "Lesson is NULL");
            return null;
        }


        Log.i(TAG, lesson.toString());

        String[] strings = {
                String.format("Аудитория №%s", lesson.getAud()),
                lesson.getSubject(),
                lesson.getFio(),
                lesson.getDegree()
        };

        int maxWidth = 0;
        int cur = 0;
        for (String string : strings) {
            if (string.contains("Aудитория")) {
                cur = getStringWidth(string, boldFontPath);
                if (cur > maxWidth)
                    maxWidth = cur;
            } else {
                cur = getStringWidth(string, regularFontPath);
                if (cur > maxWidth)
                    maxWidth = cur;
            }
        }

        String firstname = lesson.getFio().split(" ")[0];

        teacherProfileBitmap = getBitmapFromAsset(mainActivity, String.format("teachers/%s.jpg", firstname));
        teacherProfileBitmap = Bitmap.createScaledBitmap(teacherProfileBitmap, (teacherProfileBitmap.getWidth() * 4), (teacherProfileBitmap.getHeight() * 4), true);

        mainBitmap = Bitmap.createBitmap(maxWidth + teacherProfileBitmap.getWidth() + marginLeft * 2, 600, Bitmap.Config.ARGB_8888);

        mainBitmap.eraseColor(Color.WHITE);

        rubberStamp = new RubberStamp(mainActivity);

        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(teacherProfileBitmap)
                .rubberStampPosition(RubberStampPosition.CUSTOM, 0, 0)
                .margin(30, 30)
                .build();

        mainBitmap = rubberStamp.addStamp(config);

        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(strings[0])
                .rubberStampPosition(RubberStampPosition.CUSTOM, teacherProfileBitmap.getWidth(), fontSize)
                .margin(30, 30)
                .textColor(Color.BLACK)
                .textSize(fontSize)
                .textFont(boldFontPath)
                .build();

        mainBitmap = rubberStamp.addStamp(config);


        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(strings[1])
                .rubberStampPosition(RubberStampPosition.CUSTOM, teacherProfileBitmap.getWidth(), 2 * fontSize)
                .margin(30, 30)
                .textColor(Color.BLACK)
                .textSize(fontSize)
                .textFont(regularFontPath)
                .build();

        mainBitmap = rubberStamp.addStamp(config);

        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(strings[2])
                .rubberStampPosition(RubberStampPosition.CUSTOM, teacherProfileBitmap.getWidth(), 3 * fontSize)
                .margin(30, 30)
                .textColor(Color.BLACK)
                .textSize(fontSize)
                .textFont(regularFontPath)
                .build();


        mainBitmap = rubberStamp.addStamp(config);

        config = new RubberStampConfig.RubberStampConfigBuilder()
                .base(mainBitmap)
                .rubberStamp(strings[3])
                .rubberStampPosition(RubberStampPosition.CUSTOM, teacherProfileBitmap.getWidth(), 4 * fontSize)
                .margin(30, 30)
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
            // handle exception
        }

        return bitmap;
    }
}
