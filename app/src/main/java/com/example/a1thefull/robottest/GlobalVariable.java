package com.example.a1thefull.robottest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GlobalVariable {
    public final static String SERVERURL = "http://35.234.43.199:8080/";
    public static String IDByANDROID = "";
    public static String IDOfOPPONENT = "";
    public static String DIRECTORY_NAME = "";
    public static ArrayList<RecordFile> myRecordFiles = new ArrayList<>();
    public static ArrayList<RecordFile> yourRecordFiles = new ArrayList<>();
    public static boolean isThreadEnd1 = false;
    public static boolean isThreadEnd2 = false;

    public static void getFilesFromServer(final Context context, final String id, final int recordFileNum) {
        Thread back = new Thread(new Runnable() {
            @Override
            public void run() {
                int num = 0;
                if(recordFileNum == 1)
                    yourRecordFiles.clear();
                else
                    myRecordFiles.clear();
                String line = "";
                URL url;
                HttpURLConnection conn = null;
                try {
                    url = new URL("http://35.234.43.199:8080/getVoice/" + id + num);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    conn.connect();

                    InputStream is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    line = rd.readLine();

                    while (line.indexOf("[{") != -1) {
                        // JSON parsing
                        JSONParsing(context, line);
                        num++;
                        url = new URL(GlobalVariable.SERVERURL + "/getVoice/" + id + num);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        line = rd.readLine();
                    }
                    rd.close();
                } catch (Exception e) {
                } finally {
                    conn.disconnect();
                }
                if(recordFileNum == 1)
                    isThreadEnd1 = true;
                if(recordFileNum == 2)
                    isThreadEnd2 = true;
            }
        });
        back.start();
    }

    public static void JSONParsing(final Context context, String line) throws IOException {
        String startStr = "[";
        String endStr = "]";
        String jsonStr = line.substring(line.indexOf(startStr) + 1, line.indexOf(endStr));
        System.out.println(jsonStr);
        JsonParser parser = new JsonParser();

        Object json = parser.parse(jsonStr);
        JsonObject jsonObj = (JsonObject) json;

        long datetime = Long.valueOf(jsonObj.get("DATETIME").toString());
        String receiver = String.valueOf(jsonObj.get("RECEIVER").toString()).replaceAll("\"", "");
        String sender = String.valueOf(jsonObj.get("SENDER").toString()).replaceAll("\"", "");

        if(sender.equals(IDOfOPPONENT) && receiver.equals(IDByANDROID)){
            Date date = new Date(datetime);
            String file_url = String.valueOf(jsonObj.get("FILE_URL"));
            file_url = file_url.replaceAll("\"", "");

//        String file_path = DIRECTORY_NAME + "/" + file_name;

//        URL url2 = new URL(file_url);
//        BufferedInputStream in = new BufferedInputStream(url2.openStream());
//        FileOutputStream fileOutputStream = new FileOutputStream(file_path);
//        byte dataBuffer[] = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1)
//            fileOutputStream.write(dataBuffer, 0, bytesRead);
            MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file_url));
            int duration = mediaPlayer.getDuration();
            RecordFile tempRecordFile = new RecordFile(datetime, "your", file_url, duration);
            yourRecordFiles.add(tempRecordFile);
        } else if(receiver.equals(IDOfOPPONENT) && sender.equals(IDByANDROID)){
            Date date = new Date(datetime);
            String file_url = String.valueOf(jsonObj.get("FILE_URL"));
            file_url = file_url.replaceAll("\"", "");

            MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file_url));
            int duration = mediaPlayer.getDuration();
            RecordFile tempRecordFile = new RecordFile(datetime,"my", file_url, duration);
            myRecordFiles.add(tempRecordFile);
        }
    }
    static boolean isYes;
    public static boolean isGetFile(final Context context, final String id, final int num) {
        isYes = false;
        Thread back = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection conn = null;
                String line;
                try {
                    url = new URL("http://35.234.43.199:8080/getVoice/" + id + num);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(200 * 100);
                    conn.setReadTimeout(200 * 100);
                    conn.setRequestMethod("GET");

                    conn.connect();

                    InputStream is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    line = rd.readLine();
                    if(line.indexOf("[{") != -1) {
                        isYes = true;
                        JSONParsing(context, line);
                    }

                    rd.close();
                } catch (Exception e) {
                } finally {
                    conn.disconnect();
                }
            }
        });
        back.start();
        try {
            back.join();
        } catch (Exception e){}

        return isYes;
    }
}
