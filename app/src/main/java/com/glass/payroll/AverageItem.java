package com.glass.payroll;

public class AverageItem {

    String title = "";
    String extra = "";
    String content = "";

    public AverageItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public AverageItem(String title, String extra, String content) {
        this.title = title;
        this.extra = extra;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
