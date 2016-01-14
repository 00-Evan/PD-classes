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

import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderedTextMultiline extends Component {

	private int maxWidth = Integer.MAX_VALUE;
	public int nLines;

	private String text;
	private List<String> tokens;
	private ArrayList<RenderedText> words = new ArrayList<>();

	private int size;
	private float zoom;
	private int color = -1;

	private static final String SPACE = " ";
	private static final String NEWLINE = "\n";


	public RenderedTextMultiline(int size){
		this.size = size;
	}

	public RenderedTextMultiline(String text, int size){
		this.size = size;
		text(text);
	}

	public void text(String text){
		this.text = text;

		if (text != null && !text.equals("")) {
			//conversion for chinese text
			text = text.replaceAll("，", ", ");
			text = text.replaceAll("， _", "，_ ");
			text = text.replaceAll("。", "。 ");
			text = text.replaceAll("。 _", "。_ ");
			tokens = Arrays.asList(text.split("(?<= )|(?= )|(?<=\n)|(?=\n)"));
			build();
		}
	}

	public void text(String text, int maxWidth){
		this.maxWidth = maxWidth;
		text(text);
	}

	public String text(){
		return text;
	}

	public void maxWidth(int maxWidth){
		if (this.maxWidth != maxWidth){
			this.maxWidth = maxWidth;
			layout();
		}
	}

	public int maxWidth(){
		return maxWidth;
	}

	private void build(){
		clear();
		words = new ArrayList<>();
		for (String str : tokens){
			if (!str.equals(SPACE) && !str.equals(NEWLINE)){
				RenderedText word;
				if (str.startsWith("_") && str.endsWith("_")){
					word = new RenderedText(str.substring(1, str.length()-1), size);
					word.hardlight(0xFFFF44);
				} else {
					word = new RenderedText(str, size);
					if (color != -1) word.hardlight(color);
				}
				word.scale.set(zoom);
				words.add(word);
				add(word);

				if (height < word.baseLine()) height = word.baseLine();

			} else if (str.equals(NEWLINE)){
				words.add(null);
			}
		}
		layout();
	}

	public void zoom(float zoom){
		this.zoom = zoom;
		for (RenderedText word : words) {
			if (word != null) word.scale.set(zoom);
		}
	}

	public void hardlight(int color){
		this.color = color;
		for (RenderedText word : words) {
			if (word != null) word.hardlight( color );
		}
	}

	public void invert(){
		if (words != null) {
			for (RenderedText word : words) {
				if (word != null) {
					word.ra = 0.77f;
					word.ga = 0.73f;
					word.ba = 0.62f;
					word.rm = -0.77f;
					word.gm = -0.73f;
					word.bm = -0.62f;
				}
			}
		}
	}

	@Override
	protected void layout() {
		super.layout();
		float x = this.x;
		float y = this.y;
		float height = 0;
		nLines = 1;

		for (RenderedText word : words){
			if (word == null) {
				//newline
				y += height;
				x = this.x;
				nLines++;
			} else {
				if (word.height() > height) height = word.baseLine();

				if ((x - this.x) + word.width() > maxWidth){
					y += height;
					x = this.x;
					nLines++;
				}

				word.x = x;
				word.y = y;
				x += word.width() + 2;
				if ((x - this.x) > width) width = (x - this.x);

			}
		}
		this.height = (y - this.y) + height;
	}
}
