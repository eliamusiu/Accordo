package com.example.accordo;

import androidx.room.Entity;

@Entity (tableName = "Images")
public class TextImagePost extends Post {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
