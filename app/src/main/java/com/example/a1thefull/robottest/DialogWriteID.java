package com.example.a1thefull.robottest;

import android.app.Dialog;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DialogWriteID {
    Context context;
    public DialogWriteID(Context context){
        this.context = context;
    }

    EditText write_id;
    Button okButton;
    Button cancelButton;

    public void callFunction(){
        final Dialog dlg = new Dialog(context);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.activity_dialog_write_id);

        write_id = (EditText) dlg.findViewById(R.id.write_id);
        okButton = (Button) dlg.findViewById(R.id.okButton);
        cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        write_id.setText(GlobalVariable.IDOfOPPONENT);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalVariable.IDOfOPPONENT = write_id.getText().toString();
                File file = new File(GlobalVariable.DIRECTORY_NAME + "/opponent.txt");
                FileWriter writer = null;
                try {
                    // 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
                    writer = new FileWriter(file, false);
                    writer.write(write_id.getText().toString());
                    writer.flush();
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(writer != null) writer.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }


                Toast.makeText(context, "등록되었습니다", Toast.LENGTH_SHORT).show();
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "취소했습니다", Toast.LENGTH_SHORT).show();
                dlg.dismiss();
            }
        });

        dlg.show();


    }
}