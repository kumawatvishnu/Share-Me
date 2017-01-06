package project.com.share_me.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import project.com.share_me.route.RouteTable;
import project.com.share_me.WifiPeerConst;

public class ServerService extends IntentService {

    private int port;
    private File saveLocation;
    private ResultReceiver serverResult;
    private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;
    private Boolean routingRequired = false;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        saveLocation = (File) intent.getExtras().get("saveLocation");
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");
        targetDevice = (WifiP2pDevice) intent.getExtras().get("wifiP2pDevice");
        wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");

        signalActivity("Starting to download");
        String fileName = "";
        String targetIp = "";

        ServerSocket welcomeSocket = null;
        Socket socket = null;

        try {
            welcomeSocket = new ServerSocket(port);
            //Listen for incoming connections on specified port
            //Block thread until someone connects
            socket = welcomeSocket.accept();

            signalActivity("TCP Connection Established: " + socket.toString() +
                    " Starting file transfer with client : " + socket.getInetAddress());

            RouteTable.setRouteTable(targetDevice.deviceName, socket.getInetAddress());

            byte[] buffer = new byte[4096];
            int bytesRead;
            String handshakeMessage = "";

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            //Client-Server handshake
            signalActivity("------ " + WifiPeerConst.START_HANDSHAKE + " ------");

            signalActivity("server listening... ");

            while (inputStream.available() == 0) ;
            while ((bytesRead = inputStream.read()) != '\n') {
                handshakeMessage += (char) bytesRead;
            }
            signalActivity("server received " + handshakeMessage);

            if (!handshakeMessage.equals(WifiPeerConst.CLIENT_HELLO)) {
                throw new IOException("Invalid WDFT protocol message");
            }

            handshakeMessage = WifiPeerConst.SERVER_HELLO + "\n";
            outputStream.write(handshakeMessage.getBytes());
            outputStream.flush();
            signalActivity("server sending " + handshakeMessage);

            handshakeMessage = "";
            while ((bytesRead = inputStream.read()) != '\n') {
                handshakeMessage += (char) bytesRead;
            }
            signalActivity("server received receiver's IP -> " + handshakeMessage);

            if (handshakeMessage == null) {
                throw new IOException("receiver's IP was null");
            }

            if (!isCorrect(handshakeMessage)) {
                throw new IOException("incorrect receiver's IP");
            }
            targetIp = handshakeMessage;

            handshakeMessage = "";
            while ((bytesRead = inputStream.read()) != '\n') {
                handshakeMessage += (char) bytesRead;
            }
            fileName = handshakeMessage;
            signalActivity("server received file name -> " + fileName);

            if (handshakeMessage == null) {
                throw new IOException("File name was null");
            }

            handshakeMessage = WifiPeerConst.SERVER_READY + "\n";
            outputStream.write(handshakeMessage.getBytes());
            outputStream.flush();
            signalActivity("server sending " + handshakeMessage);

            signalActivity("------ Handshake complete, getting file: " + fileName + " ------");
            //handshake done !

            File file = new File(saveLocation, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            while (true) {
                bytesRead = inputStream.read(buffer, 0, buffer.length);
                if (bytesRead == -1)
                    break;
                bufferedOutputStream.write(buffer, 0, bytesRead);
                bufferedOutputStream.flush();
            }

            outputStream.close();
            inputStream.close();
            fileOutputStream.close();
            bufferedOutputStream.close();
            socket.close();

            signalActivity("------ File Transfer Complete, saved as: " + fileName + " ------");

        } catch (IOException e) {
            signalActivity(e.getMessage());


        } catch (Exception e) {
            signalActivity(e.getMessage());

        }
        if (!routingRequired)
            serverResult.send(port, null);
        else {
            prepareForRouting(saveLocation + "/" + fileName, targetIp);
        }
    }

    private void prepareForRouting(String filePath, String targetIp) {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("targetIp", targetIp);
        serverResult.send(WifiPeerConst.ROUTE, bundle);
    }

    private boolean isCorrect(String inetAddressString) {
        if (!wifiInfo.isGroupOwner) { // if i am a client then no need for routing, just receive the data...
            routingRequired = false;
            return true;
        } else { // if GO

            if (wifiInfo.groupOwnerAddress.getHostAddress().equals(inetAddressString)) { // if receiver's addr is equal to me
                routingRequired = false;
                return true;
            } else if (RouteTable.hasEntry(inetAddressString)) { // if receiver's addr is in table
                routingRequired = true;
                return true;
            } else {  // not present in the table
                routingRequired = false;
                return false;
            }
        }
    }

    public void signalActivity(String message) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        serverResult.send(port, bundle);
    }


    public void onDestroy() {
        stopSelf();
    }

}