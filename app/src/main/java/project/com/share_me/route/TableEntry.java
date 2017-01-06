package project.com.share_me.route;

import java.net.InetAddress;

public class TableEntry {

    public String name; // client's name
    public InetAddress inetAddr; // client's IP Address

    TableEntry(String name, InetAddress inetAddr){
        this.name = name;
        this.inetAddr = inetAddr;
    }
}
