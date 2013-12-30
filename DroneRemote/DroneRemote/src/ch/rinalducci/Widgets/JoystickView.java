/*****************************
 * JoystickView
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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import ch.rinalducci.DroneRemote.R;

public class JoystickView extends View
{
	public static final int INVALID_POINTER_ID = -1;

	private static final boolean D = false;
	String TAG = "JoystickView";

	// joystick model
	private        Paint  bgPaint;
	private        Paint  handlePaint;
	private        Paint  stickPaint;
	private        Paint  basePaint;
	private        Paint  baseStrokePaint;
	private static Bitmap handleBitmap;
	private static Bitmap handleResizedBitmap;
	private        Matrix scaleMatrix;

	private int innerPadding;
	@SuppressWarnings("unused")
	private int bgRadius;
	private int handleRadius;
	private int movementRadius;
	private int handleInnerBoundaries;

	private JoystickMovedListener   moveListener;
	private JoystickClickedListener clickListener;

	//# of pixels movement required between reporting to the listener
	private float moveResolution;

	private boolean yAxisInverted;
	private boolean autoReturnToCenter;

	//Max range of movement in user coordinate system
	public final static int CONSTRAIN_BOX    = 0;
	public final static int CONSTRAIN_CIRCLE = 1;
	private int   movementConstraint;
	private float movementRange;

	public final static int COORDINATE_CARTESIAN    = 0;        //Regular cartesian coordinates
	public final static int COORDINATE_DIFFERENTIAL = 1;    //Uses polar rotation of 45 degrees to calc differential drive paramaters
	private int userCoordinateSystem;

	//Records touch pressure for click handling
	private float   touchPressure;
	private boolean clicked;
	private float   clickThreshold;

	//Last touch point in view coordinates
	private int pointerId = INVALID_POINTER_ID;
	private float touchX, touchY;

	//Last reported position in view coordinates (allows different reporting sensitivities)
	private float reportX, reportY;

	//Handle center in view coordinates
	private float handleX, handleY;

	//Center of the view in view coordinates
	private int cX, cY;

	//Size of the view in view coordinates
	private int dimX;//, dimY;

	//Cartesian coordinates of last touch point - joystick center is (0,0)
	private int cartX, cartY;

	//Polar coordinates of the touch point from joystick center
	private double radial;
	private double angle;

	//User coordinates of last touch point
	private int userX, userY;

	//Offset co-ordinates (used when touch events are received from parent's coordinate origin)
	private int offsetX;
	private int offsetY;

	private boolean showCircle = false;

	/**
	 * *****************************************
	 * Constructors
	 * <p/>
	 * *******************************************
	 */
	public JoystickView(Context context)
	{
		super(context);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initJoystickView();
	}

	/**
	 * *****************************************
	 * Initialization
	 * <p/>
	 * *******************************************
	 */
	private void initJoystickView()
	{
		setFocusable(true);

		bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(Color.GRAY);
		bgPaint.setStrokeWidth(1);
		bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		bgPaint.setDither(true);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(Color.DKGRAY);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		handlePaint.setDither(true);

		stickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//stickPaint.setColor(Color.DKGRAY);
		stickPaint.setColor(getResources().getColor(R.color.gray));
		stickPaint.setStrokeWidth(15);
		stickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		stickPaint.setStrokeJoin(Paint.Join.ROUND);
		stickPaint.setStrokeCap(Paint.Cap.ROUND);
		stickPaint.setDither(true);

		basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//basePaint.setColor(Color.rgb(0x40, 0x40, 0x20));
		//basePaint.setColor(Color.GRAY);
		basePaint.setColor(Color.rgb(10, 83, 156));
		basePaint.setStrokeWidth(1);
		basePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		basePaint.setDither(true);
		basePaint.setShader(new RadialGradient(0, 0, 90, getResources().getColor(R.color.blue_light), getResources().getColor(R.color.blue_dark_custom), Shader.TileMode.MIRROR));

		baseStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		baseStrokePaint.setColor(getResources().getColor(R.color.gray));
		baseStrokePaint.setStrokeWidth(3);
		baseStrokePaint.setStyle(Paint.Style.STROKE);
		baseStrokePaint.setDither(true);

		//Grey Size
		innerPadding = 1000;

		setMovementRange(50);
		setMoveResolution(1.0f);
		setClickThreshold(0.4f);
		setYAxisInverted(true);
		setUserCoordinateSystem(COORDINATE_CARTESIAN);
		setAutoReturnToCenter(true);
	}

	public void setHandleColor(int color)
	{
		this.handlePaint.setColor(color);
	}

	public void setHandleBitmap(Bitmap bitmap)
	{
		JoystickView.handleBitmap = bitmap;
	}

	public void setAutoReturnToCenter(boolean autoReturnToCenter)
	{
		this.autoReturnToCenter = autoReturnToCenter;
	}

	public boolean isAutoReturnToCenter()
	{
		return autoReturnToCenter;
	}

	public void setUserCoordinateSystem(int userCoordinateSystem)
	{
		if (userCoordinateSystem < COORDINATE_CARTESIAN || movementConstraint > COORDINATE_DIFFERENTIAL)
			Log.e(TAG, "invalid value for userCoordinateSystem");
		else this.userCoordinateSystem = userCoordinateSystem;
	}

	public int getUserCoordinateSystem()
	{
		return userCoordinateSystem;
	}

	public void setMovementConstraint(int movementConstraint)
	{
		if (movementConstraint < CONSTRAIN_BOX || movementConstraint > CONSTRAIN_CIRCLE)
			Log.e(TAG, "invalid value for movementConstraint");
		else this.movementConstraint = movementConstraint;
	}

	public int getMovementConstraint()
	{
		return movementConstraint;
	}

	public boolean isYAxisInverted()
	{
		return yAxisInverted;
	}

	public void setYAxisInverted(boolean yAxisInverted)
	{
		this.yAxisInverted = yAxisInverted;
	}

	/**
	 * Set the pressure sensitivity for registering a click
	 *
	 * @param clickThreshold threshold 0...1.0f inclusive. 0 will cause clicks to never be reported, 1.0 is a very hard click
	 */
	public void setClickThreshold(float clickThreshold)
	{
		if (clickThreshold < 0 || clickThreshold > 1.0f)
			Log.e(TAG, "clickThreshold must range from 0...1.0f inclusive");
		else this.clickThreshold = clickThreshold;
	}

	public float getClickThreshold()
	{
		return clickThreshold;
	}

	public void setMovementRange(float movementRange)
	{
		this.movementRange = movementRange;
	}

	public float getMovementRange()
	{
		return movementRange;
	}

	public void setMoveResolution(float moveResolution)
	{
		this.moveResolution = moveResolution;
	}

	public float getMoveResolution()
	{
		return moveResolution;
	}

	/**
	 * *****************************************
	 * Public Methods
	 * <p/>
	 * *******************************************
	 */

	public void setOnJostickMovedListener(JoystickMovedListener listener)
	{
		this.moveListener = listener;
	}

	public void setOnJostickClickedListener(JoystickClickedListener listener)
	{
		this.clickListener = listener;
	}

	/**
	 * *****************************************
	 * Drawing Functionality
	 * <p/>
	 * *******************************************
	 */

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// Here we make sure that we have a perfect circle
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);

		int d = Math.min(getMeasuredWidth(), getMeasuredHeight());

		dimX = d;
		//dimY = d;

		cX = d / 2;
		cY = d / 2;

		bgRadius = dimX / 2 - innerPadding;
		//handleRadius = (int)(d * 0.25);
		handleRadius = (int) (d * 0.22);
		handleInnerBoundaries = handleRadius;
		movementRadius = Math.min(cX, cY) - handleInnerBoundaries;

		if (handleBitmap != null)
			handleResizedBitmap = Bitmap.createScaledBitmap(handleBitmap, handleRadius * 2, handleRadius * 2, false);
		else showCircle = true;
	}

	private int measure(int measureSpec)
	{
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED)
		{
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		}
		else
		{
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		// Draw the background
		//canvas.drawCircle(cX, cY, bgRadius, bgPaint);

		// Draw the handle
		handleX = touchX + cX;
		handleY = touchY + cY;
		canvas.drawCircle(cX, cY, handleRadius >> 1, basePaint);
		canvas.drawCircle(cX, cY, handleRadius >> 1, baseStrokePaint);
		canvas.drawLine(cX, cY, handleX, handleY, stickPaint);

		if (showCircle) canvas.drawCircle(handleX, handleY, handleRadius, handlePaint);
		else
		{
			scaleMatrix = new Matrix();
			scaleMatrix.postTranslate(handleX - handleRadius, handleY - handleRadius);
			canvas.drawBitmap(handleResizedBitmap, scaleMatrix, null);
		}

		if (D)
		{
			Log.d(TAG, String.format("(%.0f, %.0f)", touchX, touchY));
			Log.d(TAG, String.format("(%.0f, %.0f\u00B0)", radial, angle * 180.0 / Math.PI));
			Log.d(TAG, String.format("touch(%f,%f)", touchX, touchY));
			Log.d(TAG, String.format("onDraw(%.1f,%.1f)\n\n", handleX, handleY));
		}
		canvas.restore();
	}

	// Constrain touch within a box
	private void constrainBox()
	{
		touchX = Math.max(Math.min(touchX, movementRadius), -movementRadius);
		touchY = Math.max(Math.min(touchY, movementRadius), -movementRadius);
	}

	// Constrain touch within a circle
	private void constrainCircle()
	{
		float diffX = touchX;
		float diffY = touchY;
		double radial = Math.sqrt((diffX * diffX) + (diffY * diffY));
		if (radial > movementRadius)
		{
			touchX = (int) ((diffX / radial) * movementRadius);
			touchY = (int) ((diffY / radial) * movementRadius);
		}
	}

	public void setPointerId(int id)
	{
		this.pointerId = id;
	}

	public int getPointerId()
	{
		return pointerId;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_MOVE:
			{
				return processMoveEvent(ev);
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			{
				if (pointerId != INVALID_POINTER_ID)
				{
					if (D) Log.d(TAG, "ACTION_UP");
					returnHandleToCenter();
					setPointerId(INVALID_POINTER_ID);
				}
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				if (pointerId != INVALID_POINTER_ID)
				{
					final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					final int pointerId = ev.getPointerId(pointerIndex);
					if (pointerId == this.pointerId)
					{
						if (D) Log.d(TAG, "ACTION_POINTER_UP: " + pointerId);
						returnHandleToCenter();
						setPointerId(INVALID_POINTER_ID);
						return true;
					}
				}
				break;
			}
			case MotionEvent.ACTION_DOWN:
			{
				if (pointerId == INVALID_POINTER_ID)
				{
					int x = (int) ev.getX();
					if (x >= offsetX && x < offsetX + dimX)
					{
						setPointerId(ev.getPointerId(0));
						if (D) Log.d(TAG, "ACTION_DOWN: " + getPointerId());
						return true;
					}
				}
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				if (pointerId == INVALID_POINTER_ID)
				{
					final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					final int pointerId = ev.getPointerId(pointerIndex);
					int x = (int) ev.getX(pointerId);
					if (x >= offsetX && x < offsetX + dimX)
					{
						if (D) Log.d(TAG, "ACTION_POINTER_DOWN: " + pointerId);
						setPointerId(pointerId);
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

	private boolean processMoveEvent(MotionEvent ev)
	{
		if (pointerId != INVALID_POINTER_ID)
		{
			final int pointerIndex = ev.findPointerIndex(pointerId);

			// Translate touch position to center of view
			float x = ev.getX(pointerIndex);
			touchX = x - cX - offsetX;
			float y = ev.getY(pointerIndex);
			touchY = y - cY - offsetY;

			if (D) Log.d(TAG, String.format("ACTION_MOVE: (%03.0f, %03.0f) => (%03.0f, %03.0f)", x, y, touchX, touchY));

			reportOnMoved();
			invalidate();

			touchPressure = ev.getPressure(pointerIndex);
			reportOnPressure();

			return true;
		}
		return false;
	}

	private void reportOnMoved()
	{
		if (movementConstraint == CONSTRAIN_CIRCLE) constrainCircle();
		else constrainBox();

		calcUserCoordinates();

		if (moveListener != null)
		{
			boolean rx = Math.abs(touchX - reportX) >= moveResolution;
			boolean ry = Math.abs(touchY - reportY) >= moveResolution;
			if (rx || ry)
			{
				this.reportX = touchX;
				this.reportY = touchY;

				if (D) Log.d(TAG, String.format("moveListener.OnMoved(%d,%d)", (int) userX, (int) userY));
				//moveListener.OnMoved(userX, userY);
				moveListener.OnMoved(cartX, cartY);
			}
		}
	}

	private void calcUserCoordinates()
	{
		//First convert to cartesian coordinates
		cartX = (int) (touchX / movementRadius * movementRange);
		cartY = (int) (touchY / movementRadius * movementRange);

		radial = Math.sqrt((cartX * cartX) + (cartY * cartY));
		angle = Math.atan2(cartY, cartX);

		//Invert Y axis if requested
		if (!yAxisInverted) cartY *= -1;

		if (userCoordinateSystem == COORDINATE_CARTESIAN)
		{
			userX = cartX;
			userY = cartY;
		}
		else if (userCoordinateSystem == COORDINATE_DIFFERENTIAL)
		{
			userX = cartY + cartX / 4;
			userY = cartY - cartX / 4;

			if (userX < -movementRange) userX = (int) -movementRange;
			if (userX > movementRange) userX = (int) movementRange;

			if (userY < -movementRange) userY = (int) -movementRange;
			if (userY > movementRange) userY = (int) movementRange;
		}

	}

	//Simple pressure click
	private void reportOnPressure()
	{
		if (D) Log.d(TAG, String.format("touchPressure=%.2f", this.touchPressure));
		if (clickListener != null)
		{
			if (clicked && touchPressure < clickThreshold)
			{
				clickListener.OnReleased();
				this.clicked = false;
				if (D) Log.d(TAG, "reset click");
				invalidate();
			}
			else if (!clicked && touchPressure >= clickThreshold)
			{
				clicked = true;
				clickListener.OnClicked();
				if (D) Log.d(TAG, "click");
				invalidate();
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			}
		}
	}

	private void returnHandleToCenter()
	{
		if (autoReturnToCenter)
		{
			final int numberOfFrames = 5;
			final double intervalsX = (0 - touchX) / numberOfFrames;
			final double intervalsY = (0 - touchY) / numberOfFrames;

			for (int i = 0; i < numberOfFrames; i++)
			{
				final int j = i;
				postDelayed(new Runnable()
				{
					public void run()
					{
						touchX += intervalsX;
						touchY += intervalsY;

						reportOnMoved();
						invalidate();

						if (moveListener != null && j == numberOfFrames - 1)
						{
							moveListener.OnReturnedToCenter();
						}
					}
				}, i * 40);
			}

			if (moveListener != null)
			{
				moveListener.OnReleased();
			}
		}
	}

	public void setTouchOffset(int x, int y)
	{
		offsetX = x;
		offsetY = y;
	}
}
