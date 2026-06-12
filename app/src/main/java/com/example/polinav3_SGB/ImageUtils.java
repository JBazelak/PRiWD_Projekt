package com.example.polinav3_SGB;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
public class ImageUtils {
    public static byte[] encodeBitmapToJpeg(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }
}
