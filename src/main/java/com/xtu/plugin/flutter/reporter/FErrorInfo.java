package com.xtu.plugin.flutter.reporter;

import java.io.Serializable;

public class FErrorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public String title;
    public String body;

    public FErrorInfo(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
