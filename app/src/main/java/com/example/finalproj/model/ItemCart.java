package com.example.finalproj.model;

import java.util.ArrayList;

public class ItemCart {

    String id;
    ArrayList<Item> arr;
    public ItemCart(String id, ArrayList<Item> arr) {
        this.id = id;
        this.arr = arr;
    }
    public ItemCart() {
    }
    public String getId() { return id; }
    public void setId(String id) {this.id = id;}
    public ArrayList<Item> getArr() {
        return arr;
    }
    public void setArr(ArrayList<Item> arr) {
        this.arr = arr;
    }

    @Override
    public String toString() {
        return "ItemCart{" +
                "arr=" + arr +
                '}';
    }
}
