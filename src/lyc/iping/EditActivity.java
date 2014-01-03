package lyc.iping;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lyc.iping.LoginActivity.UserLoginTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	private Button mBtnSend;
	private Button mBtnBack;
	private TextView nameText;
	private EditText from;
	private EditText to;
	private EditText date = null;
	private EditText detail;
//	private ListView mListView;
//	private EditMsgViewAdapter mAdapter;
//	private List<EditMsgEntity> mDataArrays = new ArrayList<EditMsgEntity>();
	private Handler handler = new Handler();
	public String GroupID, ID, userHead, cnfrom, cnto, cndate, cnname,
			cndetail;
	public String publishMsg;
	public int i;
	public String nameString;// = "ussam";
	private boolean released;

	private publishInfoTask mAuthTask = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);

		DatabaseHelper dbHelper = new DatabaseHelper(EditActivity.this, "iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("LoginUser", new String[] { "username",
				"ID", "sex", "autoLogin" }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			nameString = cursor.getString(cursor.getColumnIndex("username"));
			ID = cursor.getString(cursor.getColumnIndex("ID"));
		}
		db.close();
		dbHelper.close();
		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		released = false;
		initView();
		initData();

		date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});

	}

	public void initView() {
//		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(this);
		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(this);

		from = (EditText) findViewById(R.id.et_sendfrom);
		to = (EditText) findViewById(R.id.et_sendto);
		date = (EditText) findViewById(R.id.et_senddate);
		detail = (EditText) findViewById(R.id.et_senddetail);

		nameText = (TextView) findViewById(R.id.et_name);
//		nameText.setText(nameString);

	}

