package project.com.share_me;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import project.com.share_me.route.ShowRouteTable;
import project.com.socio_fi.R;

/**
 * Created by PRATIK GAUTAM on 18-10-2016.
 */

public class MainActivity extends AppCompatActivity implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private ProgressDialog progressDialog = null;
    private WifiP2pDevice device;
    Toolbar toolbar;
    private boolean wifiEnabled = false;
    private ListView peerListView;
    WifiPeerListAdapter wifiPeerListAdapter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tabtoolbar);
        peerListView = (ListView) findViewById(R.id.devices_list);

        setSupportActionBar(toolbar);  // setting toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle(R.string.app_name);

        PeerUtil mPeerUtil = new PeerUtil(this); // initializing the channel and intents
        mPeerUtil.initSetup();
        manager = PeerUtil.manager;
        channel = PeerUtil.channel;

        wifiPeerListAdapter = new WifiPeerListAdapter(this, R.layout.activity_main, peers);
        peerListView.setAdapter(wifiPeerListAdapter); // initializing the list-view for peers
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

    public void updateThisDevice(WifiP2pDevice device) {  // called if device name or status has changed...
        this.device = device;
        TextView my_name = (TextView) findViewById(R.id.my_name);
        TextView my_status = (TextView) findViewById(R.id.my_status);

        my_name.setText(device.deviceName);
        my_status.setText(getDeviceStatus(device.status));

        WifiPeerConst.MY_DEVICE_NAME = device.deviceName;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {  // if peers are present then dismiss dialog box and refresh the list-view
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        wifiPeerListAdapter.notifyDataSetChanged();
    }

    public void clearPeers() {  // method to remove all the peers from the list-view
        peers.clear();
        wifiPeerListAdapter.notifyDataSetChanged();
    }

    public void onInitiateDiscovery() { // show dialog box and make a call to search for the peers in the neighbour...
        showDialogBox();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogBox() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(this, "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                });
    }

    public void setWifiEnabled(boolean wifiEnabled) { // mtethod to set a flag to check status of WiFi
        this.wifiEnabled = wifiEnabled;
        if (wifiEnabled == false) clearPeers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // options in the action bar
        if (item.getItemId() == android.R.id.home) finish();
        if (item.getItemId() == R.id.refresh) { // if refresh button is pressed
            if (!wifiEnabled) {  // if wifi is not enabled then throw a message
                Toast.makeText(this, "Please turn on WiFi...",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            onInitiateDiscovery();  // else start discovery of peers
        }
        if (item.getItemId() == R.id.table) { // if "routing table" button is pressed then show table.
            startActivity(new Intent(MainActivity.this, ShowRouteTable.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (!wifiEnabled) {
            Toast.makeText(this, "Please turn on WiFi...",
                    Toast.LENGTH_SHORT).show();
        } else
            onInitiateDiscovery();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
