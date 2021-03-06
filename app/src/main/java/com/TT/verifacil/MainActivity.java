package com.TT.verifacil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    // Tipos de mensajes enviados por el BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    BluetoothAdapter mBluetoothAdapter;
    private boolean DeviceSelected;
    private BluetoothService mBTService;
    private boolean mBound;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private Button mFindDevice;
    private TextView mLoadingDisplay;
    private TextView mReadyReadDisplay;
    private Button mVerification;
    private TextView mWelcomeDisplay;
    private Button mRequestPermButton;
    private TextView mRequestPermDisplay;
    private Button mRequestBTOnButton;
    private TextView mRequestBTOnDisplay;
    private ProgressBar mConectingPB;

    private String mConnectedDeviceName = null;
    private String addressDeviceSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        // Inicializamos el adaptador  de Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Se verifica si el dispositivo soporta conexones por Bluetooth
        if(BluetoothAdapter.getDefaultAdapter() == null){
            Toast.makeText(this, "Tu dispositivo no soporta conexiones bluetooth",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        DeviceSelected = false;

        mFindDevice = (Button) findViewById(R.id.connectButton);
        mLoadingDisplay = (TextView)findViewById(R.id.loadingDisplay);
        mReadyReadDisplay = (TextView)findViewById(R.id.readyDisplay);
        mWelcomeDisplay = (TextView)findViewById(R.id.welcomeDisplay);
        mVerification = (Button) findViewById(R.id.verificationButton);
        mRequestPermButton = (Button) findViewById(R.id.requestPermButton);
        mRequestPermDisplay = (TextView) findViewById(R.id.requestPermDisplay);
        mRequestBTOnButton = (Button) findViewById(R.id.requestBTOnButton);
        mRequestBTOnDisplay = (TextView) findViewById(R.id.requestBTOnDisplay);
        mConectingPB = (ProgressBar) findViewById(R.id.conectingPB);

        mFindDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDevice();
            }
        });
        mVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verification();
            }
        });
        mRequestPermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerm();
            }
        });

        mRequestBTOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBluetoothEnabled();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO(0): Hacer que cuando se regrese a este activity se verifique si hay un bind y el estado de la conexi??n

        setViewConnectReadyState();
        // Verficamos que se tengan los permisos necesarios en el dispositivo
        requestPerm();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothService.CONNECTION_STATUS);
        registerReceiver(mReceiver, filter);
    }

    private void requestPerm() {
        // Verficamos que se tengan los permisos necesarios en el dispositivo
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // Se verifica si el bluetooth est?? activo
            setViewConnectReadyState();
            checkBluetoothEnabled();
        }else {
            // Si no se cuenta con los permisos es necesario que se solicite al usuario los permisos necesarios
            // Se crea un request para solicitar los permisos
            ActivityCompat.requestPermissions(MainActivity.this
                    ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,200);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, BluetoothService.class);
        stopService(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast mensaje;
        switch (requestCode) {
            // Se verifica la respuesta obtenida, para evitar errores
            case 200:
                // Se verifica si los permisos fueron otorgados
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Se verifica si el bluetooth est?? activo
                    checkBluetoothEnabled();
                    setViewConnectReadyState();
                } else {
                    // Si no se otorga el permiso, mostramos un mensaje y cerramos la aplicaci??n
                    setViewRequestPermState();
                }
                break;
            default:
                // Si existe un error se indica
                Toast.makeText(this,"Ocurri?? un error",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean checkBluetoothEnabled(){
        // Se verifica si el bluetooth est?? activo
        if (!this.mBluetoothAdapter.isEnabled()) {
            // Si no est?? activo se crea un Intent Para solicitar su activaci??n
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    private void findDevice(){
        if(!DeviceSelected){ // Este if es para verificar si el dispositivo est?? conectado
            // Se inicia activity DeviceList, para seleccionar un dispositivo para conectarse
            Intent intent = new Intent(this, FindDevice.class);
            // Se registra el Intent esperando una respuesta
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
        }
    }

    private void readCodes(){
        Intent intent = new Intent(this, ReadCodesVehicle.class);
        startActivity(intent);
    }

    private void verification(){
        Intent intent = new Intent(this, Verification.class);
        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // Cuando DeviceListActivity retorna un dispositivo para conectar
                if (resultCode == Activity.RESULT_OK) {
                    // Obtenemos la direcci??n MAC del dispositivos
                    String address = data.getExtras().getString(FindDevice.EXTRA_DEVICE_ADDRESS);
                    addressDeviceSelected = address;
                    DeviceSelected = true;
                    if(mBTService == null) {
                        Intent intent = new Intent(this, BluetoothService.class);
                        intent.putExtra("BTAddress", addressDeviceSelected);
                        startService(intent);
                        bindService(intent, connection, Context.BIND_AUTO_CREATE);
                    }else {
                        try {
                            mBTService.connect(addressDeviceSelected);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    setViewConnectingState();
                }
                // Cuando DeviceListActivity retorna un error
                else{
                    Toast.makeText(this, "Hubo un error al seleccionar un dispositivo", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_BT:
                // Cuando la solicitud de activar el Bluetooth retorna
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth est?? activo
                    Toast.makeText(this, "El bluetooth se activ?? correctamente.", Toast.LENGTH_SHORT).show();
                } else {
                    // El usuario no permiti?? que se activara el Bluetooth
                    Toast.makeText(this, "El bluetooth debe estar activo para poder utilizar la aplicaci??n.", Toast.LENGTH_SHORT).show();
                    setViewRequestBTOnState();
                }
                break;
            default:
                Toast.makeText(this,"Ocurri?? un error",Toast.LENGTH_LONG).show();
                break;
        }
    }

    // Esta funci??n se utiliza para mostrar los views necesarios cuando se est?? realizando la conexi??n
    private void setViewConnectingState (){
        mLoadingDisplay.setVisibility(View.VISIBLE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mVerification.setVisibility(View.GONE);
        mRequestPermButton.setVisibility(View.GONE);
        mRequestPermDisplay.setVisibility(View.GONE);
        mRequestBTOnButton.setVisibility(View.GONE);
        mRequestBTOnDisplay.setVisibility(View.GONE);
        mConectingPB.setVisibility(View.VISIBLE);
    }

    // Esta funci??n se utiliza para mostrar los views necesarios cuando se va a relizar la lectura de los c??digos de error
    private void setViewReadyReadState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.VISIBLE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mVerification.setVisibility(View.VISIBLE);
        mRequestPermButton.setVisibility(View.GONE);
        mRequestPermDisplay.setVisibility(View.GONE);
        mRequestBTOnButton.setVisibility(View.GONE);
        mRequestBTOnDisplay.setVisibility(View.GONE);
        mConectingPB.setVisibility(View.GONE);

    }

    // Esta funci??n se utiliza para mostrar los views necesarios cuando se requiere iniciar con la conexi??n
    private void setViewConnectReadyState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.VISIBLE);
        mVerification.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.VISIBLE);
        mRequestPermButton.setVisibility(View.GONE);
        mRequestPermDisplay.setVisibility(View.GONE);
        mRequestBTOnButton.setVisibility(View.GONE);
        mRequestBTOnDisplay.setVisibility(View.GONE);
        mConectingPB.setVisibility(View.GONE);

        if(mBTService != null){
            Intent intent = new Intent(this,BluetoothService.class);
            unbindService(connection);
            stopService(intent);
            mBTService = null;
        }
        DeviceSelected = false;
    }

    // Esta funci??n se utiliza para mostrar los views necesarios cuando se va a solicitar de nuevo los permisos
    private void setViewRequestPermState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mVerification.setVisibility(View.GONE);
        mRequestPermButton.setVisibility(View.VISIBLE);
        mRequestPermDisplay.setVisibility(View.VISIBLE);
        mRequestBTOnButton.setVisibility(View.GONE);
        mRequestBTOnDisplay.setVisibility(View.GONE);
        mConectingPB.setVisibility(View.GONE);

    }

    // Esta funci??n se utiliza para mostrar los views necesarios cuando se va a solicitar de nuevo los permisos
    private void setViewRequestBTOnState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mVerification.setVisibility(View.GONE);
        mRequestPermButton.setVisibility(View.GONE);
        mRequestPermDisplay.setVisibility(View.GONE);
        mRequestBTOnButton.setVisibility(View.VISIBLE);
        mRequestBTOnDisplay.setVisibility(View.VISIBLE);
        mConectingPB.setVisibility(View.GONE);
    }

    //TODO(1): Quitar Handler
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context,"Se ha apagado el adaptador de Bluetooth",Toast.LENGTH_LONG).show();
                        setViewRequestBTOnState();
                    case BluetoothAdapter.STATE_ON:
                        setViewConnectReadyState();
                        break;
                }
            }
            if (action.equals(BluetoothService.CONNECTION_STATUS)){
                int state = intent.getIntExtra("CONN_STATUS", 0);
                switch (state){
                    case BluetoothService.STATE_NONE:
                    case BluetoothService.STATE_READY_TO_CONNECT:
                        setViewConnectReadyState();
                        break;
                    case BluetoothService.STATE_CONNECTING:
                        setViewConnectingState();
                        break;
                    case BluetoothService.STATE_CONNECTED:
                        setViewReadyReadState();
                        break;
                    default:
                        setViewConnectReadyState();
                }

            }
        }
    };

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBTService = binder.getService();
            //TODO(3): Quitar cuando se quite el handler
            try {
                mBTService.connect(addressDeviceSelected);
            } catch (IOException e) {
                e.printStackTrace();
                setViewConnectReadyState();
            }
            mBTService.setActivityHandler(mHandler);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}