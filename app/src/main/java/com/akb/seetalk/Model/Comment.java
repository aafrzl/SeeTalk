package com.akb.seetalk.Model;

public class Comment {

    private String comment;
    private String publisher;
    private String commentid;
    private String time;

    public Comment(String comment, String publisher, String commentid, String time) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.time = time;
    }

    public Comment() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }
}
