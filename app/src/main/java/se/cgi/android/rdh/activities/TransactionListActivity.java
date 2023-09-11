package se.cgi.android.rdh.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.adapters.TransactionRecyclerViewAdapter;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.Trans;

/***
 * TransactionListActivity - Activity class with list of transactions.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class TransactionListActivity extends NonBcrActivity implements TransactionRecyclerViewAdapter.OnTransactionListener {
    //private static final String TAG = TransactionListActivity.class.getSimpleName();
    private TransactionRecyclerViewAdapter adapter;
    List<Trans> transList;
    private long numberOfRecords = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        transList = dbHelper.getAllTransList();
        numberOfRecords = transList.size();

        setTitle("Transaktioner" + " (" + String.valueOf(numberOfRecords) + ")");

        recyclerView = findViewById(R.id.rv_transList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionRecyclerViewAdapter(transList, this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.trans_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Sök transaktion");

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
    public void onTransClick(int position) {
        Intent intent = new Intent(this, TransactionAddEditActivity.class);
        intent.putExtra("action", "edit");
        intent.putExtra("trans_id",  transList.get(position).getId());
        intent.putExtra("trans_type", String.valueOf(transList.get(position).getTransType()));
        intent.putExtra("trans_work_order_id", transList.get(position).getWorkOrderId());
        intent.putExtra("trans_work_order_no", String.valueOf(transList.get(position).getWorkOrderNo()));
        intent.putExtra("trans_article_no", String.valueOf(transList.get(position).getArticleNo()));
        intent.putExtra("trans_quantity", transList.get(position).getQuantity());
        intent.putExtra("trans_date_time", String.valueOf(transList.get(position).getDateTime()));
        startActivity(intent);
    }

    // Needed to navigate correct, back to parent menu, when coming from Maintenance (Control) activity
    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
    }
}