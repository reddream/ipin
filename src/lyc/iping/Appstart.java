package lyc.iping;



import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import lyc.iping.InfoListActivity.InfoRefreshTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class Appstart extends Activity{

	private VersionCheckTask mVersionCheckTask = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.appstart);
		String AppPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		System.out.print("AppPath:" + AppPath);
		File file = new File(AppPath + "HeadImage");
		if(!file.exists()) file.mkdir();
		
		mVersionCheckTask = new VersionCheckTask();
		mVersionCheckTask.execute((Void) null); 
    
		
		Handler handler = new Handler();
		
		handler.postDelayed(new Runnable(){
		@Override
		public void run(){
			Intent intent = new Intent (Appstart.this,LoginActivity.class);			
			startActivity(intent);			
			Appstart.this.finish();
		}
	}, 2000);
   }
	
	public class VersionCheckTask extends AsyncTask<Void, Void, Boolean> {
		private Socket socket = null;		
        PrintWriter out =  null;
        
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			try{
				socket = new Socket(getString(R.string.Server_IP),Integer.parseInt(getString(R.string.Server_Port)));
				out = new PrintWriter(socket.getOutputStream(),true);
				out.print("VersionCheck");
				out.flush();
				InputStream br = socket.getInputStream();
				byte[] buffer =new byte[2048];
				int readSize=br.read(buffer);
				if(readSize>0)
				{
					String VersionCheckMsg=new String(buffer,0,readSize);
					String[] temp = VersionCheckMsg.split(" ");
					String Version=temp[1];	
					/*
					DatabaseHelper dbHelper = new DatabaseHelper(Appstart.this,"iPin");
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					ContentValues value=new ContentValues();
					value.put("Current", Version);
					db.update("Version",value,"Local==?",new String[]{getString(R.string.Version)});
					
					db.close();
					*/
					socket.close();					
					if(!Version.equals(getString(R.string.Version))) return true;
					else return false;
				}
			}catch(Exception e)
			{
				return false;
			}
			return false;
			// TODO: register the new account here.
			//return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mVersionCheckTask = null;			
			if (success) {
				Toast.makeText(Appstart.this, "软件版本过时，请尽快更新", Toast.LENGTH_SHORT).show();
			} else {
				
			}			
			
		}

		@Override
		protected void onCancelled() {
			mVersionCheckTask = null;
		}		
	}
}