package project.com.share_me;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import project.com.share_me.route.RouteTable;
import project.com.share_me.route.ShowRouteTable;
import project.com.share_me.service.ClientService;
import project.com.share_me.service.ServerService;
import project.com.socio_fi.R;

/**
 * Created by PRATIK GAUTAM on 18-10-2016.
 */

public class PeerDetail extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    Toolbar toolbar;
    private WifiP2pDevice wifiP2pDevice;
    private WifiP2pInfo wifiInfo;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ShareMeBroadcastReceiver receiver;

    TextView peer_name, peer_details, peer_conn_status, peer_send_file_path, dis_peer_info, dis_peer_file_transfer_info;
    Button peer_conn_change, peer_select_file, peer_send_file, peer_recv_file;
    ProgressDialog progressDialog = null;
    LinearLayout peer_send_recv_file_layout;

    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    private File downloadTarget = null;
    private String inputIpAddr = "";

    private Boolean transferActive = false; // flag to check clientService is running
    private Boolean serverThreadActive = false;
    private Boolean isConnected = false; // if already in connected state
    private Boolean routingRequired = false; // if routing is required

    private Intent clientServiceIntent = null;
    private Intent serverServiceIntent = null;

    private String routingFilePath = "";
    private String routingTargetIp = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.peer_detail);
        manager = PeerUtil.manager;
        channel = PeerUtil.channel;
        receiver = (ShareMeBroadcastReceiver) PeerUtil.receiver;
        receiver.setUpPeerDetailContext(this);

        toolbar = (Toolbar) findViewById(R.id.peertoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle(R.string.app_name);

        wifiP2pDevice = (WifiP2pDevice) getIntent().getExtras().get("data");

        peer_name = (TextView) findViewById(R.id.dis_peer_name);
        peer_details = (TextView) findViewById(R.id.dis_peer_details);
        peer_conn_status = (TextView) findViewById(R.id.dis_peer_status);
        peer_conn_change = (Button) findViewById(R.id.peer_conn_change);
        peer_select_file = (Button) findViewById(R.id.peer_select_file);
        peer_send_file = (Button) findViewById(R.id.peer_send_file);
        peer_recv_file = (Button) findViewById(R.id.peer_recv_file);
        peer_send_recv_file_layout = (LinearLayout) findViewById(R.id.peer_send_recv_file_layout);
        peer_send_file_path = (TextView) findViewById(R.id.peer_send_file_path);
        dis_peer_info = (TextView) findViewById(R.id.dis_peer_info);
        dis_peer_file_transfer_info = (TextView) findViewById(R.id.dis_peer_file_transfer_info);

        peer_name.setText(wifiP2pDevice.deviceName + "");
        peer_details.setText("Device Address : " + wifiP2pDevice.deviceAddress + "\n"
                + "Primary Type : " + wifiP2pDevice.primaryDeviceType + "\n"
                + "Secondary Type : " + wifiP2pDevice.secondaryDeviceType + "\n");

        setConnectionStatus();
        // if "connect" button is pressed
        peer_conn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConnectionStatus();
            }
        });

        // if "select file" button is pressed
        peer_select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PeerDetail.this, FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);
            }
        });

        // if "send" button is pressed
        peer_send_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!peer_send_file_path.getText().toString().equals("file path")) {
                    if (!transferActive) { // if no trasfer active
                        Toast.makeText(PeerDetail.this, "sending " + peer_send_file_path.getText().toString(), Toast.LENGTH_SHORT).show();
                        showIpDialogBox();
                    }
                } else {
                    Toast.makeText(PeerDetail.this, "Please select a file first !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // if "receive" button is pressed
        peer_recv_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServerService();
            }
        });
    }

    private void showIpDialogBox() {  // to show a dialog box for entering destination IP address

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter receiver's IP");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText("192.168.49.");
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputIpAddr = input.getText().toString();
                try {
                    if (wifiInfo.isGroupOwner) {
                        if (inputIpAddr.equals("go"))
                            Toast.makeText(PeerDetail.this, "can't send to self !!!", Toast.LENGTH_SHORT).show();
                        else
                            startClientService(InetAddress.getByName(inputIpAddr), InetAddress.getByName(inputIpAddr), new File(peer_send_file_path.getText().toString()));
                    } else {
                        if (inputIpAddr.equals("go"))
                            startClientService(wifiInfo.groupOwnerAddress, wifiInfo.groupOwnerAddress, new File(peer_send_file_path.getText().toString()));
                        else
                            startClientService(wifiInfo.groupOwnerAddress, InetAddress.getByName(inputIpAddr), new File(peer_send_file_path.getText().toString()));
                    }
                } catch (UnknownHostException e) {
                    Toast.makeText(PeerDetail.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PeerDetail.this, "Unable to send file, enter valid IP", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void toggleConnectionStatus() {
        if (!isConnected) {  // if not connected then establish the connection
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = wifiP2pDevice.deviceAddress; // setting MAC address
            config.wps.setup = WpsInfo.PBC; // wifi protected service as Push Button Config.

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = ProgressDialog.show(this, "Press back to cancel",
                    "Connecting to : " + wifiP2pDevice.deviceName, true, true,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                        }
                    }
            );
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    setConnectionStatusByOwn("Invited");
                    isConnected = true;
                    peer_conn_change.setText("Disconnect"); // setting button text
                }

                @Override
                public void onFailure(int reason) {
                    progressDialog.dismiss();
                    Toast.makeText(PeerDetail.this, "Unable to connect, try again !", Toast.LENGTH_SHORT).show();
                }
            });
        } else { // if already connected then disconnect the devices.
            manager.cancelConnect(channel, null);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) { // first time conn is setup...
        wifiInfo = info;
        if (!wifiInfo.isGroupOwner) {  // if I am a client then enter the GO address in the routing table.
            RouteTable.setRouteTable(wifiP2pDevice.deviceName, info.groupOwnerAddress);
        }
        WifiPeerConst.mapping.put(wifiP2pDevice.deviceName, Pair.create(wifiP2pDevice, wifiInfo)); // putting (device, info)
        readyToTransfer(wifiInfo);
    }

    private void readyToTransfer(WifiP2pInfo info) {
        isConnected = true;
        dis_peer_info.setText(info + ""); // showing conn. details
        Toast.makeText(PeerDetail.this, "Connected", Toast.LENGTH_SHORT).show(); // showing toast
        setConnectionStatusByOwn("Connected"); // setting conn. status
        peer_conn_change.setText("Disconnect"); // setting button text
        peer_send_recv_file_layout.setVisibility(View.VISIBLE); // make buttons visible...
    }

    public void resetData() {
        isConnected = false;
        Toast.makeText(PeerDetail.this, "Disconnected", Toast.LENGTH_SHORT).show();
        setConnectionStatusByOwn("Available"); // setting conn. status
        dis_peer_info.setText(""); // removing conn. details
        peer_conn_change.setText("Connect"); // setting button text

        peer_send_recv_file_layout.setVisibility(View.GONE);
    }

    private void startServerService() {
        if (!serverThreadActive) {
            //Create new thread, open socket, wait for connection, and transfer file
            Toast.makeText(PeerDetail.this, "receiving", Toast.LENGTH_SHORT).show();
            downloadTarget = new File(Environment.getExternalStorageDirectory() + "/shareMe/");
            if (!downloadTarget.exists()) {
                downloadTarget.mkdirs();
            }
            serverServiceIntent = new Intent(this, ServerService.class);  // starting the ServerService.java
            serverServiceIntent.putExtra("saveLocation", downloadTarget);
            serverServiceIntent.putExtra("port", new Integer(WifiPeerConst.PORT));
            serverServiceIntent.putExtra("wifiP2pDevice", wifiP2pDevice);
            serverServiceIntent.putExtra("wifiInfo", wifiInfo);
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if (resultCode == WifiPeerConst.PORT) {
                        if (resultData == null) {
                            serverThreadActive = false;
                            dis_peer_file_transfer_info.post(new Runnable() {
                                public void run() {
                                    dis_peer_file_transfer_info.append("\n\n");
                                }
                            });
                        } else {
                            dis_peer_file_transfer_info.post(new Runnable() {
                                public void run() {
                                    dis_peer_file_transfer_info.append(resultData.get("message") + "\n\n");
                                }
                            });
                        }
                    }
                    if (resultCode == WifiPeerConst.ROUTE) {  // if routin is required then call reached here.
                        serverThreadActive = false;
                        routingRequired = true;
                        routingFilePath = (String) resultData.get("filePath");
                        routingTargetIp = (String) resultData.get("targetIp");
                        if (routingRequired) startRouting(); // make a call to start the routing
                    }
                }
            });

            serverThreadActive = true;
            startService(serverServiceIntent);
        }
    }

    private void startRouting() { // run the client service
        dis_peer_file_transfer_info.append("---------- Routing ----------\n\n");
        dis_peer_file_transfer_info.append("starting routing file " + routingFilePath + " to " + routingTargetIp);
        try {
            startClientService(InetAddress.getByName(routingTargetIp), InetAddress.getByName(routingTargetIp), new File(routingFilePath));
        } catch (UnknownHostException e) {
            Toast.makeText(PeerDetail.this, "Unable to route\n" + e.toString(), Toast.LENGTH_SHORT).show(); // showing toast
        }
    }

    private void startClientService(InetAddress targetDeviceIP, InetAddress destDeviceIP, File file) {
        //Launch client service
        clientServiceIntent = new Intent(this, ClientService.class);
        clientServiceIntent.putExtra("fileToSend", file);
        clientServiceIntent.putExtra("port", new Integer(WifiPeerConst.PORT));
        clientServiceIntent.putExtra("targetDeviceIP", targetDeviceIP);
        clientServiceIntent.putExtra("destDeviceIP", destDeviceIP);
        clientServiceIntent.putExtra("wifiInfo", wifiInfo);
        clientServiceIntent.putExtra("wifiP2pDevice", wifiP2pDevice);
        clientServiceIntent.putExtra("clientResult", new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {

                if (resultCode == WifiPeerConst.PORT) {
                    if (resultData == null) {
                        transferActive = false;
                        dis_peer_file_transfer_info.post(new Runnable() {
                            public void run() {
                                dis_peer_file_transfer_info.append("\n\n");
                            }
                        });
                    } else {
                        dis_peer_file_transfer_info.post(new Runnable() {
                            public void run() {
                                dis_peer_file_transfer_info.append(resultData.get("message") + "\n\n");
                            }
                        });
                    }
                }

            }
        });
        transferActive = true;
        startService(clientServiceIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_FILE: // if File has been chosen then show it's path in text box
                    if (data.hasExtra(WifiPeerConst.EXTRA_FILE_PATH)) {
                        selectedFile = new File
                                (data.getStringExtra(WifiPeerConst.EXTRA_FILE_PATH));
                        peer_send_file_path.setText(selectedFile.getPath());
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish(); // if back button is pressed then kill this activity.
        if (item.getItemId() == R.id.table) { // if "routing table" is pressed then show the routing table
            startActivity(new Intent(PeerDetail.this, ShowRouteTable.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void setConnectionStatus() { // setting the status eg: connection status, file status...
        peer_conn_status.setText(getDeviceStatus(wifiP2pDevice.status));

        if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) { // already connected...
            Pair pair = WifiPeerConst.mapping.get(wifiP2pDevice.deviceName);
            wifiP2pDevice = (WifiP2pDevice) pair.first;
            wifiInfo = (WifiP2pInfo) pair.second;
            wifiP2pDevice.status = WifiP2pDevice.CONNECTED;
            if (wifiInfo != null) readyToTransfer(wifiInfo);
        }
        if (wifiP2pDevice.status == WifiP2pDevice.INVITED) { // already invited...
            isConnected = true;
            Toast.makeText(PeerDetail.this, "Invited", Toast.LENGTH_SHORT).show(); // showing toast
            setConnectionStatusByOwn("Invited"); // setting conn. status
            peer_conn_change.setText("Disconnect"); // setting button text
        }
    }

    public void setConnectionStatusByOwn(String text) {
        peer_conn_status.setText(text);
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
