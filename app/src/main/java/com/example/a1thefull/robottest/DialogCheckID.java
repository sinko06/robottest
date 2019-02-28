package com.example.a1thefull.robottest;

import android.app.Dialog;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class DialogCheckID {
    Context context;
    public DialogCheckID(Context context){
        this.context = context;
    }

    TextView textView;

    public void callFunction(){
        final Dialog dlg = new Dialog(context);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.activity_dialog_check_id);

        dlg.show();
        textView = (TextView) dlg.findViewById(R.id.check_id);
        textView.setText(GlobalVariable.IDByANDROID);
    }
}
