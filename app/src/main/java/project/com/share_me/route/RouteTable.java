package project.com.share_me.route;

import java.net.InetAddress;
import java.util.ArrayList;

public class RouteTable {

    private static ArrayList<TableEntry> table = new ArrayList<>();

    public static void setRouteTable(String deviceName, InetAddress inetAddress) { // to store the entries
        Boolean found = false;
        TableEntry tableEntry = new TableEntry(deviceName, inetAddress);
        for (int idx = 0; idx < RouteTable.table.size(); idx++) {
            TableEntry tableRow = RouteTable.table.get(idx);
            if (tableRow.name.equals(deviceName) && tableRow.inetAddr.equals(inetAddress))
                found = true;
        }

        if (!found)
            RouteTable.table.add(tableEntry);
    }

    public static ArrayList<TableEntry> getRouteTable() {
        return table;
    } // to returnt the table

    public static Boolean hasEntry(String inetAddressString) { // to check for a particular entry
        for (int idx = 0; idx < RouteTable.table.size(); idx++) {
            TableEntry tableRow = RouteTable.table.get(idx);
            if (tableRow.inetAddr.getHostAddress().equals(inetAddressString))
                return true;
        }
        return false;
    }
}
