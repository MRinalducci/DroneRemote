/****************************
 * DroneRemoteActivity
 * ---------------
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
 ****************************/

package ch.rinalducci.DroneRemote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.makeramen.rounded.RoundedImageView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ch.rinalducci.Toolbox.RinalTool;
import ch.rinalducci.Widgets.DualJoystickView;
import ch.rinalducci.Widgets.JoystickMovedListener;
import ch.rinalducci.Widgets.VerticalProgressBar;

@SuppressWarnings("ConstantConditions")
public class DroneRemoteActivity extends Activity implements OnSharedPreferenceChangeListener, SensorEventListener
{

	public static Context context;

	// Debug / logs
	private              boolean D   = false;
	private static final String  TAG = DroneRemoteActivity.class.getSimpleName();

	// Layout view
	private TextView            mStatus;
	private TextView            mCountUp;
	private TextView            mCountDown;
	private TextView            mPower;
	private TextView            mCharge;
	private TextView            mTemp;
	private TextView            mGazPercent;
	private VerticalProgressBar mProgressGaz;
	private ToggleButton        mtoggleButtonCap;
	private ToggleButton        mtoggleButtonTanguage;
	private ToggleButton        mtoggleButtonRoulis;
	private ToggleButton        mtoggleButtonLed;
	private ToggleButton        mtoggleButtonRec;
	private boolean             mtoggleCap;
	private boolean             mtoggleTanguage;
	private boolean             mtoggleRoulis;
	private int                 mtoggleLed;
	private int                 mtoggleRec;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT      = 2;
	private static final int TTS_DATA_CHECK         = 3;

	// TextToSpeech
	private TextToSpeech tts       = null;
	private boolean      ttsIsInit = false;

	// Language
	private Resources lang     = null;
	private boolean   langFlag = false;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ         = 2;
	public static final int MESSAGE_DEVICE_NAME  = 3;
	public static final int MESSAGE_TOAST        = 4;

	// Key names received from the BluetoothCommandService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST       = "toast";

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	// Member object for Bluetooth Command Service
	private BluetoothCommandService mCommandService  = null;
	private boolean                 atemptConnecting = false;

	//private       int readCount  = 0;
	private final int startFrame = 240;
	private final int endFrame   = 250;

	// Polar coordinates
	private final int corrAxe = 50;
	private final int maxAxe  = 100;
	private       int mGaz    = 0, mCap = corrAxe;
	private int mTanguage = corrAxe, mRoulis = corrAxe;
	private boolean mCenterL = true, mCenterR = true;
	private boolean mArme      = false;
	private boolean ArmeDearme = false;

	// Timer task
	private Timer mUpdateTimer;
	private int     mTimeoutCounter  = 0;
	private int     mMaxTimeoutCount = 10;
	private long    mUpdatePeriod    = 200;
	private boolean mRequestUpdate   = false;

	private Timer mGazTimer;
	private long    mGazPeriod  = 100;
	private int     mRealGaz    = 0;
	private int     mSubmitGaz  = 0;
	private int     mGazMaxMin  = 15;
	final   Handler mGazHandler = new Handler();

	final   Handler mCountUpHandler = new Handler();
	private long    elapsedTime     = 0;
	private boolean mCountUpOnOff   = false;
	private boolean startCountUp    = false;

	private CountDownTimer mCountDownTimer;
	private long    mCountDownPeriod = 600000;
	private boolean mCountDownOnOff  = false;
	private boolean startCountDown   = true;

	private CountDownTimer ProgressDialogTimer;
	private ProgressDialog progressDialog;

	// Accelerometer
	private SensorManager mSensorManager;
	private Sensor        mAccelerometer;
    private Sensor        mMagnetic;

	private final int corrAxeYAcc = 110;
	private final int corrAxeXAcc = 60;
	private       int mAccCounter = 0;
	private       int yRealAcc    = corrAxeYAcc, zRealAcc = corrAxeXAcc;
	private boolean mCamera = false;

	@SuppressWarnings("UnusedDeclaration")
    float[] acceleromterVector=new float[3];
	@SuppressWarnings("UnusedDeclaration")
    float[] magneticVector=new float[3];
	@SuppressWarnings("UnusedDeclaration")
    float[] resultMatrix=new float[9];
	@SuppressWarnings("UnusedDeclaration")
    float[] values=new float[3];
	@SuppressWarnings("UnusedDeclaration")
    float x = 0, y = 0, z = 0;

	// Vibrator
	Vibrator vibr;
	int    onVibr  = 200;
	int    offVibr = 200;
	long[] pattern = { // Vibration pattern
					   0,  // Start immediately
					   onVibr, offVibr, onVibr, offVibr, onVibr, offVibr};

	// Preferences
	SharedPreferences pref;
	Editor            prefEditor;
	private int mTrimCap = 0, mTrimTanguage = 0, mTrimRoulis = 0;
	private int mTrimXCam = 10, mTrimYCam = 10;
	private float mSensibilityCap = 4, mSensibilityTanguage = 4, mSensibilityRoulis = 4;
	private boolean mTTS       = true;
	private boolean mRead      = false;
	private boolean mInvertGaz = false, mInvertCap = false, mInvertTanguage = false, mInvertRoulis = false;
	private boolean mInvertXCam = false, mInvertYCam = false;
    private final String userManual = "http://dev.rinalducci.ch/usermanual.pdf";

	/*********************************************************************************
	 *
	 * @category Smartphone override method
	 *
	 *********************************************************************************/
	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		context = this.getApplicationContext();
		setContentView(R.layout.drone_remote);

		// Init TextToSpeech
		initTextToSpeech();

		// Get language
		lang = getResources();

		// Bind components
		mStatus = (TextView) findViewById(R.id.status);
		mCountUp = (TextView) findViewById(R.id.countup);
		mCountDown = (TextView) findViewById(R.id.countdown);
		mPower = (TextView) findViewById(R.id.power);
		mCharge = (TextView) findViewById(R.id.charge);
		mTemp = (TextView) findViewById(R.id.temp);
		mGazPercent = (TextView) findViewById(R.id.gaz);

		RoundedImageView mPanel = (RoundedImageView) findViewById(R.id.panel);
		mPanel.setScaleType(ScaleType.CENTER_CROP);
		mPanel.setCornerRadius(25);
		mPanel.setBorderWidth(2);
		mPanel.setBorderColor(getResources().getColor(R.color.gray));
		mPanel.setRoundBackground(true);
		mPanel.setImageDrawable(getResources().getDrawable(R.drawable.steel));

