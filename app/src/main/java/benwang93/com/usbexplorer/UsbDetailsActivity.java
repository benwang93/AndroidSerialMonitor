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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UsbDetailsActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION =
            "com.benwang93.USB_PERMISSION";
    public static final String TAG = "USB_Details";

    private UsbDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_details);

        // Show details of this device
        showDetails();

        // Set onclick listener
        Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        Button buttonClose = (Button) findViewById(R.id.buttonClose);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);;

                // Request permission to access device
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);

                // Ask for permission
                mUsbManager.requestPermission(device, mPermissionIntent);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            //scanUSB();
                            Toast.makeText(getApplicationContext(), "USB Connection Successful!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                        Toast.makeText(getApplicationContext(), "USB permission denied :(", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_usb_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDetails(){
        Intent intent = getIntent();

        device = intent.getParcelableExtra(MainActivity.EXTRA_USB_DEVICE);

        TextView textViewDeviceId = (TextView) findViewById(R.id.textViewDeviceId);

        // DEBUG: Detect dummy test
        if (device == (null)){
            textViewDeviceId.setText("DEBUG MODE");

            return;
        }

        Log.d(TAG, "device id: " + device.getDeviceId());
//        Toast.makeText(getApplicationContext(), "device id: "+device.getDeviceId(), Toast.LENGTH_SHORT).show();
        textViewDeviceId.setText(Integer.toString(device.getDeviceId()));

        // Display Device Name
        TextView textViewDeviceName = (TextView) findViewById(R.id.textViewDeviceName);
        textViewDeviceName.setText(device.getDeviceName());

        // Display Device Protocol
        TextView textViewDeviceProtocol = (TextView) findViewById(R.id.textViewDeviceProtocol);
        textViewDeviceProtocol.setText(Integer.toString(device.getDeviceProtocol()));

        // Display Device Subclass
        TextView textViewDeviceSubclass = (TextView) findViewById(R.id.textViewDeviceSubclass);
        textViewDeviceSubclass.setText(Integer.toString(device.getDeviceSubclass()));

        // Display Manufacturer Name
//        TextView textViewManufacturerName = (TextView) findViewById(R.id.textViewManufacturerName);
//        textViewManufacturerName.setText(device.getManufacturerName());

        // Display Product ID
        TextView textViewProductId = (TextView) findViewById(R.id.textViewProductId);
        textViewProductId.setText(Integer.toString(device.getProductId()));

        // Display Product Name
//        TextView textViewProductName = (TextView) findViewById(R.id.textViewProductName);
//        textViewProductName.setText(device.getProductName());

        // Display Serial Number
//        TextView textViewSerialNumber = (TextView) findViewById(R.id.textViewSerialNumber);
//        textViewSerialNumber.setText(device.getSerialNumber());

        // Display Vendor ID
        TextView textViewVendorId = (TextView) findViewById(R.id.textViewVendorId);
        textViewVendorId.setText(Integer.toString(device.getVendorId()));
    }
}
