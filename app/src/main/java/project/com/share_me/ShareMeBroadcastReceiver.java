package project.com.share_me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

public class ShareMeBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity mainActivity;
    private PeerDetail peerDetail = null;

    public ShareMeBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                    MainActivity mainActivity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    public void setUpPeerDetailContext(PeerDetail peerDetail){
        this.peerDetail = peerDetail;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) { // wifi is ON or OFF

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mainActivity.setWifiEnabled(true);
            } else {
                mainActivity.setWifiEnabled(false);
            }

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { // peers's list has changed

            if (manager != null) {
                manager.requestPeers(channel, mainActivity);
            }

        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { // status of any peer is changed

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                manager.requestConnectionInfo(channel, peerDetail);
            } else {
                // It's a disconnect
                if(peerDetail != null) peerDetail.resetData();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { // if current device's status is changed.
            mainActivity.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }
}
