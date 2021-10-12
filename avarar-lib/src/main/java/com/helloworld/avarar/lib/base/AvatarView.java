package com.helloworld.avarar.lib.base;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class AvatarView extends View {
    private String zhText;          //中文头像
    private String enText;          //英文头像
    private Bitmap defaultBitmap;   //默认头像
    private Bitmap networkBitmap;   //网络头像

    private int backgroundColor;    //背景色
    private int zhTextColor;        //中文颜色
    private int enTextColor;        //英文颜色
    private int zhTextSize;         //中文字体大小,px
    private int enTextSize;         //英文字体大小,px

    private BitmapShader mBitmapShader;
    private Paint mTextPaint;
    private Paint mBitmapPaint;

    public AvatarView(Context context) {
        this(context,null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint.setFakeBoldText(true);

        backgroundColor = Color.parseColor("#ffffff");
        zhTextColor = Color.parseColor("#5124E8");
        enTextColor = Color.parseColor("#5124E8");
        zhTextSize = (int) sp2px(16);
        enTextSize = (int) sp2px(16);
    }

    public Bitmap getBitmap() {
        if (this.networkBitmap != null) {
            return networkBitmap;
        } else {
            return defaultBitmap;
        }
    }

    public void setDefaultBitmap(Bitmap defaultBitmap){
        clearTextAndBitmap();
        this.defaultBitmap = defaultBitmap;
        invalidate();
    }

    public void setNetworkBitmap(Bitmap networkBitmap,String url){
        if(networkBitmap != null && networkBitmap == this.networkBitmap){
            return;
        }

        clearTextAndBitmap();
        this.networkBitmap = networkBitmap;
        invalidate();
    }

    public void setZhText(String zhText){
        if(!TextUtils.isEmpty(zhText) && zhText.equals(this.zhText)){
            return;
        }

        clearTextAndBitmap();
        this.zhText = zhText;
        invalidate();
    }

    public void setEnText(String enText){
        if(!TextUtils.isEmpty(enText) && enText.equals(this.enText)){
            return;
        }

        clearTextAndBitmap();
        this.enText = enText;
        invalidate();
    }

    public void setBackgroundColor(int color){
        if(color != 0 && color == backgroundColor){
            return;
        }

        backgroundColor = color;
        invalidate();
    }

    public void setZhTextColor(int zhTextColor) {
        if(zhTextColor != 0 && zhTextColor == this.zhTextColor){
            return;
        }

        this.zhTextColor = zhTextColor;
        invalidate();
    }

    public void setEnTextColor(int enTextColor) {
        if(enTextColor != 0 && enTextColor == this.enTextColor){
            return;
        }

        this.enTextColor = enTextColor;
        invalidate();
    }

    public void setZhTextSize(int zhTextSize) {
        if(zhTextSize != 0 && zhTextSize == this.zhTextSize){
            return;
        }

        this.zhTextSize = zhTextSize;
        invalidate();
    }

    public void setEnTextSize(int enTextSize) {
        if(enTextSize != 0 && enTextSize == this.enTextSize){
            return;
        }

        this.enTextSize = enTextSize;
        invalidate();
    }

    private void clearTextAndBitmap(){
        this.networkBitmap = null;
        this.defaultBitmap = null;
        this.zhText = null;
        this.enText = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(networkBitmap != null){
            drawNetworkBitmap(canvas);
            return;
        }

        if(!isEmpty(zhText)){
            drawZhText(canvas);
        }else if(!isEmpty(enText)){
            drawEnText(canvas);
        }else {
            drawDefaultBitmap(canvas);
        }
    }

    //画图片(网络)
    private void drawNetworkBitmap(Canvas canvas) {
        drawBitmap(canvas,false);
    }

    //画默认图标
    private void drawDefaultBitmap(Canvas canvas) {
        drawBitmap(canvas,true);
    }

    private void drawBitmap(Canvas canvas,boolean isDefaultBitmap){
        Bitmap bm = isDefaultBitmap ? defaultBitmap : networkBitmap;
        if(bm == null) return;

        int radius = Math.min(getWidth(),getHeight()) / 2;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        mBitmapShader = new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        //下面三句很重要,可以保证显示的时候图片内容照应Shader大小,否者图片显示偏移
        float scale = ((radius * 2.0f) / Math.min(bm.getHeight(), bm.getWidth()));
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        mBitmapShader.setLocalMatrix(matrix);
        mBitmapPaint.setShader(mBitmapShader);
        mBitmapPaint.setStyle(Paint.Style.FILL);

        //画背景
        if(isDefaultBitmap){
            mTextPaint.setColor(backgroundColor);
            canvas.drawCircle(cx,cy,radius,mTextPaint);
        }

        //画图
        canvas.drawCircle(cx,cy,radius,mBitmapPaint);
    }

    //画中文
    private void drawZhText(Canvas canvas) {
        drawText(canvas,true);
    }

    //画英文
    private void drawEnText(Canvas canvas) {
        drawText(canvas,false);
    }

    private void drawText(Canvas canvas,boolean isChinese){
        String text = isChinese ? zhText : enText;
        int textColor = isChinese ? zhTextColor : enTextColor;
        int textSize = isChinese ? zhTextSize : enTextSize;

        //1 画背景
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
        mTextPaint.setColor(backgroundColor);
        canvas.drawCircle(cx,cy,radius,mTextPaint);

        //2 写字
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        int baseX = (int) (getWidth() / 2 - mTextPaint.measureText(text) / 2);
        int baseY = (int) ((getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
        canvas.drawText(text, baseX, baseY, mTextPaint);
    }

   private boolean isEmpty(String text){
        if(text == null || "".equals(text)){
            return true;
        }

        return false;
    }

    private float sp2px(float sp){
        float scale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }
}
