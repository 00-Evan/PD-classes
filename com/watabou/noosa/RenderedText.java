package com.watabou.noosa;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Evan on 10/01/2016.
 */
public class RenderedText extends Image {

	private static Canvas canvas = new Canvas();
	private int size;
	private String text;

	public RenderedText( int size ){
		text = null;
		this.size = size;
	}

	public RenderedText(String text, int size){
		this.text = text;
		this.size = size;

		render();
	}

	public void text( String text ){
		this.text = text;

		render();
	}

	public String text(){
		return text;
	}

	public float baseLine(){
		return size * scale.y;
	}

	private void render(){
		if (text == null) text = "";
		Bitmap bitmap = Bitmap.createBitmap(size*text.length(), (int)(size*1.25f), Bitmap.Config.ARGB_4444);
		bitmap.eraseColor(0x00000000);

		Paint strokePaint = new Paint();
		strokePaint.setARGB(0xff, 0, 0, 0);
		strokePaint.setTextSize(size);
		strokePaint.setStyle(Paint.Style.STROKE);
		//strokePaint.setTextScaleX(0.9f);
		strokePaint.setStrokeWidth(size/5);

		Paint textPaint = new Paint();
		textPaint.setTextSize(size);
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		//textPaint.setTextScaleX(0.9f);

		canvas.setBitmap(bitmap);
		canvas.drawText(text, 0, Math.round(size*0.85f), strokePaint);
		canvas.drawText(text, 0, Math.round(size*0.85f), textPaint);

		int right = bitmap.getWidth();
		boolean found = false;
		while(!found){
			for(int y = 0; y < bitmap.getHeight(); y++){
				if (bitmap.getPixel(right-1, y) != 0x00000000)
					found = true;
			}
			if (!found) right--;
		}

		int bottom = bitmap.getHeight();
		found = false;
		while(!found){
			for(int x = 0; x < bitmap.getWidth(); x++){
				if (bitmap.getPixel(x, bottom-1) != 0x00000000)
					found = true;
			}
			if (!found) bottom--;
		}

		texture( bitmap );
		frame( texture.uvRect(0, 0, right, bottom));
	}
}
