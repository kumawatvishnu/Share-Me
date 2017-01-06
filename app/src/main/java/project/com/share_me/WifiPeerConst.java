package project.com.share_me;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Pair;

import java.util.HashMap;

public class WifiPeerConst {

    public final static String EXTRA_FILE_PATH = "file_path";
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";
    public final static String DEFAULT_INITIAL_DIRECTORY = "/";

    public final static String START_HANDSHAKE = "About to start handshake";
    public final static String CLIENT_HELLO = "WDFT_client_hello";
    public final static String SERVER_HELLO = "WDFT_server_hello";
    public final static String SERVER_READY = "WDFT_server_ready";

    public final static int PORT = 7950; // port for TCP connection
    public final static int ROUTE = 1234;

    public static String MY_DEVICE_NAME = "";
    public static HashMap<String, Pair<WifiP2pDevice, WifiP2pInfo>> mapping = new HashMap<>();
}
