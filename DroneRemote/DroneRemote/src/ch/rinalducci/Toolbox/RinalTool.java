/*****************************
 * RinalTool
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
 *****************************/

package ch.rinalducci.Toolbox;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.net.ConnectivityManager;

@SuppressWarnings({"UnusedDeclaration", "ConstantConditions"})
public class RinalTool
{
	/**
	 * Avoid to point on this class
	 */
	private RinalTool () {}

	/**
	 * Method to convert a byte in a unsigned integer
	 *
	 * @param b byte
	 * @return unsigned integer
	 */
	public static int byteToUnsignedInt(byte b)
	{
		return b & 0xff;
	}

	/**
	 * Method for calculating the checksum
	 *
	 * @param datas    datas in byte array
	 * @param decode   code or decode checksum?
	 * @param function function false is one error detection
	 *                 function true is multiple error detection
	 *
	 * @return checksum in byte
	 */
	public static byte checksumCalc(byte[] datas, boolean decode, boolean function)
	{
		int checksum = 0;

		if (function)
		{
			if (decode)
			{
				for (int i = 0; i < datas.length - 1; i++)
					checksum += datas[i];
			}
			else
			{
				for (byte data : datas) checksum += data;
			}
			checksum = checksum & 0xff;
			checksum = 0xff - checksum;
		}
		else
		{
			if (decode)
			{
				for (int i = 0; i < datas.length - 1; i++)
					checksum ^= datas[i];
			}
			else
			{
				for (byte data : datas) checksum ^= data;
			}
		}
		return (byte) checksum;
	}

	/**
	 * Called for formatting time in ms
	 *
	 * @param millis milliseconds
	 * @return string 00 : 00 : 00
	 */
	public static String formatTime(long millis)
	{
		String output;
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours);

		if (seconds < 10) secondsD = "0" + seconds;
		if (minutes < 10) minutesD = "0" + minutes;
		if (hours < 10) hoursD = "0" + hours;

		output = hoursD + " : " + minutesD + " : " + secondsD;
		return output;
	}

	/**
	 * Load a Scaled Down Version into Memory
	 *
	 * @param res resource
	 * @param resId resource ID
	 * @param reqWidth bitmap required width
	 * @param reqHeight bitmap required height
	 * @return bitmap
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Read Bitmap Dimensions and Type
	 *
	 * @param options bitmap options
	 * @param reqWidth bitmap required width
	 * @param reqHeight bitmap required height
	 * @return integer
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth)
		{

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * Test if network is connected
	 * Required <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	 *
	 * @param context context
	 * @return string
	 */
	public static boolean isNetworkConnected(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected());
	}

	/**
	 * Encode password
	 *
	 * @param password password
	 * @param type MD5 or SHA1
	 * @return encoded password
	 */
	public static String String_Encode(String password, String type)
	{
		byte[] uniqueKey = password.getBytes();
		byte[] hash;

		try
		{
			hash = MessageDigest.getInstance(type).digest(uniqueKey);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new Error("No MD5 support in this VM.");
		}

		StringBuilder hashString = new StringBuilder();
		for (byte aHash : hash)
		{
			String hex = Integer.toHexString(aHash);
			if (hex.length() == 1)
			{
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			}
			else hashString.append(hex.substring(hex.length() - 2));
		}
		return hashString.toString();
	}
}