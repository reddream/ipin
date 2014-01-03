package lyc.iping;

import java.io.BufferedReader;

import lyc.iping.ChatMsgEntity;
import lyc.iping.MessageDB;
import lyc.iping.Group;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;

import com.readystatesoftware.viewbadger.BadgeView;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class service extends Service{
	
	public static PrintWriter clientoutput;
	public static InputStream clientBuff;
	private String str;
	public static Socket clientsocket = null;
	private static MyThread td;
	private String msg;
	private MessageDB messageDB;
	private static String ID;
	private static String username;
	
	private int flag = 0;
	public static String []Gid = new String[100];
	public static int group_msg_num = 0;
	static Handler handler=new Handler();
	public static int tdrun = 1;
	private String[] smsg;
	private boolean oldMsg=false;
	@Override
	public void onCreate() {
	// TODO Auto-generated method stub
		super.onCreate();
		Log.i("Service","Oncreate");
		
		DatabaseHelper dbHelper = new DatabaseHelper(service.this,"iPin");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("LoginUser", new String[]{"ID","username","password","sex","telephone","HeadImageVersion","autoLogin"}, "username<>?", new String[]{"null"}, null, null, null);
		while(cursor.moveToNext())
		{
			ID=cursor.getString(cursor.getColumnIndex("ID"));
			username=cursor.getString(cursor.getColumnIndex("username"));
		}
		db.close();
		dbHelper.close();
		messageDB = new MessageDB(ID,this);
		//����socket����		
		td = new MyThread();
		
		//ע��һ���㲥
		ClientActivityReceiver rcv = new ClientActivityReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.send");
		this.registerReceiver(rcv, filter);
		System.out.println("ע��㲥��ɣ�");
		
	}

	@Override
	public void onDestroy() {
	// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("Service","OnDestroy");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
	// TODO Auto-generated method stub
		if(!td.isAlive())
		{
			tdrun = 1;
			td.start();	
		}
				
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class ClientActivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			//unregisterReceiver(this);
			String type = intent.getStringExtra("type");
			if(type.equals("message"))
			{
				String client_LoginUser = intent.getStringExtra("LoginUser");
				String client_groupID = intent.getStringExtra("groupID");
				String client_date = intent.getStringExtra("date");
				String client_msg = intent.getStringExtra("Msg");
				System.out.println("service_recv_message��Ϣ�ɹ���");
				clientoutput.println("send_msg"+" "+client_groupID+"`"+client_LoginUser+"`"+client_date+"`"+client_msg+"`");
				clientoutput.flush();
				System.out.println("������Ϣ�ɹ���");
			}
			else
			{
				String cmd=intent.getStringExtra("command");
				System.out.println("service������Ϣ�ɹ���"+cmd);
				clientoutput.println(cmd);
				clientoutput.flush();
				System.out.println("service������Ϣ�ɹ���");
			}
			/*
			else if(type.equals("dischargeGroup"))
			{
				
			}
			else if(type.equals("exitGroup"))
			{
				
			}
			else if(type.equals("deleteMember"))
			{
				
			}*/
		}
	}
	
	public void connect()
	{
		try
		{
			clientsocket = new Socket(getString(R.string.Server_IP),Integer.parseInt(getString(R.string.Server_MessagePort)));
			//clientsocket = new Socket("121.248.50.90",Integer.parseInt(getString(R.string.Server_MessagePort)));
			clientoutput = new PrintWriter(clientsocket.getOutputStream(),true);
			
			clientBuff = clientsocket.getInputStream();
			flag = 1;
			System.out.println("����socket���");
			
			handler.postDelayed(runnable, 1);
		}
		catch (Exception e) 
		{
			System.out.println("Error"+e); 
		}
	}

	 
	 class MyThread extends Thread{ 

         public void run() {
        	 
        	 connect(); 
        	 
        	 
        	 byte[] buffer =new byte[1024];
        	 int readSize = 0;	
        	 clientoutput.println("ServiceConnect "+ID+" "+username);
 			 clientoutput.flush();	 
        	 while(tdrun==1)
        	 { 
        		 //System.out.println("Now client is listening...");
	        		 
	        		 
	        	 try {
	        		 readSize = clientBuff.read(buffer);
	     		} catch (IOException e) {
	     			// TODO Auto-generated catch block
	     			System.out.println("Error"+e);
	     		}
	       	 
	        	if(tdrun == 0)
	        		break;
	        	 
	        	 
	     		if( readSize > 0)
	             {
	    			msg=new String(buffer,0,readSize);
	    			System.out.println("receive msg:"+msg);
	     			smsg = msg.split("`");     			
	     			
	     			//�����ݼ������ݿ�   
	    //test 			String[] smsg = new String[]{"getmsg","000000003", "hx", "2010-03-08", "what is this"};
	     			
	     			     			
	     			ChatMsgEntity entity = new ChatMsgEntity();
					
	     			if(smsg[0].equals("get_msg"))
	     			{     				
	     				entity.setName(smsg[2]);
	     				entity.setDate(smsg[3]);
	     				entity.setMessage(smsg[4]);     				
	     				entity.setMsgType(true);
	     				messageDB.saveMsg(Integer.parseInt(smsg[1]), entity);	         			
	         			
	         			//�洢δ������
	         			int yes=0;
	         			for(int i=0;i<group_msg_num;i++)
	         			{
	         				if(smsg[1].equals(Gid[i]))
	         				{
	         					yes = 1;
	         					break;
	         				}
	         			}
	         			//add by hx 1024
	         			ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	         			ComponentName cn = activityManager.getRunningTasks(2).get(0).topActivity;
	         			
	         			System.out.println("class name now:"+cn.getClassName());
	         			int light = 1;
	         			if(cn.getClassName().equals("lyc.iping.ChatActivity"))
	         			{
	         				if(DiscussActivity.GroupIDnow.equals(smsg[1]))
	         				{
	         					light = 0;
	         				}
	         			}
	         			if((yes == 0)&&(light==1))
	         			{
	         				Gid[group_msg_num] = smsg[1];
	        				group_msg_num++;	        				
	         			}
	         			
	         			//end
	         			
	         			//���͹㲥
	         			Intent intent = new Intent();
	         			intent.setAction("com.rcv");
	         			intent.putExtra("type","message");
	         			intent.putExtra("groupID",smsg[1]);
	         			intent.putExtra("name",smsg[2]);
	         			intent.putExtra("date", smsg[3]);
	         			intent.putExtra("msg", smsg[4]);		
	         			
	         			service.this.sendBroadcast(intent);
	         			//add by hx
	         			//��
	         			if(smsg[5].equals("liveMsg"))
	         			{
	         				Vibrator mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);//��ȡ����
		         			mVibrator.vibrate(500);//����������
		         			//������� 2013-11-18
		         	//		MediaPlayer mp = new MediaPlayer();                    
		         			try
		         			{
		         	//			mp.setDataSource(service.this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION ));
		         				MediaPlayer mp = MediaPlayer.create(service.this, R.raw.msg);
		         				System.out.println("Music");
		         			//	mp.reset();
		         				mp.start();
		         				     				
		         			}
		         			catch(Exception e)
		         			{
		         				e.printStackTrace();
		         			}
	         			}
	         			else if(smsg[5].equals("oldMsg") && !oldMsg)
	         			{
	         				Vibrator mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);//��ȡ����
		         			mVibrator.vibrate(500);//����������
		         			//������� 2013-11-18
		         	//		MediaPlayer mp = new MediaPlayer();                    
		         			try
		         			{
		         	//			mp.setDataSource(service.this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION ));
		         				MediaPlayer mp = MediaPlayer.create(service.this, R.raw.msg);
		         				System.out.println("Music");
		         			//	mp.reset();
		         				mp.start();
		         				     				
		         			}
		         			catch(Exception e)
		         			{
		         				e.printStackTrace();
		         			}
		         			oldMsg=true;
	         			}
	        			//end
	        			System.out.println("num:"+service.group_msg_num+" "+"hello:"+service.Gid[0]);
	         			//end
	     			}
	     			else if(smsg[0].equals("deleteMember"))
	     			{
	     				
	     				if(smsg[2].equals(username))
	     				{
	     					handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									// Toast.makeText(getApplicationContext(),
									// "���ѱ�����Ա���������",
									// Toast.LENGTH_SHORT).show();
									Builder builder = new AlertDialog.Builder(
											getApplicationContext());
									builder.setTitle("iƴ��ʾ");
									builder.setMessage("���ѱ�����Ա����������"+smsg[3]+"�� "+smsg[4]);
									builder.setPositiveButton("ȷ��",
											new OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

												}
											});
									final AlertDialog dialog = builder.create();
									// ��dialog
									// show����֮ǰ������´��룬��ʾ��dialog��һ��ϵͳ��dialog**
									dialog.getWindow()
											.setType(
													(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
									Window window = dialog.getWindow();
									window.setGravity(Gravity.CENTER);
									new Thread() {
										public void run() {
											handler.post(new Runnable() {
												@Override
												public void run() {
													dialog.show();
													
												}
											});
										};
									}.start();
								}

							});	      					
	     					ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		         			ComponentName cn = activityManager.getRunningTasks(2).get(0).topActivity;
		         			String activityName = cn.getClassName();
		         			System.out.println(activityName);
		         			if(activityName.equals("lyc.iping.DiscussActivity") || activityName.equals("lyc.iping.ChatActivity") || 
		         				activityName.equals("lyc.iping.GroupInfoActivity") || activityName.equals("lyc.iping.MemberInfoActivity") ||
		         				activityName.equals("lyc.iping.MemberInfoHeadActivity"))
		         			{
		         				DatabaseHelper dbHelper = new DatabaseHelper(service.this, "iPin");
		        				SQLiteDatabase db = dbHelper.getWritableDatabase();
		        				Cursor cursor = db.query("GroupMember", new String[] { "GroupID" }, null, null, null, null, null);
		        				cursor.moveToNext();
		        				String GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
		        				if(GroupID.equals(smsg[1]))
		        				{
		        					Intent intent = new Intent();  
			         				intent.setClassName("lyc.iping","lyc.iping.DiscussActivity");  
			         				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  
			         				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			         				service.this.startActivity(intent);
		        				}		         				 
		         			}
	     					
	     				}
	     				else
	     				{
	        				DatabaseHelper dbHelper = new DatabaseHelper(service.this, "iPin");
	    					SQLiteDatabase db = dbHelper.getWritableDatabase();
	    					db.delete("GroupMember", "GroupID=? and username=?",new String[]{smsg[1],smsg[2]});
	    					db.close();
	    					dbHelper.close();
	    					handler.post(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(getApplicationContext(), "��Ա"+smsg[2]+"�����������", Toast.LENGTH_SHORT).show();
								}
	    						
	    					});
	    					Intent intent = new Intent();
		         			intent.setAction("com.rcv");
		         			intent.putExtra("type","minusCount");
		         			intent.putExtra("GroupID",smsg[1]);
		         			intent.putExtra("name",smsg[2]);
		         			service.this.sendBroadcast(intent);
	     				}        				
	     			}
	     			else if(smsg[0].equals("dischargeGroup"))
	     			{
	     				handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								// Toast.makeText(getApplicationContext(),
								// "���ѱ�����Ա���������",
								// Toast.LENGTH_SHORT).show();
								Builder builder = new AlertDialog.Builder(
										getApplicationContext());
								builder.setTitle("iƴ��ʾ");
								builder.setMessage("�������"+smsg[2]+"�� "+smsg[3]+"�ѱ�����Ա��ɢ");
								builder.setPositiveButton("ȷ��",
										new OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

											}
										});
								final AlertDialog dialog = builder.create();
								// ��dialog
								// show����֮ǰ������´��룬��ʾ��dialog��һ��ϵͳ��dialog**
								dialog.getWindow()
										.setType(
												(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
								Window window = dialog.getWindow();
								window.setGravity(Gravity.CENTER);
								new Thread() {
									public void run() {
										handler.post(new Runnable() {
											@Override
											public void run() {
												dialog.show();
												
											}
										});
									};
								}.start();
							}

						});	     					
     					ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	         			ComponentName cn = activityManager.getRunningTasks(2).get(0).topActivity;
	         			String activityName = cn.getClassName();
	         			System.out.println(activityName);
	         			if(activityName.equals("lyc.iping.DiscussActivity") || activityName.equals("lyc.iping.ChatActivity") || 
	         				activityName.equals("lyc.iping.GroupInfoActivity") || activityName.equals("lyc.iping.MemberInfoActivity") ||
	         				activityName.equals("lyc.iping.MemberInfoHeadActivity"))
	         			{
	         				DatabaseHelper dbHelper = new DatabaseHelper(service.this, "iPin");
	        				SQLiteDatabase db = dbHelper.getWritableDatabase();
	        				Cursor cursor = db.query("GroupMember", new String[] { "GroupID" }, null, null, null, null, null);
	        				cursor.moveToNext();
	        				String GroupID = cursor.getString(cursor.getColumnIndex("GroupID"));
	        				if(GroupID.equals(smsg[1]))
	        				{
	        					Intent intent = new Intent();  
		         				intent.setClassName("lyc.iping","lyc.iping.DiscussActivity");  
		         				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  
		         				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		         				service.this.startActivity(intent);
	        				}
	         			}
	     				     				
	     			}
	     			else if(smsg[0].equals("exitGroup"))
	     			{
	     				DatabaseHelper dbHelper = new DatabaseHelper(service.this, "iPin");
    					SQLiteDatabase db = dbHelper.getWritableDatabase();
    					db.delete("GroupMember", "GroupID=? and username=?",new String[]{smsg[1],smsg[2]});
    					db.close();
    					dbHelper.close();
    					handler.post(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplicationContext(), "��Ա"+smsg[2]+"�˳�������", Toast.LENGTH_SHORT).show();
							}
    						
    					});
    					Intent intent = new Intent();
	         			intent.setAction("com.rcv");
	         			intent.putExtra("type","minusCount");
	         			intent.putExtra("GroupID",smsg[1]);
	         			intent.putExtra("name",smsg[2]);
	         			service.this.sendBroadcast(intent);
	     			}
	     			else if(smsg[0].equals("joinGroup"))
	     			{
//	     				DatabaseHelper dbHelper = new DatabaseHelper(service.this, "iPin");
//    					SQLiteDatabase db = dbHelper.getWritableDatabase();
//    					db.delete("GroupMember", "GroupID=? and username=?",new String[]{smsg[1],smsg[2]});
//    					db.close();
//    					dbHelper.close();
    					handler.post(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplicationContext(), "��Ա"+smsg[2]+"����������", Toast.LENGTH_SHORT).show();
							}
    						
    					});
    					Intent intent = new Intent();
	         			intent.setAction("com.rcv");
	         			intent.putExtra("type","plusCount");
	         			intent.putExtra("GroupID",smsg[1]);
	         			intent.putExtra("name",smsg[2]);
	         			service.this.sendBroadcast(intent);
	     			}
	     			
					
	             }
        	 }
        	 
        }
            
   } 
	
	 
	//add by hx
		 //��ʱ������
		 static Runnable runnable = new Runnable()
		 {
			 @Override 
			 public void run()
			 {
				 
				clientoutput.println("alive "+ID);
				clientoutput.flush();
				
		//		System.out.println("This is alive!!!");
				handler.postDelayed(this,2000);
			 }
		 };

}



