/*****************************
 * DualJoystickView
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DualJoystickView extends LinearLayout
{
	@SuppressWarnings("unused")
	private static final String TAG = DualJoystickView.class.getSimpleName();

	private final boolean D = false;
	private Paint dbgPaint1;

	private JoystickView stickL;
	private JoystickView stickR;

	private View pad;

	public DualJoystickView(Context context)
	{
		super(context);
		stickL = new JoystickView(context);
		stickR = new JoystickView(context);
		initDualJoystickView();
	}

	public DualJoystickView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		stickL = new JoystickView(context, attrs);
		stickR = new JoystickView(context, attrs);
		initDualJoystickView();
	}

	private void initDualJoystickView()
	{
		setOrientation(LinearLayout.HORIZONTAL);
		stickL.setHandleColor(Color.rgb(10, 83, 156));
		stickR.setHandleColor(Color.rgb(10, 83, 156));

		if (D)
		{
			dbgPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			dbgPaint1.setColor(Color.CYAN);
			dbgPaint1.setStrokeWidth(1);
			dbgPaint1.setStyle(Paint.Style.STROKE);
		}

		pad = new View(getContext());
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		removeView(stickL);
		removeView(stickR);

		float padW = getMeasuredWidth() - (getMeasuredHeight() * 2);
		int joyWidth = (int) ((getMeasuredWidth() - padW) / 2);
		LayoutParams joyLParams = new LayoutParams(joyWidth, getMeasuredHeight());

		stickL.setLayoutParams(joyLParams);
		stickR.setLayoutParams(joyLParams);

		stickL.TAG = "L";
		stickR.TAG = "R";
		stickL.setPointerId(JoystickView.INVALID_POINTER_ID);
		stickR.setPointerId(JoystickView.INVALID_POINTER_ID);

		addView(stickL);

		ViewGroup.LayoutParams padLParams = new ViewGroup.LayoutParams((int) padW, getMeasuredHeight());
		removeView(pad);
		pad.setLayoutParams(padLParams);
		addView(pad);

		addView(stickR);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, r, b);
		stickR.setTouchOffset(stickR.getLeft(), stickR.getTop());
	}

	public void setAutoReturnToCenter(boolean left, boolean right)
	{
		stickL.setAutoReturnToCenter(left);
		stickR.setAutoReturnToCenter(right);
	}

	public void setHandleBitmap(Bitmap bitmap)
	{
		stickL.setHandleBitmap(bitmap);
		stickR.setHandleBitmap(bitmap);
	}

	public void setOnJostickMovedListener(JoystickMovedListener left, JoystickMovedListener right)
	{
		stickL.setOnJostickMovedListener(left);
		stickR.setOnJostickMovedListener(right);
	}

	public void setOnJostickClickedListener(JoystickClickedListener left, JoystickClickedListener right)
	{
		stickL.setOnJostickClickedListener(left);
		stickR.setOnJostickClickedListener(right);
	}

	public void setYAxisInverted(boolean leftYAxisInverted, boolean rightYAxisInverted)
	{
		stickL.setYAxisInverted(leftYAxisInverted);
		stickR.setYAxisInverted(rightYAxisInverted);
	}

	public void setMovementConstraint(int movementConstraint)
	{
		stickL.setMovementConstraint(movementConstraint);
		stickR.setMovementConstraint(movementConstraint);
	}

	public void setMovementRange(float movementRangeLeft, float movementRangeRight)
	{
		stickL.setMovementRange(movementRangeLeft);
		stickR.setMovementRange(movementRangeRight);
	}

	public void setMoveResolution(float leftMoveResolution, float rightMoveResolution)
	{
		stickL.setMoveResolution(leftMoveResolution);
		stickR.setMoveResolution(rightMoveResolution);
	}

	public void setUserCoordinateSystem(int leftCoordinateSystem, int rightCoordinateSystem)
	{
		stickL.setUserCoordinateSystem(leftCoordinateSystem);
		stickR.setUserCoordinateSystem(rightCoordinateSystem);
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		if (D)
		{
			canvas.drawRect(1, 1, getMeasuredWidth() - 1, getMeasuredHeight() - 1, dbgPaint1);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		boolean l = stickL.dispatchTouchEvent(ev);
		boolean r = stickR.dispatchTouchEvent(ev);
		return l || r;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		boolean l = stickL.onTouchEvent(ev);
		boolean r = stickR.onTouchEvent(ev);
		return l || r;
	}
}
