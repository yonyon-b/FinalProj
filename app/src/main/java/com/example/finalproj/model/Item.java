package com.example.finalproj.model;

public class Item {
    String id;
    String name;
    boolean lost;
    String position;
    String date;
    String pic;
    String details;
    String userId;
    public Item(String name, String id, boolean lost, String position, String date, String pic, String details, String userId) {
        this.name = name;
        this.id = id;
        this.lost = lost;
        this.position = position;
        this.date = date;
        this.pic = pic;
        this.details = details;
        this.userId = userId;
    }
    public Item() {
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getUserId(){return userId; }

    public void setUserId (String userId){ this.userId = userId; }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lost=" + lost +
                ", position='" + position + '\'' +
                ", date='" + date + '\'' +
                ", pic='" + pic + '\'' +
                ", details='" + details + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
