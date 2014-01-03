package lyc.iping;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private TextView version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutipin);

		// �˳�ʱ�������Activity
		ActivityManager.getInstance().addActivity(this);

		version = (TextView) findViewById(R.id.current_version);
		version.setText(getString(R.string.Version));
	}

	public void btn_back(View v) { // ������ ���ذ�ť
		this.finish();
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
