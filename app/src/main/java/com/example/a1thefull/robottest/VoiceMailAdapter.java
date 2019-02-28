package com.example.a1thefull.robottest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.URI;

public class VoiceMailAdapter extends RecyclerView.Adapter<ViewHolder> {
    List<RecordFile> items;
    Context context;
    boolean isPlaying = false;
    MediaPlayer player;
    ViewHolder viewHolder;
    int nowRow;
    SeekBar sb = null;
    int mpDuration;
    int thousands;

    public VoiceMailAdapter(List<RecordFile> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Context를 부모로 부터 받아와서
        context = viewGroup.getContext();
        //받은 Context를 기반으로 LayoutInflater를 생성
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //생성된 LayoutInflater로 어떤 Layout을 가져와서 어떻게 View를 그릴지 결정
        View studentView = layoutInflater.inflate(R.layout.voicemail_row, viewGroup, false);
        //View 생성 후, 이 View를 관리하기위한 ViewHolder를 생성
        ViewHolder viewHolder = new ViewHolder(studentView);
        //생성된 ViewHolder를 OnBindViewHolder로 넘겨줌
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        viewHolder = holder;
        final RecordFile nowItem = items.get(position);
        final ImageView play = holder.play;
        final TextView tv_duration = holder.tv_duration;
        final ImageView profile = holder.profile;

//        try {
//            if(nowItem.getUri() != null)
//            {
//                Uri uri = Uri.parse(nowItem.getUri());
//                holder.profile.setImageURI(uri);
//            }
//        } catch (Exception uriException){}
        if (nowItem.getPicture().equals("your")) {
            holder.profile.setImageResource(R.drawable.person_blue);
        }
        else{
            holder.profile.setImageResource(R.drawable.person_black);
        }

        long dateTime = nowItem.getTime();
        SimpleDateFormat fileDate = new SimpleDateFormat("yyyy년 MM월 yy일 E요일");
        SimpleDateFormat fileTime = new SimpleDateFormat("hh시 mm분");
        String dateText = fileDate.format(dateTime);
        String timeText = fileTime.format(dateTime);

        holder.tv_date.setText(dateText);
        holder.tv_time.setText(timeText);
        String durationText = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(nowItem.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(nowItem.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(nowItem.getDuration())));
        holder.tv_duration.setText(durationText);
        final SeekBar seekBar = holder.seekBar;

        // play button 이벤트 처리
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    try {
                        play.setImageResource(R.drawable.playstop);
                        player = new MediaPlayer();
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(items.get(position).getUri());
                        player.prepare();
                        player.start();
                        nowRow = position;
                        seekBar.setMax(player.getDuration());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mpDuration = items.get(position).getDuration();
                                thousands = 1000;
                                while (isPlaying) {
                                    try {
                                        final int curTime = player.getCurrentPosition();
                                        seekBar.setProgress(curTime);
                                        if (curTime >= thousands) {
                                            final String remainingTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(mpDuration - thousands),
                                                    TimeUnit.MILLISECONDS.toSeconds(mpDuration - thousands) -
                                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mpDuration - thousands)));
                                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tv_duration.setText(remainingTime);
                                                }
                                            });
                                            thousands += 1000;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                seekBar.setProgress(0);
                                ((MainActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        play.setImageResource(R.drawable.playstart);
                                        String durationText = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(nowItem.getDuration()),
                                                TimeUnit.MILLISECONDS.toSeconds(nowItem.getDuration()) -
                                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(nowItem.getDuration())));
                                        tv_duration.setText(durationText);
                                    }
                                });
                            }
                        }).start();
                    } catch (Exception e) {
                    }
                } else {
                    if (nowRow == position) {
                        isPlaying = false;
                        play.setImageResource(R.drawable.playstart);
                        player.stop();
                        player.release();
                        if (seekBar != null) {
                            seekBar.setProgress(0);
                        }
                    } else {
                        Toast.makeText(context, "이미 파일이 재생중입니다. 중지 후 실행하세요", Toast.LENGTH_SHORT).show();
                    }
                }

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isPlaying = false;
                        player.stop();
                        player.release();
                    }
                });
            }
        });

        // seekbar 이벤트 처리
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (seekBar.getMax() == progress) {
                        if (isPlaying) {
                            isPlaying = false;
                            player.stop();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (isPlaying) {
                        player.pause();
                        isPlaying = false;
                    }
                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                    if (player != null) {
                        isPlaying = true;
                        int ttt = seekBar.getProgress(); // 사용자가 움직여놓은 위치
                        player.seekTo(ttt);
                        player.start();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (isPlaying) {
                                    try {
                                        seekBar.setProgress(player.getCurrentPosition());
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }).start();
                    } else {
                        seekBar.setProgress(0);
                    }
                }
            });
        }
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    TextView tv_date, tv_time, tv_duration;
    ImageView play, profile;
    SeekBar seekBar;

    public ViewHolder(View itemView) {
        super(itemView);
        seekBar = (SeekBar) itemView.findViewById(R.id.seekBar);
        play = (ImageView) itemView.findViewById(R.id.imageView);
        tv_date = (TextView) itemView.findViewById(R.id.voicemail_date);
        tv_time = (TextView) itemView.findViewById(R.id.voicemail_time);
        tv_duration = (TextView) itemView.findViewById(R.id.voicemail_duration);
        profile = (ImageView) itemView.findViewById(R.id.voicemail_profile);
    }
}