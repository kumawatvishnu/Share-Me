package project.com.share_me.route;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import project.com.socio_fi.R;

/**
 * Created by PRATIK GAUTAM on 31-10-2016.
 */
public class ShowRouteTable extends AppCompatActivity {
    Toolbar toolbar;
    private ListView routeListView;
    RouteTableAdapter routeTableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.route_table);

        toolbar = (Toolbar) findViewById(R.id.tabletoolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle(R.string.app_name);

        routeListView = (ListView) findViewById(R.id.route_table_list);
        routeTableAdapter = new RouteTableAdapter(this, R.layout.route_table, RouteTable.getRouteTable());
        routeListView.setAdapter(routeTableAdapter); // shwoing routing entries in the list-view
    }

    private class RouteTableAdapter extends ArrayAdapter<TableEntry> { // adapter to set the entries
        private final Context mContext;
        private List<TableEntry> tableEntryList;

        public RouteTableAdapter(Context context, int resource, ArrayList<TableEntry> tableEntryList) {
            super(context, resource, tableEntryList);
            mContext = context;
            this.tableEntryList = tableEntryList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.route_table_item, null);
            }
            final TableEntry tableEntry = tableEntryList.get(position);
            TextView route_name = (TextView) view.findViewById(R.id.route_name);
            TextView route_ip_addr = (TextView) view.findViewById(R.id.route_ip_addr);

            route_name.setText(tableEntry.name);
            route_ip_addr.setText(tableEntry.inetAddr + "");

            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
