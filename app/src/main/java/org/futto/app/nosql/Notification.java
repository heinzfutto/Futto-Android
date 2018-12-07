package org.futto.app.nosql;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Notification {

    private String userId;
    private Double creationDate;
    private String content;
    private Boolean isReaded;
    private String noteId;
    private String title;

    public Notification(String userId, Double creationDate, String content, Boolean isReaded, String noteId, String title) {
        this.userId = userId;
        this.creationDate = creationDate;
        this.content = content;
        this.isReaded = isReaded;
        this.noteId = noteId;
        this.title = title;
    }



    public Double getCreationDate() {
        return creationDate;
    }

    public String getContent() {
        return content;
    }

    public Boolean getReaded() {
        return isReaded;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getTitle() {
        return title;
    }

}
