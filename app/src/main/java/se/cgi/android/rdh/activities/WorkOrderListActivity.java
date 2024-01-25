package se.cgi.android.rdh.activities;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.List;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.adapters.WorkOrderRecyclerViewAdapter;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.WorkOrder;

/***
 * WorkOrderListActivity - Activity class with list of work orders.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class WorkOrderListActivity extends NonBcrActivity  implements WorkOrderRecyclerViewAdapter.OnWorkOrderListener {
    //private static final String TAG = WorkOrderListActivity.class.getSimpleName();
    private WorkOrderRecyclerViewAdapter adapter;
    List<WorkOrder> workOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Arbetsordrar");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        workOrderList = dbHelper.getAllWorkOrderListByWorkOrderNameAndWorkOrderNo();

        recyclerView = findViewById(R.id.rv_workOrderList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkOrderRecyclerViewAdapter(workOrderList, this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.work_order_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Sök arbetsorder");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_work_order_add){
            Intent intent = new Intent(this, WorkOrderAddEditActivity.class);
            intent.putExtra("action", "add");
            intent.putExtra("comingFrom", "WorkOrderListActivity");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWorkOrderClick(int position) {
        Intent intent = new Intent(this, WorkOrderAddEditActivity.class);
        intent.putExtra("action", "edit");
        intent.putExtra("work_order_id", workOrderList.get(position).getId());
        intent.putExtra("work_order_no", String.valueOf(workOrderList.get(position).getWorkOrderNo()));
        intent.putExtra("work_order_name", String.valueOf(workOrderList.get(position).getWorkOrderName()));
        startActivity(intent);
    }

    // Needed to navigate correct, back to parent menu
    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
    }
}