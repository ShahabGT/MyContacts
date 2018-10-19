package ir.shahabazimi.mycontacts.classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PermissionUtil {
    private SharedPreferences sp;

    public PermissionUtil(Activity activity){

        sp = activity.getSharedPreferences("Permission",Context.MODE_PRIVATE);
    }

    public void update(String Permission){
        switch (Permission){
            case "contacts":
                sp.edit().putBoolean("contacts",true).apply();
                break;
            case "call":
                sp.edit().putBoolean("call",true).apply();
                break;
        }
    }
    public boolean check(String Permission){
        boolean isRequested=false;
        switch (Permission){
            case "contacts":
                isRequested = sp.getBoolean("contacts",false);
                break;
            case "call":
                isRequested = sp.getBoolean("call",false);
                break;
        }
        return isRequested;
    }
}
