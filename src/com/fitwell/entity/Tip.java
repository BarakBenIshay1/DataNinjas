package com.fitwell.entity;

public class Tip {

    private String content;

    public Tip(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
