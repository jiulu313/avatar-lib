package com.helloworld.avarar.lib.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Size;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.helloworld.avarar.lib.cache.CacheEntity;
import com.helloworld.avarar.lib.cache.CacheManager;
import com.helloworld.avarar.lib.util.NetworkUtil;
import com.helloworld.avarar.lib.util.RegexUtil;
import com.helloworld.avarar.lib.util.StringUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class AvatarViewHelper {
    //头像等级
    public static final int LEVEL_1 = 1;    //对应的width,height为24dp
    public static final int LEVEL_2 = 2;    //对应的width,height为36dp
    public static final int LEVEL_3 = 3;    //对应的width,height为40dp
    public static final int LEVEL_4 = 4;    //对应的width,height为48dp
    public static final int LEVEL_5 = 5;    //对应的width,height为56dp
    public static final int LEVEL_6 = 6;    //对应的width,height为72dp

    //头像显示等级
    private int level = -1;
    private String name;
    private String email;
    private int defaultIcon;    //默认头像 , drawable
    private String networkIcon; //网络头像 , url

    private Bitmap bitmap;//直接显示bitmap
    private boolean removeCache;//是否删除当前的缓存

    private AvatarView avatarView;
    private Context context;

    private AvatarViewHelper(Context context) {
        this.context = context;
    }

    public static AvatarViewHelper with(Context context) {
        return new AvatarViewHelper(context);
    }

    public AvatarViewHelper nameAndEmail(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }

    public AvatarViewHelper defaultIcon(int defaultIcon) {
        this.defaultIcon = defaultIcon;
        return this;
    }

    public AvatarViewHelper bitmap(Bitmap bitmap){
        this.bitmap = bitmap;
        return this;
    }

    public AvatarViewHelper resource(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, null);
        this.bitmap = bitmap;
        return this;
    }

    public AvatarViewHelper networkIconFromEmail(String email) {
        if (isEmpty(email))
            return this;
        this.networkIcon = Constant.baseUrl() + "s/a/e-" + Base64.encode(email);
        return this;
    }

    public AvatarViewHelper networkIconFromUid(String uid) {
        if (isEmpty(uid))
            return this;

        this.networkIcon = Constant.baseUrl() + "s/a/u-" + uid;
        return this;
    }

    public AvatarViewHelper removeCacheWithKey(boolean removeCache){
        this.removeCache = removeCache;
        return this;
    }

    public AvatarViewHelper networkIcon(String url) {
        this.networkIcon = url;
        return this;
    }

    public AvatarViewHelper level(int level) {
        this.level = level;
        return this;
    }

    public void into(AvatarView avatarView) {
        if (avatarView == null) {
            throw new IllegalArgumentException("avatarView must not be null");
        }

        if (level == -1) {
            throw new IllegalArgumentException("AvatarViewHelper not called level method");
        }

        //是否删除当前的缓存
        if(this.removeCache && !TextUtils.isEmpty(networkIcon)){
            CacheManager.getInstance().remove(networkIcon);
        }

        this.avatarView = avatarView;
        this.avatarView.setTag(this.networkIcon);
        display();
    }

    private void display() {
        if(avatarView.getVisibility() != View.VISIBLE){
            return;
        }

        if(bitmap != null){
            displayBitmap();
            return;
        }

        if (!isEmpty(networkIcon)) {
            displayNetworkImageIcon();
            return;
        }

        if (!isEmpty(name)) {
            int count = StringUtil.getChineseCount(name);
            if (count > 0 && count < 5) {// [1,4]
                displayName();
            } else if(count > 5){
                displayDefaultImageIcon(getColor(name));
            } else if(StringUtil.getEnglishCount(name) > 0){
                displayName();
            }else {
                displayDefaultImageIcon();
            }
        } else if (!isEmpty(email)) {
            displayEmail();
        } else {
            displayDefaultImageIcon();
        }
    }

    private void displayBitmap(){
        avatarView.setNetworkBitmap(scaleBitmap(bitmap),networkIcon);
    }

    //网络头像
    private void displayNetworkImageIcon() {
        /**
         1 缓存存在,且没有过期,用缓存
         2 缓存存在,但是过期了,重新请求,并把缓存清除
         3 缓存不存在
         3.1 但是还没有过期,不请求
         3.2 但是过期了,请求

         请求结果:
         3.3 如果有头像,缓存下来,显示头像
         3.4 如果没有头像,也缓存下来,显示名字
         *
         */
        CacheEntity<Bitmap> entity = CacheManager.getInstance().get(networkIcon);
        if(entity != null){
            if(entity.hasCache()){ //有缓存
                if(entity.isCacheExpired()){//过期
                    CacheManager.getInstance().remove(networkIcon);
                }else {//未过期
                    avatarView.setNetworkBitmap(entity.getEntity(),networkIcon);
                    return;
                }
            }else {//没有缓存
                if(entity.isCacheExpired()){//过期了,需要重新发起请求
                    CacheManager.getInstance().remove(networkIcon);
                }else {//没有过期,需要显示名字或者email
                    networkIcon = null;
                    avatarView.setNetworkBitmap(null,null);
                    display();
                    return;
                }
            }
        }

        Map<String,String> params = new HashMap<>();

        String cookie = NetworkUtil.generateCookie(params);

        // 如果需要，可以添加cookie
        // Headers headers = new LazyHeaders.Builder().addHeader("Cookie", cookie).build();
        WMGlideUrl url = new WMGlideUrl(networkIcon);
        WMRequestListener listener = new WMRequestListener<WMGlideUrl,Bitmap>(){
            @Override
            public boolean onException(Exception e, WMGlideUrl model, Target<Bitmap> target, boolean isFirstResource) {
                CacheEntity<Bitmap> cacheEntity = new CacheEntity<>(null, SystemClock.elapsedRealtime() / 1000,false);
                CacheManager.getInstance().save(networkIcon,cacheEntity);

                String tag = (String) avatarView.getTag();
                if(url != null && url.getCacheKey() != null &&  url.getCacheKey().equals(tag)){
                    networkIcon = null;
                    avatarView.setNetworkBitmap(null,null);
                    display();
                }

                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, WMGlideUrl model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Bitmap bitmap = scaleBitmap(resource);
                CacheEntity<Bitmap> cacheEntity = new CacheEntity<>(bitmap, SystemClock.elapsedRealtime() / 1000,true);
                CacheManager.getInstance().save(networkIcon,cacheEntity);

                String tag = (String) avatarView.getTag();
                if(url != null && url.getCacheKey() != null &&  url.getCacheKey().equals(tag)){
                    avatarView.setNetworkBitmap(bitmap,networkIcon);
                }
                return false;
            }
        };

        Glide.with(context)
                .load(url)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(listener)
                .into(getSizeByLevel().getWidth(), getSizeByLevel().getHeight());
    }

    private void displayName() {
        avatarView.setBackgroundColor(getColor(name));
        if (RegexUtil.containEnglish(name) && RegexUtil.containChinese(name)) { //中英文都包含
            String str = StringUtil.getChineseFromAllString(name);
            avatarView.setZhTextSize((int) sp2px(context, getTextSizeByHeaderLevel("zh")));
            avatarView.setZhTextColor(Color.parseColor("#e6ffffff"));
            avatarView.setZhText(str.toUpperCase());
        } else if (RegexUtil.containChinese(name) && !RegexUtil.containEnglish(name)) { //只包含中文
            String str = StringUtil.getChineseFromAllString(name);
            avatarView.setZhTextSize((int) sp2px(context, getTextSizeByHeaderLevel("zh")));
            avatarView.setZhTextColor(Color.parseColor("#e6ffffff"));
            avatarView.setZhText(str.toUpperCase());
        } else if (RegexUtil.containEnglish(name) && !RegexUtil.containChinese(name)) { //只包含英文
            String str = name.substring(0, 1);
            avatarView.setEnTextSize((int) sp2px(context, getTextSizeByHeaderLevel("en")));
            avatarView.setEnTextColor(Color.parseColor("#e6ffffff"));
            avatarView.setEnText(str.toUpperCase());
        }
    }

    private void displayEmail() {
        avatarView.setBackgroundColor(getColor(email));

        avatarView.setZhTextSize((int)sp2px(context,getTextSizeByHeaderLevel("en")));
        avatarView.setEnTextColor(Color.parseColor("#e6ffffff"));

        Character ch = email.charAt(0);
        if(Character.isLetter(ch)){
            String str = new Character(Character.toUpperCase(ch)).toString();
            avatarView.setEnText(str);
        }else {
            avatarView.setEnText(ch.toString());
        }
    }

    private void displayDefaultImageIcon(int backgroundColor) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), defaultIcon, null);
        avatarView.setBackgroundColor(backgroundColor);
        avatarView.setDefaultBitmap(scaleBitmap(bitmap));
    }

    private void displayDefaultImageIcon() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), defaultIcon, null);
        int color = getColor(defaultIcon + "");
        avatarView.setBackgroundColor(color);
        avatarView.setDefaultBitmap(scaleBitmap(bitmap));
    }

    private boolean isEmpty(String text) {
        return text == null || "".equals(text);
    }

    private float sp2px(Context context, float sp) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void setTag(String name, String email) {
        String userName = name == null ? "" : name;
        String userEmail = email == null ? "" : email;
        avatarView.setTag(userName + "-" + userEmail);
    }

    @SuppressLint("Range")
    private int getColor(String text) {
        String color = "#FFE87D6A";
        if (!TextUtils.isEmpty(text)) {
            int index = Math.abs(text.hashCode()) % 6;
            if (index < 0 || index > 5) {
                index = 0;
            }
            switch (index) {
                case 0:
                    color = "#FFE87D6A";
                    break;
                case 1:
                    color = "#FFF4B73F";
                    break;
                case 2:
                    color = "#FF5AB8A6";
                    break;
                case 3:
                    color = "#FF5CC8E4";
                    break;
                case 4:
                    color = "#FF6AA4E5";
                    break;
                case 5:
                    color = "#FF9D6CD9";
                    break;
            }
        }

        return Color.parseColor(color);
    }

    @SuppressLint("Range")
    private int getColor(int number) {
        String text = number + "";
        String color = "";
        if (!TextUtils.isEmpty(text)) {
            int index = Math.abs(text.hashCode()) % 6;
            switch (index) {
                case 0:
                    color = "#FFE87D6A";
                    break;
                case 1:
                    color = "#FFF4B73F";
                    break;
                case 2:
                    color = "#FF5AB8A6";
                    break;
                case 3:
                    color = "#FF5CC8E4";
                    break;
                case 4:
                    color = "#FF6AA4E5";
                    break;
                case 5:
                    color = "#FF9D6CD9";
                    break;
            }
        }

        return Color.parseColor(color);
    }

    //根据等级获取显示的文本字体大小，单位 sp
    private float getTextSizeByHeaderLevel(String language) {
        float size = 18;
        switch (level) {
            case LEVEL_1:
                if ("en".equals(language)) {
                    size = 12;
                } else {
                    size = 10;
                }
                break;
            case LEVEL_2:
                if ("en".equals(language)) {
                    size = 16;
                } else {
                    size = 12;
                }
                break;
            case LEVEL_3:
                if ("en".equals(language)) {
                    size = 18;
                } else {
                    size = 13;
                }
                break;
            case LEVEL_4:
                if ("en".equals(language)) {
                    size = 22;
                } else {
                    size = 15;
                }
                break;
            case LEVEL_5:
                if ("en".equals(language)) {
                    size = 25;
                } else {
                    size = 17;
                }
            case LEVEL_6:
                if("en".equals(language)){
                    size = 32;
                }else {
                    size = 21;
                }
                break;
        }

        return size;
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        float targetWidth = 1;
        float targetHeight = 1;

        switch (level) {
            case AvatarViewHelper.LEVEL_1:
                targetWidth = dp2px(context, 24);
                targetHeight = dp2px(context, 24);
                break;
            case AvatarViewHelper.LEVEL_2:
                targetWidth = dp2px(context, 36);
                targetHeight = dp2px(context, 36);
                break;
            case AvatarViewHelper.LEVEL_3:
                targetWidth = dp2px(context, 40);
                targetHeight = dp2px(context, 40);
                break;
            case AvatarViewHelper.LEVEL_4:
                targetWidth = dp2px(context, 48);
                targetHeight = dp2px(context, 48);
                break;
            case AvatarViewHelper.LEVEL_5:
                targetWidth = dp2px(context, 56);
                targetHeight = dp2px(context, 56);
                break;
            case AvatarViewHelper.LEVEL_6:
                targetWidth = dp2px(context, 72);
                targetHeight = dp2px(context, 72);
                break;
        }

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,(int)targetWidth,(int)targetHeight,true);
        return newBitmap;
    }

    private Size getSizeByLevel() {

        float targetWidth = 40;
        float targetHeight = 40;

        switch (level) {
            case AvatarViewHelper.LEVEL_1:
                targetWidth = dp2px(context, 24);
                targetHeight = dp2px(context, 24);
                break;
            case AvatarViewHelper.LEVEL_2:
                targetWidth = dp2px(context, 36);
                targetHeight = dp2px(context, 36);
                break;
            case AvatarViewHelper.LEVEL_3:
                targetWidth = dp2px(context, 40);
                targetHeight = dp2px(context, 40);
                break;
            case AvatarViewHelper.LEVEL_4:
                targetWidth = dp2px(context, 48);
                targetHeight = dp2px(context, 48);
                break;
            case AvatarViewHelper.LEVEL_5:
                targetWidth = dp2px(context, 56);
                targetHeight = dp2px(context, 56);
                break;
            case AvatarViewHelper.LEVEL_6:
                targetWidth = dp2px(context, 72);
                targetHeight = dp2px(context, 72);
                break;
        }

        Size size = new Size((int) targetWidth, (int) targetHeight);
        return size;
    }

    private String makeTag(String name ,String email){
        String userName = name == null ? "" : name;
        String userMail = email == null ? "" : email;

        return userName + "-" + userMail;
    }

    public static class WMGlideUrl extends GlideUrl{
        public String name;
        public String email;

        public WMGlideUrl(URL url) {
            super(url);
        }

        public WMGlideUrl(String url) {
            super(url);
        }

        public WMGlideUrl(URL url, Headers headers) {
            super(url, headers);
        }

        public WMGlideUrl(String url, Headers headers) {
            super(url, headers);
        }
    }

    public static class WMRequestListener<T,R> implements RequestListener<T,R>{
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(R resource, T model, Target<R> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    }
}


















