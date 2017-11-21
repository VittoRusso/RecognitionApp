package com.donaydc.activitiesrecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import static java.lang.Boolean.TRUE;

public class Main2Activity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    TextView sensorView0, sensorView1, sensorView2, Activity;
    private String MegaData;
    private String MegaX = "[";
    private String MegaY = "[";
    private String MegaZ = "[";
    private String mDeviceName = "Savitar";
    private String mDeviceAddress = "F8:76:6C:D1:B2:1C";
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String stringData;

    LineChart lineChart;
    Button BtnActivity, BtRecor, BtCancel;
    ListView LAct;

    ArrayList<Entry> AXESx = new ArrayList<>();
    ArrayList<Entry> AXESy = new ArrayList<>();
    ArrayList<Entry> AXESz = new ArrayList<>();

    ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

    private float numsensor0;
    private float numsensor1;
    private float numsensor2;
    private int Time = 11;
    private int itemPosition = 1;

    private static final int REQUEST_ENABLE_BT = 1;

    private StringBuilder recDataString = new StringBuilder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        BtnActivity = findViewById(R.id.BtAct);
        BtRecor = findViewById(R.id.Record);
        BtCancel = findViewById(R.id.Cancel);
        sensorView0 = findViewById(R.id.textX);
        sensorView1 = findViewById(R.id.textY);
        sensorView2 = findViewById(R.id.textZ);
        LAct = findViewById(R.id.ActList);
        Activity = findViewById(R.id.ActivityLabel);
        lineChart = findViewById(R.id.lineChart);

        InitialGraph();

        final AlertDialog.Builder AlertAct = new AlertDialog.Builder(Main2Activity.this);
        AlertAct.setMessage("Please enter a Activity");
        AlertAct.setCancelable(true);
        AlertAct.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }});

        final String UserMail = getIntent().getStringExtra("Email");

        String[] values = new String[] {
                "Stand Still",
                "Walking",
                "Jogging",
                "Going Up Stairs",
                "Going Down Stairs",
                "Jumping"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        LAct.setAdapter(adapter);

        LAct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                itemPosition = position;
                String  itemValue = (String) LAct.getItemAtPosition(position);
                BtnActivity.setText(itemValue);
                LAct.setVisibility(View.GONE);
            }
        });

        BtnActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LAct.getVisibility() == View.VISIBLE) {
                    LAct.setVisibility(View.GONE);
                } else if (LAct.getVisibility() == View.GONE){
                    LAct.setVisibility(View.VISIBLE);
                }
            }
        });

        BtRecor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BtRecor.getText().equals("Record")) {
                    BtRecor.setText("Stop");
                    MegaX = "[";
                    MegaY = "[";
                    MegaZ = "[";
                } else if (BtRecor.getText().equals("Stop")){
                    mBluetoothLeService.disconnect();
                    BtCancel.setVisibility(View.VISIBLE);
                    BtRecor.setText("Send");
                } else if (BtRecor.getText().equals("Send")) {
                    if (BtnActivity.getText().equals("Activities")) {
                        AlertAct.show();
                    } else {
                        BtCancel.setVisibility(View.GONE);
                        BtRecor.setText("Record");
                        MegaData="http://track-mymovement.tk/save_har_training.php?value_x=";
                        MegaData=MegaData.concat(MegaX).concat("1]&value_y=").concat(MegaY).concat("1]&value_z=").concat(MegaZ).concat("1]&idPersonal=%22").concat(UserMail).concat("%22&label=%22").concat(String.valueOf(itemPosition+1)).concat("%22");
                        new LoadData().execute(MegaData);
                        mBluetoothLeService.connect(mDeviceAddress);
                    }
                }
            }
        });

        BtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtCancel.setVisibility(View.GONE);
                BtRecor.setText("Record");
                MegaX = "[";
                MegaY = "[";
                MegaZ = "[";
                mBluetoothLeService.connect(mDeviceAddress);
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        mGattServicesList = findViewById(R.id.gatt_services_list);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                BtRecor.setEnabled(TRUE);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                stringData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                recDataString.append(stringData);
                int endOfLineIndex = recDataString.indexOf("~");
                if (endOfLineIndex > 0) {
                    String dataInPrint = recDataString.substring(0, endOfLineIndex);
                    if (recDataString.charAt(0) == '#'){

                        StringTokenizer tokens = new StringTokenizer(dataInPrint, "+");
                        String sensor0 = tokens.nextToken().replace("#","");
                        String sensor1 = tokens.nextToken();
                        String sensor2 = tokens.nextToken();

                        numsensor0 = (Float.valueOf(sensor0)-518)/100;  // Pin X
                        numsensor1 = (Float.valueOf(sensor1)-525)/100;  // Pin Y
                        numsensor2 = (Float.valueOf(sensor2)-532)/100+1;  // Pin Z

                        sensorView0.setText(" X Axis: Acceleration = " + String.format("%.2f", numsensor0) + "G");
                        sensorView1.setText(" Y Axis: Acceleration = " + String.format("%.2f", numsensor1) + "G");
                        sensorView2.setText(" Z Axis: Acceleration = " + String.format("%.2f", numsensor2) + "G");

                        Time+=1;
                        AXESx.add(new Entry(Time,numsensor0));
                        AXESy.add(new Entry(Time,numsensor1));
                        AXESz.add(new Entry(Time,numsensor2));

                        if (BtRecor.getText().equals("Stop")){
                            MegaX=MegaX.concat(String.valueOf(numsensor0)).concat(",");
                            MegaY=MegaY.concat(String.valueOf(numsensor1)).concat(",");
                            MegaZ=MegaZ.concat(String.valueOf(numsensor2)).concat(",");
                        }

                        if(Time==12){
                            mBluetoothLeService.disconnect();
                            mBluetoothLeService.connect(mDeviceAddress);
                        }

                    }
                    recDataString.delete(0, recDataString.length());

                }
                if(Time%5==0){
                    Plot();
                }
            }
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    public void InitialGraph(){

        for (int i=0;i<=11;i++){
            AXESx.add(new Entry(i,0));
            AXESy.add(new Entry(i,1));
            AXESz.add(new Entry(i,-1));
        }

        LineDataSet lineDataSet1 = new LineDataSet(AXESx,"X Axis");
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setColor(Color.BLUE);

        LineDataSet lineDataSet2 = new LineDataSet(AXESy,"Y Axis");
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setColor(Color.RED);

        LineDataSet lineDataSet3 = new LineDataSet(AXESz,"Z Axis");
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setColor(Color.GREEN);

        lineDataSets.add(lineDataSet1);
        lineDataSets.add(lineDataSet2);
        lineDataSets.add(lineDataSet3);

        lineChart.setData(new LineData(lineDataSets));
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }

    public void Plot(){
        LineDataSet lineDataSet1 = new LineDataSet(AXESx,null);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSet1.removeFirst();

        LineDataSet lineDataSet2 = new LineDataSet(AXESy,null);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setColor(Color.RED);
        lineDataSet2.removeFirst();

        LineDataSet lineDataSet3 = new LineDataSet(AXESz,null);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setColor(Color.GREEN);
        lineDataSet3.removeFirst();

        lineDataSets.add(lineDataSet1);
        lineDataSets.add(lineDataSet2);
        lineDataSets.add(lineDataSet3);

        lineChart.setData(new LineData(lineDataSets));
        lineChart.setVisibleXRangeMaximum(10);
        lineChart.moveViewToX(Time);
    }

    void getData(){
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(0).get(0);
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                Intent IntentSplash = new Intent(this, SplashScreen.class);
                startActivity(IntentSplash);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if(SampleGattAttributes.lookup(uuid, unknownServiceString)!=unknownServiceString){
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                currentServiceData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put(
                            LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put(LIST_UUID, uuid);
                    if(SampleGattAttributes.lookup(uuid, unknownCharaString)!=unknownCharaString){
                        gattCharacteristicGroupData.add(currentCharaData);}
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }

            SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                    this,
                    gattServiceData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[] {LIST_NAME, LIST_UUID},
                    new int[] { android.R.id.text1, android.R.id.text2 },
                    gattCharacteristicData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[] {LIST_NAME, LIST_UUID},
                    new int[] { android.R.id.text1, android.R.id.text2 }
            );
            mGattServicesList.setAdapter(gattServiceAdapter);}
        getData();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private class LoadData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Send Susessfull", Toast.LENGTH_LONG).show();
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();
            String contentAsString = readIt(is, len);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}

