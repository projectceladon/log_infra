/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
			setValue(prepareToStoreStrings(values));
		}
	}

	public static String prepareToStoreStrings(List<String> strings) {
		Iterator<String> sIter;
		if (strings == null || !(sIter = strings.iterator()).hasNext())
			return "";
		StringBuilder sBuilder = new StringBuilder(sIter.next());
		while (sIter.hasNext())
			sBuilder.append(DEFAULT_SEPARATOR).append(sIter.next());
		return sBuilder.toString();
	}

	public static String[] parseStoredValue(CharSequence val) {
		if (val == null || val.length() == 0)
			return null;
		else
			return ((String)val).split(DEFAULT_SEPARATOR);
	}

}
