package com.intel.crashreport;

import com.intel.crashreport.GcmMessage.GCM_ACTION;
import com.intel.crashreport.specific.EventDB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GcmMessageDialog extends Activity{

	private Context context;
	private GCM_ACTION type;
	private String data;
	private int rowId;
	private Button ok;
	private Button cancel;
	private Button validate;

	private TextView contentView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_gcm_message);

		cancel = (Button)findViewById(R.id.dialog_gcm_button_cancel);
		if(cancel != null) {
			cancel.setOnClickListener(cancelListener);
		}
		ok = (Button)findViewById(R.id.dialog_gcm_button_ok);
		if(ok != null) {
			ok.setOnClickListener(okListener);
		}
		validate = (Button)findViewById(R.id.dialog_gcm_button_validate);
		if(validate != null) {
			validate.setOnClickListener(okListener);
		}
		contentView = (TextView)findViewById(R.id.dialog_gcm_content_view);
	}

	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		boolean result = false;
		if(intent != null) {
			if(intent.hasExtra("rowId")) {
				rowId = intent.getIntExtra("rowId", -1);
				EventDB db = new EventDB(getApplicationContext());
				GcmMessage message = null;
				try {
					db.open();
					message = db.getGcmMessageFromId(rowId);
					if(null != message) {
						contentView.setText("Title : " + message.getTitle() + "\n" +
								"Text : " +  message.getText() + "\n" );
						type = message.getType();
						data = "";
						if(GCM_ACTION.GCM_NONE != type) {
							data = message.getData();
							validate.setVisibility(View.GONE);
						}
						else {
							ok.setVisibility(View.GONE);
							cancel.setVisibility(View.GONE);
						}
						result = true;
					}
					db.close();
				}
				catch (SQLException e){
					Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
				}
			}
		}
		if(!result)
			finish();
	}

	private OnClickListener okListener = new OnClickListener(){

		public void onClick(View v) {
			GcmEvent.INSTANCE.takeGcmAction(rowId, type, data);
			finish();
		}

	};

	private OnClickListener cancelListener = new OnClickListener(){

		public void onClick(View v) {
			finish();
		}

	};

}
