package ir.shahabazimi.mycontacts.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;



@Database(entities = {Contacts.class},version = 1,exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

    public abstract MyDao myDao();
}
