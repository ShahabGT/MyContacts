package ir.shahabazimi.mycontacts.classes;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ir.shahabazimi.mycontacts.DetailsActivity;
import ir.shahabazimi.mycontacts.R;
import ir.shahabazimi.mycontacts.database.Contacts;
import ir.shahabazimi.mycontacts.database.MyDatabase;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private Activity context;
    private List<Contacts> contactsList,filterlist;
    private MyDatabase myDatabase;



    public ContactsAdapter(Activity context,List<Contacts> contactsList){
        this.contactsList=contactsList;
        this.context =context;
        filterlist=new ArrayList<>();
        filterlist.addAll(contactsList);
        myDatabase = Room.databaseBuilder(context,MyDatabase.class,"ContactsDB").allowMainThreadQueries().build();

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_row,parent,false);

      return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Contacts contacts = filterlist.get(position);
        holder.name.setText(contacts.getName());
        holder.number.setText(contacts.getNumber());
        holder.id.setText(contacts.getId()+"");
        holder.namechar.setText(contacts.getName().charAt(0)+"");

    }

    @Override
    public int getItemCount() {
        return filterlist.size();
    }

    public void search (final String title){
        filterlist.clear();
        if(TextUtils.isEmpty(title))
        {
            filterlist.addAll(contactsList);

        }else{
            for(Contacts item: contactsList){
                if(item.getName().toLowerCase().contains(title.toLowerCase())){
                    filterlist.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.row_name) TextView name;
        @BindView(R.id.row_id) TextView id;
        @BindView(R.id.row_char) TextView namechar;
        @BindView(R.id.row_number) TextView number;
        @BindView(R.id.row_details) LinearLayout details;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

        }

        @OnClick(R.id.row_call)
        void callClick(){
            requestCallNumber(number.getText().toString());
        }
        @OnClick(R.id.row_info)
        void infoClick(){
            Intent intent = new Intent(context,DetailsActivity.class);
            intent.putExtra("where","info");
            intent.putExtra("id",Integer.parseInt(id.getText().toString()));
            context.startActivity(intent);
        }

        @OnClick(R.id.row_message)
        void messageClick(){
            sendMessage(number.getText().toString());
        }

        @OnLongClick(R.id.row_number)
        boolean numberLongClick(){
            ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(name.getText().toString(),number.getText().toString()+"");
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(context,context.getString(R.string.copy_toast),Toast.LENGTH_LONG).show();
            return true;
        }

        @OnClick(R.id.row_share)
        void shareClick(){
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = number.getText().toString();
            String shareSub = name.getText().toString();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_toast)));
        }

        @Override
        public void onClick(View v) {

                if(details.getVisibility()==View.GONE)
                    details.setVisibility(View.VISIBLE);
                else
                    details.setVisibility(View.GONE);




        }
        private int checkPermission(String Permission){
            int status = PackageManager.PERMISSION_DENIED;
            switch (Permission){
                case "call":
                    status = ActivityCompat.checkSelfPermission(context,Manifest.permission.CALL_PHONE);
                    break;

            }
            return status;
        }

        private void requestPermission(String Permission){
            switch (Permission){
                case "call":
                    ActivityCompat.requestPermissions(context,
                            new String[]{Manifest.permission.CALL_PHONE},
                           569
                    );
                    break;
            }

        }
        private void showExplanation(final String Permission){
            AlertDialog.Builder builder;
            AlertDialog alertDialog;
            builder = new AlertDialog.Builder(context);
            switch (Permission){
                case "call":
                    builder.setTitle(context.getString(R.string.call_permission_title));
                    builder.setMessage(context.getString(R.string.call_permission_message));
                    break;
            }
            builder.setPositiveButton(context.getString(R.string.call_permission_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission(Permission);
                }
            });
            builder.setNegativeButton(context.getString(R.string.call_permission_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialog = builder.create();
            alertDialog.show();
        }
        private void requestCallNumber(String number){
            PermissionUtil permissionUtil = new PermissionUtil(context);

            if(checkPermission("call")!=PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(context,
                        Manifest.permission.READ_CONTACTS)){
                    showExplanation("call");
                }

                else if(!permissionUtil.check("call")){
                    requestPermission("call");
                    permissionUtil.update("call");
                }else{
                    dialNumber(number);
                }
            }else{
                callNumber(number);
            }

        }
        private void callNumber(String number){
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+number));
            try {
                context.startActivity(callIntent);
            }catch (SecurityException e){

            }
        }
        private void dialNumber(String number){
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+number));
            context.startActivity(callIntent);
        }
        private void sendMessage(String number){
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:"));
            sendIntent.putExtra("address", number);
            context.startActivity(sendIntent);
        }

        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.delete_title));
            builder.setMessage(context.getString(R.string.delete_message,name.getText().toString()));
            builder.setNegativeButton(context.getString(R.string.delete_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(context.getString(R.string.delete_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myDatabase.myDao().deleteContact(Integer.parseInt(id.getText().toString()));
                    context.recreate();

                }
            });
            builder.show();
            return true;
        }
    }
}
