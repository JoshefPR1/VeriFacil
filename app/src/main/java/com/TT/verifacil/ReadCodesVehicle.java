package com.TT.verifacil;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class ReadCodesVehicle extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    BluetoothAdapter mBluetoothAdapter;
    private String mBTDeviceAddress;
    private BluetoothService mBTService;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private String mConnectedDeviceName = null;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_codes_vehicle);

        /*Intent intent = this.getIntent();

        mBTDeviceAddress = intent.getStringExtra("BTDeviceAddress");

        mBTService = new BluetoothService(this,mHandler);

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mBTDeviceAddress);

        mBTService.connect(device);*/
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void readCodes(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        ATZ reset = new ATZ();
        ATE_ echoOff = new ATE_(false);
        ATSP_ selectProtocol = new ATSP_(Protocol.AUTO.getId());
        ATDPN selectedProtocol = new ATDPN();
        ATAL longMessage = new ATAL();
        CountDTC countDTC = new CountDTC();
        ReadDTC readDTC = new ReadDTC();

        List<TroubleCode> troubleCodes = null;
        String versionELM = "";
        Protocol selectedProtocolInfo = null;

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            // Starts procedure for request DTC
            try {
                // Reset ELM
                reset.run(out, in);
                if (!reset.isOK())
                    return;
                versionELM = reset.getELMVersion();
                echoOff.run(out, in);
                if (!echoOff.isOK())
                    return;
                selectProtocol.run(out, in);
                if (!selectProtocol.isOK())
                    return;
                selectedProtocol.run(out, in);
                List<String> isoIds = Arrays.asList("6", "7", "8", "9");
                if (isoIds.contains(selectedProtocol.getProtocol().getId())) {
                    longMessage.run(out, in);
                    if (!longMessage.isOK())
                        return;
                    readDTC.setISO(true);
                }
                selectedProtocolInfo = selectedProtocol.getProtocol();
                readDTC.run(out, in);
                troubleCodes = readDTC.getTroubleCodes();
                System.out.println(troubleCodes);

            } catch (IOException e) {
                e.printStackTrace();
//                mBTService.connectionLost();
                finish();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (DecoderException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(this, CarInfo.class);
            intent.putExtra("versionELM", versionELM);
            intent.putExtra("protocol", selectedProtocolInfo);
            intent.putParcelableArrayListExtra("troubleCodes", (ArrayList<? extends Parcelable>) troubleCodes);
            startActivity(intent);
            finish();
        }
        else
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

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
                        finish();
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
            mBound = true;
            readCodes();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}