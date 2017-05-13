package nl.hu.zrb.btserver;

import nl.hu.zrb.btserver.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HitCountServiceMonitor extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
	
	private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private SharedPreferences countstorage;
    private TextView tv ;
    private static final int REQUEST_ENABLE_BT = 2947;

    private void showAantal(){
        int aantal = getSharedPreferences("COUNTSTORAGE", 0).getInt("COUNT", 0);
        tv.setText("" + aantal);
    }

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        setContentView(R.layout.servicemonitor);
        tv =   (TextView) findViewById(R.id.textView1);
        countstorage = getSharedPreferences("COUNTSTORAGE", 0);
        countstorage.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.id_start:
                startService();
                return true;
            case R.id.id_stop:
                stopService(new Intent(HitCountServiceMonitor.this, HitCountService.class));
                return true;
            case R.id.id_reset:
                countstorage.edit().putInt("COUNT", 0).apply();
                showAantal();
                return true;
        }
        return false;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        showAantal();
    }


    
    private int REQUEST_DISCOVERABLE = 234;
    
    public void startService(){
    	Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
    	startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);  	
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode == REQUEST_DISCOVERABLE && resultCode != RESULT_CANCELED){
    		startService(new Intent(this, HitCountService.class));
    	}
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showAantal();

    }
}
