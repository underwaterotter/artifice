package com.underwaterotter.ceto;

public class Article {
    public boolean active;
    public boolean visible; //fog
    public boolean awake;
    public boolean exists;

    public Group parent;

    public Camera camera;

    public Article(){
        active = true;
        visible = true;
        awake = true;
        exists = true;
    }

    public void update(){
    }

    public void draw(){
    }

    public Camera camera(){

        if (camera != null) {
            return camera;
        } else if (parent != null) {
            return parent.camera();
        } else {
            return null;
        }
    }

    public void destroy(){
        parent = null;
    }

    public void kill(){
        active = false;
        exists = false;
    }

    public void resurrect(){
        active = true;
        exists = true;
    }

    public boolean isActive(){

        if (parent == null){
            return this.active;
        } else {
            return active && parent.isActive(); //make sure the group is work
        }
    }

    public boolean isVisible(){

        if (parent == null) {
            return this.visible;
        } else {
            return visible && parent.isVisible(); //make sure the group is visible
        }
    }

    //group child functions
    public void remove(){
        parent.entities.remove(this);
    }
}
