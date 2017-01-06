package project.com.share_me.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import project.com.share_me.route.RouteTable;
import project.com.share_me.WifiPeerConst;

public class ClientService extends IntentService {

    private boolean serviceEnabled;

    private int port;
    private File fileToSend;
    private ResultReceiver clientResult;
    private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;

    public ClientService() {
        super("ClientService");
        serviceEnabled = true;

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        fileToSend = (File) intent.getExtras().get("fileToSend");
        clientResult = (ResultReceiver) intent.getExtras().get("clientResult");
        InetAddress targetIP = (InetAddress) intent.getExtras().get("targetDeviceIP");
        InetAddress destDeviceIP = (InetAddress) intent.getExtras().get("destDeviceIP");
        targetDevice = (WifiP2pDevice) intent.getExtras().get("wifiP2pDevice");
        wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");

        signalActivity("Transfering file " + fileToSend.getName() + " to " + targetIP + " on TCP Port: " + port);
        RouteTable.setRouteTable(targetDevice.deviceName, targetIP);

        Socket clientSocket = null;

        try {
            byte[] buffer = new byte[4096];
            String handshakeMessage = "";
            int bytesRead;

            clientSocket = new Socket(targetIP, port);
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();

            //Client-Server handshake
            signalActivity("------ " + WifiPeerConst.START_HANDSHAKE + " ------");

            handshakeMessage = WifiPeerConst.CLIENT_HELLO + "\n";
            outputStream.write(handshakeMessage.getBytes());
            outputStream.flush();
            signalActivity("client sending " + handshakeMessage);

            handshakeMessage = "";
            while ((bytesRead = inputStream.read()) != '\n') {
                handshakeMessage += (char) bytesRead;
            }
            signalActivity("client received " + handshakeMessage);

            if (!handshakeMessage.equals(WifiPeerConst.SERVER_HELLO)) {
                throw new IOException("Invalid WDFT protocol message");
            }

            handshakeMessage = destDeviceIP.getHostAddress() + "\n";
            outputStream.write(handshakeMessage.getBytes());
            outputStream.flush();
            signalActivity("client sending receiver's IP -> " + handshakeMessage);

            handshakeMessage = fileToSend.getName() + "\n";
            outputStream.write(handshakeMessage.getBytes());
            outputStream.flush();
            signalActivity("client sending file name -> " + handshakeMessage);

            handshakeMessage = "";
            while ((bytesRead = inputStream.read()) != '\n') {
                handshakeMessage += (char) bytesRead;
            }
            signalActivity("client received " + handshakeMessage);

            if (!handshakeMessage.equals(WifiPeerConst.SERVER_READY)) {
                throw new IOException("Invalid WDFT protocol message");
            }

            signalActivity("------ Handshake complete, sending file: " + fileToSend.getName() + " ------");
            //Handshake complete, start file transfer

            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            while (true) {
                bytesRead = bufferedInputStream.read(buffer, 0, buffer.length);
                if (bytesRead == -1)
                    break;
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }


            fileInputStream.close();
            bufferedInputStream.close();
            inputStream.close();
            outputStream.close();
            clientSocket.close();

            signalActivity("------ File Transfer Complete, sent file: " + fileToSend.getName() + "------");

        } catch (IOException e) {
            signalActivity(e.getMessage());
        } catch (Exception e) {
            signalActivity(e.getMessage());
        }
        clientResult.send(port, null);
    }

    public void signalActivity(String message) {
        Bundle b = new Bundle();
        b.putString("message", message);
        clientResult.send(port, b);
    }

    public void onDestroy() {
        serviceEnabled = false;

        //Signal that the service was stopped
        //serverResult.send(port, new Bundle());

        stopSelf();
    }

}