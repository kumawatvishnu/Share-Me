package project.com.share_me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

public class PeerUtil {
    private final IntentFilter intentFilter = new IntentFilter();
    public static WifiP2pManager manager;
    public static WifiP2pManager.Channel channel;
    public static BroadcastReceiver receiver = null;
    MainActivity mainActivity;

    public PeerUtil(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void initSetup() {
        addIntentFilters();
        manager = (WifiP2pManager) mainActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(mainActivity, mainActivity.getMainLooper(), null);

        receiver = new ShareMeBroadcastReceiver(manager, channel, mainActivity);
        mainActivity.registerReceiver(receiver, intentFilter);

    }

    private void addIntentFilters() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


}
