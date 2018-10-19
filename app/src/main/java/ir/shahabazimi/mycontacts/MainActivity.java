package ir.shahabazimi.mycontacts;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ir.shahabazimi.mycontacts.classes.ContactsAdapter;
import ir.shahabazimi.mycontacts.classes.PermissionUtil;
import ir.shahabazimi.mycontacts.database.Contacts;
import ir.shahabazimi.mycontacts.database.MyDatabase;

public class MainActivity extends AppCompatActivity {
    private final int CONTACTS_REQUEST_CODE=156;



    private MyDatabase myDatabase;
    private ContactsAdapter adapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.search_view) MaterialSearchView searchView;
    @BindView(R.id.main_recycler) RecyclerView recyclerView;
    @BindView(R.id.main_fab) FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();



    }

    private void init(){
        myDatabase = Room.databaseBuilder(this,MyDatabase.class,"ContactsDB").allowMainThreadQueries().build();
        setSupportActionBar(toolbar);

    }

    private void onClicks(){
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.search(newText);

                return true;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 && fab.isShown())
                {
                    fab.hide();
                }
                else
                    fab.show();
            }

        });

    }

    @OnClick(R.id.main_fab)
    void fabClick(){
        Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
        intent.putExtra("where","fab");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecycler();
        onClicks();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_import:
                displayDialog();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(searchView.isSearchOpen()){
            searchView.closeSearch();
        }else
        super.onBackPressed();
    }

    private void displayDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.import_title));
        builder.setMessage(getString(R.string.import_message));
        builder.setNegativeButton(getString(R.string.import_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.import_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestReadContacts();
            }
        });
        builder.show();

    }

    private int checkPermission(String Permission){
        int status = PackageManager.PERMISSION_DENIED;
        switch (Permission){
            case "contacts":
                status = ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CONTACTS);
                break;

        }
        return status;
    }

    private void requestPermission(String Permission){
        switch (Permission){
            case "contacts":
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        CONTACTS_REQUEST_CODE
                );
                break;
        }

    }
    private void showExplanation(final String Permission){
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        builder = new AlertDialog.Builder(this);
        switch (Permission){
            case "contacts":
                builder.setTitle(getString(R.string.contact_permission_title));
                builder.setMessage(getString(R.string.contact_permission_message));
                break;
        }
        builder.setPositiveButton(getString(R.string.contact_permission_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission(Permission);
            }
        });
        builder.setNegativeButton(getString(R.string.contact_permission_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void requestReadContacts(){
        PermissionUtil permissionUtil = new PermissionUtil(MainActivity.this);

        if(checkPermission("contacts")!=PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)){
                showExplanation("contacts");
            }

            else if(!permissionUtil.check("contacts")){
                requestPermission("contacts");
                permissionUtil.update("contacts");
            }else{
                Toast.makeText(MainActivity.this,getString(R.string.permission_toast),Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);
            }
        }else{
            readContacts();
        }

    }
    private void readContacts(){
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.Contacts.DISPLAY_NAME+" ASC ");

        while (c.moveToNext()){
            String cname= c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String cnumber= c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int count =myDatabase.myDao().getContactCount(cname,cnumber.replace(" ",""));
            if(count==0){
                Contacts contact = new Contacts();
                contact.setName(cname);
                contact.setNumber(cnumber.replace(" ",""));
                myDatabase.myDao().addUser(contact);
            }

        }
        c.close();
        updateRecycler();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CONTACTS_REQUEST_CODE:
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                readContacts();
            }else{
                Toast.makeText(MainActivity.this,getString(R.string.permission_denied_toast),Toast.LENGTH_LONG).show();
            }


        }
    }

    private void updateRecycler(){
        List<Contacts> contacts = myDatabase.myDao().getContacts();
        if(contacts!=null) {
            adapter = new ContactsAdapter(this, contacts);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }

    }
}
