package com.intel.amtl;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Trace_in_coredumpActivity extends Activity {

    private Button button_trace_disable;
    private Button button_trace_ma_artemis;
    private Button button_trace_ma_artemis_digrf;
    private Button button_trace_coredump;
    private ProgressDialog progressDialog;
    private TextView coredump_text;
    Runtime rtm=java.lang.Runtime.getRuntime();

    void writeSimple(String iout,String ival) throws IOException {
        RandomAccessFile f = new RandomAccessFile(iout, "rws");
        f.writeBytes(ival);
        f.close();
    }

    private void InfoMessage() {
        coredump_text.setVisibility(1);
        coredump_text.setText("If you REBOOT, please apply AGAIN!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace_in_coredump);

        button_trace_disable = (Button) findViewById(R.id.disable_all_traces_button);
        button_trace_ma_artemis = (Button) findViewById(R.id.trace_ma_artemis_button);
        button_trace_ma_artemis_digrf = (Button) findViewById(R.id.trace_ma_artemis_digrf_button);
        button_trace_coredump = (Button) findViewById(R.id.apply_trace_coredump_button);
        coredump_text=(TextView) findViewById(R.id.text_coredump);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) button_trace_disable).setChecked(preferences.getBoolean("button_trace_coredump_disable_value", true));
        ((CompoundButton) button_trace_ma_artemis).setChecked(preferences.getBoolean("button_trace_coredump_ma_artemis_value", false));
        ((CompoundButton) button_trace_ma_artemis_digrf).setChecked(preferences.getBoolean("button_trace_coredump_ma_artemis_digrf_value", false));

        /*Listener for button_trace_coredump*/
        button_trace_coredump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                progressDialog = ProgressDialog.show(Trace_in_coredumpActivity.this, "Please wait...", "Apply trace level in Progress");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (((CompoundButton) button_trace_ma_artemis).isChecked()) {
                                /*Enable traces MA & Artemis*/
                                writeSimple("/dev/gsmtty1","at+trace=1\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=1,\"bb_sw=1;3g_sw=1\",,\"oct=4\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            } else if (((CompoundButton) button_trace_ma_artemis_digrf).isChecked()) {
                                /*Enable MA & Artemis & Digrf*/
                                writeSimple("/dev/gsmtty1","at+trace=1\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            } else {
                                /*Disable trace*/
                                writeSimple("/dev/gsmtty1","at+trace=0\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=0\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            }
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Save_status_coredump();
                InfoMessage();
            }
        });
    }

    protected void Save_status_coredump() {
        /*Store values between instances here*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*Put the values from the UI*/
        boolean button_trace_coredump_disable_value = ((CompoundButton) button_trace_disable).isChecked();
        boolean button_trace_coredump_ma_artemis_value = ((CompoundButton) button_trace_ma_artemis).isChecked();
        boolean button_trace_coredump_ma_artemis_digrf_value = ((CompoundButton) button_trace_ma_artemis_digrf).isChecked();

        /*Value to store*/
        editor.putBoolean("button_trace_coredump_disable_value", button_trace_coredump_disable_value);
        editor.putBoolean("button_trace_coredump_ma_artemis_value", button_trace_coredump_ma_artemis_value);
        editor.putBoolean("button_trace_coredump_ma_artemis_digrf_value", button_trace_coredump_ma_artemis_digrf_value);

        /*Commit to storage*/
        editor.commit();
    }
}