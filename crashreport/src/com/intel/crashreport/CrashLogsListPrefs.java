/* Crash Report (CLOTA)
 *
 * Copyright (C) Intel 2012
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class CrashLogsListPrefs extends ListPreference {

	private static final String DEFAULT_SEPARATOR = ",";
	private boolean[] clickedDialogEntryIndices;

	public CrashLogsListPrefs(Context context) {
		this(context, null);
	}

	public CrashLogsListPrefs(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		clickedDialogEntryIndices = new boolean[entries.length];
	}

	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null || entries.length != entryValues.length )
			throw new IllegalStateException("ListPreference entries.length != entryValues.length");

		clickedDialogEntryIndices = new boolean[entries.length];
		restoreCheckedEntries();
		builder.setMultiChoiceItems(entries, clickedDialogEntryIndices,
				new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean val) {
				clickedDialogEntryIndices[which] = val;
			}
		});
	}

	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();
		String[] vals = parseStoredValue(getValue());

		if ( vals != null ) {
			List<String> valuesList = Arrays.asList(vals);
			for ( int i=0; i<entryValues.length; i++ ) {
				CharSequence entry = entryValues[i];
				if ( valuesList.contains(entry) ) {
					clickedDialogEntryIndices[i] = true;
				}
			}
		}
	}

	protected void onDialogClosed(boolean positiveResult) {
		ArrayList<String> values = new ArrayList<String>();
		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
			for (int i=0; i<entryValues.length; i++) {
				if (clickedDialogEntryIndices[i]) {
					String val = (String) entryValues[i];
					values.add(val);
				}
			}
			setValue(joinStrings(values, DEFAULT_SEPARATOR));
		}
	}

	private static String joinStrings(ArrayList<String> strings, String separator) {
		Iterator<String> sIter;
		if (strings == null || !(sIter = strings.iterator()).hasNext())
			return "";
		StringBuilder sBuilder = new StringBuilder(sIter.next());
		while (sIter.hasNext())
			sBuilder.append(separator).append(sIter.next());
		return sBuilder.toString();
	}

	public static String[] parseStoredValue(CharSequence val) {
		if ("".equals(val))
			return null;
		else
			return ((String)val).split(DEFAULT_SEPARATOR);
	}

}
