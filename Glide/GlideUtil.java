package cn.com.egova.securities_police.model.util;

import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import cn.com.egova.securities_police.R;
import cn.com.egova.securities_police.model.http.HttpRequstConstant;

/**
 * 对Glide进行的封装，添加了默认图片，请求失败图片设置 <br>
 * Created by ZhangHaosSong on 2016/07/07.
 */
public class GlideUtil {

    /**
     * 利用Gilde加载图片
     *
     * @param context
     * @param imgUrl
     * @param targetView
     */
    public void LoadHttpImg(Context context, String imgUrl, ImageView targetView) {
        String imageUrl = imgUrl;
        final Context mContext = context;
        final ImageView imgView = targetView;
        //首先设置一个正在载入的图片
        imgView.setImageResource(R.mipmap.default_img_bad_loading);
        //错误的URL
        if (imageUrl == null || imageUrl.length() == 0 || imageUrl.equals("NULL")) {
            imgView.setImageResource(R.mipmap.default_img_bad_url);
            return;
        }
        //判断Url的合法性
        if (imageUrl != null) {
            if (!imageUrl.startsWith("http"))
                imageUrl = HttpRequstConstant.BASE_IP + imgUrl;
        }
        //加载
        Glide.with(context).load(imageUrl).listener(new RequestImgListener(mContext, false)).into(imgView);
    }


    /**
     * 利用Gilde加载本地图片
     *
     * @param context
     * @param imgFil
     * @param imgFil
     */
    public void LoadLocalFile(Context context, File imgFil, ImageView targetView) {
        File imageFile = imgFil;
        final Context mContext = context;
        final ImageView imgView = targetView;
        //首先设置一个正在载入的图片
        imgView.setImageResource(R.mipmap.default_img_bad_loading);
        //错误的URL
        if (!imageFile.exists()) {
            imgView.setImageResource(R.mipmap.default_img_bad_url);
            return;
        }
        //加载
        Glide.with(context).load(imageFile).into(imgView);
    }

    /**
     * 利用Glide加载用户图像
     *
     * @param context
     * @param imgUrl
     * @param targetView
     */
    public void LoadAvatar(Context context, String imgUrl, ImageView targetView) {
        String imageUrl = imgUrl;
        final Context mContext = context;
        final ImageView imgView = targetView;
        LogUtil.e("LoadAvatar", "LoadAvatar imageUrl=" + imageUrl);
        //首先设置一个正在载入的图片
        imgView.setImageResource(R.mipmap.default_avatar);
        //错误的URL
        if (imageUrl == null || imageUrl.length() == 0 || imageUrl.equals("NULL")) {
            imgView.setImageResource(R.mipmap.default_avatar);
            return;
        }
        //判断Url的合法性
        if (imageUrl != null) {
            if (!imageUrl.startsWith("http"))
                imageUrl = HttpRequstConstant.BASE_IP + imgUrl;
        }
        //加载
        Glide.with(context).load(imageUrl).listener(new RequestImgListener(mContext, true)).into(imgView);
    }


    /**
     * 加载网络图片数据
     */
    private class RequestImgListener implements RequestListener {
        private int tryTimes = 0;
        private Context context;
        private boolean isAvatar = false;

        public RequestImgListener(Context context, boolean isAvatar) {
            this.context = context;
            this.isAvatar = isAvatar;
        }

        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            if (tryTimes < 5) {
                Glide.with(context).load(model).listener(this).into(target);
                tryTimes++;
            } else {
                if (isAvatar) {
                    Glide.with(context).load(R.mipmap.default_avatar).into(target);
                    ToastUtil.showText(context, "头像获取失败出错", Toast.LENGTH_SHORT);
                } else {
                    Glide.with(context).load(R.mipmap.default_img_bad_loading).into(target);
                    ToastUtil.showText(context, "图片获取失败出错", Toast.LENGTH_SHORT);
                }
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    }
}
