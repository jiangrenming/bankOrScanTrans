package com.nld.cloudpos.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wqz on 2017/10/30.
 */

public class printBitmap {
    public static Bitmap getPrintBitmap(Context context) {
        AssetManager assets = context.getAssets();
        Bitmap bitmap = null;
        try {
            InputStream open = assets.open("caishen.bmp");
            bitmap = BitmapFactory.decodeStream(open);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
