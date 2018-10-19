package ir.shahabazimi.mycontacts.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MyDao {



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(Contacts contacts);

    @Query("update Contacts set name=:name, number=:number, email=:email, birthday=:bday where id=:id")
    void updateContact(String name,String number, String email,String bday, int id);

    @Query("Delete FROM Contacts where id=:id")
    void deleteContact(int id);

    @Query("select * FROM Contacts where id=:id")
    Contacts getContact(int id);



    @Query("SELECT * FROM Contacts  ORDER BY name ASC")
    List<Contacts> getContacts();

    @Query("SELECT Count(*) FROM Contacts Where name=:name And number=:number  ORDER BY name ASC")
    int getContactCount(String name, String number);

}
