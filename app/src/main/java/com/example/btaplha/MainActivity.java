package com.example.btaplha;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.DragStartHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Switch switch1;
    TextView textView3;
    BluetoothAdapter bluetoothAdapter;
    public static final int BLUETOOTH_REQ_CODE=1;
    public static final int REQUEST_CODE=123;

    Button button;
    ListView pairlist;
    ListView discoverlist;
    ArrayList arrayList;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> arrayAdapter1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter1);

        switch1 = findViewById(R.id.switch1);
        pairlist=findViewById(R.id.pairlist);
        discoverlist=findViewById(R.id.discoverlist);
        arrayList=new ArrayList();

        textView3=findViewById(R.id.textView3);
        //progressBar=findViewById(R.id.progressBar);

        arrayAdapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        discoverlist.setAdapter(arrayAdapter1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            switch1.setChecked(false);
            pairlist.setVisibility(View.INVISIBLE);
        }
        else if (bluetoothAdapter.isEnabled()) {
            switch1.setChecked(true);
            findPairedDevices();
            pairlist.setVisibility(View.VISIBLE);
        }

        switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(switch1.isChecked()) {
                    Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(bluetoothIntent, BLUETOOTH_REQ_CODE);
                }
                else if (!switch1.isChecked()) {
                    bluetoothAdapter.disable();
                    pairlist.setVisibility(View.INVISIBLE);
                }
            }
        });

        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrayList.clear();
                discoverDevices();
            }
        });
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                    switch1.setChecked(false);
                        pairlist.setVisibility(View.INVISIBLE);
                    break;
                    case BluetoothAdapter.STATE_ON:
                    switch1.setChecked(true);
                    findPairedDevices();
                        pairlist.setVisibility(View.VISIBLE);
                    break;

                }

            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Toast.makeText(MainActivity.this, "Bluetooth is ON", Toast.LENGTH_SHORT).show();
            findPairedDevices();
            pairlist.setVisibility(View.VISIBLE);
        }
        else{
            if(resultCode==RESULT_CANCELED){
                Toast.makeText(MainActivity.this, "Bluetooth operation is cancelled", Toast.LENGTH_SHORT).show();
                switch1.setChecked(false);
                pairlist.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver2, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {
            switch1.setChecked(false);
            pairlist.setVisibility(View.INVISIBLE);
        }
        else if (bluetoothAdapter.isEnabled()) {
            switch1.setChecked(true);
            findPairedDevices();
            pairlist.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver1);
    }

    //Paired Devices
    private void findPairedDevices ()
    {
        int index = 0;
        Set<BluetoothDevice> bluetoothSet = bluetoothAdapter.getBondedDevices();
        String[] str = new String [bluetoothSet.size()];

        if(bluetoothSet.size()>0)
        {
            for(BluetoothDevice device : bluetoothSet)
            {
                str[index] = device.getName();
                index++;
            }

            arrayAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,str);
            pairlist.setAdapter(arrayAdapter);
        }
    }

    //Scan for Devices
    public void discoverDevices()
    {

        bluetoothAdapter.startDiscovery();


    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayList.add(device.getName());
                arrayAdapter1.notifyDataSetChanged();
            }
        }
    };


}