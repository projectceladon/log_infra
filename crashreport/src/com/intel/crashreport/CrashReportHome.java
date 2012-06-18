
package com.intel.crashreport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.intel.crashreport.logconfig.ui.LogConfigHomeActivity;

public class CrashReportHome extends Activity {
    private MenuItem aboutMenu;
    private MenuItem settingsMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        Button button_start = (Button) findViewById(R.id.button_report_events);
        // Attach a click listener for launching the system settings.
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("com.intel.crashreport.intent.START_SERVICE");
                startActivity(intent);
            }
        });

        Button button_logconfig = (Button) findViewById(R.id.button_logconfig);
        // Attach a click listener for launching the system settings.
        button_logconfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LogConfigHomeActivity.class);
                startActivity(intent);
            }
        });

        setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        settingsMenu = menu.add(R.string.menu_settings);
        aboutMenu = menu.add(R.string.menu_about);
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.equals(settingsMenu)) {
            startCrashReport();
            return true;
        }
        if (item.equals(aboutMenu)) {
            showDialog();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.settings:
                startCrashReport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startCrashReport() {
        Intent intent = new Intent(getApplicationContext(), CrashReportActivity.class);
        startActivity(intent);
    }

    public static class AboutDialog extends DialogFragment {

        public static AboutDialog newInstance() {
            AboutDialog frag = new AboutDialog();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.about_title))
                    .setMessage(
                            getString(R.string.app_name) + " v" + getString(R.string.app_version)
                                    + "\n" + "created by Intel DnT team.")
                    .create();
        }
    }

    public void showDialog() {
        DialogFragment newFragment = AboutDialog.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }
}
