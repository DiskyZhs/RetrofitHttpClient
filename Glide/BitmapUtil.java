package cn.com.egova.securities_police.model.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.view.Display;
import android.view.WindowManager;

import com.loopj.android.http.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.com.egova.securities_police.model.accident.ProofPhotoMap;

/**
 * Utils工具类 <br>
 * Bitmap转化 <br>
 * 证据缩放，头像缩放,签名缩放 <br>
 * Created by ZhangHaoSong on 2016/04/14.
 */
public class BitmapUtil {
    /**
     * 默认签名高度
     */
    public static final int DEFAULT_SIGN_HEIGHT = 150;
    /**
     * 默认头像宽
     */
    public static final int DEFAULT_AVATAR_WIDTH = 180;
    /**
     * 头像宽高比
     */
    public static float avatarWidth_Height = 1;

    /**
     * 对证据照片进行分辨率裁剪为上传的图片
     *
     * @param src
     * @return
     */
    public static Bitmap getPostPhoto(Bitmap src) {
        //首先缩放大小
        Bitmap dest = src.createScaledBitmap(src, ProofPhotoMap.DEFAULT_POST_PHOTO_WIDTH, ProofPhotoMap.DEFAULT_POST_PHOTO_HEIGHT, false);//分辨率修改（有效）
        //控制图片大小
        ByteArrayOutputStream dataByte = new ByteArrayOutputStream();
        dest.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, dataByte); // 50压缩（质量压缩）（有效）
        LogUtil.e("getPostPhoto", "src size = " + dataByte.size());
        while (dataByte.toByteArray().length / 1024 > 300) {//循环判断如果压缩后图片是否大于300kb,大于继续压缩
            dataByte.reset();//重置dataByte即清空dataByte
            ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY = ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY / 2;//每次除以2
            dest.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, dataByte);//这里压缩options%，把压缩后的数据存放到baos中
        }
        dest.recycle();
        //生成bitmap
        dest = BitmapFactory.decodeByteArray(dataByte.toByteArray(), 0, dataByte.size());
        return dest;
    }


    /**
     * 将证据照片文件存储在SD卡上面
     *
     * @param src         Proof原图
     * @param newFilePath 新文件的地址
     */
    public static void saveProofAsFile(Bitmap src, String newFilePath) {
        //首先缩放大小
        Bitmap dest = src.createScaledBitmap(src, ProofPhotoMap.DEFAULT_POST_PHOTO_WIDTH, ProofPhotoMap.DEFAULT_POST_PHOTO_HEIGHT, false);//分辨率修改（有效
        //创建文件
        File file = new File(newFilePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            //控制图片大小
            ByteArrayOutputStream dataByte = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, dataByte); //(质量压缩）（有效）
            while (dataByte.toByteArray().length / 1024 > 300) {//循环判断如果压缩后图片是否大于300kb,大于继续压缩
                dataByte.reset();//重置dataByte即清空dataByte
                ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY = ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY / 2;//每次除以2
                dest.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, dataByte);//这里压缩options%，把压缩后的数据存放到baos中
            }
            //写入文件
            dataByte.writeTo(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * 对证据照片进行分辨率裁剪为缩略图
     *
     * @param src
     * @return
     */
    public static Bitmap getThumbnail(Bitmap src) {
        return ThumbnailUtils.extractThumbnail(src, ProofPhotoMap.DEFAULT_THUMBNAIL_WIDTH, ProofPhotoMap.DEFAULT_THUMBNAIL_HEIGHT);
    }

    /**
     * 对证据照片进行分辨率裁剪为缩略图
     *
     * @param filePath
     * @return
     */
    public static Bitmap getThumbnail(String filePath) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath), ProofPhotoMap.DEFAULT_THUMBNAIL_WIDTH, ProofPhotoMap.DEFAULT_THUMBNAIL_HEIGHT);
    }

    /**
     * 对签名图片进行分辨率缩放
     *
     * @param signSrc
     * @return
     */
    public static Bitmap getDefaultSign(Bitmap signSrc) {
        //计算长宽比，然后按比例缩放
        int srcHeight = signSrc.getHeight();
        int srcWidth = signSrc.getWidth();
        //由竖直变为横屏
        Bitmap dest = adjustPhotoRotation(Bitmap.createScaledBitmap(signSrc, DEFAULT_SIGN_HEIGHT * srcWidth / srcHeight, DEFAULT_SIGN_HEIGHT, false), 270);
        return dest;
    }

    /**
     * 将Bitmap按照指定路径缩放为证据图片后存储(随手拍使用)
     *
     * @param bmp
     * @param filePath
     */
    public static void saveBitmapAsProof(Bitmap bmp, String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            Bitmap.createScaledBitmap(bmp, ProofPhotoMap.DEFAULT_POST_PHOTO_HEIGHT, ProofPhotoMap.DEFAULT_POST_PHOTO_WIDTH, false).compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对头像进行分辨率缩放
     *
     * @return
     */
    public static Bitmap getAvatar(Context ctx, String filePath) {
        Bitmap src = getScaleBitmap(ctx, filePath);
        LogUtil.e("BitmapUtil", "getAvatar  avatarWidth_Height =" + avatarWidth_Height);
        LogUtil.e("BitmapUtil", "getAvatar  DEFAULT_AVATAR_HEIGHT =" + DEFAULT_AVATAR_WIDTH / avatarWidth_Height);
        Bitmap dest = Bitmap.createScaledBitmap(src, DEFAULT_AVATAR_WIDTH, (int) (DEFAULT_AVATAR_WIDTH / avatarWidth_Height), false);
        recycleBitmap(src);
        return dest;
    }

    public static Bitmap getScaleBitmap(Context ctx, String filePath) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, opt);

        int bmpWidth = opt.outWidth;
        int bmpHeght = opt.outHeight;
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        avatarWidth_Height = (float) bmpWidth / bmpHeght;
        LogUtil.e("BitmapUtil", "getScaleBitmap  avatarWidth_Height =" + avatarWidth_Height);
        opt.inSampleSize = 1;
        if (bmpWidth > bmpHeght) {
            if (bmpWidth > screenWidth)
                opt.inSampleSize = bmpWidth / screenWidth;
        } else {
            if (bmpHeght > screenHeight)
                opt.inSampleSize = bmpHeght / screenHeight;
        }
        opt.inJustDecodeBounds = false;

        bmp = BitmapFactory.decodeFile(filePath, opt);
        return bmp;
    }

    /**
     * 回收Bitmap所占用的内存
     *
     * @param bmp
     */
    public static void recycleBitmap(Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) {
            // 回收并且置为null
            bmp.recycle();
            bmp = null;
        }
        System.gc();
    }

    /**
     * 将两张bitmap叠加生成新的bitmap
     *
     * @param background
     * @param foreground
     * @return
     */
    public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
        if (background == null || foreground == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.RGB_565);
        Canvas cv = new Canvas(newbmp);
        cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg
        cv.drawBitmap(foreground, 0, 0, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        cv.restore();//存储
        return newbmp;
    }


    /**
     * 按照ProofPhotoMap中的照片质量转化inputStream
     *
     * @param bm
     * @return
     */
    public static InputStream Bitmap2IS(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, baos);
        InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
        return sbs;
    }

    /**
     * @param bm
     * @return
     */
    public static ByteArrayOutputStream Bitmap2BAS(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, ProofPhotoMap.DEFAULT_POST_PHOTO_QUALITITY, baos);
        return baos;
    }

    /**
     * @param bm
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    /**
     * 将bitmap转换成base64字符串
     *
     * @param bitmap
     * @return base64 字符串
     */
    public static String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
        // 将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * 将base64转换成bitmap图片
     *
     * @param string base64字符串
     * @return bitmap
     */
    public static Bitmap stringtoBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        if (base64Data != null) {
            byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }


    /**
     * 旋转
     *
     * @param bm
     * @param orientationDegree
     * @return
     */
    public static Bitmap
    adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

}

