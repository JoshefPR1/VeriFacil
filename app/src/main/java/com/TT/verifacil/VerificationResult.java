package com.TT.verifacil;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Utils.ATCommands.Protocol;
import Utils.ECU;
import Utils.OBDCommands.Monitor;
import Utils.OBDCommands.TypesOBD;
import Utils.TroubleCode;

public class VerificationResult extends AppCompatActivity {

    private TextView mVersionVRValueText;
    private TextView mProtocolVRValueText;
    private TextView mTipoOBDValueText;
    private ListView mListECUs;
    private Button mReadCodesButton;

    private List<ECU> ecus;
    private String versionELM = "";
    private Protocol protocolInfo = null;
    private TypesOBD typeOBD;
    private float engineRPM;
    private AdapterECU mEcusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_result);
        Intent intent = getIntent();

        versionELM = intent.getStringExtra("versionELM");
        protocolInfo =(Protocol) intent.getSerializableExtra("protocol");
        typeOBD = (TypesOBD) intent.getSerializableExtra("typeOBD");
        engineRPM = intent.getFloatExtra("engineRPM", (float) 0.0);
        ecus = intent.getParcelableArrayListExtra("ecus");

        mVersionVRValueText = (TextView) findViewById(R.id.versionVRValueText);
        mProtocolVRValueText = (TextView) findViewById(R.id.protocolVRValueText);
        mTipoOBDValueText = (TextView) findViewById(R.id.tipoOBDValueText);
        mListECUs = (ListView) findViewById(R.id.listECUs);
        mReadCodesButton = (Button) findViewById(R.id.readCodesVRButton);

        for(ECU ecu : ecus){
            if (ecu.isMILOn()){
                mReadCodesButton.setVisibility(View.VISIBLE);
                break;
            }
        }

        mEcusAdapter = new AdapterECU(this,0,ecus);
        mListECUs.setAdapter(mEcusAdapter);

        mVersionVRValueText.setText(versionELM);
        mProtocolVRValueText.setText(protocolInfo.getDescription());
        mTipoOBDValueText.setText(typeOBD.getName());

        mReadCodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadCodesVehicle();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();



    }

    private void ReadCodesVehicle(){
        Intent intent = new Intent(this, ReadCodesVehicle.class);
        intent.putParcelableArrayListExtra("ecus", (ArrayList<? extends Parcelable>) ecus);
        intent.putExtra("versionELM",versionELM);
        intent.putExtra("protocol", protocolInfo);
        intent.putExtra("typeOBD", typeOBD);
        intent.putExtra("engineRPM",engineRPM);
        startActivity(intent);
    }

    public class AdapterECU extends ArrayAdapter<ECU> {
        private ArrayList<ECU> lECU;
        private LayoutInflater inflater = null;

        public AdapterECU(@NonNull Context context, int resource, @NonNull List<ECU> objects) {
            super(context, resource, objects);
            try {

                this.lECU = (ArrayList<ECU>) objects;

                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (Exception e) {

            }
        }

        public int getCount() {
            return lECU.size();
        }

        public ECU getItem(ECU position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder {
            public TextView display_ecuIDValueText;
            public TextView display_MILValueText;
            public ListView display_monitors;
            public TextView display_textMonitors;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            final VerificationResult.AdapterECU.ViewHolder holder;
            try {
                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(VerificationResult.this, R.layout.device_name);
                if (convertView == null) {
                    vi = inflater.inflate(R.layout.ecu_item, null);
                    holder = new VerificationResult.AdapterECU.ViewHolder();

                    holder.display_ecuIDValueText = (TextView) vi.findViewById(R.id.ecuIDValueText);
                    holder.display_MILValueText = (TextView) vi.findViewById(R.id.MILValueText);
                    holder.display_monitors = (ListView) vi.findViewById(R.id.monitorsLV);
                    holder.display_textMonitors = (TextView) vi.findViewById(R.id.monitorsValueText);
                    //holder.display_monitors.setAdapter(arrayAdapter);

                    vi.setTag(holder);
                } else {
                    holder = (VerificationResult.AdapterECU.ViewHolder) vi.getTag();
                }

                holder.display_ecuIDValueText.setText(lECU.get(position).getId());
                holder.display_MILValueText.setText(lECU.get(position).isMILOn()?"Encendida":"Apagada");
                if (lECU.get(position).isMILOn())
                    holder.display_MILValueText.setTextColor(Color.RED);
                else
                    holder.display_MILValueText.setTextColor(Color.GREEN);
                List<Monitor> monitors = lECU.get(position).getSupportedMonitorsB();
                List<Integer> statusMonitors = lECU.get(position).getCompleteMonitorsB();
                String textMonitors = "";
                for (int i = 0; i < monitors.size(); i++){
                    //arrayAdapter.add(monitors.get(i).getName()+": "+(statusMonitors.get(i)==1?"No completado":"Completado"));
                    textMonitors += monitors.get(i).getName()+": "+(statusMonitors.get(i)==1?"No completado":"Completado") +"\n";
                }

                monitors = lECU.get(position).getSupportedMonitorsC();
                statusMonitors = lECU.get(position).getCompleteMonitorsC();
                for (int i = 0; i < monitors.size(); i++){
                    //arrayAdapter.add(monitors.get(i).getName()+": "+(statusMonitors.get(i)==1?"No completado":"Completado"));
                    textMonitors += monitors.get(i).getName()+": "+(statusMonitors.get(i)==1?"No completado":"Completado") +"\n";
                }
                holder.display_textMonitors.setText(textMonitors);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return vi;
        }
    }
}