package com.TT.verifacil;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    // Nombre para el Service Device Protocol (SDP)
    private static final String NAME = "Verifacil";
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothSocket mmSocket;

    // UUID para la aplicación
    private static final UUID VF_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private static final UUID VF_UUID = UUID.fromString("b545a148-8f76-11eb-8dcd-0242ac130003");

    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private ConnectThread mConnectThread;
    private int mState;
    private Context context;

    public static final int STATE_NONE = 0; // No estamos haciendo nada
    public static final int STATE_READY_TO_CONNECT = 1; //Está listo para comenzar la conexión
    public static final int STATE_CONNECTING = 2; // Iniciando una conexión de salida
    public static final int STATE_CONNECTED = 3; // Estamos conectados a un dispositivo
    public static final String CONNECTION_STATUS = "com.TT.verifacil.BluetoothService.ChangeStatusNotification";

    public BluetoothService(){
        super();
    }

    public void setActivityHandler(Handler handler){
        this.mHandler = handler;
    }

    public void setActivityContext(Context context){
        this.context = context;
    }

    /**
     * Cambia el estado actual de la conexión
     *
     * @param state Un int que define el estado actual de la conexión
     * */
    private synchronized void setState(int state){
        mState = state;
        // Enviamos el nuevo estado al Handler, así el UI Activity se puede actualizar
        //mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

        Intent intent = new Intent();
        intent.setAction(CONNECTION_STATUS);
        intent.putExtra("CONN_STATUS", mState);
        context = getApplicationContext();
        context.sendBroadcast(intent);
    }

    public synchronized int getState(){
        return mState;
    }

    public synchronized void connect(String address) throws IOException {

        BluetoothDevice device = mAdapter.getRemoteDevice(address);

        // Cancelamos cualquier intento para realizar una conexión
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancelamos cualquier Stream de una conexión
        if (mmSocket != null) {
            closeStreams();
        }
        // Iniciamos el thread para conectar con el dispositivo dado
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Inciamos se obtienen los Streams que vamos a utilizar para enviar y recibir datos
     *
     * @param socket El BluetoothSocket es donde se realizó la conexión
     * @param device El BluetoothDevice al dispositivo al cual se está conectado
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancelamos cualquier intento para realizar una conexión
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Ya que creamos el socket se obtienen los Streams de entrada y salida
        getStreams(socket);
        mmSocket = socket;

        if(mHandler!=null){
            // Enviamos el nombre del dispositivo al que nos conectamos para mostarlo en el UI
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        setState(STATE_CONNECTED);
    }

    /**
     * Detenemos todos los threads y el socket
     */
    public synchronized void stop() throws IOException {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancelamos cualquier Stream de una conexión
        if (mmSocket != null) {
            closeStreams();
        }

        this.mState = STATE_NONE;
    }

    /**
     * Indica que el intento de realizar la conexión falló
     */
    public void connectionFailed() {
        setState(STATE_READY_TO_CONNECT);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indica que la conexión se perdió
     */
    public void connectionLost() {
        setState(STATE_READY_TO_CONNECT);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public InputStream getMmInStream() {
        return mmInStream;
    }

    public OutputStream getMmOutStream() {
        return mmOutStream;
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    /**
     * Este hilo se ejecuta cuando se intenta establecer un conexión saliente
     * con un dispositivo. Si la cocexión es exitosa se obtiene un socket.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(VF_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Se obtienen el Stream de entreada y salida, los cuales
     * se van a utilizar para enviar y recibir mensajes
     */
    private void getStreams(BluetoothSocket socket){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    private void closeStreams() throws IOException {
        mmSocket.close();
        if(mmInStream != null)
            try{
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     *
     * Comienzan métodos, atributos y clases utilizadas por Service
     *
     * */

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Devuelve una instancia de LocalBinder en el cual el usuario puede llamar a los método públicos
            return BluetoothService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mmInStream = null;
        mmOutStream = null;
        return START_STICKY;
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
