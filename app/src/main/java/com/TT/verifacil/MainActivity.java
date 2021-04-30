package com.TT.verifacil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.ATCommands.ATAL;
import Utils.ATCommands.ATDPN;
import Utils.ATCommands.ATE_;
import Utils.ATCommands.ATSP_;
import Utils.ATCommands.ATZ;
import Utils.ATCommands.Protocol;
import Utils.OBDCommands.CountDTC;
import Utils.OBDCommands.ReadDTC;
import Utils.TroubleCode;

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
    private Button mReadCodes;
    private TextView mWelcomeDisplay;

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
        mReadCodes = (Button) findViewById(R.id.readCodesButton);
        mFindDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDevice();
            }
        });
        mReadCodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readCodes();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mBTService = new BluetoothService(this, mHandler);
        // Verficamos que se tengan los permisos necesarios en el dispositivo
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // Se verifica si el bluetooth está activo
            checkBluetoothEnabled();
        }else {
            // Si no se cuenta con los permisos es necesario que se solicite al usuario los permisos necesarios
            // Se crea un request para solicitar los permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },200);
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothService.CONNECTION_STATUS);
        registerReceiver(mReceiver, filter);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast mensaje;
        switch (requestCode) {
            // Se verifica la respuesta obtenida, para evitar errores
            case 200:
                // Se verifica si los permisos fueron otorgados
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    // Se verifica si el bluetooth está activo
                    checkBluetoothEnabled();
                } else {
                    // Si no se otorga el permiso, mostramos un mensaje y cerramos la aplicación
                    //TODO(2): Mejorar el mesaje para volver a solicitar la activación del bluetooth
                    Toast.makeText(this,"Permiso no se otorgó, adiós",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                // Si existe un error se indica
                Toast.makeText(this,"Ocurrió un error",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean checkBluetoothEnabled(){
        // Se verifica si el bluetooth está activo
        if (!this.mBluetoothAdapter.isEnabled()) {
            // Si no está activo se crea un Intent Para solicitar su activación
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    private void findDevice(){
        if(!DeviceSelected){ // Este if es para verificar si el dispositivo está conectado
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // Cuando DeviceListActivity retorna un dispositivo para conectar
                if (resultCode == Activity.RESULT_OK) {
                    // Obtenemos la dirección MAC del dispositivos
                    String address = data.getExtras().getString(FindDevice.EXTRA_DEVICE_ADDRESS);
                    addressDeviceSelected = address;
                    DeviceSelected = true;

                    Intent intent = new Intent(this,BluetoothService.class);
                    intent.putExtra("BTAddress", addressDeviceSelected);
                    startService(intent);
                    bindService(intent,connection,Context.BIND_AUTO_CREATE);
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
                    // Bluetooth está activo
                    Toast.makeText(this, "El bluetooth se activó correctamente.", Toast.LENGTH_SHORT).show();
                } else {
                    // El usuario no permitió que se activara el Bluetooth
                    Toast.makeText(this, "El bluetooth debe estar activo para poder utilizar la aplicación.", Toast.LENGTH_SHORT).show();
                    // Cerramos la aplicación
                    finish();
                }
                break;
            default:
                Toast.makeText(this,"Ocurrió un error",Toast.LENGTH_LONG).show();
                break;
        }
    }

    // Esta función se utiliza para mostrar los views necesarios cuando se está realizando la conexión
    private void setViewConnectingState (){
        mLoadingDisplay.setVisibility(View.VISIBLE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mReadCodes.setVisibility(View.GONE);
    }

    // Esta función se utiliza para mostrar los views necesarios cuando se va a relizar la lectura de los códigos de error
    private void setViewReadyReadState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.VISIBLE);
        mWelcomeDisplay.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.GONE);
        mReadCodes.setVisibility(View.VISIBLE);
    }

    // Esta función se utiliza para mostrar los views necesarios cuando se requiere iniciar con la conexión
    private void setViewConnectReadyState (){
        mLoadingDisplay.setVisibility(View.GONE);
        mReadyReadDisplay.setVisibility(View.GONE);
        mWelcomeDisplay.setVisibility(View.VISIBLE);
        mReadCodes.setVisibility(View.GONE);
        mFindDevice.setVisibility(View.VISIBLE);
        DeviceSelected = false;
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
                        /*MyArrayAdapter adapter = (MyArrayAdapter) deviceSpinner.getAdapter();
                        if (adapter != null) {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        }
                        break;*/
                        Toast.makeText(context,"Se ha apagado el adaptador de Bluetooth",Toast.LENGTH_LONG).show();
                        //TODO(0): Solicitar activar Bluetooth
                    case BluetoothAdapter.STATE_ON:
                        /*populateDeviceSpinner();
                        break;*/
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
            mBTService.setActivityHandler(mHandler);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}