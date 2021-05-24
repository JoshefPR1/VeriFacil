package com.TT.verifacil;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.ATCommands.ATAL;
import Utils.ATCommands.ATDPN;
import Utils.ATCommands.ATE_;
import Utils.ATCommands.ATH_;
import Utils.ATCommands.ATSP_;
import Utils.ATCommands.ATZ;
import Utils.ATCommands.Protocol;
import Utils.ECU;
import Utils.OBDCommands.CountDTC;
import Utils.OBDCommands.EngineRPM;
import Utils.OBDCommands.MonitorsStatus;
import Utils.OBDCommands.ReadDTC;
import Utils.OBDCommands.SupportedPIDs;
import Utils.OBDCommands.TypeOBD;
import Utils.OBDCommands.TypesOBD;

public class Verification extends AppCompatActivity {
    private BluetoothService mBTService;

    private boolean mBound = false;

    private TextView actionDisplay;
    private ProgressBar readProgressBar;
    private TextView errorDisplay;
    private Button tryAgainButton;

    private ExecutorService executor;
    private Handler handler;

    private List<ECU> ecus;
    private String versionELM = "";
    private Protocol selectedProtocolInfo = null;
    private TypesOBD currentTypeOBD;
    private float currentRPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        actionDisplay = (TextView) findViewById(R.id.readingInfoDisplay);
        readProgressBar = (ProgressBar) findViewById(R.id.readInfoPB);
        errorDisplay = (TextView) findViewById(R.id.errorInfoDisplay);
        tryAgainButton = (Button) findViewById(R.id.buttonTryAgainInfo);

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewReadingInfoState();
                executeReadInfo();
            }
        });

        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        setViewReadingInfoState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void setViewReadingInfoState(){
        actionDisplay.setVisibility(View.VISIBLE);
        readProgressBar.setVisibility(View.VISIBLE);
        errorDisplay.setVisibility(View.GONE);
        tryAgainButton.setVisibility(View.GONE);
    }

    private void setViewErrorState(){
        actionDisplay.setVisibility(View.GONE);
        readProgressBar.setVisibility(View.GONE);
        errorDisplay.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.VISIBLE);
    }

    private boolean selectCommunicationProtocol(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        ATZ reset = new ATZ();
        ATE_ echoOff = new ATE_(false);
        ATSP_ selectProtocol = new ATSP_(Protocol.AUTO.getId());
        ATDPN selectedProtocol = new ATDPN();

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            for(Protocol p : Protocol.values()) {
                // Starts procedure for select protocol
                if (!p.equals(Protocol.AUTO))
                try {
                    // Reset ELM
                    reset.run(out, in);
                    if (!reset.isOK())
                        continue;
                    versionELM = reset.getELMVersion();
                    echoOff.run(out, in);
                    if (!echoOff.isOK())
                        continue;
                    selectProtocol.run(out, in);
                    if (!selectProtocol.isOK())
                        continue;
                    selectedProtocol.run(out, in);
                    this.selectedProtocolInfo = selectedProtocol.getProtocol();
                    System.out.println(selectedProtocol);
                    break;
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
            }
            return true;
        }
        else
            return false;
    }

    private boolean requestSupportedPIDs(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        ATH_ headerOn = new ATH_(true);
        SupportedPIDs suppPIDs = new SupportedPIDs();
        suppPIDs.setISO(selectedProtocolInfo.isISO());

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            // Starts procedure for request DTC
            try {
                // Reset ELM
                headerOn.run(out,in);
                if (!headerOn.isOK())
                    return false;
                suppPIDs.run(out, in);
                this.ecus = suppPIDs.getEcus();
                System.out.println(ecus);

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

    private boolean requestOBDType(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        TypeOBD typeOBD = new TypeOBD();

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            // Starts procedure for request DTC
            try {
                // Reset ELM
                typeOBD.run(out, in);
                this.currentTypeOBD = typeOBD.getTypeOBD();
                System.out.println(this.currentTypeOBD);

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

    private boolean requestEngineRPM(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        EngineRPM engineRPM = new EngineRPM();

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            try {
                engineRPM.run(out, in);
                this.currentRPM = engineRPM.getRpm();
                System.out.println(this.currentRPM);

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
            if(currentRPM <= 250.0)
                return false;
            return true;
        }
        else
            return false;
    }

    private boolean requestMonitors(){
        InputStream in = mBTService.getMmInStream();
        OutputStream out = mBTService.getMmOutStream();

        MonitorsStatus monStatus = new MonitorsStatus();
        monStatus.setEcus(this.ecus);

        if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
            try {
                monStatus.run(out, in);
                this.ecus = monStatus.getEcus();
                System.out.println(this.ecus);

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
            executeReadInfo();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}