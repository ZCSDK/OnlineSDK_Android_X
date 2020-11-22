package com.sobot.online.imageloader;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.sobot.chat.imageloader.SobotGlideV4ImageLoader;
import com.sobot.chat.imageloader.SobotImageLoader;
import com.sobot.common.utils.SobotResourceUtils;

public class SobotBitmapUtil {

    private static SobotImageLoader sImageLoader;

    private static final SobotImageLoader getImageLoader() {
        if (sImageLoader == null) {
            synchronized (SobotBitmapUtil.class) {
                if (sImageLoader == null) {
                    if (isClassExists("com.bumptech.glide.request.RequestOptions")){
                        sImageLoader = new SobotGlideV4ImageLoader();
                    } else if (isClassExists("com.bumptech.glide.Glide")) {
                        sImageLoader = new SobotGlideImageLoader();
                    } else if (isClassExists("com.squareup.picasso.Picasso")) {
                        sImageLoader = new SobotPicassoImageLoader();
                    } else if (isClassExists("com.nostra13.universalimageloader.core.ImageLoader")) {
                        sImageLoader = new SobotUILImageLoader();
                    } else {
                        throw new RuntimeException("必须在(Glide、Picasso、universal-image-loader)中选择一个图片加载库添加依赖,或者检查是否添加了相应的混淆配置");
                    }
                }
            }
        }
        return sImageLoader;
    }

    private static final boolean isClassExists(String classFullName) {
        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void setImageLoader(SobotImageLoader imageLoader) {
        sImageLoader = imageLoader;
    }

    public static void display(Context context, String url,
                               ImageView imageView) {

        if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
            url = "file://" + url;
        }
        getImageLoader().displayImage(context, imageView, url, SobotResourceUtils.getDrawableId(context, "online_img_loading"), SobotResourceUtils.getDrawableId(context, "online_img_error"), imageView.getWidth(), imageView.getHeight(), null);
    }


    public static void display(Context context, String url,
                               ImageView imageView, int defaultPic, int errorPic) {

        if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
            url = "file://" + url;
        }

        getImageLoader().displayImage(context, imageView, url, defaultPic, errorPic, imageView.getWidth(), imageView.getHeight(), null);
    }

    public static void display(Context context, int resourceId, ImageView imageView) {
        getImageLoader().displayImage(context, imageView, resourceId, 0, 0, imageView.getWidth(), imageView.getHeight(), null);
    }

}