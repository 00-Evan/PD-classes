/*
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.noosa;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Texture;

import java.util.LinkedHashMap;

public class RenderedText extends Image {

	private static Canvas canvas = new Canvas();

	private static Typeface font;

	//this is basically a LRU cache. capacity is determined by character count, not entry count.
	private static LinkedHashMap<String, CachedText> textCache =
			new LinkedHashMap<String, CachedText>(100, 0.75f, true){
				private int cachedChars = 0;
				private final int MAX_CACHED = 1000;

				@Override
				public CachedText put(String key, CachedText value) {
					cachedChars += value.length;
					return super.put(key, value);
				}

				@Override
				public CachedText remove(Object key) {
					CachedText removed = super.remove(key);
					if (removed != null) cachedChars-= removed.length;
					return removed;
				}

				@Override
				public void clear() {
					super.clear();
					cachedChars = 0;
				}

				@Override
				protected boolean removeEldestEntry(Entry eldest) {
					return cachedChars > MAX_CACHED;
				}
	};

	private int size;
	private String text;

	public RenderedText( ){
		text = null;
	}

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

	public void size( int size ){
		this.size = size;
		render();
	}

	public float baseLine(){
		return size * scale.y;
	}

	private void render(){
		render(font);
	}

	private void render(Typeface font){
		if ( text == null || text.equals("") ) {
			text = "";
			width=height=0;
			visible = false;
			return;
		} else {
			visible = true;
		}

		String key = "text:" + size + " " + text;
		if (textCache.containsKey(key)){
			CachedText text = textCache.get(key);
			texture = text.texture;
			frame(text.rect);
		} else {

			Paint strokePaint = new Paint();
			strokePaint.setARGB(0xff, 0, 0, 0);
			strokePaint.setTextSize(size);
			strokePaint.setStyle(Paint.Style.STROKE);
			strokePaint.setAntiAlias(true);
			strokePaint.setStrokeWidth(size / 5f);

			Paint textPaint = new Paint();
			textPaint.setTextSize(size);
			textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
			textPaint.setAntiAlias(true);

			if (font != null) {
				textPaint.setTypeface(font);
				strokePaint.setTypeface(font);
			}

			int right = (int)(strokePaint.measureText(text)+ (size/5f));
			int bottom = (int)(-strokePaint.ascent() + strokePaint.descent()+ (size/5f));
			//bitmap has to be in a power of 2 for some devices (as we're using openGL methods to render to texture)
			Bitmap bitmap = Bitmap.createBitmap(Integer.highestOneBit(right)*2, Integer.highestOneBit(bottom)*2, Bitmap.Config.ARGB_4444);
			bitmap.eraseColor(0x00000000);

			canvas.setBitmap(bitmap);
			canvas.drawText(text, (size/10f), size, strokePaint);
			canvas.drawText(text, (size/10f), size, textPaint);
			texture = new SmartTexture(bitmap, Texture.NEAREST, Texture.CLAMP, true);

			RectF rect = texture.uvRect(0, 0, right, bottom);
			frame(rect);

			CachedText toCache = new CachedText();
			toCache.rect = rect;
			toCache.texture = texture;
			toCache.length = text.length();
			textCache.put("text:" + size + " " + text, toCache);
		}
	}

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		//the y value is set at the top of the character, not at the top of accents.
		Matrix.translate( matrix, 0, -(baseLine()*0.15f)/scale.y );
	}

	public static void clearCache(){
		textCache.clear();
	}

	public static void reloadCache(){
		for (CachedText txt : textCache.values()){
			txt.texture.reload();
		}
	}

	public static void setFont(String asset){
		font = Typeface.createFromAsset(Game.instance.getAssets(), asset);
		clearCache();
	}

	private class CachedText{
		public SmartTexture texture;
		public RectF rect;
		public int length;
	}
}