//	private String[] msgArray = new String[] { "����������ƴ��������Ҫ����������㡢�յ㡢���ڡ�����ϸ�ڣ�������㽨������������ϵ�绰�Ա��ڳ�����ϵ����" };

	public void initData() {
//		EditMsgEntity entity = new EditMsgEntity();
//		entity.setUserHead("System");
//		entity.setTime(getTime());
//		entity.setName("ϵͳ��ʾ");
//		entity.setMsgType(true);
//		entity.setDetail(msgArray[i]);
//		entity.setDate("ƴ������");
//		entity.setFrom("���");
//		entity.setTo("�յ�");
//		mDataArrays.add(entity);
		
		
//		mAdapter = new EditMsgViewAdapter(this, mDataArrays);
//		mListView.setAdapter(mAdapter);

	}

	private void showDialog() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datedialog = new DatePickerDialog(EditActivity.this,
				new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// TODO Auto-generated method stub
						date.setText(year + "-" + (monthOfYear + 1) + "-"
								+ dayOfMonth);
						date.setError(null);
						from.requestFocus();
					}
				}, year, monthOfYear, dayOfMonth);
		datedialog.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			send();
			break;
		case R.id.btn_back:
			Intent data = new Intent();
			setResult(50, data);
			finish();
			break;
		}
	}

	private void send() {

		String fromString = from.getText().toString();
		String toString = to.getText().toString();
		String dateString = date.getText().toString();
		String detailString = detail.getText().toString();
		if (TextUtils.isEmpty(dateString)) {
			date.setError("�������ڲ���Ϊ��");
			date.requestFocus();
			return;
		} else if (TextUtils.isEmpty(fromString)) {
			from.setError("�����ز���Ϊ��");
			from.requestFocus();
			return;
		} else if (fromString.length() > 5) {
			from.setError("�����س��ȳ���5���ַ�");
			from.requestFocus();
			return;
		} else if (TextUtils.isEmpty(toString)) {
			to.setError("Ŀ�ĵز���Ϊ��");
			to.requestFocus();
			return;
		} else if (toString.length() > 5) {
			to.setError("Ŀ�ĵس��ȳ���5���ַ�");
			to.requestFocus();
			return;
		} else if (TextUtils.isEmpty(detailString)) {
			detail.setError("��ϸ��Ϣ����Ϊ��");
			detail.requestFocus();
			return;
		} else if (detailString.length() > 200) {
			detail.setError("��ϸ��Ϣ���ȳ���200���ַ�");
			detail.requestFocus();
			return;
		}
		cnfrom = fromString;
		cnto = toString;
		cndate = dateString;
		cnname = nameString;
		cndetail = detailString;
//		if ((fromString.length() > 0) && (toString.length() > 0)
//				&& (dateString.length() > 0) && (detailString.length() > 0)) {
//			DatabaseHelper dbHelper = new DatabaseHelper(EditActivity.this,
//					"iPin");
//			SQLiteDatabase db = dbHelper.getWritableDatabase();
//			Cursor cursor = db.query("LoginUser", new String[] { "ID",
//					"HeadImageVersion" }, null, null, null, null, null);
//
//			while (cursor.moveToNext()) {
//				ID = cursor.getString(cursor.getColumnIndex("ID"));
//				userHead = ID
//						+ "_"
//						+ cursor.getInt(cursor
//								.getColumnIndex("HeadImageVersion")) + ".jpg";
//
//			}
//			db.close();
//			dbHelper.close();
//			EditMsgEntity entity = new EditMsgEntity();
//			entity.setUserHead(userHead);
//			entity.setFrom(fromString);
//			entity.setTo(toString);
//			entity.setDate(dateString);
//			entity.setDetail(detailString);
//			entity.setName(nameString);
//			entity.setTime(getTime());
//			entity.setMsgType(false);
//
			mAuthTask = new publishInfoTask();
			mAuthTask.execute((Void) null);
			/*
			 * //handler.post(new Runnable(){
			 * 
			 * // @Override // public void run() { // TODO Auto-generated method
			 * stub try{ Socket socket = new
			 * Socket(getString(R.string.Server_IP)
			 * ,Integer.parseInt(getString(R.string.Server_Port))); PrintWriter
			 * out = new PrintWriter(socket.getOutputStream(),true);
			 * out.print("publishInfo " + ID + " " + cnname + " " + cnfrom + " "
			 * + cnto + " " + cndate + " " + cndetail + " " + getTime());
			 * out.flush(); InputStream br = socket.getInputStream(); byte[]
			 * buffer =new byte[1024]; int readSize=br.read(buffer);
			 * if(readSize>0) { String publishMsg=new String(buffer,0,readSize);
			 * if(publishMsg.contains("publishInfoSuccess"))
			 * Toast.makeText(EditActivity.this, "�����ɹ�", Toast.LENGTH_SHORT);
			 * else Toast.makeText(EditActivity.this, "����ʧ��",
			 * Toast.LENGTH_SHORT); }
			 * 
			 * }catch(Exception e) {
			 * 
			 * } //}
			 * 
			 * 
			 * //});
			 */
//			mDataArrays.add(entity);
//			mAdapter.notifyDataSetChanged();

			/*
			 * DatabaseHelper dbHelper2 = new DatabaseHelper(EditActivity.this,
			 * "info"); SQLiteDatabase db2 = dbHelper2.getWritableDatabase();
			 * 
			 * db2.insert("info", null, values);
			 * 
			 * 
			 * //from HuangXin ContentValues values=new ContentValues();
			 * values.put("info_from",fromString);
			 * values.put("info_to",toString);
			 * values.put("info_username",nameString);
			 * values.put("info_date",dateString);
			 * values.put("info_detail",detailString); DatabaseHelper dbHelper =
			 * new DatabaseHelper(EditActivity.this,"info"); SQLiteDatabase db =
			 * dbHelper.getWritableDatabase(); db.insert("info", null, values);
			 * db.close(); dbHelper.close();
			 */

			// �������������ݿ��table����

			// ����Ҫ��������������Ϣ�ϴ��������������ϴ������������ݵĴ������һ��

			from.setText("");
			to.setText("");
			date.setText("");
			detail.setText("");
			released = true;

//			mListView.setSelection(mListView.getCount() - 1);
//		}

		Intent intent = new Intent(EditActivity.this, DiscussActivity.class);
		startActivity(intent);
	}

	private String getTime() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY) + 8);
		String mins = String.valueOf(c.get(Calendar.MINUTE));

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":"
				+ mins);

		return sbBuffer.toString();
	}

	public class publishInfoTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				Socket socket = new Socket(getString(R.string.Server_IP),
						Integer.parseInt(getString(R.string.Server_Port)));
				PrintWriter out = new PrintWriter(socket.getOutputStream(),
						true);
				String[] time = getTime().split(" ");
				out.print("publishInfo " + ID + "`" + cnname + "`" + cnfrom
						+ "`" + cnto + "`" + cndate + "`" + cndetail + "`"
						+ time[0] + "|" + time[1]);
				System.out.println("publishInfo " + ID + "`" + cnname + "`" + cnfrom
						+ "`" + cnto + "`" + cndate + "`" + cndetail + "`"
						+ time[0] + "|" + time[1]);
				out.flush();
				InputStream br = socket.getInputStream();
				byte[] buffer = new byte[1024];
				int readSize = br.read(buffer);
				if (readSize > 0) {
					publishMsg = new String(buffer, 0, readSize);
					socket.close();
					return publishMsg.contains("publishInfoSuccess");
				}
			} catch (Exception e) {
				return false;
			}

			// TODO: register the new account here.
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			if (success) {
				String[] PublishrMsg = publishMsg.split(":");
				ContentValues values = new ContentValues();
				values.put("GroupID", PublishrMsg[1]);
				values.put("info_ID", ID);
				DatabaseHelper dbHelper = new DatabaseHelper(EditActivity.this,
						"iPin");
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor cursor = db.query("LoginUser", new String[] { "ID",
						"HeadImageVersion" }, null, null, null, null, null);
				int Head = 0;
				while (cursor.moveToNext()) {
					Head = cursor.getInt(cursor
							.getColumnIndex("HeadImageVersion"));

				}
				db.close();
				dbHelper.close();
				values.put("info_HeadImageVersion", Head);
				values.put("info_from", cnfrom);
				values.put("info_to", cnto);
				values.put("info_username", cnname);
				values.put("info_date", cndate);
				values.put("info_detail", cndetail);
				values.put("memberCount", 0);
				values.put("memberList", "");
				DatabaseHelper dbHelper_discuss = new DatabaseHelper(
						EditActivity.this, "iPin");
				SQLiteDatabase db_discuss = dbHelper_discuss
						.getWritableDatabase();
				db_discuss.insert("discuss", null, values);
				db_discuss.close();
				dbHelper_discuss.close();

				Toast.makeText(EditActivity.this, "�����ɹ�", Toast.LENGTH_SHORT)
						.show();
				Intent data = new Intent();
				setResult(50, data);
				EditActivity.this.finish();
			} else {
				Toast.makeText(EditActivity.this, "����ʧ��", Toast.LENGTH_SHORT)
						.show();
			}

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}

	// �����˵�
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "�˳�����");
		return super.onCreateOptionsMenu(menu);
	}

	// �˵���Ӧ
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			ActivityManager.getInstance().exit();
		}
		return true;
	}
}
