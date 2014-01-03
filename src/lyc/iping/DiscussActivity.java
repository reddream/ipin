package lyc.iping;

import android.util.Log;
import lyc.iping.Group;
import lyc.iping.InfoListActivity.InfoRefreshTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.readystatesoftware.viewbadger.BadgeView;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class DiscussActivity extends ListActivity {

	String GroupID = null;
	String info_ID = null;
	String info_HeadImageVersion = null;
	String info_username = null;
	String info_from = null;
	String info_to = null;
	String info_date = null;
	String info_detail = null;
	String LoginUser = null;
	View target = null;
	BadgeView badge = null;
	static public String GroupIDnow=null;

	// add by hx
	private int[] flag = new int[30];
	private static String chat_groupid = null;
	// add

	private ImageView img_info = null;
	private ImageView img_setting = null;
	private ImageView img_nearby = null;
	private List<DiscussListMsgEntity> mDataArrays = null;
	private Map<String,Bitmap> headImage = null;
	private DiscussListViewAdapter mAdapter = null;
	private DiscussRefreshTask mDiscussRefreshTask = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_discuss);
		headImage = new HashMap<String,Bitmap>();
		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);

		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		target = findViewById(R.id.img_address_1);
		badge = new BadgeView(DiscussActivity.this, target);

		// add by hx
		grouprcv rcv = new grouprcv();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.rcv");
		this.registerReceiver(rcv, filter);
		System.out.println("ע��group�㲥��ɣ�");
		// end

		initView();
	}

	public void initView() {
		mDataArrays = new ArrayList<DiscussListMsgEntity>();
		// add by hx
		for (int i = 0; i < 30; i++) {
			flag[i] = -1;
		}
		if (service.group_msg_num != 0) {
			SetGroupColor();
		} else {
			mAdapter = new DiscussListViewAdapter(this, mDataArrays, flag,headImage);
			setListAdapter(mAdapter);
		}
		// end

		DatabaseHelper dbHelper = new DatabaseHelper(DiscussActivity.this,
				"iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("LoginUser",
				new String[] { "ID", "username", "password", "sex",
						"telephone", "HeadImageVersion", "autoLogin" },
				"username<>?", new String[] { "null" }, null, null, null);
		while (cursor.moveToNext()) {
			LoginUser = cursor.getString(cursor.getColumnIndex("username"));
		}
		db.close();
		dbHelper.close();

		img_info = (ImageView) findViewById(R.id.img_weixin_1);
		img_nearby = (ImageView) findViewById(R.id.img_nearby_1);
		img_setting = (ImageView) findViewById(R.id.img_settings_1);

		mDiscussRefreshTask = new DiscussRefreshTask();
		mDiscussRefreshTask.execute((Void) null);

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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		DatabaseHelper dbHelper = new DatabaseHelper(DiscussActivity.this,
				"iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		Cursor cursor = db.query("discuss", new String[] { "GroupID",
				"info_ID", "info_HeadImageVersion", "info_username",
				"info_from", "info_to", "info_date", "info_detail",
				"memberCount", "memberList" }, null, null, null, null, null);

		cursor.move((int) id + 1);
		GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
		info_ID = cursor.getString(cursor.getColumnIndex("info_ID"));
		info_from = cursor.getString(cursor.getColumnIndex("info_from"));
		info_to = cursor.getString(cursor.getColumnIndex("info_to"));
		info_date = cursor.getString(cursor.getColumnIndex("info_date"));
		info_username = cursor
				.getString(cursor.getColumnIndex("info_username"));
		info_detail = cursor.getString(cursor.getColumnIndex("info_detail"));
		int Head = cursor
				.getInt(cursor.getColumnIndex("info_HeadImageVersion"));
		String memberCount = cursor.getString(cursor
				.getColumnIndex("memberCount"));
		String memberList = cursor.getString(cursor
				.getColumnIndex("memberList"));
		db.close();
		dbHelper.close();

		Group g = new Group();
		g.setGroupID(GroupID);
		g.setCreatorID(info_ID);
		g.setCreatorUsername(info_username);
		g.setCreatorHeadImageVersion(String.valueOf(Head));
		g.setFrom(info_from);
		g.setTo(info_to);
		g.setDate(info_date);
		g.setDetail(info_detail);
		g.setMemberCount(memberCount);
		g.setMemberList(memberList);
		// Log.d("from",info_from);
		Intent intent = new Intent(DiscussActivity.this, ChatActivity.class);
		intent.putExtra("group", g);
		startActivity(intent);
		
		GroupIDnow = GroupID;

		// add by hx
		System.out.println("group_msg_num:" + service.group_msg_num);
		if (service.group_msg_num > 0) {
			Arrays.sort(flag); // ���ȶ���������
			int result = Arrays.binarySearch(flag, (int) id); // �������������Ƿ���id
			int num = 0;
			if (result >= 0) {
				for (int i = 0; i < service.group_msg_num; i++) {
					if (service.Gid[i].equals(GroupID)) {
						num = i;
						break;
					}
				}
				for (int j = num; j < (service.group_msg_num - 1); j++) {
					service.Gid[j] = service.Gid[j + 1];
				}

				service.group_msg_num--;
			}

			System.out.println("group_msg_num:" + service.group_msg_num);
		}

		//initView();
		SetGroupColor();
		
		if (service.group_msg_num == 0) {
			badge.hide();
		} else {
			badge.setText(service.group_msg_num + "");
			// badge.setTextColor(Color.RED);
			badge.setTextSize(10);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setBadgeMargin(0, 0);
			badge.show();
		}

		// end

	}

	public class DiscussRefreshTask extends AsyncTask<Void, Void, Boolean> {
		private Socket socket = null;
		PrintWriter out = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			try {
				socket = new Socket(getString(R.string.Server_IP),
						Integer.parseInt(getString(R.string.Server_Port)));
				out = new PrintWriter(socket.getOutputStream(), true);
				out.print("DiscussRefresh " + LoginUser);
				out.flush();
				mDataArrays.clear();
				InputStream br = socket.getInputStream();
				byte[] buffer = new byte[2048];
				int readSize = br.read(buffer);
				if (readSize > 0) {
					String DiscussRefreshMsg = new String(buffer, 0, readSize);
					if (DiscussRefreshMsg.contains("DiscussRefreshNone"))
						return false;
					String[] temp = DiscussRefreshMsg.split("`");

					HeadImageDownloader downloader = new HeadImageDownloader();

					DatabaseHelper dbHelper = new DatabaseHelper(
							DiscussActivity.this, "iPin");
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.delete("discuss", null, null);
					for (int i = 0; i < Integer.parseInt(temp[0]); i++) {
						DiscussListMsgEntity entity = new DiscussListMsgEntity();
						String GroupID, ID, HeadImageVersion, username, from, to, date, detail, memberCount, memberList;
						GroupID = temp[i * 10 + 1];
						ID = temp[i * 10 + 2];
						HeadImageVersion = temp[i * 10 + 3];
						username = temp[i * 10 + 4];
						from = temp[i * 10 + 5];
						to = temp[i * 10 + 6];
						date = temp[i * 10 + 7];
						detail = temp[i * 10 + 8];
						memberCount = temp[i * 10 + 9];
						memberList = temp[i * 10 + 10];
						ContentValues values = new ContentValues();
						values.put("GroupID", GroupID);
						values.put("info_ID", ID);
						values.put("info_HeadImageVersion", HeadImageVersion);
						values.put("info_username", username);
						values.put("info_from", from);
						values.put("info_to", to);
						values.put("info_date", date);
						values.put("info_detail", detail);
						values.put("memberCount", memberCount);
						values.put("memberList", memberList);
						db.insert("discuss", null, values);
						entity.setID(ID);
						entity.setHeadImageVersion(HeadImageVersion);
						entity.setUsername(username);
						entity.setFrom(from);
						entity.setTo(to);
						entity.setDate(date);
						entity.setMemberCount(memberCount);
						mDataArrays.add(entity);
						downloader.download(DiscussActivity.this, ID,
								HeadImageVersion);
					}
					db.close();
					dbHelper.close();
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
			mDiscussRefreshTask = null;
			if (success) {
				mAdapter.notifyDataSetChanged();
			} else {

			}

		}

		@Override
		protected void onCancelled() {
			mDiscussRefreshTask = null;
		}
	}

	public class grouprcv extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// unregisterReceiver(this);
//			String chat_name = intent.getStringExtra("name");
//			String chat_groupid = intent.getStringExtra("groupID");
//			String chat_msg = intent.getStringExtra("msg");
//			String chat_date = intent.getStringExtra("date");

			// ��ʾ��Ϣ��Ŀ
			if (service.group_msg_num != 0) {
				badge.setText(service.group_msg_num + "");
				// badge.setTextColor(Color.RED);
				badge.setTextSize(10);
				badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
				badge.show();
			}

			SetGroupColor();
		}
	}

	public void SetGroupColor() {
		//flag�����ʼ��
		for (int i = 0; i < 30; i++) {
			flag[i] = -1;
		}
		// ����groupID�������ݿ�
		DatabaseHelper dbHelper = new DatabaseHelper(DiscussActivity.this,
				"iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		Cursor cursor = db.query("discuss", new String[] { "GroupID",
				"info_ID", "info_HeadImageVersion", "info_username",
				"info_from", "info_to", "info_date", "info_detail",
				"memberCount", "memberList" }, null, null, null, null, null);

		int i, j, k = 0;
		cursor.moveToFirst();
		for (i = 0; i < cursor.getCount(); cursor.moveToPosition(i)) {
			String temp = cursor.getString(cursor.getColumnIndex("GroupID"));
			for (j = 0; j < service.group_msg_num; j++) {
				if (temp.equals(service.Gid[j])) {
					flag[k] = i;
					k++;
					break;
				}

			}
			i++;
		}

		db.close();

		System.out.println("flag0:"+flag[0]);
		// ���ݷ���ֵ������ɫ
		if(flag[0] != -1)
		{
			mAdapter = new DiscussListViewAdapter(DiscussActivity.this,
				mDataArrays, flag,headImage);
		}
		else
		{
			mAdapter = new DiscussListViewAdapter(DiscussActivity.this,
					mDataArrays, null,headImage);
		}
		setListAdapter(mAdapter);
	}

	// end

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, InfoListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		this.finish();
		// super.onBackPressed();

	}

	public void btn_infolist(View v) {
		Intent intent = new Intent(DiscussActivity.this, InfoListActivity.class);
		startActivity(intent);
		DiscussActivity.this.finish();
	}

	public void btn_nearby(View v) {
		Intent intent = new Intent(DiscussActivity.this, NearByActivity.class);
		startActivity(intent);
	}

	public void btn_setting(View v) {
		Intent intent = new Intent(DiscussActivity.this, SettingActivity.class);
		startActivity(intent);
		DiscussActivity.this.finish();
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
