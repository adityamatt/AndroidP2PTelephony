package com.sinch.android.rtc.sample.video.filter;

import android.graphics.ImageFormat;
import android.graphics.YuvImage;

/**
 * Created by jonaslindroth on 6/9/17.
 */

public class RotateImage {
    private int count = 0;
    public void rotateNV21(byte[] input, byte[] output, int width, int height, int rotation) {
        boolean swap = (rotation == 90 || rotation == 270);
        boolean yflip = (rotation == 90 || rotation == 180);
        boolean xflip = (rotation == 270 || rotation == 180);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (yflip) {
                    yi = h - yi - 1;
                }
                if (xflip) {
                    xi = w - xi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
                // adjust for interleave here
                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + (w * yo + xo) * 2;
                // and here
                int vi = ui + 1;
                int vo = uo + 1;
                output[uo] = input[ui];
                output[vo] = input[vi];
            }
        }
    }
    //rotate image 90 degrees every second
    public YuvImage rotateNV21(YuvImage image) {
        byte[] rotated = new byte[image.getYuvData().length];
        rotateNV21(image.getYuvData(), rotated, image.getWidth(), image.getHeight(), getRotation());
        return new YuvImage(rotated, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
    }
    private int getRotation()
    {
        if (++count > 200)
            count = 0;

        //rotate 90 degrees every second (50 frames per sec)
        return (count / 50) * 90;
    }
}
