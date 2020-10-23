package pers.teemo.accompany.view.extension;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import pers.teemo.accompany.R;

/**
 * 自定义形状图片，可以设置圆角和边框
 */
public class ShapeImageView extends AppCompatImageView {
    private static final int DEFAULT_BORDER_COLOR = Color.WHITE;

    private final int borderColor;
    private final float borderWidth;
    private final float radius;

    private final Paint border;
    private final Paint image;

    public ShapeImageView(@NonNull Context context) {
        this(context, null);
    }

    public ShapeImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView, defStyleAttr, 0);
        borderColor = typedArray.getColor(R.styleable.ShapeImageView_border_color, DEFAULT_BORDER_COLOR);
        borderWidth = typedArray.getDimension(R.styleable.ShapeImageView_border_width, 0);
        radius = typedArray.getDimension(R.styleable.ShapeImageView_radius, 0);
        typedArray.recycle();

        border = new Paint();
        border.setAntiAlias(true); // 抗锯齿
        border.setColor(borderColor);
        border.setStrokeWidth(borderWidth);
        border.setStyle(Paint.Style.STROKE);

        image = new Paint();
        image.setAntiAlias(true);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        // 计算实际的显示图片大小
        int imageWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        int imageHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        // Drawable 转 Bitmap
        Bitmap bitmap = toBitmap(getDrawable());
        if (bitmap == null) {
            return;
        }
        // 减去上下左右的边框，得到图片大小
        bitmap = resize(bitmap, imageWidth - borderWidth * 2, imageHeight - borderWidth * 2);
        // 绘制 FIXME Avoid object allocations during draw/layout operations (preallocate and reuse instead)\
        float borderOffset = borderWidth / 2;
        canvas.drawRoundRect(
                new RectF(borderOffset, borderOffset, imageWidth - borderOffset, imageHeight - borderOffset),
                radius, radius, border);
        // 边界遮罩
        image.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        // 画布移动(x,y), 移动量为边框宽度
        canvas.translate(borderWidth, borderWidth);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), radius, radius, image);

    }

    private Bitmap resize(Bitmap bitmap, float imageWidth, float imageHeight) {
        // 计算出缩放比
        Matrix matrix = new Matrix();
        matrix.postScale(imageWidth / bitmap.getWidth(), imageHeight / bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    private Bitmap toBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
