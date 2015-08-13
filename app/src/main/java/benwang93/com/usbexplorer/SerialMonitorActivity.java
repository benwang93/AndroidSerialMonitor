package benwang93.com.usbexplorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
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
    private UsbDevice device;

    private UsbInterface intf;
    private UsbEndpoint endpointSend;
    private UsbEndpoint endpointReceive;
    private UsbDeviceConnection connection;

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

        intf = device.getInterface(1);
//        endpointReceive = intf.getEndpoint(1);
        endpointSend = intf.getEndpoint(0);
        connection = mUsbManager.openDevice(device);

        // UI elements
        textViewSM = (TextView) findViewById(R.id.textViewSerialMonitor);
        textViewSM.setMovementMethod(new ScrollingMovementMethod());
        editTextSM = (EditText) findViewById(R.id.editTextSerialMonitor);

//        editTextSM.append("\nDevice is null?: " + (device == null));

        // Send button
        findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send and display message
                sendSerial(editTextSM.getText().toString());
                displayMessage(textViewSM, "\n" + editTextSM.getText());
                editTextSM.setText("");
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
        Toast.makeText(getApplicationContext(), "Sending: " + new String(bytes), Toast.LENGTH_SHORT).show();

        // Send text over serial
        connection.claimInterface(intf, forceClaim);
        connection.bulkTransfer(endpointSend, bytes, bytes.length, TIMEOUT); //do in another thread
    }

    private void debugPrint(){
//        textViewSM.append("\ndebugPrint() says hello world!");
        displayMessage(textViewSM, "\nNum USB interfaces: " + device.getInterfaceCount());

        for (int intNum = 0; intNum < device.getInterfaceCount(); ++ intNum){
            UsbInterface intf = device.getInterface(intNum);

            displayMessage(textViewSM, "\n\nScanning interface " + intNum + " for endpoints");

            for (int endptNum = 0; endptNum < intf.getEndpointCount(); ++endptNum){
                UsbEndpoint endpt = intf.getEndpoint(endptNum);

                displayMessage(textViewSM, "\n  " + endpt.toString());
            }
        }
    }

    private void displayMessage(TextView textView, String message){
        // Append text
        textView.append(message);

        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height

        int scrollAmount;

        // Calculate scroll amount
        if (textView.getLineCount() == 0)
            scrollAmount = 0;
        else
            scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();

        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            textView.scrollTo(0, scrollAmount);
        else
            textView.scrollTo(0, 0);
    }

    private boolean mRunning = false;
    private class SerialReceiveTask extends AsyncTask<Void, String, Integer> {
        @Override
        protected Integer doInBackground(Void... endpt) {
            mRunning = true;

            //READ VALUE UNTIL DISCONNECT
            while (mRunning) {

                byte[] bytes = new byte[endpointReceive.getMaxPacketSize()];
                int result = connection.bulkTransfer(endpointReceive, bytes, bytes.length, 1000);
                if (result > 0) {
//                    Log.e("RESULT : " + result, "  VALUE : " + new String(bytes));
                    publishProgress(new String(bytes));

                }
            }

            return 0;
        }

        protected void onProgressUpdate(String message) {
            // Show message in textViewSM
            displayMessage(textViewSM, message);
        }

        protected void onPostExecute(Integer status) {
            // Show message in textViewSM
            displayMessage(textViewSM, "Error: Connection closed. Exit status: " + status);
        }
    }
}
