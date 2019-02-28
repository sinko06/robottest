package com.example.a1thefull.robottest;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.roobo.core.scene.SceneHelper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.roobo.base.task.TaskStatus.finish;

public class FirstFragment extends Fragment {
    // 녹음 파일을 저장할 경로
    final private static String DIRECTORY_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WonderfullRecord";
    String nowFile = "";
    FileOutputStream outputStream;
    long nowTime;

    Button back_button, send_button;
    ImageView rec_button;

    int fileNum = 0;

    boolean isRecording = false;
    boolean isPlaying = false;
    boolean recordingTime = true;

    public FirstFragment() {
        // Required empty public constructor
    }

    View view;

    MediaRecorder mediaRecorder; //녹음을 도와주는 객체
    MediaPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_first, container, false);
        init_view();

        rec_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recordingTime){
                    record_voicemail();
                }
                else{
                    play_voicemail();
                }
            }

        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 녹음 파일 retrofit을 통해서 upload
                if(nowFile != "")
                    upload3GPFile();
            }
        });

        return view;
    }

    private void upload3GPFile() {
        fileNum = GlobalVariable.myRecordFiles.size();

        final String nowFileName = GlobalVariable.IDOfOPPONENT+fileNum;
        Log.e("nowNum", nowFileName);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("VOICEFILE", nowFileName+".3gp", RequestBody.create(MultipartBody.FORM, new File(nowFile)))
                .addFormDataPart("ID", nowFileName)
                .addFormDataPart("FILE_NAME", nowFileName+".3gp")
                .addFormDataPart("RECEIVER", GlobalVariable.IDOfOPPONENT)
                .addFormDataPart("SENDER", GlobalVariable.IDByANDROID)
                .build();

        Request request = new Request.Builder()
                .url("http://35.234.43.199:8080/setVoice/")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        String url = "http://35.234.43.199:8080/getVoice/"+ nowFileName;
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(nowFile));
        int duration = mediaPlayer.getDuration();
        GlobalVariable.myRecordFiles.add(new RecordFile(nowTime, "my", url, duration));
        FragmentManager fm = getFragmentManager();
        SecondFragment sf = (SecondFragment) fm.findFragmentById(R.id.second_fragment);
        sf.getAllFiles(sf.tempN);
        Toast.makeText(getContext(), "메시지 전송을 완료했습니다", Toast.LENGTH_SHORT).show();
        Log.e("lulu", GlobalVariable.myRecordFiles.size() + " : " + GlobalVariable.yourRecordFiles.size());
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

            }
        });
    }



//    public void uploadFile(Uri fileUri) {
//        Toast.makeText(getContext(), fileUri.getPath(), Toast.LENGTH_SHORT).show();
//        RequestBody IDpart = RequestBody.create(MultipartBody.FORM, "wonder");
//        RequestBody FILE_NAMEpart = RequestBody.create(MultipartBody.FORM, nowFile);
//        RequestBody RECEIVERpart = RequestBody.create(MultipartBody.FORM, "receiver");
//        RequestBody SENDERpart = RequestBody.create(MultipartBody.FORM, "sender");
//
//        File originalFile = new File(fileUri.getPath());
//        RequestBody filepart = RequestBody.create(
//                MediaType.parse("*"),
//                originalFile
//        );
//
//        MultipartBody.Part file = MultipartBody.Part.createFormData("data", originalFile.getName(), filepart);
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://35.234.43.199:8080/setVoice/")
//                .addConverterFactory(GsonConverterFactory.create()).build();
//        FileUploadService client = retrofit.create(FileUploadService.class);
//
//        Call<ResponseBody> call = client.upload(file, IDpart, FILE_NAMEpart, RECEIVERpart, SENDERpart);
//        call.enqueue(new Callback<ResponseBody>(){
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    public void downloadFile(){
//        // VOICEFILE / ID / FILE_NAME / RECEIVER / SENDER
//        String id = "id01";
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://35.234.43.199:8080/getVoice/"+id+"/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        FileDownloadService service = retrofit.create(FileDownloadService.class);
//
//        Call<ArrayList<JsonObject>> res = service.getListRepos(id);
//        res.enqueue(new Callback<ArrayList<JsonObject>>() {
//            @Override
//            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
//                if(response.body() != null)
//                    Toast.makeText(getContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
//                Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void record_voicemail(){
        if (!isRecording) {
            rec_button.setImageResource(R.drawable.rec_stop);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //마이크로 녹음하겠다
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //저장파일 형식 녹음파일은 3gp로 저장
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //인코딩 방식설정
            nowTime = System.currentTimeMillis();
            nowFile = DIRECTORY_NAME + "/record"+ nowTime + ".3gp";
            mediaRecorder.setOutputFile(nowFile); //경로설정

            try {
                mediaRecorder.prepare(); //녹음을 준비함 : 지금까지의 옵션에서 문제가 발생했는지 검사함
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
        }
        else{
            rec_button.setImageResource(R.drawable.rec_play);
            mediaRecorder.stop();
            mediaRecorder.release();
            recordingTime = false;
        }
        isRecording = !isRecording;
    }

    public void play_voicemail(){
        if(!isPlaying){
            try {
                rec_button.setImageResource(R.drawable.rec_stop);
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(nowFile);
                player.prepare();
            } catch (Exception e) {
            }
            player.start();
        }
        else{
            recordingTime = true;
            rec_button.setImageResource(R.drawable.rec_basic);
            if(player != null) {
                player.stop();
                player.release();
            }
        }
        isPlaying = !isPlaying;

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                recordingTime = true;
                rec_button.setImageResource(R.drawable.rec_basic);
                if(player != null) {
                    player.stop();
                    player.release();
                }
                isPlaying = false;
            }
        });
    }

    public void init_view(){
        back_button = (Button) view.findViewById(R.id.voicemail_back);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SceneHelper.switchOut();
                getActivity().finishAffinity(); // 루트 액티비티 종료
                System.runFinalization();       // 현재 작업중인 쓰레드가 종료되면 종료
                System.exit(0);         // 현재 액티비티 종료
            }
        });
        rec_button = (ImageView) view.findViewById(R.id.voicemail_rec);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            send_button = (Button) view.findViewById(R.id.voicemail_send);
        }else{
            send_button = (Button) view.findViewById(R.id.voicemail_send2);
        }
        send_button.setVisibility(View.VISIBLE);
    }
}
