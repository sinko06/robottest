package com.example.a1thefull.robottest;

import java.io.Serializable;

public class RecordFile implements Serializable {
    long time;
    String picture;
    String uri;
    int duration;

    public RecordFile(long time, String picture, String uri, int duration) {
        this.time = time;
        this.picture = picture;
        this.uri = uri;
        this.duration = duration;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
