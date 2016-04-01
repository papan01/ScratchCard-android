package com.example.scratchcard.view;

import com.example.scratchcard.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class ScratchCard extends View {

	private Paint mOutterPaint;
	private Path mPath;
	private Canvas mCanvas;
	private Bitmap mBitmap;

	private int mLastX;
	private int mLastY;
	private Bitmap mOutterBitmap;

	// -----------------------------
	// private Bitmap Bitmap;

	private String mText;
	private Paint mBackPaint;

	/**
	 * �O�����d�T���e��
	 */
	private Rect mTextBound;
	private int mTextSize;
	private int mTextColor;

	// �P�_�O�_�B�\�h�����F��w��(60%)
	private volatile boolean mComplete = false;

	/**
	 * ���d���^��
	 * 
	 */
	public interface OnScratchCardCompleteListener {
		void complete();

	}

	public OnScratchCardCompleteListener mListener;

	public void setOnScratchCardCompleteListener(
			OnScratchCardCompleteListener mListener) {
		this.mListener = mListener;
	}

	public ScratchCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScratchCard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		TypedArray a = null;
		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.ScratchCard, defStyleAttr, 0);
			int n = a.getIndexCount();
			for (int i = 0; i < n; i++) {
				int attr = a.getIndex(i);
				switch (attr) {
				case R.styleable.ScratchCard_prizetext:
					mText = a.getString(attr);
					break;
				case R.styleable.ScratchCard_textColor:
					mTextColor = a.getColor(attr, 0x000000);
					break;
				case R.styleable.ScratchCard_textSize:
					mTextSize = (int) a.getDimension(attr, TypedValue
							.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
									getResources().getDisplayMetrics()));
					break;
				}

			}
		} finally {
			if (a != null)
				a.recycle();
		}

	}
	
	public void setText(String mText)
	{
		this.mText = mText;		
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	public ScratchCard(Context context) {
		this(context, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		// ��l��Bitmap
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		// �]�mpath�e���ݩ�
		setOutterPaint();
		setUpBackPaint();

		// mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		// �]�i�H(0x123),(Color.parseColor("#111111"));
		mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30,
				mOutterPaint);
		mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height),
				null);

	}

	/**
	 * �]�m����T�����e���ݩ�
	 */
	private void setUpBackPaint() {
		mBackPaint.setColor(mTextColor);
		mBackPaint.setStyle(Style.FILL);
		mBackPaint.setTextSize(mTextSize);
		// �����e�e��ø�s�奻���e�M��
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	/**
	 * �e���ݩ�
	 */

	private void setOutterPaint() {
		mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
		mOutterPaint.setAntiAlias(true);
		mOutterPaint.setDither(true);
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
		mOutterPaint.setStyle(Style.FILL);
		mOutterPaint.setStrokeWidth(20);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;

			mPath.moveTo(mLastX, mLastY);
			break;
		case MotionEvent.ACTION_MOVE:

			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);

			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}

			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
			new Thread(mRunnable).start();
			break;
		}

		invalidate();
		return true;
	}

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			int w = getWidth();
			int h = getHeight();
			float wipeArea = 0;
			float totalArea = w * h;
			Bitmap bitmap = mBitmap;
			int[] mPixels = new int[w * h];

			// ��obitmap�W�Ҧ������T��
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j * w;

					if (mPixels[index] == 0) {
						wipeArea++;
					}
				}
			}

			if (wipeArea > 0 && totalArea > 0) {
				int percent = (int) (wipeArea * 100 / totalArea);
				Log.e("TAG", percent + "");

				if (percent > 60) {
					// �M���ϼh�ϰ�
					mComplete = true;
					postInvalidate();
				}
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {

		// canvas.drawBitmap(Bitmap, 0, 0, null);
		canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
				getHeight() / 2 + mTextBound.height() / 2, mBackPaint);

		if (!mComplete) {
			drawPath();
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

		if (mComplete) {
			if (mListener != null) {
				mListener.complete();
			}
		}
	}

	private void drawPath() {
		mOutterPaint.setStyle(Style.STROKE);
		mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		mCanvas.drawPath(mPath, mOutterPaint);
	}

	/**
	 * ��l��
	 */
	private void init() {
		mOutterPaint = new Paint();
		mPath = new Path();

		// Bitmap=BitmapFactory.decodeResource(getResources(),
		// com.example.scratchcard.R.drawable.test123);
		mOutterBitmap = BitmapFactory.decodeResource(getResources(),
				com.example.scratchcard.R.drawable.test123);

		mText = "���´f�U";
		mTextBound = new Rect();
		mBackPaint = new Paint();
		mTextSize = (int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
						getResources().getDisplayMetrics());
	}
}
