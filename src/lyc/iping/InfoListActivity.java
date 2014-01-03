package lyc.iping;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lyc.iping.DiscussActivity.grouprcv;
import lyc.iping.LoginActivity.UserLoginTask;

import org.w3c.dom.Text;

import com.readystatesoftware.viewbadger.BadgeView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class InfoListActivity extends ListActivity {
	/** Called when the activity is first created. */

	// ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,
	// String>>();

	String info_username = null;
	String info_from = null;
	String info_to = null;
	String info_date = null;
	String info_detail = null;

	private ImageView img_setting = null;
	private ImageView img_discuss = null;
	private ImageView img_nearby = null;

	private ImageView img_refresh = null;

	private EditText searchText_from;
	private EditText searchText_to;
	private EditText searchText_date = null;
	View target = null;
	BadgeView badge = null;

	private List<InfoListMsgEntity> mDataArrays = null;
	private Map<String,Bitmap> headImage = null;
	private InfoListMsgViewAdapter mAdapter = null;
	private InfoRefreshTask mInfoRefreshTask = null;
	private int really_out = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_info);
		headImage = new HashMap<String,Bitmap>();
		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);
		
		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		target = findViewById(R.id.img_address);
		badge = new BadgeView(InfoListActivity.this, target);
		initView();
		img_refresh = (ImageView) findViewById(R.id.infolist_refresh);
		img_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mInfoRefreshTask = new InfoRefreshTask();
				mInfoRefreshTask.execute((Void) null);
			}
		});
		// add by hx
		Listrcv rcv = new Listrcv();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.rcv");
		this.registerReceiver(rcv, filter);
		System.out.println("ע��List�㲥��ɣ�");
		// end

		searchText_date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
		
	}

	private void showDialog() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datedialog = new DatePickerDialog(
				InfoListActivity.this, new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// TODO Auto-generated method stub
						searchText_date.setText(year + "-" + (monthOfYear + 1)
								+ "-" + dayOfMonth);
						searchText_date.setSelection(searchText_date.length());

					}
				}, year, monthOfYear, dayOfMonth);
		datedialog.show();
	}

	public void initView() {
		/*
		 * DatabaseHelper dbHelper = new
		 * DatabaseHelper(InfoListActivity.this,"info"); SQLiteDatabase db =
		 * dbHelper.getWritableDatabase(); Cursor cursor = db.query("info", new
		 * String
		 * []{"info_username","info_from","info_to","info_date","info_detail"},
		 * null, null, null, null, null); while(cursor.moveToNext()) { info_from
		 * = cursor.getString(cursor.getColumnIndex("info_from")); info_to =
		 * cursor.getString(cursor.getColumnIndex("info_to")); info_date =
		 * cursor.getString(cursor.getColumnIndex("info_date")); info_username =
		 * cursor.getString(cursor.getColumnIndex("info_username"));
		 * 
		 * HashMap<String, String> map = new HashMap<String, String>();
		 * map.put("info_username", info_username); map.put("info_date",
		 * info_date); map.put("info_from", info_from); map.put("info_to",
		 * info_to); list.add(map); } db.close(); dbHelper.close();
		 * InfoListMsgViewAdapter listAdapter = new InfoListMsgViewAdapter(this,
		 * list, R.layout.info_item_msg_text, new String[] { "info_username",
		 * "info_date", "info_from", "info_to"}, new int[] {
		 * R.id.info_username,R.id.info_date, R.id.info_from,R.id.info_to});
		 * setListAdapter(listAdapter);
		 */
		img_discuss = (ImageView) findViewById(R.id.img_address);
		img_nearby = (ImageView) findViewById(R.id.img_nearby);
		img_setting = (ImageView) findViewById(R.id.img_settings);

		mDataArrays = new ArrayList<InfoListMsgEntity>();
		List<InfoListMsgEntity> mOriginalValues;
		/*
		 * InfoListMsgEntity entity = new InfoListMsgEntity();
		 * entity.setID("000000001"); entity.setHeadImageVersion("1");
		 * entity.setUsername("user"); entity.setFrom("aaa");
		 * entity.setTo("bbb"); entity.setDate("7/7"); mDataArrays.add(entity);
		 */
		mAdapter = new InfoListMsgViewAdapter(this, mDataArrays, headImage);
		setListAdapter(mAdapter);
		EditText filterEditText1 = (EditText) findViewById(R.id.editText1);
		EditText filterEditText2 = (EditText) findViewById(R.id.searchText2);
		EditText filterEditText3 = (EditText) findViewById(R.id.searchText3);
		searchText_date = filterEditText3;
		mInfoRefreshTask = new InfoRefreshTask();
		mInfoRefreshTask.execute((Void) null);
		// Add Text Change Listener to EditText
		filterEditText1.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Call back the Adapter with current character to Filter
				EditText filterString1 = (EditText) findViewById(R.id.editText1);
				EditText filterString2 = (EditText) findViewById(R.id.searchText2);
				EditText filterString3 = (EditText) findViewById(R.id.searchText3);
				CharSequence s1, s2, s3;
				if ("".equals(filterString1.getText().toString().trim())) {
					s1 = null;
				} else {
					s1 = (CharSequence) (filterString1.getText().toString());
				}

				if ("".equals(filterString2.getText().toString().trim())) {
					s2 = null;
				} else {
					s2 = (CharSequence) (filterString2.getText().toString());
				}
				if ("".equals(filterString3.getText().toString().trim())) {
					s3 = null;
				} else {
					s3 = (CharSequence) (filterString3.getText().toString());
				}
				s = s1 + " " + s2 + " " + s3;
				mAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		filterEditText2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Call back the Adapter with current character to Filter
				EditText filterString1 = (EditText) findViewById(R.id.editText1);
				EditText filterString2 = (EditText) findViewById(R.id.searchText2);
				EditText filterString3 = (EditText) findViewById(R.id.searchText3);
				CharSequence s1, s2, s3;
				if ("".equals(filterString1.getText().toString().trim())) {
					s1 = null;
				} else {
					s1 = (CharSequence) (filterString1.getText().toString());
				}

				if ("".equals(filterString2.getText().toString().trim())) {
					s2 = null;
				} else {
					s2 = (CharSequence) (filterString2.getText().toString());
				}
				if ("".equals(filterString3.getText().toString().trim())) {
					s3 = null;
				} else {
					s3 = (CharSequence) (filterString3.getText().toString());
				}
				s = s1 + " " + s2 + " " + s3;
				mAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		filterEditText3.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Call back the Adapter with current character to Filter
				EditText filterString1 = (EditText) findViewById(R.id.editText1);
				EditText filterString2 = (EditText) findViewById(R.id.searchText2);
				EditText filterString3 = (EditText) findViewById(R.id.searchText3);
				CharSequence s1, s2, s3;
				if ("".equals(filterString1.getText().toString().trim())) {
					s1 = null;
				} else {
					s1 = (CharSequence) (filterString1.getText().toString());
				}

				if ("".equals(filterString2.getText().toString().trim())) {
					s2 = null;
				} else {
					s2 = (CharSequence) (filterString2.getText().toString());
				}
				if ("".equals(filterString3.getText().toString().trim())) {
					s3 = null;
				} else {
					s3 = (CharSequence) (filterString3.getText().toString());
				}
				s = s1 + " " + s2 + " " + s3;
				mAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// ��ʾ��Ϣ��Ŀ
		if (service.group_msg_num != 0) {
			badge.setText(service.group_msg_num + "");
			// badge.setTextColor(Color.RED);
			badge.setTextSize(10);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setBadgeMargin(0, 0);
			badge.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ���Ը��ݶ���������������Ӧ�Ĳ���
		if (20 == resultCode) { /*
								 * String
								 * cnfrom=data.getExtras().getString("cnfrom");
								 * String
								 * cnto=data.getExtras().getString("cnto");
								 * String
								 * cndate=data.getExtras().getString("cndate");
								 * String
								 * cnname=data.getExtras().getString("cnname");
								 * 
								 * HashMap<String, String> map = new
								 * HashMap<String, String>();
								 * map.put("info_username", cnname);
								 * map.put("info_date", cndate);
								 * map.put("info_from", cnfrom);
								 * map.put("info_to", cnto); list.add(map);
								 * InfoListMsgViewAdapter listAdapter = new
								 * InfoListMsgViewAdapter(this, list,
								 * R.layout.info_item_msg_text, new String[] {
								 * "info_username", "info_date", "info_from",
								 * "info_to"}, new int[] {
								 * R.id.info_username,R.id.info_date,
								 * R.id.info_from,R.id.info_to});
								 * setListAdapter(listAdapter);
								 */
		} else if (50 == resultCode) {
			mInfoRefreshTask = new InfoRefreshTask();
			mInfoRefreshTask.execute((Void) null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// ���ñ������Ҳఴť������
	public void btnmainright(View v) {
		Intent intent = new Intent(InfoListActivity.this, EditActivity.class);
		startActivityForResult(intent, 100);
		// finish();
	}

	public void btn_discuss(View v) {
		Intent intent = new Intent(InfoListActivity.this, DiscussActivity.class);
		startActivity(intent);
		InfoListActivity.this.finish();
	}

	public void btn_nearby(View v) {
		Intent intent = new Intent(InfoListActivity.this, NearByActivity.class);
		startActivity(intent);
	}

	public void btn_setting(View v) {
		Intent intent = new Intent(InfoListActivity.this, SettingActivity.class);
		startActivity(intent);
		InfoListActivity.this.finish();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(InfoListActivity.this,
				ReleasedActivity.class);
		intent.putExtra("position", position);
		startActivity(intent);
	}

	public class InfoRefreshTask extends AsyncTask<Void, Void, Boolean> {
		private Socket socket = null;
		PrintWriter out = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			try {
				socket = new Socket(getString(R.string.Server_IP),
						Integer.parseInt(getString(R.string.Server_Port)));
				out = new PrintWriter(socket.getOutputStream(), true);
				out.print("InfoListRefresh");
				out.flush();
				mDataArrays.clear();
				InputStream br = socket.getInputStream();
				byte[] buffer = new byte[4096];
				int readSize = br.read(buffer);
				if (readSize > 0) {
					String InfoRefreshMsg = new String(buffer, 0, readSize);
					System.out.println(InfoRefreshMsg);
					if (InfoRefreshMsg.contains("InfoRefreshNone"))
						return false;
					String[] temp = InfoRefreshMsg.split("`");

					HeadImageDownloader downloader = new HeadImageDownloader();

					DatabaseHelper dbHelper = new DatabaseHelper(
							InfoListActivity.this, "iPin");
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.delete("info", null, null);
					for (int i = 0; i < Integer.parseInt(temp[0]); i++) {
						InfoListMsgEntity entity = new InfoListMsgEntity();
						String Index, ID, HeadImageVersion, username, from, to, date, detail, time, memberCount, memberList;
						Index = temp[i * 11 + 1];
						ID = temp[i * 11 + 2];
						HeadImageVersion = temp[i * 11 + 3];
						username = temp[i * 11 + 4];
						from = temp[i * 11 + 5];
						to = temp[i * 11 + 6];
						date = temp[i * 11 + 7];
						detail = temp[i * 11 + 8];
						time = temp[i * 11 + 9];
						memberCount = temp[i * 11 + 10];
						memberList = temp[i * 11 + 11];
						entity.setID(ID);
						entity.setHeadImageVersion(HeadImageVersion);
						entity.setUsername(username);
						entity.setFrom(from);
						entity.setTo(to);
						entity.setDate(date);
						entity.setMemberCount(memberCount);
						mDataArrays.add(entity);
						ContentValues values = new ContentValues();
						values.put("GroupID", Index);
						values.put("info_ID", ID);
						values.put("info_HeadImageVersion", HeadImageVersion);
						values.put("info_username", username);
						values.put("info_from", from);
						values.put("info_to", to);
						values.put("info_date", date);
						values.put("info_detail", detail);
						values.put("info_time", time);
						values.put("memberCount", memberCount);
						values.put("memberList", memberList);
						db.insert("info", null, values);
						downloader.download(InfoListActivity.this, ID,
								HeadImageVersion);
					}
					db.close();
					socket.close();
					return true;
				}
			} catch (Exception e) {
				return false;
			}

			// TODO: register the new account here.
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mInfoRefreshTask = null;
			if (success) {
				mAdapter.notifyDataSetChanged();
			} else {

			}

		}

		@Override
		protected void onCancelled() {
			mInfoRefreshTask = null;
		}
	}

	/*
	 * //add by hx
	 * 
	 * @Override public void onBackPressed() {
	 * 
	 * try { service.clientsocket.close(); } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * service.handler.removeCallbacks(service.runnable); service.tdrun = 0;
	 * Intent intent2=new Intent(this,service.class); stopService(intent2);
	 * 
	 * super.onBackPressed();
	 * 
	 * } //end
	 */

	public class Listrcv extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// ��ʾ��Ϣ��Ŀ
			badge.setText(service.group_msg_num + "");
			// badge.setTextColor(Color.RED);
			badge.setTextSize(10);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setBadgeMargin(0, 0);
			badge.show();

		}
	}

	// add by hx
	@Override
	public void onBackPressed() {
		if (really_out == 0) {
			Toast.makeText(InfoListActivity.this, "�ٰ�һ���˳�������ɾ���ľ��˳���Ŷ~",
					Toast.LENGTH_SHORT).show();
			really_out = 1;
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					really_out = 0;
				}
			}, 2500);
		}

		else {
			service.tdrun = 0;
			try {
				service.clientBuff.close();
				service.clientoutput.close();
				service.clientsocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			service.handler.removeCallbacks(service.runnable);
			Intent intent2 = new Intent(this, service.class);
			stopService(intent2);

			super.onBackPressed();
		}
	}

	// end

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