/****************************
 * Legal Infos Activity
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

@SuppressWarnings("ConstantConditions")
public class LegalsInfosActivity extends Activity
{

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
		setContentView(R.layout.legals_infos);

		// Bind components
		TextView version = (TextView) findViewById(R.id.version);
		TextView build = (TextView) findViewById(R.id.build);
		TextView packages = (TextView) findViewById(R.id.packages);

		// Get application infos
		PackageInfo pInfo = null;
		try
		{
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		}
		catch (NameNotFoundException e)
		{
			//
			e.printStackTrace();
		}

		// Set application infos
		version.setText(pInfo.versionName);
		build.setText(String.valueOf(pInfo.versionCode));
		packages.setText(pInfo.packageName);
	}
}
