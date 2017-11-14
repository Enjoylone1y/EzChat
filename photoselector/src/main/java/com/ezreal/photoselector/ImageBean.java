package com.ezreal.photoselector;

import java.io.Serializable;

/**
 * Created by wudeng on 2017/11/7.
 */

public class ImageBean implements Serializable{

    private long id;
    private String name;
    private String path;
    private long size;
    private long date;
    private boolean isSelected = false;

    public ImageBean() {
    }

    public ImageBean(long id, String name, String path, long size, long date) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.size = size;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
