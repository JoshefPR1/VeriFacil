package com.TT.verifacil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.ATCommands.Protocol;
import Utils.TroubleCode;


public class CarInfo extends AppCompatActivity {

    private TextView mVersionELMValue;
    private TextView mProtocolValue;
    private ListView mListTroubleCodes;
    private LinearLayout mLLListCodesView;
    private TextView mNoCodesDisplay;
    private Button mSendCodesButton;

    private Protocol protocolSelected;
    private String versionELM;
    private List<TroubleCode> troubleCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        Intent intent = getIntent();

        protocolSelected = (Protocol) intent.getSerializableExtra("protocol");
        versionELM = intent.getStringExtra("versionELM");
        troubleCodes = intent.getParcelableArrayListExtra("troubleCodes");

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        mVersionELMValue = (TextView) findViewById(R.id.versionValueText);
        mProtocolValue = (TextView) findViewById(R.id.protocolValueText);
        mVersionELMValue.setText(versionELM);
        mProtocolValue.setText(protocolSelected.getDescription());

        if (troubleCodes == null || troubleCodes.isEmpty()) {
            mLLListCodesView = (LinearLayout) findViewById(R.id.listCodesView);
            mNoCodesDisplay = (TextView) findViewById(R.id.noCodesDisplay);
            mSendCodesButton = (Button) findViewById(R.id.sendCodesButton);
            mLLListCodesView.setVisibility(View.GONE);
            mSendCodesButton.setVisibility(View.GONE);
            mNoCodesDisplay.setVisibility(View.VISIBLE);

        }else {
            mListTroubleCodes = (ListView) findViewById(R.id.listDTCs);
            AdapterTroubleCode adapter = new AdapterTroubleCode(this,0,new ArrayList<>());
            mListTroubleCodes.setAdapter(adapter);
            try {
                TroubleCode.getDescriptions(troubleCodes, adapter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public class AdapterTroubleCode extends ArrayAdapter<TroubleCode>{
        private ArrayList<TroubleCode> lTroubleCode;
        private LayoutInflater inflater = null;

        public AdapterTroubleCode(@NonNull Context context, int resource, @NonNull List<TroubleCode> objects) {
            super(context, resource, objects);
            try {

                this.lTroubleCode = (ArrayList<TroubleCode>) objects;

                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (Exception e) {

            }
        }

        public int getCount() {
            return lTroubleCode.size();
        }

        public TroubleCode getItem(TroubleCode position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder {
            public TextView display_name;
            public TextView display_desc;

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            final ViewHolder holder;
            try {
                if (convertView == null) {
                    vi = inflater.inflate(R.layout.trouble_code_item, null);
                    holder = new ViewHolder();

                    holder.display_name = (TextView) vi.findViewById(R.id.nameDTC);
                    holder.display_desc = (TextView) vi.findViewById(R.id.descriptionDTC);


                    vi.setTag(holder);
                } else {
                    holder = (ViewHolder) vi.getTag();
                }



                holder.display_name.setText(lTroubleCode.get(position).getName());
                holder.display_desc.setText(lTroubleCode.get(position).getDescription());


            } catch (Exception e) {


            }
            return vi;
        }
    }
}