		DualJoystickView mDualJoystick = (DualJoystickView) findViewById(R.id.dualjoystickView);
		mDualJoystick.setOnJostickMovedListener(_listenerLeft, _listenerRight);
		mDualJoystick.setAutoReturnToCenter(true, true);
		mDualJoystick.setHandleBitmap(RinalTool.decodeSampledBitmapFromResource(getResources(), R.drawable.round_metal_128_ar, 128, 128));

		mProgressGaz = (VerticalProgressBar) findViewById(R.id.progressGaz);
		mProgressGaz.setEnabled(false);

		mtoggleButtonCap = (ToggleButton) findViewById(R.id.toggleButtonCap);
		mtoggleButtonCap.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				mtoggleButtonTanguage.setChecked(false);
				mtoggleButtonRoulis.setChecked(false);
				if (mtoggleButtonCap.isChecked())
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_cap_select));
					mtoggleCap = true;
					mtoggleTanguage = false;
					mtoggleRoulis = false;
					mtoggleButtonCap.setText(String.valueOf(mTrimCap));
					mRequestUpdate = true;
				}
				else
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_cap_deselect));
					mtoggleCap = false;
					mtoggleTanguage = false;
					mtoggleRoulis = false;
					mRequestUpdate = true;
				}
			}
		});

		mtoggleButtonTanguage = (ToggleButton) findViewById(R.id.toggleButtonTang);
		mtoggleButtonTanguage.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				mtoggleButtonCap.setChecked(false);
				mtoggleButtonRoulis.setChecked(false);
				if (mtoggleButtonTanguage.isChecked())
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_tanguage_select));
					mtoggleTanguage = true;
					mtoggleCap = false;
					mtoggleRoulis = false;
					mtoggleButtonTanguage.setText(String.valueOf(mTrimTanguage));
					mRequestUpdate = true;
				}
				else
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_tanguage_deselect));
					mtoggleTanguage = false;
					mtoggleCap = false;
					mtoggleRoulis = false;
					mRequestUpdate = true;
				}
			}
		});

		mtoggleButtonRoulis = (ToggleButton) findViewById(R.id.toggleButtonRoul);
		mtoggleButtonRoulis.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				mtoggleButtonCap.setChecked(false);
				mtoggleButtonTanguage.setChecked(false);
				if (mtoggleButtonRoulis.isChecked())
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_roulis_select));
					mtoggleRoulis = true;
					mtoggleTanguage = false;
					mtoggleCap = false;
					mtoggleButtonRoulis.setText(String.valueOf(mTrimRoulis));
					mRequestUpdate = true;
				}
				else
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_trim_roulis_deselect));
					mtoggleRoulis = false;
					mtoggleTanguage = false;
					mtoggleCap = false;
					mRequestUpdate = true;
				}
			}
		});

		mtoggleButtonLed = (ToggleButton) findViewById(R.id.toggleButtonLed);
		mtoggleButtonLed.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mtoggleButtonLed.isChecked())
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_led_select));
					mtoggleLed = 1;
					mRequestUpdate = true;
				}
				else
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_led_deselect));
					mtoggleLed = 0;
					mRequestUpdate = true;
				}
			}
		});

		mtoggleButtonRec = (ToggleButton) findViewById(R.id.toggleButtonRec);
		mtoggleButtonRec.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mtoggleButtonRec.isChecked())
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_rec_select));
					mtoggleRec = 1;
					mRequestUpdate = true;
				}
				else
				{
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_rec_deselect));
					mtoggleRec = 0;
					mRequestUpdate = true;
				}
			}
		});

		// Get the sensor service
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Get the accelerometer sensor
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Get the magnetic sensor
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		// Get the vibrator service
		vibr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null)
		{
			Toast.makeText(this, getResources().getString(R.string.bluetooth_not_available), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	/**
	 * Called when the activity will be launched
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	protected void onStart()
	{
		super.onStart();

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		boolean airplaneIsEnabled;
		if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1){
			airplaneIsEnabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
		} else{
			airplaneIsEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		}

		if (!airplaneIsEnabled)
		{
			if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1){
				//
			} else{
				// toggle airplane mode
				Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);

				// Change so that only phone is turned off
				Settings.System.putString(context.getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS, "cell");

				// Reload
				Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				intent.putExtra("state", !airplaneIsEnabled);
				sendBroadcast(intent);
			}
		}

		// Request enable bluetooth
		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

		if (mBluetoothAdapter.isEnabled())
		{
			if (mCommandService == null) initBluetooth();
		}

		// Initialisation of preferences
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);
		loadPref();

		// Timer for update
		mUpdateTimer = new Timer();
		mUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				UpdateMethod();
			}
		}, mUpdatePeriod, mUpdatePeriod);

		// Timer for gaz
		mGazTimer = new Timer();
		mGazTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				GazMethod();
			}
		}, mGazPeriod, mGazPeriod);

		// Timer for CountUp
		mCountUp.setText("00 : 00 : 00");
		Timer mCountUpTimer = new Timer();
		mCountUpTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (mCountUpOnOff && startCountUp)
				{
					elapsedTime = elapsedTime + 1000;
					mCountUpHandler.post(mCountUpRunnable);
				}
			}
		}, 1000, 1000);

		// Timer for CountDown
		mCountDown.setText("00 : 00 : 00");
		mCountDownTimer = new CountDownTimer(mCountDownPeriod, 1000)
		{

			public void onTick(long millisUntilFinished)
			{
				mCountDown.setText(RinalTool.formatTime(millisUntilFinished));

				if (mTTS)
				{
					// Margin because time is in ms
					if (millisUntilFinished <= 61000 && millisUntilFinished >= 60000)
						ttsTalk(getResources().getString(R.string.tts_countdown_1_minute));
					else if (millisUntilFinished <= 31000 && millisUntilFinished >= 30000)
						ttsTalk(getResources().getString(R.string.tts_countdown_30_second));
					else if (millisUntilFinished <= 11000 && millisUntilFinished >= 10000)
						ttsTalk(getResources().getString(R.string.tts_countdown_10_second));
					else if (millisUntilFinished <= 6000 && millisUntilFinished >= 5000)
						ttsTalk(getResources().getString(R.string.tts_countdown_5_second));
				}
			}

			public void onFinish()
			{
				mCountDown.setText("00 : 00 : 00");
				if (mTTS) ttsTalk(getResources().getString(R.string.tts_countdown_finish));
				// Vibrate the pattern and don't repeat
				if (vibr != null) vibr.vibrate(pattern, -1);
			}
		};

		long mProgressDialogPeriod = 3000;
		ProgressDialogTimer = new CountDownTimer(mProgressDialogPeriod, mProgressDialogPeriod)
		{

			public void onTick(long millisUntilFinished)
			{
				// Nothing
			}

			public void onFinish()
			{
				progressDialog.dismiss();
			}
		};
	}

	/**
	 * Called when the activity will be paused
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		// Unset the accelerometer listener
		mSensorManager.unregisterListener(this);
	}

	/**
	 * Called when the activity will be resumed
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		//set accelerometer the listener
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_UI);

		// Check if bluetooth is enabled
		if (mCommandService != null)
		{
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE)
			{
				mCommandService.start();
			}
		}
	}

	/**
	 * Called when the activity will be stopped
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	protected void onStop()
	{
		super.onStop();

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		boolean airplaneIsEnabled;
		if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1){
			airplaneIsEnabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
		} else{
			airplaneIsEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		}

		if (airplaneIsEnabled)
		{
			if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1){
				//
			} else{
				// toggle airplane mode
				Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);

				// Reload
				Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				intent.putExtra("state", !airplaneIsEnabled);
				sendBroadcast(intent);
			}
		}
	}

	/**
	 * Called when the activity will be destroyed
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// Stop bluetooth
		if (mCommandService != null) mCommandService.stop();

		//Close the Text to Speech Library
		if (tts != null)
		{

			tts.stop();
			tts.shutdown();
		}
	}

	/*********************************************************************************
	 *
	 * @category On results
	 *
	 *********************************************************************************/
	/**
	 * Called when the Handler get information back from the mCommandService
	 */
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MESSAGE_STATE_CHANGE:
					switch (msg.arg1)
					{
						case BluetoothCommandService.STATE_CONNECTED:
							mStatus.setText(getResources().getString(R.string.title_connected_to) + " " + mConnectedDeviceName);
							if (mTTS)
								ttsTalk(getResources().getString(R.string.tts_bluetooth_connected_at) + " " + mConnectedDeviceName);
							break;
						case BluetoothCommandService.STATE_CONNECTING:
							mStatus.setText(R.string.title_connecting);
							mPower.setText("0.0V");
							mCharge.setText("0%");
							mTemp.setText("0°C");
							if (mTTS) ttsTalk(getResources().getString(R.string.tts_bluetooth_in_connection));
							atemptConnecting = true;
							break;
						case BluetoothCommandService.STATE_LISTEN:
							//
						case BluetoothCommandService.STATE_NONE:
							mStatus.setText(R.string.title_not_connected);
							mPower.setText("0.0V");
							mCharge.setText("0%");
							mTemp.setText("0°C");
							if (atemptConnecting)
							{
								if (mTTS) ttsTalk(getResources().getString(R.string.tts_bluetooth_not_connected));
								atemptConnecting = false;
							}
							break;
					}
					break;
				case MESSAGE_DEVICE_NAME:
					// Save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					break;
				case MESSAGE_TOAST:
					Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_READ:

					if (mRead)
					{
						try
						{
							// Start = 240, end = 250
							@SuppressWarnings("UnusedDeclaration") int readCount = msg.arg1;

							byte[] dataReadByte = (byte[]) msg.obj;
							int[] dataReadInt = new int[dataReadByte.length];
							int nbDataRead = 4;
							int[] dataComInt = new int[nbDataRead];
							byte[] dataComByte = new byte[nbDataRead];

							for (int i = 0; i < dataReadByte.length; i++)
								dataReadInt[i] = RinalTool.byteToUnsignedInt(dataReadByte[i]);

							boolean startingFrame = false;
							int j = 0;

							for (int i = 0; i < dataReadInt.length; i++)
							{
								// Start frame
								if ((i + nbDataRead + 1) <= dataReadInt.length && !startingFrame)
								{
									if (dataReadInt[i] == startFrame && dataReadInt[i + nbDataRead + 1] == endFrame)
									{
										startingFrame = true;
									}
								}
								// End frame
								else if (dataReadInt[i] == endFrame && startingFrame && j == nbDataRead)
								{

									byte checksumRead = RinalTool.checksumCalc(dataComByte, true, true);
									if (checksumRead == dataComByte[3])
									{
										mPower.setText(String.valueOf((float) dataComInt[0] / 10) + "V");

										mCharge.setText(String.valueOf(dataComInt[1]) + "%");
										if (mTTS) if (dataComInt[1] == 10 || dataComInt[1] == 5 || dataComInt[1] == 0)
											ttsTalk(getResources().getString(R.string.tts_charge) + String.valueOf(dataComInt[1]) + " %");

										mTemp.setText(String.valueOf(dataComInt[2]) + "°C");

										if (D) Log.d(TAG, "Update datas");
										break;
									}
									else
									{
										if (D) Log.i(TAG, "Checksum error: " + checksumRead);
										startingFrame = false;
										j = 0;
									}
								}
								// Get values
								else if (startingFrame && j < nbDataRead)
								{
									dataComInt[j] = dataReadInt[i];
									dataComByte[j] = dataReadByte[i];
									j++;
								}
								else if (j >= nbDataRead)
								{
									Log.i(TAG, "Error! Too much data recieved: " + j + " data");
									startingFrame = false;
									j = 0;
								}
							}
						}
						catch (Exception e)
						{
							Log.e(TAG, "Read error!");
						}
					} break;
			}
		}
	};

	/**
	 * Called when Activity return results
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK)
				{
					// Get the device MAC address
					String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					// Attempt to connect to the device
					mCommandService.connect(device);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK)
				{
					// Bluetooth is now enabled, so initialize bluetooth
					initBluetooth();
				}
				else
				{
					// User did not enable Bluetooth or an error occured
					Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			case TTS_DATA_CHECK:
				// check if text to speech data are available
				if (resultCode == Engine.CHECK_VOICE_DATA_PASS)
				{
					tts = new TextToSpeech(this, new OnInitListener()
					{
						public void onInit(int status)
						{
							if (status == TextToSpeech.SUCCESS)
							{
								ttsIsInit = true;
								// Set text to speech language
								if (tts.isLanguageAvailable(lang.getConfiguration().locale) >= 0)
									tts.setLanguage(lang.getConfiguration().locale);
								tts.setPitch(0.8f);
								tts.setSpeechRate(1.1f);
							}
						}
					});
				}
				else if (resultCode == Engine.CHECK_VOICE_DATA_FAIL)
				{
					Intent installVoice = new Intent(Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installVoice);
				}
		}
	}

	/*********************************************************************************
	 *
	 * @category Option Menu
	 *
	 *********************************************************************************/
	/**
	 * Inflate option menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	/**
	 * Called when option menu button selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent in;
		switch (item.getItemId())
		{
			case R.id.arme:
				// Check if device is connected
				if (mCommandService != null)
				{
					if (mCommandService.getState() == BluetoothCommandService.STATE_CONNECTED)
					{
						// Check if gaz is 0
						if (mSubmitGaz == 0)
						{
							// Arme Drone
							if (!ArmeDearme)
							{
								new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_arme_on)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getResources().getString(R.string.message_yes), new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										ArmeDearme = true;
										mArme = true;
										progressDialogArming(getResources().getString(R.string.message_arme_progress_arme));
										mStatus.setText(getResources().getString(R.string.title_arme));
										if (mTTS) ttsTalk(getResources().getString(R.string.tts_arme));
									}
								}).setNegativeButton(getResources().getString(R.string.message_no), null).show();
							}
							//Dearme Drone
							else
							{
								new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_arme_off)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getResources().getString(R.string.message_yes), new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										ArmeDearme = false;
										mArme = true;
										progressDialogArming(getResources().getString(R.string.message_arme_progress_dearme));
										mStatus.setText(getResources().getString(R.string.title_dearme));
										if (mTTS) ttsTalk(getResources().getString(R.string.tts_dearme));
									}
								}).setNegativeButton(getResources().getString(R.string.message_no), null).show();
							}
						}
						// Gaz not 0
						else
						{
							new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_arme_gaz)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("OK", null).show();
						}
					}
					// Drone not connected
					else
					{
						new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_arme_not_connected)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("OK", null).show();
					}
				}
				return true;
			case R.id.checklist:
				// Launch the DeviceListActivity to see devices and do scan
				in = new Intent(this, ChecklistActivity.class);
				startActivity(in);
				return true;
			case R.id.scan:
				// Launch the DeviceListActivity to see devices and do scan
				in = new Intent(this, DeviceListActivity.class);
				startActivityForResult(in, REQUEST_CONNECT_DEVICE);
				return true;
			case R.id.option:
				// Hide when opening options
				mtoggleButtonCap.setChecked(false);
				mtoggleButtonTanguage.setChecked(false);
				mtoggleButtonRoulis.setChecked(false);
				mtoggleRoulis = false;
				mtoggleTanguage = false;
				mtoggleCap = false;
				// Launch the SettingsActivity to set up
				in = new Intent(this, SettingsActivity.class);
				startActivity(in);
				return true;
			case R.id.emplois:
				new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_propos)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getResources().getString(R.string.message_yes), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						// Launch the browser for PDF
						Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(userManual));
						startActivity(in);
					}
				}).setNegativeButton(getResources().getString(R.string.message_no), null).show();
				return true;
			case R.id.propos:
				new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_propos)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getResources().getString(R.string.message_yes), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						// Launch the MenuLegalsInfosActivity to see legals infos
						Intent in = new Intent(context, LegalsInfosActivity.class);
						startActivity(in);
					}
				}).setNegativeButton(getResources().getString(R.string.message_no), null).show();
				return true;
		}
		return false;
	}

	/**
	 * Method for showing the arming progress Dialog
	 */
	public void progressDialogArming(String message)
	{
		progressDialog = ProgressDialog.show(DroneRemoteActivity.this, getResources().getString(R.string.app_name), message, true, true, null);
		ProgressDialogTimer.start();
	}

	/*********************************************************************************
	 *
	 * @category Smartphone button
	 *
	 *********************************************************************************/
	/**
	 * Called when smartphone key down
	 */
	@SuppressWarnings("NullableProblems")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_VOLUME_UP:
				if (mtoggleCap)
				{
					//ttsTalk(getResources().getString(R.string.tts_trim_augmentation));
					if (mTrimCap < corrAxe) mTrimCap++;

					if (mCap > maxAxe) mCap = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mCap < 0) mCap = 0;
					else mCap++; // Add trim
					mtoggleButtonCap.setText(String.valueOf(mTrimCap));
				}
				else if (mtoggleTanguage)
				{
					//ttsTalk(getResources().getString(R.string.tts_trim_augmentation));
					if (mTrimTanguage < corrAxe) mTrimTanguage++;

					if (mTanguage > maxAxe) mTanguage = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mTanguage < 0) mTanguage = 0;
					else mTanguage++; // Add trim
					mtoggleButtonTanguage.setText(String.valueOf(mTrimTanguage));
				}
				else if (mtoggleRoulis)
				{
					//ttsTalk(getResources().getString(R.string.tts_trim_augmentation));
					if (mTrimRoulis < corrAxe) mTrimRoulis++;

					if (mRoulis > maxAxe) mRoulis = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mRoulis < 0) mRoulis = 0;
					else mRoulis++; // Add trim
					mtoggleButtonRoulis.setText(String.valueOf(mTrimRoulis));
				}
				savePref();
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (mtoggleCap)
				{
					//ttsTalk(getResources().getString(R.string.tts_trim_diminution));
					if (mTrimCap > -corrAxe) mTrimCap--;

					if (mCap > maxAxe) mCap = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mCap <= 0) mCap = 0;
					else mCap--; // Add trim
					mtoggleButtonCap.setText(String.valueOf(mTrimCap));
				}
				else if (mtoggleTanguage)
				{
					//(getResources().getString(R.string.tts_trim_diminution));
					if (mTrimTanguage > -corrAxe) mTrimTanguage--;

					if (mTanguage > maxAxe) mTanguage = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mTanguage <= 0) mTanguage = 0;
					else mTanguage--; // Add trim
					mtoggleButtonTanguage.setText(String.valueOf(mTrimTanguage));
				}
				else if (mtoggleRoulis)
				{
					//ttsTalk(getResources().getString(R.string.tts_trim_diminution));
					if (mTrimRoulis > -corrAxe) mTrimRoulis--;

					if (mRoulis > maxAxe) mRoulis = maxAxe; // To be sure that mCap is between 0 and 100
					else if (mRoulis <= 0) mRoulis = 0;
					else mRoulis--; // Add trim
					mtoggleButtonRoulis.setText(String.valueOf(mTrimRoulis));
				}
				savePref();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Called when smartphone back pressed
	 */
	@Override
	public void onBackPressed()
	{
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_close)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getResources().getString(R.string.message_yes), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}
		}).setNegativeButton(getResources().getString(R.string.message_no), null).show();
	}

	/*********************************************************************************
	 *
	 * @category Text to speech
	 *
	 *********************************************************************************/
	/**
	 * Initialisation text to speech
	 */
	private void initTextToSpeech()
	{
		Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, TTS_DATA_CHECK);
	}

	/**
	 * Method to say a string with text to speech
	 */
	public void ttsTalk(String texte)
	{
		if (tts != null && ttsIsInit) tts.speak(texte, TextToSpeech.QUEUE_ADD, null);
	}

	/*********************************************************************************
	 *
	 * @category Joystick
	 *
	 *********************************************************************************/
	/**
	 * Called when the left joystick is moved
	 */
	private JoystickMovedListener _listenerLeft = new JoystickMovedListener()
	{
		/*            __________
		 *  _________|  	    |________
		 * |         | Tilt -50 |        |
		 * | Pan -50 |__________| Pan 50 |
		 * |_________|          |________|
		 *           | Tilt -50 |
		 *           |__________|
		 */
		public void OnMoved(int pan, int tilt)
		{
			mGaz = -tilt; // Minus for inverting the sign

			int mTempCap;
			if (mInvertCap)
				mTempCap = (int) (-pan / mSensibilityCap) + corrAxe + mTrimCap; // Minus for inverting the sign and +50 for no negative byte and + trim
			else mTempCap = (int) (pan / mSensibilityCap) + corrAxe + mTrimCap; // +50 for no negative byte and + trim

			if (mTempCap > maxAxe) mCap = maxAxe; // To be sure that mCap is between 0 and 100
			else if (mTempCap < 0) mCap = 0;
			else mCap = mTempCap;

			mCenterL = false;
		}

		public void OnReleased()
		{
			// Nothing
		}

		public void OnReturnedToCenter()
		{
			mGaz = 0;
			mCap = corrAxe + mTrimCap; // +50 for no negative byte
			UpdateMethod();
			mCenterL = true;
		}
	};

	/**
	 * Called when the right joystick is moved
	 */
	private JoystickMovedListener _listenerRight = new JoystickMovedListener()
	{

		public void OnMoved(int pan, int tilt)
		{
			int mTempTanguage;
			if (mInvertTanguage)
				mTempTanguage = (int) (-tilt / mSensibilityTanguage) + corrAxe + mTrimTanguage;  // Minus for inverting the sign and +50 for no negative byte and + trim
			else
				mTempTanguage = (int) (tilt / mSensibilityTanguage) + corrAxe + mTrimTanguage;  // +50 for no negative byte and + trim

			if (mTempTanguage > maxAxe) mTanguage = maxAxe; // To be sure that mCap is between 0 and 100
			else if (mTempTanguage < 0) mTanguage = 0;
			else mTanguage = mTempTanguage;

			int mTempRoulis;
			if (mInvertRoulis)
				mTempRoulis = (int) (-pan / mSensibilityRoulis) + corrAxe + mTrimRoulis;  // Minus for inverting the sign and +50 for no negative byte and + trim
			else
				mTempRoulis = (int) (pan / mSensibilityRoulis) + corrAxe + mTrimRoulis;  // +50 for no negative byte and + trim

			if (mTempRoulis > maxAxe) mRoulis = maxAxe; // To be sure that mCap is between 0 and 100
			else if (mTempRoulis < 0) mRoulis = 0;
			else mRoulis = mTempRoulis;

			mCenterR = false;
		}

		public void OnReleased()
		{
			//
		}

		public void OnReturnedToCenter()
		{
			mTanguage = corrAxe + mTrimTanguage; // +50 for no negative byte + trim
			mRoulis = corrAxe + mTrimRoulis; // +50 for no negative byte + trim
			UpdateMethod();
			mCenterR = true;
		}
	};

	/*********************************************************************************
	 *
	 * @category Timers
	 *
	 *********************************************************************************/
	/**
	 * Called when update is needed
	 */
	private void UpdateMethod()
	{

		// if either of the joysticks is not on the center, or timeout occurred
		if (mRequestUpdate || !mCenterL || !mCenterR || (mTimeoutCounter >= mMaxTimeoutCount && mMaxTimeoutCount > -1))
		{
			if (mRequestUpdate) mRequestUpdate = false;

			if (mInvertGaz) mSubmitGaz = -mRealGaz;
			else mSubmitGaz = mRealGaz;

			if (mArme)
			{
				mArme = false;
				if (ArmeDearme) mCap = 100;
				else mCap = 0;
			}

	    	/* Sent Frame (11 byte):
			 *  _______ ________ ________ __________ ________ ________________ ________________ _____________ _____________ _______________________ _____
    		 * |       |        |        |          |        |                |                |			 |             |                       |     |
    		 * | Start |  Gaz   |  Cap   | Tanguage | Roulis | yAccelerometer | zAccelerometer |     LED     |     REC     |        Checksum       | End |
    		 * |  240  | 0-100% | 0-100% |  0-100%  | 0-100% | 60-110 degrees | 60-110 degrees | 0-1 boolean | 0-1 boolean | checksum ^= values[i] | 250 |
    		 * |_______|________|________|__________|________|________________|________________|_____________|_____________|_______________________|_____|
    		 * 
    		 */

			byte checksumSend = RinalTool.checksumCalc(new byte[]{(byte) mSubmitGaz, (byte) mCap, (byte) mTanguage, (byte) mRoulis, (byte) yRealAcc, (byte) zRealAcc, (byte) mtoggleLed, (byte) mtoggleRec}, false, true);

			byte[] dataSendByte = new byte[]{(byte) startFrame, (byte) mSubmitGaz, (byte) mCap, (byte) mTanguage, (byte) mRoulis, (byte) yRealAcc, (byte) zRealAcc, (byte) mtoggleLed, (byte) mtoggleRec, checksumSend, (byte) endFrame};

			if (D)
			{
				Log.d(TAG, String.format("%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d", startFrame, mSubmitGaz, mCap, mTanguage, mRoulis, yRealAcc, zRealAcc, mtoggleLed, mtoggleRec, RinalTool.byteToUnsignedInt(checksumSend), endFrame));
			}

			sendByte(dataSendByte);

			mTimeoutCounter = 0;
		}
		else
		{
			if (mMaxTimeoutCount > -1) mTimeoutCounter++;
		}
	}

	/**
	 * Called when gaz is selected
	 */
	private void GazMethod()
	{
		// Give gaz if joystick is pushed up
		if (mGaz > mGazMaxMin && mRealGaz < 100)
		{
			mRealGaz++;
			startCountUp = true;
			if (startCountDown && mCountDownOnOff)
			{
				startCountDown = false;
				mCountDownTimer.start();
			}
		}
		// Slow down if joystick is pushed down
		else if (mGaz < -mGazMaxMin && mRealGaz > 0)
		{
			mRealGaz--;
		}

		// Check if gaz is zero and cancel timers
		if (mRealGaz == 0)
		{
			startCountUp = false;
			elapsedTime = 0;

			startCountDown = true;
			mCountDownTimer.cancel();
		}

		mGazHandler.post(mGazRunnable);
	}

	/**
	 * Called when update gaz GUI is needed
	 */
	final Runnable mGazRunnable = new Runnable()
	{
		public void run()
		{
			mGazPercent.setText(getResources().getString(R.string.gaz_percent) + String.valueOf(mRealGaz) + " %");
			mProgressGaz.setProgress(mRealGaz);
			mProgressGaz.invalidate();
			if (!startCountUp) mCountUp.setText("00 : 00 : 00");
			if (startCountDown) mCountDown.setText("00 : 00 : 00");
		}
	};

	/**
	 * Called when update CounterUp GUI is needed
	 */
	final Runnable mCountUpRunnable = new Runnable()
	{
		public void run()
		{
			mCountUp.setText(RinalTool.formatTime(elapsedTime));
			mCountUp.invalidate();
		}
	};

	/*********************************************************************************
	 *
	 * @category Bluetooth
	 *
	 *********************************************************************************/
	/**
	 * Called to initialise bluetooth
	 */
	private void initBluetooth()
	{
		// Initialize the BluetoothCommandService to perform bluetooth connections
		mCommandService = new BluetoothCommandService(mHandler);
	}

	/**
	 * Called to sending bytes with bluetooth
	 *
	 * @param send Byte Array to send
	 */
	private void sendByte(byte[] send)
	{
		// Check that we're actually connected before trying anything
		if (mCommandService != null)
		{
			if (mCommandService.getState() != BluetoothCommandService.STATE_CONNECTED)
			{
				return;
			}
			// Check that there's actually something to send
			if (send.length > 0)
			{
				mCommandService.write(send);
			}
		}
	}

	/*********************************************************************************
	 *
	 * @category Preferences
	 *
	 *********************************************************************************/
	/**
	 * Called when preferences are changed
	 */
	public void onSharedPreferenceChanged(final SharedPreferences pref, String key)
	{
		// Unable to use switch for String under Java 1.7, Android needs 1.5 or 1.6 for compatibility
		if (key.equals("Language"))
		{
			if (langFlag) langFlag = false;
			else
			{
				lang.getConfiguration().locale = new Locale(pref.getString("Language", "en"));
				lang.updateConfiguration(lang.getConfiguration(), lang.getDisplayMetrics());

				Intent intent = getIntent();
				finish();
				startActivity(intent);
			}
		}
		else if (key.equals("AnnounceOnOff"))
		{
			mTTS = pref.getBoolean("AnnounceOnOff", true);
		}
		else if (key.equals("ReadOnOff"))
		{
			mRead = pref.getBoolean("ReadOnOff", false);
			if (mRead)
			{
				findViewById(R.id._power).setVisibility(View.VISIBLE);
				findViewById(R.id._charge).setVisibility(View.VISIBLE);
				findViewById(R.id._temp).setVisibility(View.VISIBLE);
				mPower.setVisibility(View.VISIBLE);
				mCharge.setVisibility(View.VISIBLE);
				mTemp.setVisibility(View.VISIBLE);
				mPower.setText("0.0V");
				mCharge.setText("0%");
				mTemp.setText("0°C");
			}
			else
			{
				findViewById(R.id._power).setVisibility(View.GONE);
				findViewById(R.id._charge).setVisibility(View.GONE);
				findViewById(R.id._temp).setVisibility(View.GONE);
				mPower.setVisibility(View.GONE);
				mCharge.setVisibility(View.GONE);
				mTemp.setVisibility(View.GONE);
			}
		}
		else if (key.equals("CameraOnOff"))
		{
			mCamera = pref.getBoolean("CameraOnOff", false);
		}
		else if (key.equals("DebugOnOff"))
		{
			D = pref.getBoolean("DebugOnOff", false);
		}
		else if (key.equals("UpdatePeriod"))
		{
			// Reschedule timer
			mUpdateTimer.cancel();
			mUpdateTimer.purge();
			mUpdatePeriod = prefStringGetLong("UpdatePeriod", "50");
			mUpdateTimer = new Timer();
			mUpdateTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					UpdateMethod();
				}
			}, mUpdatePeriod, mUpdatePeriod);
		}
		else if (key.equals("GazPeriod"))
		{
			// Reschedule timer
			mGazTimer.cancel();
			mGazTimer.purge();
			mGazPeriod = prefStringGetLong("GazPeriod", "100");
			mGazTimer = new Timer();
			mGazTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					GazMethod();
				}
			}, mGazPeriod, mGazPeriod);
		}
		else if (key.equals("MaxTimeout"))
		{
			mMaxTimeoutCount = prefStringGetInt("MaxTimeout", "10");
		}
		else if (key.equals("GazMaxMin"))
		{
			mGazMaxMin = prefStringGetInt("GazMaxMin", "15");
		}
		else if (key.equals("SensibilityCap"))
		{
			mSensibilityCap = prefStringGetFloat("SensibilityCap", "4");
		}
		else if (key.equals("SensibilityTanguage"))
		{
			mSensibilityTanguage = prefStringGetFloat("SensibilityTanguage", "4");
		}
		else if (key.equals("SensibilityRoulis"))
		{
			mSensibilityRoulis = prefStringGetFloat("SensibilityRoulis", "4");
		}
		else if (key.equals("CountUpOnOff"))
		{
			mCountUpOnOff = pref.getBoolean("CountUpOnOff", false);
			if (!mCountUpOnOff) mCountUp.setText("00 : 00 : 00");
		}
		else if (key.equals("CountDownOnOff"))
		{
			mCountDownOnOff = pref.getBoolean("CountDownOnOff", false);
			if (!mCountDownOnOff)
			{
				mCountDownTimer.cancel();
				mCountDown.setText("00 : 00 : 00");
			}
		}
		else if (key.equals("CountDown"))
		{
			mCountDownTimer.cancel();
			mCountDownPeriod = prefStringGetLong("CountDown", "10") * 1000 * 60;
			mCountDownTimer = new CountDownTimer(mCountDownPeriod, 1000)
			{

				public void onTick(long millisUntilFinished)
				{
					mCountDown.setText(RinalTool.formatTime(millisUntilFinished));

					if (mTTS)
					{
						if (millisUntilFinished <= 61000 && millisUntilFinished >= 60000)
							ttsTalk(getResources().getString(R.string.tts_countdown_1_minute));
						else if (millisUntilFinished <= 31000 && millisUntilFinished >= 30000)
							ttsTalk(getResources().getString(R.string.tts_countdown_30_second));
						else if (millisUntilFinished <= 11000 && millisUntilFinished >= 10000)
							ttsTalk(getResources().getString(R.string.tts_countdown_10_second));
						else if (millisUntilFinished <= 6000 && millisUntilFinished >= 5000)
							ttsTalk(getResources().getString(R.string.tts_countdown_5_second));
					}
				}

				public void onFinish()
				{
					mCountDown.setText("00 : 00 : 00");
					if (mTTS) ttsTalk(getResources().getString(R.string.tts_countdown_finish));
					// Vibrate the pattern and don't repeat
					if (vibr != null) vibr.vibrate(pattern, -1);
				}
			};
		}
		else if (key.equals("TrimCap"))
		{
			mTrimCap = prefStringGetInt("TrimCap", "0");
		}
		else if (key.equals("TrimTanguage"))
		{
			mTrimTanguage = prefStringGetInt("TrimTanguage", "0");
		}
		else if (key.equals("TrimRoulis"))
		{
			mTrimRoulis = prefStringGetInt("TrimRoulis", "0");
		}
		else if (key.equals("TrimXCam"))
		{
			mTrimXCam = prefStringGetInt("TrimXCam", "10");
		}
		else if (key.equals("TrimYCam"))
		{
			mTrimYCam = prefStringGetInt("TrimYCam", "10");
		}
		else if (key.equals("InvertGaz"))
		{
			new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name)).setMessage(getResources().getString(R.string.message_gaz_invert)).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Ok", null).show();
			mInvertGaz = pref.getBoolean("InvertGaz", false);
		}
		else if (key.equals("InvertCap"))
		{
			mInvertCap = pref.getBoolean("InvertCap", false);
		}
		else if (key.equals("InvertTanguage"))
		{
			mInvertTanguage = pref.getBoolean("InvertTanguage", false);
		}
		else if (key.equals("InvertRoulis"))
		{
			mInvertRoulis = pref.getBoolean("InvertRoulis", false);
		}
		else if (key.equals("InvertXCam"))
		{
			mInvertXCam = pref.getBoolean("InvertXCam", false);
		}
		else if (key.equals("InvertYCam"))
		{
			mInvertYCam = pref.getBoolean("InvertYCam", false);
		}
	}

	/**
	 * Called for loading preferences
	 */
	private void loadPref()
	{
		// If application first launched store the language
		if (pref.getBoolean("Firstrun", true))
		{
			langFlag = true;
			prefEditor = pref.edit();
			prefEditor.putString("Language", lang.getConfiguration().locale.getLanguage());
			prefEditor.putBoolean("Firstrun", false);
			prefEditor.commit();
		}
		else
		{
			// Set the language from the preferences
			lang.getConfiguration().locale = new Locale(pref.getString("Language", "en"));
			lang.updateConfiguration(lang.getConfiguration(), lang.getDisplayMetrics());
		}

		mTTS = pref.getBoolean("AnnounceOnOff", true);
		mRead = pref.getBoolean("ReadOnOff", false);
		if (mRead)
		{
			findViewById(R.id._power).setVisibility(View.VISIBLE);
			findViewById(R.id._charge).setVisibility(View.VISIBLE);
			findViewById(R.id._temp).setVisibility(View.VISIBLE);
			mPower.setVisibility(View.VISIBLE);
			mCharge.setVisibility(View.VISIBLE);
			mTemp.setVisibility(View.VISIBLE);
			mPower.setText("0.0V");
			mCharge.setText("0%");
			mTemp.setText("0°C");
		}
		else
		{
			findViewById(R.id._power).setVisibility(View.GONE);
			findViewById(R.id._charge).setVisibility(View.GONE);
			findViewById(R.id._temp).setVisibility(View.GONE);
			mPower.setVisibility(View.GONE);
			mCharge.setVisibility(View.GONE);
			mTemp.setVisibility(View.GONE);
		}
		mCamera = pref.getBoolean("CameraOnOff", false);
		D = pref.getBoolean("DebugOnOff", false);
		mUpdatePeriod = prefStringGetLong("UpdatePeriod", "50");
		mMaxTimeoutCount = prefStringGetInt("MaxTimeout", "10");
		mGazPeriod = prefStringGetLong("GazPeriod", "100");
		mGazMaxMin = prefStringGetInt("GazMaxMin", "15");
		mSensibilityCap = prefStringGetFloat("SensibilityCap", "4");
		mSensibilityTanguage = prefStringGetFloat("SensibilityTanguage", "4");
		mSensibilityRoulis = prefStringGetFloat("SensibilityRoulis", "4");
		mCountUpOnOff = pref.getBoolean("CountUpOnOff", false);
		mCountDownOnOff = pref.getBoolean("CountDownOnOff", false);
		mCountDownPeriod = prefStringGetLong("CountDown", "10") * 1000 * 60;
		mTrimCap = prefStringGetInt("TrimCap", "0");
		mTrimTanguage = prefStringGetInt("TrimTanguage", "0");
		mTrimRoulis = prefStringGetInt("TrimRoulis", "0");
		mTrimXCam = prefStringGetInt("TrimXCam", "10");
		mTrimYCam = prefStringGetInt("TrimYCam", "10");
		mInvertGaz = pref.getBoolean("InvertGaz", false);
		mInvertCap = pref.getBoolean("InvertCap", false);
		mInvertTanguage = pref.getBoolean("InvertTanguage", false);
		mInvertRoulis = pref.getBoolean("InvertRoulis", false);
		mInvertXCam = pref.getBoolean("InvertXCam", false);
		mInvertYCam = pref.getBoolean("InvertYCam", false);
	}

	/**
	 * Called for saving preferences
	 */
	private void savePref()
	{
		prefEditor = pref.edit();
		prefEditor.putString("TrimCap", String.valueOf(mTrimCap));
		prefEditor.putString("TrimTanguage", String.valueOf(mTrimTanguage));
		prefEditor.putString("TrimRoulis", String.valueOf(mTrimRoulis));
		prefEditor.commit();
	}

	/**
	 * Called for getting Integer from preference
	 */
	private int prefStringGetInt(String key, String defaultValue)
	{
		int value;
		try
		{
			value = Integer.parseInt(pref.getString(key, defaultValue));
		}
		catch (Exception e)
		{
			value = Integer.parseInt(defaultValue);
			prefEditor = pref.edit();
			prefEditor.putString(key, defaultValue);
			prefEditor.commit();
			Toast.makeText(context, getResources().getString(R.string.error_pref), Toast.LENGTH_LONG).show();
		}

		return value;
	}

	/**
	 * Called for getting Long from preference
	 */
	private long prefStringGetLong(String key, String defaultValue)
	{
		long value;
		try
		{
			value = Long.parseLong(pref.getString(key, defaultValue));
			if (value == 0)
			{
				value = Long.parseLong(defaultValue);
				prefEditor = pref.edit();
				prefEditor.putString(key, defaultValue);
				prefEditor.commit();
				Toast.makeText(context, getResources().getString(R.string.error_pref), Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e)
		{
			value = Long.parseLong(defaultValue);
			prefEditor = pref.edit();
			prefEditor.putString(key, defaultValue);
			prefEditor.commit();
			Toast.makeText(context, getResources().getString(R.string.error_pref), Toast.LENGTH_LONG).show();
		}
		return value;
	}

	/**
	 * Called for getting Float from preference
	 */
	private float prefStringGetFloat(String key, String defaultValue)
	{
		float value;
		try
		{
			value = Float.parseFloat(pref.getString(key, defaultValue));
		}
		catch (Exception e)
		{
			value = Float.parseFloat(defaultValue);
			prefEditor = pref.edit();
			prefEditor.putString(key, defaultValue);
			prefEditor.commit();
			Toast.makeText(context, getResources().getString(R.string.error_pref), Toast.LENGTH_LONG).show();
		}
		return value;
	}

	/*********************************************************************************
	 *
	 * @category Sensor
	 *
	 *********************************************************************************/
	/**
	 * Called when accuracy of sensor changed
	 */
	public final void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// Nothing
	}

	/**
	 * Called when value of sensor changed
	 */
	public final void onSensorChanged(SensorEvent event)
	{
		// When the event is from the accelerometer
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			if (mCamera)
			{
				// Update if timeout occured
				int mMaxAccCount = 2;
				if (mAccCounter >= mMaxAccCount && mMaxAccCount > -1)
				{
					// Get the 3 axis values
					@SuppressWarnings("UnusedDeclaration")
					float xAcc = event.values[0];
					float yAcc = event.values[1];
					float zAcc = event.values[2];

					if (mInvertYCam) yAcc = -yAcc; // invert Y Cam

					int yTempRealAcc = (int) (yAcc * 10) + corrAxeYAcc - mTrimYCam;

					int maxAxeAcc = 160;
					int zTempRealAcc;
					if (!mInvertXCam) zTempRealAcc = maxAxeAcc - (int) (zAcc * 10) - mTrimXCam; // invert X Cam
					else
						zTempRealAcc = (int) (zAcc * 10) + corrAxeXAcc - mTrimXCam; // *10 to get 0 to 100 and +60 for offset

					int minAxeAcc = 60;
					if (yTempRealAcc > maxAxeAcc)
						yRealAcc = maxAxeAcc; // To be sure that yRealAcc is between 60 and 160
					else if (yTempRealAcc < minAxeAcc) yRealAcc = minAxeAcc;
					else yRealAcc = yTempRealAcc;

					if (zTempRealAcc > maxAxeAcc)
						zRealAcc = maxAxeAcc; // To be sure that zRealAcc is between 60 and 160
					else if (zTempRealAcc < minAxeAcc) zRealAcc = minAxeAcc;
					else zRealAcc = zTempRealAcc;

					mAccCounter = 0;
					mRequestUpdate = true;
				}
				else
				{
					if (mMaxAccCount > -1) mAccCounter++;
				}

			}
			else
			{
				yRealAcc = corrAxeYAcc - mTrimYCam;
				zRealAcc = corrAxeXAcc - mTrimXCam;
			}
		}

        /*
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            acceleromterVector=event.values;
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
        magneticVector=event.values;
        }

        SensorManager.getRotationMatrix(resultMatrix, null, acceleromterVector, magneticVector);

        SensorManager.getOrientation(resultMatrix, values);
        // Yaw
        x =(float) Math.toDegrees(values[0]);
        // Pitch
        y = (float) Math.toDegrees(values[1]);
        // Roll
        z = (float) Math.toDegrees(values[2]);

        Log.d(TAG,String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(z));
         */
	}

	/*********************************************************************************
	 *
	 * @category !Not Used!
	 *
	 *********************************************************************************/
	/*
    private void sendMessage(String message){
    	// Check that we're actually connected before trying anything
    	if (mCommandService != null) {
	    	if (mCommandService.getState() != BluetoothCommandService.STATE_CONNECTED) {
	    		// Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	// Check that there's actually something to send
	    	if (message.length() > 0) {
	    		// Get the message bytes and tell the BluetoothCommandService to write
	    		byte[] send = message.getBytes();
	    		mCommandService.write(send);
	    	}
    	}
    }
    
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	*/
}