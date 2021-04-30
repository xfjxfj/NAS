package com.viegre.nas.pad.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

public class ZxingUtils {

    public static Bitmap createQRCode(String string, int width, int height, boolean isDelWhiteBorder) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, width, height);
            if (isDelWhiteBorder) {
                matrix = deleteWhite(matrix);//删除白边
            }
            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        // pixels[y * width + x] = Color.TRANSPARENT;
                        // 设置二维码颜色 顺序是 左上 右上 左下 右下
                        if (x < width / 2 && y < height / 2) {
//                            pixels[y * width + x] = 0xFF0094FF;// 蓝色
                            pixels[y * width + x] = Color.BLACK;// 蓝色
                            Integer.toHexString(new Random().nextInt());
                        } else if (x > width / 2 && y < height / 2) {
//                            pixels[y * width + x] = Color.CYAN;// 青色
                            pixels[y * width + x] = Color.BLACK;// 青色
                        } else if (x < width / 2 && y > height / 2) {
//                            pixels[y * width + x] = 0xFFFED545;// 黄色
                            pixels[y * width + x] = Color.BLACK;// 黄色
                        } else if (x > width / 2 && y > height / 2) {
//                            pixels[y * width + x] = 0xFF5ACF00;// 绿色
                            pixels[y * width + x] = Color.BLACK;// 绿色
                        } else {
                            // pixels[y * width + x] = Color.BLACK;// 黑色
                            pixels[y * width + x] = Color.TRANSPARENT;// 透明
                        }
                        //pixels[y * width + x] = Color.BLACK;
                    } else {
                        //pixels[y * width + x] = Color.WHITE;//白色
                        pixels[y * width + x] = Color.TRANSPARENT;// 透明
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    } public static Bitmap createQRCodewhite(String string, int width, int height, boolean isDelWhiteBorder) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, width, height);
            if (isDelWhiteBorder) {
                matrix = deleteWhite(matrix);//删除白边
            }
            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        // pixels[y * width + x] = Color.TRANSPARENT;
                        // 设置二维码颜色 顺序是 左上 右上 左下 右下
                        if (x < width / 2 && y < height / 2) {
//                            pixels[y * width + x] = 0xFF0094FF;// 蓝色
                            pixels[y * width + x] = Color.WHITE;// 蓝色
                            Integer.toHexString(new Random().nextInt());
                        } else if (x > width / 2 && y < height / 2) {
//                            pixels[y * width + x] = Color.CYAN;// 青色
                            pixels[y * width + x] = Color.WHITE;// 青色
                        } else if (x < width / 2 && y > height / 2) {
//                            pixels[y * width + x] = 0xFFFED545;// 黄色
                            pixels[y * width + x] = Color.WHITE;// 黄色
                        } else if (x > width / 2 && y > height / 2) {
//                            pixels[y * width + x] = 0xFF5ACF00;// 绿色
                            pixels[y * width + x] = Color.WHITE;// 绿色
                        } else {
                            // pixels[y * width + x] = Color.BLACK;// 黑色
                            pixels[y * width + x] = Color.TRANSPARENT;// 透明
                        }
                        //pixels[y * width + x] = Color.BLACK;
                    } else {
                        //pixels[y * width + x] = Color.WHITE;//白色
                        pixels[y * width + x] = Color.TRANSPARENT;// 透明
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 删除二维码生成后的白色边框
     *
     * @param matrix
     * @return
     */
    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }
}
