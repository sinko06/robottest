package com.example.a1thefull.robottest;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {

    View view;
    TabHost tabHost;
    RecyclerView recyclerView;
    VoiceMailAdapter adapter;
    ArrayList<RecordFile> recordFiles;
    // 현재 눌려있는 탭의 번호를 저장하는 변수
    int tempN;
    // 최초 파일 다운로드가 끝나고 나면 서버를 3초씩 확인하는 thread를 시작하는 boolean 변수
    boolean initialThreadEnd = false;

    public SecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_second, container, false);

        init_listview();
        init_view();

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("Tab Spec 1"))
                    tempN = 1;
                else if (tabId.equals("Tab Spec 2"))
                    tempN = 2;
                else if (tabId.equals("Tab Spec 3"))
                    tempN = 3;
                getAllFiles(tempN);

            }
        });

        // 초기에 자신이 받은 파일들을 모두 불러오면 갱신하고 thread 종료
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        while (!GlobalVariable.isThreadEnd1) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                        }
                        GlobalVariable.isThreadEnd1 = false;
                        getAllFiles(2);
                        initialThreadEnd = true;
                    }
                });
            }
        }).start();

        // 3초 간격으로 상대방이 보낸 파일이 있는지 isGetFile을 통해서 서버에 확인
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (initialThreadEnd) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                    }
                    if (GlobalVariable.isGetFile(getContext(), GlobalVariable.IDByANDROID, GlobalVariable.yourRecordFiles.size())) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getAllFiles(tempN);
                                }
                            });
                    }
                }
            }
        }).start();

        return view;
    }

    // listView를 초기화하는 함수
    public void init_listview() {
        recyclerView = (RecyclerView) view.findViewById(R.id.voicemail_listview);
        recordFiles = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VoiceMailAdapter(recordFiles);
        recyclerView.setAdapter(adapter);
    }

    // 위젯 초기화 (tab)
    public void init_view() {
        tabHost = (TabHost) view.findViewById(R.id.tabHost1);
        tabHost.setup();

        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.voicemail_listview);
        ts1.setIndicator("모두");
        tabHost.addTab(ts1);

        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.voicemail_listview);
        ts2.setIndicator("수신");
        tabHost.addTab(ts2);

        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        TabHost.TabSpec ts3 = tabHost.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.voicemail_listview);
        ts3.setIndicator("발신");
        tabHost.addTab(ts3);

        tabHost.setCurrentTab(1);
        tempN = 2;
    }

    // 파일들을 갱신 (recyclerView를 갱신) 하는 함수
    public void getAllFiles(int tempNum) {
        recordFiles.clear();

        if (tempNum == 2) { // '수신' 탭이 눌려있을 경우
            for (int i = 0; i < GlobalVariable.yourRecordFiles.size(); i++) {
                recordFiles.add(GlobalVariable.yourRecordFiles.get(i));
            }
        } else if (tempNum == 3) {  // '발신' 탭이 눌려있을 경우
            for (int i = 0; i < GlobalVariable.myRecordFiles.size(); i++) {
                recordFiles.add(GlobalVariable.myRecordFiles.get(i));
            }
        } else if (tempNum == 1) { // '모두' 탭이 눌려있을 경우
            // 시간별 정렬을 위해서 선언하는 인덱스 변수 2개
            int myNum = GlobalVariable.myRecordFiles.size();
            int yourNum = GlobalVariable.yourRecordFiles.size();

            if (myNum == 0 || yourNum == 0) { // 보낸 파일이나 받은 파일이 한 개도 없을 경우..
                for (int i = 0; i < myNum; i++)
                    recordFiles.add(GlobalVariable.myRecordFiles.get(i));
                for (int i = 0; i < yourNum; i++)
                    recordFiles.add(GlobalVariable.yourRecordFiles.get(i));
            } else {    // 보낸 파일과 받은 파일들이 섞여 있을 경우 각 ArrayList의 인덱스를 앞에서부터 읽어서 빠른 순으로 저장
                int myIdx = 0;
                int yourIdx = 0;
                while (true) {
                    if (myIdx == myNum) {
                        if (yourIdx == yourNum) break;
                        recordFiles.add(GlobalVariable.yourRecordFiles.get(yourIdx));
                        yourIdx++;
                    } else if (yourIdx == yourNum) {
                        recordFiles.add(GlobalVariable.myRecordFiles.get(myIdx));
                        myIdx++;
                    } else {
                        if (GlobalVariable.myRecordFiles.get(myIdx).getTime() > GlobalVariable.yourRecordFiles.get(yourIdx).getTime()) {
                            recordFiles.add(GlobalVariable.yourRecordFiles.get(yourIdx));
                            yourIdx++;
                        } else {
                            recordFiles.add(GlobalVariable.myRecordFiles.get(myIdx));
                            myIdx++;
                        }
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
