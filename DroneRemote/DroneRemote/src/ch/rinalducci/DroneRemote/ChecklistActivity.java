/****************************
 * Checklist Activity
 * --------------------
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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChecklistActivity extends Activity
{
	SharedPreferences pref;
	Editor            prefEditor;
	CheckBox          cb1;
	CheckBox          cb2;
	CheckBox          cb3;
	CheckBox          cb4;
	CheckBox          cb5;
	CheckBox          cb6;
	CheckBox          cb7;

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
		setContentView(R.layout.checklist);

		// Bind components
		cb1 = (CheckBox) findViewById(R.id.cb1);
		cb2 = (CheckBox) findViewById(R.id.cb2);
		cb3 = (CheckBox) findViewById(R.id.cb3);
		cb4 = (CheckBox) findViewById(R.id.cb4);
		cb5 = (CheckBox) findViewById(R.id.cb5);
		cb6 = (CheckBox) findViewById(R.id.cb6);
		cb7 = (CheckBox) findViewById(R.id.cb7);
		Button butClear = (Button) findViewById(R.id.but_clear);

		// Initialisation of preferences
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		loadPref();

		cb1.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb2.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb3.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb4.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb5.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb6.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		cb7.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				savePref();
			}
		});

		butClear.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				cb1.setChecked(false);
				cb2.setChecked(false);
				cb3.setChecked(false);
				cb4.setChecked(false);
				cb5.setChecked(false);
				cb6.setChecked(false);
				cb7.setChecked(false);
				savePref();
			}
		});
	}

	/*********************************************************************************
	 *
	 * @category Preferences
	 *
	 *********************************************************************************/
	/**
	 * Called for loading preferences
	 */
	private void loadPref()
	{
		cb1.setChecked(pref.getBoolean("cb1", false));
		cb2.setChecked(pref.getBoolean("cb2", false));
		cb3.setChecked(pref.getBoolean("cb3", false));
		cb4.setChecked(pref.getBoolean("cb4", false));
		cb5.setChecked(pref.getBoolean("cb5", false));
		cb6.setChecked(pref.getBoolean("cb6", false));
		cb7.setChecked(pref.getBoolean("cb7", false));
	}

	/**
	 * Called for saving preferences
	 */
	private void savePref()
	{
		prefEditor = pref.edit();
		prefEditor.putBoolean("cb1", cb1.isChecked());
		prefEditor.putBoolean("cb2", cb2.isChecked());
		prefEditor.putBoolean("cb3", cb3.isChecked());
		prefEditor.putBoolean("cb4", cb4.isChecked());
		prefEditor.putBoolean("cb5", cb5.isChecked());
		prefEditor.putBoolean("cb6", cb6.isChecked());
		prefEditor.putBoolean("cb7", cb7.isChecked());
		prefEditor.commit();
	}
}
