package com.akb.seetalk.Notifications;

public class Sender {
    public Data notification;
    public String to;

    public Sender(Data notification, String to) {
        this.notification = notification;
        this.to = to;
    }
}
