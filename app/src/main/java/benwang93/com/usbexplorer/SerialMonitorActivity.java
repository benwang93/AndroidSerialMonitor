package benwang93.com.usbexplorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SerialMonitorActivity extends AppCompatActivity {
    // USB device/manager
    private UsbManager mUsbManager;
    UsbDevice device;

    // Variables for sending via USB
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;

    // UI Elements
    private TextView textViewSM;
    private EditText editTextSM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_monitor);

        // Get device from USB Details Activity
        Intent intent = getIntent();
        device = intent.getParcelableExtra(MainActivity.EXTRA_USB_DEVICE);

        // Get USB manager for send/receive
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // UI elements
        textViewSM = (TextView) findViewById(R.id.textViewSerialMonitor);
        textViewSM.setMovementMethod(new ScrollingMovementMethod());
        editTextSM = (EditText) findViewById(R.id.editTextSerialMonitor);

//        editTextSM.append("\nDevice is null?: " + (device == null));

        // Send button
        findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendSerial(editTextSM.getText().toString());
                textViewSM.append("\n"+editTextSM.getText());
                editTextSM.setText("");

                // find the amount we need to scroll.  This works by
                // asking the TextView's internal layout for the position
                // of the final line and then subtracting the TextView's height
                final int scrollAmount = textViewSM.getLayout().getLineTop(textViewSM.getLineCount()) - textViewSM.getHeight();
                // if there is no need to scroll, scrollAmount will be <=0
                if (scrollAmount > 0)
                    textViewSM.scrollTo(0, scrollAmount);
                else
                    textViewSM.scrollTo(0, 0);
            }
        });

        // Close button
        findViewById(R.id.buttonCloseSerialMonitor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        debugPrint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_serial_monitor, menu);
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

    // Broadcast receiver for detecting device removal
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    // call your method that cleans up and closes communication with the device
                    Toast.makeText(getApplicationContext(), "USB device disconnected!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    // Send serial message
    void sendSerial(String message){
        // Variables
        byte[] bytes;

        // Extract text
        bytes = message.getBytes();

        // DEBUG: Show message being sent
        Toast.makeText(getApplicationContext(), "Sending: " + bytes, Toast.LENGTH_SHORT).show();

        // Send text over serial
        UsbInterface intf = device.getInterface(1);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        connection.claimInterface(intf, forceClaim);
        connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT); //do in another thread
    }

    private void debugPrint(){
//        textViewSM.append("\ndebugPrint() says hello world!");
        textViewSM.append("\nNum USB interfaces: " + device.getInterfaceCount());

        for (int intNum = 0; intNum < device.getInterfaceCount(); ++ intNum){
            UsbInterface intf = device.getInterface(intNum);

            textViewSM.append("\nScanning interface " + intNum + " for endpoints");

            for (int endptNum = 0; endptNum < intf.getEndpointCount(); ++endptNum){
                UsbEndpoint endpt = intf.getEndpoint(endptNum);

                textViewSM.append("\n\t"+endpt.toString());
            }
        }
    }
}
