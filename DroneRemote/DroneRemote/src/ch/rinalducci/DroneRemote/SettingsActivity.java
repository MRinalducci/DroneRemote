/*****************************
 * SettingsActivity
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

package ch.rinalducci.DroneRemote;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
{
	public static Context context;

	/*********************************************************************************
	 *
	 * @category Smartphone override method
	 *
	 *********************************************************************************/
	/**
	 * Called when the activity is first created
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		context = this.getApplicationContext();

		//requestWindowFeature(Window.FEATURE_LEFT_ICON);
		addPreferencesFromResource(R.layout.settings);
		//getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_menu_manage);

		// If language preference changed finish() the activity
		Preference language = findPreference("Language");
		if (language != null) {
			language.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
			{
				public boolean onPreferenceChange(Preference preference, Object newValue)
				{
					finish();
					return true;
				}

			});
		}
	}
}