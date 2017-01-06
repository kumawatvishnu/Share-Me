package project.com.share_me;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import project.com.socio_fi.R;

/**
 * Created by PRATIK GAUTAM on 23-10-2016.
 */
public class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
    private final Context mContext;
    private List<WifiP2pDevice> items;

    public WifiPeerListAdapter(Context context, int textViewResourceId,
                               List<WifiP2pDevice> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.peer_list_row, null);
        }
        final WifiP2pDevice device = items.get(position);
        if (device != null) {
            TextView device_name = (TextView) view.findViewById(R.id.device_name); // for showing peer name
            TextView device_status = (TextView) view.findViewById(R.id.device_status); // for showing peer status
            device_name.setText(device.deviceName);
            device_status.setText(getDeviceStatus(device.status));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // if any particular peer is clicked then start a new acitivty & pass the device details as well
                Intent i = new Intent(mContext, PeerDetail.class);
                i.putExtra("data", device);
                mContext.startActivity(i);
            }
        });

        return view;
    }

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}

