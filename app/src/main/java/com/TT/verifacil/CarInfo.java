package com.TT.verifacil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Utils.ATCommands.Protocol;
import Utils.TroubleCode;


public class CarInfo extends AppCompatActivity {

    private TextView mVersionELMValue;
    private TextView mProtocolValue;
    private ListView mListTroubleCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        Intent intent = getIntent();

        Protocol protocolSelected = (Protocol) intent.getSerializableExtra("protocol");
        String versionELM = intent.getStringExtra("versionELM");
        List<TroubleCode> troubleCodes = intent.getParcelableArrayListExtra("troubleCodes");

        mVersionELMValue = (TextView) findViewById(R.id.versionValueText);
        mProtocolValue = (TextView) findViewById(R.id.protocolValueText);
        mListTroubleCodes = (ListView) findViewById(R.id.listDTCs);

        AdapterTroubleCode adapter = new AdapterTroubleCode(this,0,troubleCodes);
        mListTroubleCodes.setAdapter(adapter);
        mVersionELMValue.setText(versionELM);
        mProtocolValue.setText(protocolSelected.getDescription());

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