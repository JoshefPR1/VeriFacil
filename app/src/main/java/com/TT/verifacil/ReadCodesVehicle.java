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
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private BluetoothService mBTService;

    private boolean mBound = false;

    private TextView welcomeDisplay;
    private ProgressBar readProgressBar;
    private TextView errorDisplay;
    private Button tryAgainButton;

    private ExecutorService executor;
    private Handler handler;


    private List<TroubleCode> troubleCodes = null;
    private String versionELM = "";
    private Protocol selectedProtocolInfo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_codes_vehicle);

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        welcomeDisplay = (TextView) findViewById(R.id.readingCodesDisplay);
        readProgressBar = (ProgressBar) findViewById(R.id.readProgressBar);
        errorDisplay = (TextView) findViewById(R.id.errorDisplay);
        tryAgainButton = (Button) findViewById(R.id.buttonTryAgain);

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewReadingState();
                executeReadCodes();
            }
        });

        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        setViewReadingState();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void setViewReadingState(){
        welcomeDisplay.setVisibility(View.VISIBLE);
        readProgressBar.setVisibility(View.VISIBLE);
        errorDisplay.setVisibility(View.GONE);
        tryAgainButton.setVisibility(View.GONE);
    }

    private void setViewErrorState(){
        welcomeDisplay.setVisibility(View.GONE);
        readProgressBar.setVisibility(View.GONE);
        errorDisplay.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.VISIBLE);
    }

    private boolean readCodes(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        ATZ reset = new ATZ();
        ATE_ echoOff = new ATE_(false);
        ATSP_ selectProtocol = new ATSP_(Protocol.AUTO.getId());
        ATDPN selectedProtocol = new ATDPN();
        ATAL longMessage = new ATAL();
        CountDTC countDTC = new CountDTC();
        ReadDTC readDTC = new ReadDTC();

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            // Starts procedure for request DTC
            try {
                // Reset ELM
                reset.run(out, in);
                if (!reset.isOK())
                    return false;
                versionELM = reset.getELMVersion();
                echoOff.run(out, in);
                if (!echoOff.isOK())
                    return false;
                selectProtocol.run(out, in);
                if (!selectProtocol.isOK())
                    return false;
                selectedProtocol.run(out, in);
                List<String> isoIds = Arrays.asList("6", "7", "8", "9");
                if (isoIds.contains(selectedProtocol.getProtocol().getId())) {
                    longMessage.run(out, in);
                    if (!longMessage.isOK())
                        return false;
                    readDTC.setISO(true);
                }
                selectedProtocolInfo = selectedProtocol.getProtocol();
                readDTC.run(out, in);
                troubleCodes = readDTC.getTroubleCodes();
                System.out.println(troubleCodes);

            } catch (IOException e) {
                e.printStackTrace();
                mBTService.connectionLost();
                return false;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (DecoderException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        else
            return false;
    }

    private void executeReadCodes(){
        executor.execute(()-> {
            if (readCodes()){
                handler.post(()->{
                    Intent intent = new Intent(ReadCodesVehicle.this, CarInfo.class);
                    intent.putExtra("versionELM", versionELM);
                    intent.putExtra("protocol", selectedProtocolInfo);
                    intent.putParcelableArrayListExtra("troubleCodes", (ArrayList<? extends Parcelable>) troubleCodes);
                    startActivity(intent);
                    finish();
                });
            }
            else {
                handler.post(()->{
                    setViewErrorState();
                    if (mBTService.getState() != BluetoothService.STATE_CONNECTED)
                        finish();
                });
            }
        });
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
            executeReadCodes();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}