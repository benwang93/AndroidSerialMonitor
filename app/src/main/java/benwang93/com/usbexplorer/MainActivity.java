package benwang93.com.usbexplorer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_USB_DEVICE = "com.benwang93.usbexplorer.usbDevice";

    public static final String TAG = "USB_Explorer";


//    private UsbDevice device;;
    // USB variables
    private Vector<String> usbNames = new Vector<String>();
    private UsbManager mUsbManager;
    private HashMap<String, UsbDevice> deviceList;
    private ArrayAdapter<String> usbListAdapter;

    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // Do something in response to the click
            Toast.makeText(getApplicationContext(), "Item clicked: " + position, Toast.LENGTH_SHORT).show();

            // Start activity with device details
            Intent intent = new Intent(getApplicationContext(), UsbDetailsActivity.class);

            // Extract device
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            for (int i = 0; i < position; ++i){
                deviceIterator.next();
            }

            // DEBUG: Detect dummy device
            if (!deviceIterator.hasNext()){
                intent.putExtra(EXTRA_USB_DEVICE, (UsbDevice) null);
            } else {
                // Pass on selected device
                intent.putExtra(EXTRA_USB_DEVICE, deviceIterator.next());
            }

            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Configure USB
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Enumerate connected devices
        refreshUSB();

        // Set ListView adapter
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(usbListAdapter);


        // Set up item click listener
        listView.setOnItemClickListener(mMessageClickedHandler);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.refresh_USB_list:
                refreshUSB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
//        if (id == R.id.action_settings) {
//            return true;
//        }
    }




    private void refreshUSB(){
        deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        usbNames.clear();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();

            // Add name to array
            usbNames.add(device.getDeviceName());
            Log.d(TAG, "Found device: " + usbNames.lastElement());
        }

        // DEBUG: add dummy device
        usbNames.add("dummy");

        // No devices found
        if (usbNames.size() == 0){
            Toast.makeText(this, "No USB devices found.", Toast.LENGTH_SHORT).show();
            if (usbListAdapter != null)
                usbListAdapter.clear();
        } else {
            // Create array adapter for USB devices
            usbListAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, usbNames);


        }
    }

    private void scanUSB(){
        Toast.makeText(getApplicationContext(), "Scanning USB!", Toast.LENGTH_SHORT).show();

        return;
    }
}

