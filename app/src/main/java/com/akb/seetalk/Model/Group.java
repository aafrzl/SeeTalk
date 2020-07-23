package com.akb.seetalk.Model;

public class Group {
    private String groupId;
    private String groupTitle;
    private String groupDescription;
    private String groupIcon;
    private String timestamp;
    private String createBy;

    public Group(String groupId, String groupTitle, String groupDescription, String groupIcon, String timestamp, String createBy) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDescription = groupDescription;
        this.groupIcon = groupIcon;
        this.timestamp = timestamp;
        this.createBy = createBy;
    }

    public Group() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
