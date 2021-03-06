package com.underwaterotter.ceto;

import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.Matrix;

import com.underwaterotter.math.Vector2;
import com.underwaterotter.math.Vector3;

import java.util.Vector;

/**
 * simple color effects
 */

public class Overlay extends Article {

    protected Vector3 pos;
    protected Vector2 rOrigin;

    protected RectF boundingBox;

    protected float width;
    protected float height;

    protected float[] modelMatrix;

    //this also includes velocity (in degrees)
    //     90
    //  180-|-0/360
    //     270
    protected float angle;

    private float scaleX;
    private float scaleY;

    //color main
    //multiply
    float mr;
    float mg;
    float mb;
    float ma;

    float ar;
    float ag;
    float ab;
    float aa;

    public Overlay(){
        this(new Vector3(), 0, 0);
    }

    public Overlay(Vector3 pos, float width, float height){

        this.pos = pos;
        this.width = width;
        this.height = height;

        boundingBox = new RectF();

        rOrigin = new Vector2();
        angle = 0;

        resetColor();

        scaleX = 1.0f;
        scaleY = 1.0f;

        modelMatrix = new float[16];
    }

    @Override
    public void update(){
        boundingBox.set(pos.x, pos.y, pos.x + width, pos.y + height);
    }

    @Override
    public void draw(){
        updateMatrix();
    }

    @Override
    public boolean isVisible(){
        //check if overlay bounds intersect with those of the camera
        Camera camera = camera();

        return  pos.x + width() >= camera.distanceToFocus.x && pos.x <= camera.distanceToFocus.x + camera.viewWidth &&
                pos.y + height() >= camera.distanceToFocus.y && pos.y <= camera.distanceToFocus.y + camera.viewHeight;
    }

    //check if a point IN WORLD overlaps this overlay object
    public boolean pointOverlap(int x, int y){
        return x >= this.pos.x && x <= this.pos.x + width() &&
               y >= this.pos.y && y <= this.pos.y + height();
    }

    //check if a point ON SCREEN overlaps this overlay object
    public boolean screenPointOverlap(int x, int y){
        Camera camera = camera();
        Vector2 converted = camera.screenToCamera(x, y);
        return pointOverlap((int)converted.x, (int)converted.y);
    }

    //uniform scale
    public void scale(float s){
        scaleX = scaleY = s;
    }

    public void setScaleX(float x){
        scaleX = x;
    }

    public void setScaleY(float y){
        scaleY = y;
    }

    public void origin(Vector2 o){
        rOrigin = o;
    }

    public Vector3 getPos(){
        return pos;
    }

    public void setPos(Vector3 newPos){
        pos = newPos;
    }

    public void setPos(float x, float y, float z){
        pos = new Vector3(x, y, z);
    }

    public Vector2 center(){
        return new Vector2(pos.x + width / 2, pos.y + height / 2);
    }

    public Vector2 center(Vector2 vec2){
        pos.x = vec2.x - width / 2;
        pos.y = vec2.y - height / 2;

        return vec2;
    }

    public float width(){
        return width * scaleX;
    }

    public float getWidth(){
        return width;
    }

    public void setWidth(float w){
        width = w;
    }

    public float height(){
        return height * scaleY;
    }

    public float getHeight(){
        return height;
    }

    public void setHeight(float h){
        height = h;
    }

    public void setAngle(float a){
        angle = a;
    }

    public void setRGB_M(int color){
        setRGB_M(Color.red(color), Color.green(color), Color.blue(color));
    }

    public void setRGB_M(float r, float g, float b){
        mr = r;
        mg = g;
        mb = b;
    }

    public void setRGB_A(int color){
        setRGB_A(Color.red(color), Color.green(color), Color.blue(color));
    }

    public void setRGB_A(float r, float g, float b){
        ar = r;
        ag = g;
        ab = b;
    }

    //value between 0.0f - 1.0f inclus
    public void alpha_M(float a){
        ma = a;
    }

    public void alpha_A(float a){
        aa = a;
    }

    /**
     *
     * @param r 0-255
     * @param g 0-255
     * @param b 0-255
     * @param strength value between 0.0f - 0.5f inclusive
     */
    public void lighten(float r, float g, float b, float strength){
        ar = r / 255f;
        ag = g / 255f;
        ab = b / 255f;
        aa = strength;
    }

    public void lighten(int color, float strength){
        lighten(Color.red(color), Color.green(color), Color.blue(color), strength);
    }

    public void tint(float r, float g, float b, float strength){
        //normalize colors
        mr = r / 255f * strength;
        mg = g / 255f * strength;
        mb = b / 255f * strength;
    }

    public void tint(int color, float strength){
        tint(Color.red(color), Color.green(color), Color.blue(color), strength);
    }

    public void resetColor(){
        mr = mg = mb = ma = 1;
        ar = ag = ab = aa = 0;
    }

    public void updateMatrix(){
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y, pos.z);
        //if(angle > 0) {
         //   Matrix.rotateM(modelMatrix, 0, angle, rOrigin.x, rOrigin.y, 0);
        //}
        if (scaleX != 1f || scaleY != 1f){
            Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, 1);
        }
    }
}
