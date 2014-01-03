package lyc.iping;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.readystatesoftware.viewbadger.BadgeView;

import lyc.iping.EditActivity.publishInfoTask;
import lyc.iping.InfoListActivity.InfoRefreshTask;
import lyc.iping.InfoListActivity.Listrcv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class NearByActivity extends ListActivity {

	String nearby_username = null;
	String nearby_distance = null;
	String nearby_telnum = null;
	View target = null;
	BadgeView badge = null;

	String user_destination = null;// �����ݴ��û������Ŀ�ĵ�

	String Longitude = null;
	String Latitude = null;

	public String ID, userHead, user_name, user_telnum;

	private List<NearByMsgEntity> NearByArrays = null;
	private NearByMsgViewAdapter NearByAdapter = null;
	private NearByRefreshTask NearByRefreshTask = null;

	private ImageView img_setting = null;
	private ImageView img_discuss = null;
	private ImageView img_info = null;
	private ImageView img_setDestination = null;
	private ImageView img_refresh = null;

	private LocationManager lm;
	private static final String TAG = "GpsActivity";

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lm.removeUpdates(locationListener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_nearby);

		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);

		// add by hx
		Nearrcv rcv = new Nearrcv();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.rcv");
		this.registerReceiver(rcv, filter);
		System.out.println("ע��Near�㲥��ɣ�");
		// end

		target = findViewById(R.id.img_address_2);
		badge = new BadgeView(NearByActivity.this, target);

		DatabaseHelper dbHelper = new DatabaseHelper(NearByActivity.this,
				"iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("LoginUser", new String[] { "ID", "username",
				"password", "sex", "telephone", "autoLogin" }, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			ID = cursor.getString(cursor.getColumnIndex("ID"));
			user_name = cursor.getString(cursor.getColumnIndex("username"));
			user_telnum = cursor.getString(cursor.getColumnIndex("telephone"));
		}

		cursor = db.query("NearbyDestination", new String[] { "Destination" },
				"ID=?", new String[] { ID }, null, null, null);
		while (cursor.moveToNext()) {
			user_destination = cursor.getString(cursor
					.getColumnIndex("Destination"));
		}
		db.close();
		dbHelper.close();

		img_info = (ImageView) findViewById(R.id.img_weixin_2);
		img_discuss = (ImageView) findViewById(R.id.img_address_2);
		img_setting = (ImageView) findViewById(R.id.img_settings_2);

		img_setDestination = (ImageView) findViewById(R.id.nearby_setDestination);
		img_setDestination.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText input = new EditText(NearByActivity.this);
				Dialog dialog = new AlertDialog.Builder(NearByActivity.this)
						.setTitle("�޸�Ŀ�ĵ�")
						.setMessage("����������Ŀ�ĵ�")
						.setView(input)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										// Toast.makeText(NearByActivity.this,
										// "�Һ�ϲ�����ĵ�Ӱ��",
										// Toast.LENGTH_LONG).show();
										String value = input.getText()
												.toString();
										if (TextUtils.isEmpty(value)) {
											Toast.makeText(NearByActivity.this,
													"Ŀ�ĵز���Ϊ��",
													Toast.LENGTH_LONG).show();
											try {
												Field field = dialog
														.getClass()
														.getSuperclass()
														.getDeclaredField(
																"mShowing");
												field.setAccessible(true);
												field.set(dialog, false);
											} catch (Exception e) {
												e.printStackTrace();
											}
											// lm = (LocationManager)
											// getSystemService(Context.LOCATION_SERVICE);
											// NearByActivity.this.finish();
											// input.setError("�������ڲ���Ϊ��");
											// input.requestFocus();
											// return;
										} else {
											// System.out.println("fff");
											// System.out.println(value.toString());
											user_destination = value;// ��Ŀ�ĵص㸳�������Ա��ڴ����������
											Toast.makeText(
													NearByActivity.this,
													"Ŀ�ĵ����趨Ϊ��"
															+ user_destination,
													Toast.LENGTH_LONG).show();
											try {
												Field field = dialog
														.getClass()
														.getSuperclass()
														.getDeclaredField(
																"mShowing");
												field.setAccessible(true);
												field.set(dialog, true);
											} catch (Exception e) {
												e.printStackTrace();
											}
											ContentValues values = new ContentValues();
											values.put("ID", ID);
											values.put("Destination",
													user_destination);
											DatabaseHelper dbHelper = new DatabaseHelper(
													NearByActivity.this, "iPin");
											SQLiteDatabase db = dbHelper
													.getWritableDatabase();
											db.update("NearbyDestination",
													values, "ID=?",
													new String[] { ID });
											// setTitle(value.toString());
											Locate();
										}

									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										// Toast.makeText(NearByActivity.this,
										// "�Ҳ�ϲ�����ĵ�Ӱ��",
										// Toast.LENGTH_LONG)
										// .show();
										// lm = (LocationManager)
										// getSystemService(Context.LOCATION_SERVICE);
										// NearByActivity.this.finish();
									}
								}).setCancelable(false).show();
				Window window = dialog.getWindow();
				window.setGravity(Gravity.CENTER);
			}
		});

		img_refresh = (ImageView) findViewById(R.id.nearby_refresh);
		img_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Locate();
			}
		});

		if (TextUtils.isEmpty(user_destination)) {
			final EditText input = new EditText(this);
			Dialog dialog = new AlertDialog.Builder(this)
					.setTitle("Iƴ��ʾ")
					.setMessage("����������Ŀ�ĵز�����GPS��λ����")
					.setView(input)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// Toast.makeText(NearByActivity.this,
									// "�Һ�ϲ�����ĵ�Ӱ��",
									// Toast.LENGTH_LONG).show();
									String value = input.getText().toString();
									if (TextUtils.isEmpty(value)) {

										Toast.makeText(NearByActivity.this,
												"Ŀ�ĵز���Ϊ��", Toast.LENGTH_LONG)
												.show();
										try {
											Field field = dialog
													.getClass()
													.getSuperclass()
													.getDeclaredField(
															"mShowing");
											field.setAccessible(true);
											field.set(dialog, false);
										} catch (Exception e) {
											e.printStackTrace();
										}
										// lm = (LocationManager)
										// getSystemService(Context.LOCATION_SERVICE);
										// NearByActivity.this.finish();
										// input.setError("�������ڲ���Ϊ��");
										// input.requestFocus();
										// return;
									} else {

										// System.out.println("fff");
										// System.out.println(value.toString());
										user_destination = value;// ��Ŀ�ĵص㸳�������Ա��ڴ����������
										Toast.makeText(NearByActivity.this,
												"Ŀ�ĵ����趨Ϊ��" + user_destination,
												Toast.LENGTH_LONG).show();
										try {
											Field field = dialog
													.getClass()
													.getSuperclass()
													.getDeclaredField(
															"mShowing");
											field.setAccessible(true);
											field.set(dialog, true);
										} catch (Exception e) {
											e.printStackTrace();
										}
										ContentValues values = new ContentValues();
										values.put("ID", ID);
										values.put("Destination",
												user_destination);
										DatabaseHelper dbHelper = new DatabaseHelper(
												NearByActivity.this, "iPin");
										SQLiteDatabase db = dbHelper
												.getWritableDatabase();
										db.insert("NearbyDestination", null,
												values);
										// setTitle(value.toString());
										Locate();
									}

								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// Toast.makeText(NearByActivity.this,
									// "�Ҳ�ϲ�����ĵ�Ӱ��",
									// Toast.LENGTH_LONG)
									// .show();
									lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
									NearByActivity.this.finish();
								}
							}).setCancelable(false).show();
			Window window = dialog.getWindow();
			window.setGravity(Gravity.CENTER);
		} else {
			Toast.makeText(NearByActivity.this, "��ǰ�趨Ŀ�ĵ�Ϊ��" + user_destination,
					Toast.LENGTH_LONG).show();
			Locate();
		}

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

	public void Locate() {
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// �ж�GPS�Ƿ���������
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Toast.makeText(this, "�뿪��GPS����...", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, 0);
			return;
		}

		// Ϊ��ȡ����λ����Ϣʱ���ò�ѯ����
		String bestProvider = lm.getBestProvider(getCriteria(), true);
		// ��ȡλ����Ϣ
		// ��������ò�ѯҪ��getLastKnownLocation�������˵Ĳ���ΪLocationManager.GPS_PROVIDER
		Location location = lm.getLastKnownLocation(bestProvider);
		updateView(location);
		// ����״̬
		lm.addGpsStatusListener(listener);
		// �󶨼�������4������
		// ����1���豸����GPS_PROVIDER��NETWORK_PROVIDER����
		// ����2��λ����Ϣ�������ڣ���λ����
		// ����3��λ�ñ仯��С���룺��λ�þ���仯������ֵʱ��������λ����Ϣ
		// ����4������
		// ��ע������2��3���������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��

		// 1�����һ�Σ�����Сλ�Ʊ仯����1�׸���һ�Σ�
		// ע�⣺�˴�����׼ȷ�ȷǳ��ͣ��Ƽ���service��������һ��Thread����run��sleep(10000);Ȼ��ִ��handler.sendMessage(),����λ��
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
				locationListener);
	}

	// λ�ü���
	private LocationListener locationListener = new LocationListener() {

		/**
		 * λ����Ϣ�仯ʱ����
		 */
		public void onLocationChanged(Location location) {
			updateView(location);
			Log.i(TAG, "ʱ�䣺" + location.getTime());
			Log.i(TAG, "���ȣ�" + location.getLongitude());
			Log.i(TAG, "γ�ȣ�" + location.getLatitude());
			Log.i(TAG, "���Σ�" + location.getAltitude());
		}

		/**
		 * GPS״̬�仯ʱ����
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			// GPS״̬Ϊ�ɼ�ʱ
			case LocationProvider.AVAILABLE:
				Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬");
				break;
			// GPS״̬Ϊ��������ʱ
			case LocationProvider.OUT_OF_SERVICE:
				Log.i(TAG, "��ǰGPS״̬Ϊ��������״̬");
				break;
			// GPS״̬Ϊ��ͣ����ʱ
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.i(TAG, "��ǰGPS״̬Ϊ��ͣ����״̬");
				break;
			}
		}

		/**
		 * GPS����ʱ����
		 */
		public void onProviderEnabled(String provider) {
			Location location = lm.getLastKnownLocation(provider);
			updateView(location);
		}

		/**
		 * GPS����ʱ����
		 */
		public void onProviderDisabled(String provider) {
			updateView(null);
		}

	};

	// ״̬����
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			// ��һ�ζ�λ
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "��һ�ζ�λ");
				break;
			// ����״̬�ı�
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.i(TAG, "����״̬�ı�");
				// ��ȡ��ǰ״̬
				GpsStatus gpsStatus = lm.getGpsStatus(null);
				// ��ȡ���ǿ�����Ĭ�����ֵ
				int maxSatellites = gpsStatus.getMaxSatellites();
				// ����һ��������������������
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;
				while (iters.hasNext() && count <= maxSatellites) {
					GpsSatellite s = iters.next();
					count++;
				}
				System.out.println("��������" + count + "������");
				break;
			// ��λ����
			case GpsStatus.GPS_EVENT_STARTED:
				Log.i(TAG, "��λ����");
				break;
			// ��λ����
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.i(TAG, "��λ����");
				break;
			}
		};
	};

	/**
	 * ʵʱ�����ı�����
	 * 
	 * @param location
	 */
	private void updateView(Location location) {

		NearByArrays = new ArrayList<NearByMsgEntity>();
		NearByAdapter = new NearByMsgViewAdapter(this, NearByArrays);
		setListAdapter(NearByAdapter);
		if (location != null) {
			// �Ƚ��û������Լ�Ŀ�ĵ���Ϣȡ������Ȼ��һ���͸����������ٴӷ���������
			Longitude = String.valueOf(location.getLongitude());
			Latitude = String.valueOf(location.getLatitude());

			NearByRefreshTask = new NearByRefreshTask();
			NearByRefreshTask.execute((Void) null);
		} else {
			Toast.makeText(this, "GPS��λʧ��", Toast.LENGTH_SHORT).show();
			/*
			 * NearByMsgEntity entity = new NearByMsgEntity(); String ID,
			 * HeadImageVersion, username, telnum, distance, destination; ID =
			 * "00002"; username = "cuinan"; telnum = "15828564811"; distance =
			 * "200"; destination = "��������"; entity.setID(ID);
			 * entity.setUsername(username); entity.setTelnum(telnum);
			 * entity.setDistance(distance); entity.setDestination(destination);
			 * NearByArrays.add(entity); ContentValues values = new
			 * ContentValues(); values.put("info_ID", ID);
			 * values.put("info_username", username); values.put("info_telnum",
			 * telnum); values.put("info_distance", distance);
			 * values.put("info_destination", destination);
			 */
		}

	}

	/**
	 * ���ز�ѯ����
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// ���ö�λ��ȷ�� Criteria.ACCURACY_COARSE�Ƚϴ��ԣ�Criteria.ACCURACY_FINE��ȽϾ�ϸ
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// �����Ƿ�Ҫ���ٶ�
		criteria.setSpeedRequired(false);
		// �����Ƿ�������Ӫ���շ�
		criteria.setCostAllowed(false);
		// �����Ƿ���Ҫ��λ��Ϣ
		criteria.setBearingRequired(false);
		// �����Ƿ���Ҫ������Ϣ
		criteria.setAltitudeRequired(false);
		// ���öԵ�Դ������
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		final int object_position = position;
		Dialog dialog = new AlertDialog.Builder(NearByActivity.this)
				.setTitle("Iƴ��ʾ")
				.setMessage("�Ƿ񲦴�Է��绰")
				.setPositiveButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							Intent intent = new Intent();

							intent.setAction(Intent.ACTION_CALL);
							intent.setData(Uri.parse("tel:"
									+ NearByArrays.get(object_position)
											.getTelnum()));
							startActivity(intent);
						} catch (Exception e) {

						}
					}
				})
				.setNegativeButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).setCancelable(false).show();
		Window window = dialog.getWindow();
		window.setGravity(Gravity.CENTER);
	}

	public class NearByRefreshTask extends AsyncTask<Void, Void, Boolean> {
		private Socket socket = null;
		PrintWriter out = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			try {
				socket = new Socket(getString(R.string.Server_IP),
						Integer.parseInt(getString(R.string.Server_Port)));
				out = new PrintWriter(socket.getOutputStream(), true);
				out.print("NearByRefresh " + ID + " " + Longitude + " "
						+ Latitude + " " + user_destination);
				out.flush();
				NearByArrays.clear();
				InputStream br = socket.getInputStream();
				byte[] buffer = new byte[2048];
				int readSize = br.read(buffer);
				if (readSize > 0) {
					String NearByRefreshMsg = new String(buffer, 0, readSize);
					System.out.println("NearByMsg:" + NearByRefreshMsg);
					if (NearByRefreshMsg.contains("NearByRefreshNone"))
						return false;
					String[] temp = NearByRefreshMsg.split("`");

					HeadImageDownloader downloader = new HeadImageDownloader();

					DatabaseHelper dbHelper = new DatabaseHelper(
							NearByActivity.this, "iPin");
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.delete("NearBy", null, null);
					for (int i = 0; i < Integer.parseInt(temp[0]); i++) {
						NearByMsgEntity entity = new NearByMsgEntity();
						String ID, HeadImageVersion, username, telnum, distance, destination;
						ID = temp[i * 6 + 1];
						HeadImageVersion = temp[i * 6 + 2];
						username = temp[i * 6 + 3];
						telnum = temp[i * 6 + 4];
						distance = temp[i * 6 + 5];
						destination = temp[i * 6 + 6];
						entity.setID(ID);
						entity.setHeadImageVersion(HeadImageVersion);
						entity.setUsername(username);
						entity.setTelnum(telnum);
						entity.setDistance(distance);
						entity.setDestination(destination);
						NearByArrays.add(entity);
						ContentValues values = new ContentValues();
						values.put("info_ID", ID);
						values.put("info_HeadImageVersion", HeadImageVersion);
						values.put("info_username", username);
						values.put("info_telnum", telnum);
						values.put("info_distance", distance);
						values.put("info_destination", destination);
						db.insert("NearBy", null, values);
						downloader.download(NearByActivity.this, ID,
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
			NearByRefreshTask = null;
			if (success) {
				NearByAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(NearByActivity.this, "���ĸ���û��ƴ��Ŷ",
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			NearByRefreshTask = null;
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, InfoListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		this.finish();
		// super.onBackPressed();

	}

	// ���ñ������Ҳఴť������
	public void nearby_btn_right(View v) {
		Locate();
	}

	public void btn_infolist(View v) {
		Intent intent = new Intent(NearByActivity.this, InfoListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		NearByActivity.this.finish();
	}

	public void btn_discuss(View v) {
		Intent intent = new Intent(NearByActivity.this, DiscussActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		NearByActivity.this.finish();
	}

	public void btn_setting(View v) {
		Intent intent = new Intent(NearByActivity.this, SettingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		NearByActivity.this.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ���Ը��ݶ���������������Ӧ�Ĳ���
		if (20 == resultCode) {

		} else if (0 == resultCode) {
			Locate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public class Nearrcv extends BroadcastReceiver {
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