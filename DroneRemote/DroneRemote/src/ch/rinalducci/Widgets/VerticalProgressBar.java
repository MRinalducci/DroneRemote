/*****************************
 * VerticalProgressBar
 * ----------------
 *
 * @author Marco Rinalducci
 * @version 1.0.0
 *
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Marco Rinalducci
 *
 *****************************/

package ch.rinalducci.Widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

public class VerticalProgressBar extends ProgressBar
{
	private int x, y, z, w;

	@Override
	protected void drawableStateChanged()
	{
		//
		super.drawableStateChanged();
	}

	public VerticalProgressBar(Context context)
	{
		super(context);
	}

	public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public VerticalProgressBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(h, w, oldh, oldw);
		this.x = w;
		this.y = h;
		this.z = oldw;
		this.w = oldh;
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	protected void onDraw(Canvas c)
	{
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		super.onDraw(c);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (!isEnabled())
		{
			return false;
		}

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:

				setSelected(true);
				setPressed(true);
				break;
			case MotionEvent.ACTION_MOVE:
				setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
				onSizeChanged(getWidth(), getHeight(), 0, 0);

				break;
			case MotionEvent.ACTION_UP:
				setSelected(false);
				setPressed(false);
				break;

			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return true;
	}

	@Override
	public synchronized void setProgress(int progress)
	{

		if (progress >= 0) super.setProgress(progress);

		else super.setProgress(0);
		onSizeChanged(x, y, z, w);

	}
}