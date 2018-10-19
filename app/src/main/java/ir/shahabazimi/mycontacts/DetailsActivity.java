package ir.shahabazimi.mycontacts;

import android.app.DatePickerDialog;
import android.arch.persistence.room.Room;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ir.shahabazimi.mycontacts.database.Contacts;
import ir.shahabazimi.mycontacts.database.MyDatabase;

public class DetailsActivity extends AppCompatActivity {

    @BindView(R.id.details_name)  EditText name;
    @BindView(R.id.details_phone) EditText phone;
    @BindView(R.id.details_email) EditText email;
    @BindView(R.id.details_bday)  TextView bday;

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener datePicker;

    private MyDatabase myDatabase;
    private int id;
    private String where;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        init();
    }

    private void init(){
        ButterKnife.bind(this);

        myCalendar = Calendar.getInstance();

        myDatabase = Room.databaseBuilder(this,MyDatabase.class,"ContactsDB").allowMainThreadQueries().build();
        getContactInfo();
        onClicks();


    }
    private void getContactInfo(){
        Bundle bundle = getIntent().getExtras();
        where =bundle.getString("where","");
        if (where.equals("info")){
            id = bundle.getInt("id");
            Contacts contacts = myDatabase.myDao().getContact(id);
            name.setText(contacts.getName());
            phone.setText(contacts.getNumber());
            if(contacts.getEmail()==null)
                email.setText("");
            else
                email.setText(contacts.getEmail());
            if(contacts.getBirthday()==null)
                bday.setText("");
            else
                bday.setText(contacts.getBirthday());

        }
    }

    private void onClicks(){
        datePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
    }

    @OnClick(R.id.details_date)
     void dateClick(){
        new DatePickerDialog(DetailsActivity.this, datePicker, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.details_cancel)
     void cancelClick(){
        DetailsActivity.this.finish();
    }



    @OnClick(R.id.details_save)
     void saveClick(){
        String n = name.getText().toString();
        String p = phone.getText().toString();
        String e = email.getText().toString();
        String b = bday.getText().toString();
        if(n.isEmpty() || p.isEmpty()){
            Toast.makeText(DetailsActivity.this,getString(R.string.fill_toast),Toast.LENGTH_LONG).show();
        }else{
            if(where!=null && where.equals("info")){
                myDatabase.myDao().updateContact(n,p,e,b,id);
                DetailsActivity.this.finish();
                return;
            }
            Contacts contacts = new Contacts();
            contacts.setName(n);
            contacts.setNumber(p);
            contacts.setBirthday(b);
            contacts.setEmail(e);
            myDatabase.myDao().addUser(contacts);
            DetailsActivity.this.finish();
        }
    }
    private void updateLabel() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        bday.setText(sdf.format(myCalendar.getTime()));
    }
}
