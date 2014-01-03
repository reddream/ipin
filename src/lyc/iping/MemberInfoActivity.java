package lyc.iping;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;

import lyc.iping.GroupInfoActivity.ExitGroupTask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MemberInfoActivity extends Activity {
	private String GroupID;
	private String ID;
	private String username;
	private String HeadImageVersion;
	private String sex;
	private String telephone;
	private Button deletemember;
	private Boolean isCreator;
	private int memberIndex;
	private ImageView HeadImage;
	private TextView memberinfo_username;
	private TextView memberinfo_sex;
	private TextView memberinfo_telephone;
	private DeleteMemberTask mTask;
	private RelativeLayout OnTelephone = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memberinfo);

		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);

		GroupID = getIntent().getStringExtra("GroupID");
		ID = getIntent().getStringExtra("ID");
		username = getIntent().getStringExtra("username");
		sex = getIntent().getStringExtra("sex");
		telephone = getIntent().getStringExtra("telephone");
		HeadImageVersion = getIntent().getStringExtra("HeadImageVersion");
		memberinfo_username = (TextView) findViewById(R.id.memberinfo_username);
		memberinfo_username.setText(username);
		HeadImage = (ImageView) findViewById(R.id.memberinfo_head);
		String AppPath = getApplicationContext().getFilesDir()
				.getAbsolutePath() + "/";
		String ImgPath = AppPath + "HeadImage/" + ID + "_" + HeadImageVersion
				+ ".jpg";
		File ImgFile = new File(ImgPath);
		if (ImgFile.exists()) {
			Bitmap Image = null;
			Image = BitmapFactory.decodeFile(ImgPath, null);
			HeadImage.setImageBitmap(Image);
		}
		memberinfo_sex = (TextView) findViewById(R.id.memberinfo_sex);
		if (sex.contains("female"))
			memberinfo_sex.setText("Ů");
		memberinfo_telephone = (TextView) findViewById(R.id.memberinfo_telephone);
		memberinfo_telephone.setText(telephone);
		isCreator = getIntent().getBooleanExtra("isCreator", false);
		memberIndex = getIntent().getIntExtra("memberIndex", 0);
		deletemember = (Button) findViewById(R.id.deletemember);
		if (!isCreator || memberIndex == 0)
			deletemember.setVisibility(View.GONE);

		OnTelephone = (RelativeLayout) findViewById(R.id.onTelephone);
		OnTelephone.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(SettingActivity.this, "Click",
				// Toast.LENGTH_SHORT).show();
				/*
				 * try{ Intent intent = new Intent();
				 * 
				 * intent.setAction(Intent.ACTION_CALL);
				 * intent.setData(Uri.parse("tel:"+telephone));
				 * startActivity(intent); }catch(Exception e) {
				 * 
				 * }
				 */
				Dialog dialog = new AlertDialog.Builder(MemberInfoActivity.this)
						.setTitle("Iƴ��ʾ")
						.setMessage("�Ƿ񲦴����Ա�绰")
						.setPositiveButton("��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										try {
											Intent intent = new Intent();

											intent.setAction(Intent.ACTION_CALL);
											intent.setData(Uri.parse("tel:"
													+ telephone));
											startActivity(intent);
										} catch (Exception e) {

										}
									}
								})
						.setNegativeButton("��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).setCancelable(false).show();
				Window window = dialog.getWindow();
				window.setGravity(Gravity.CENTER);
			}
		});
	}

	public void btn_back(View v) { // ������ ���ذ�ť
		this.finish();
	}

	public void btn_deletemember(View v) { // ������ ���ذ�ť
		//mTask = new DeleteMemberTask();
		//mTask.execute((Void) null);
		Intent intent = new Intent();
		intent.setAction("com.send");
		intent.putExtra("type", "deleteMember");
		intent.putExtra("command", "DeleteMember " + GroupID + " " + username);
		MemberInfoActivity.this.sendBroadcast(intent);
		Toast.makeText(MemberInfoActivity.this, "��ɾ���ó�Ա",Toast.LENGTH_SHORT).show();
		Intent intent2 = new Intent(MemberInfoActivity.this,DiscussActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent2);
		MemberInfoActivity.this.finish();
	}

	public void head_xiaohei(View v) { // ͷ��ť
		Intent intent = new Intent();
		intent.setClass(MemberInfoActivity.this, MemberInfoHeadActivity.class);
		intent.putExtra("ImageFile", ID + "_" + HeadImageVersion + ".jpg");
		startActivity(intent);
	}

	public class DeleteMemberTask extends AsyncTask<Void, Void, Boolean> {
		private Socket socket = null;
		PrintWriter out = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			try {
				socket = new Socket(getString(R.string.Server_IP),
						Integer.parseInt(getString(R.string.Server_Port)));
				out = new PrintWriter(socket.getOutputStream(), true);
				//out.print("DeleteMember " + GroupID + " " + username);				
				//out.flush();
				socket.close();
				Intent intent = new Intent();
				intent.setAction("com.send");
				intent.putExtra("type", "deleteMember");
				intent.putExtra("command", "DeleteMember " + GroupID + " " + username);
				MemberInfoActivity.this.sendBroadcast(intent);
				return true;
			} catch (Exception e) {
				return false;
			}
			// TODO: register the new account here.

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mTask = null;
			if (success) {
				Toast.makeText(MemberInfoActivity.this, "��ɾ���ó�Ա",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MemberInfoActivity.this,
						DiscussActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				MemberInfoActivity.this.finish();
			} else {

			}

		}

		@Override
		protected void onCancelled() {
			mTask = null;
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
