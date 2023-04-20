package cn.xiaomayo.notebook;

import android.content.Intent;

public class MainWindowListCell {

    private String title,time,summary,content;
    private Intent intent;
    int id;


    MainWindowListCell(int id,String title,String time,String summary,Intent intent){
        this.title = title;
        this.time = time;
        this.summary = summary;
        this.intent = intent;
        this.id = id;
    }


    MainWindowListCell(int id,String title,String time,Intent intent){
        this.title = title;
        this.time = time;
        this.summary = null;
        this.intent = intent;
        this.id = id;
    }



    public Intent getIntent() {
        return intent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
