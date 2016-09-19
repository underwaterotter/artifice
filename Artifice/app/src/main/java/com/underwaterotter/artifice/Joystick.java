package com.underwaterotter.artifice;

import android.graphics.Color;

import com.underwaterotter.ceto.Image;
import com.underwaterotter.ceto.ui.CirclePad;
import com.underwaterotter.cetoinput.Motions;
import com.underwaterotter.artifice.entities.main.CharController;
import com.underwaterotter.artifice.world.Assets;
import com.underwaterotter.glesutils.TextureCache;
import com.underwaterotter.math.Vector2;

public class Joystick extends CirclePad {

    public static final String JOY_X = "joy_x";
    public static final String JOY_Y = "joy_y";

    public static final int SIZE_R = 16;
    public static final int SIZE_N = 8;

    private Image joystick;
    private Image nob;

    private Vector2 lastPoint;
    private boolean initialDrag = false;

    public Joystick(){
        super();

        resize(SIZE_R);
    }

    @Override
    public void createContent(){
        super.createContent();

        joystick = new Image(Assets.JOY);
        add(joystick);

        nob = new Image(TextureCache.createCircle(SIZE_R / 2, Color.GRAY, true));
        add(nob);
    }

    @Override
    public void updateHitbox(){
        super.updateHitbox();

        nob.pos.x = x + SIZE_R - (nob.width / 2);
        nob.pos.y = y + SIZE_R - (nob.height / 2);

        joystick.pos.x = x;
        joystick.pos.y = y;
    }

    public double angle(Motions.Point p){
        Vector2 pos = camera().screenToCamera((int)p.endPos.x, (int)p.endPos.y);
        pos.set(pos.x - center().x, pos.y - center().y);
        Vector2 refPoint = new Vector2(SIZE_R, 0);

        double angle = Math.toDegrees(Math.atan2(refPoint.x, refPoint.y) - Math.atan2(pos.x, pos.y));
        if (angle < 0) angle += 2 * Math.PI;

        return angle;
    }

    protected void onLongTouch(Motions.Point p){

    }

    protected void onTouch(Motions.Point p){

    }

    protected void onRelease(Motions.Point p){
        //recenter the nob
        Vector2 lcJoy = position();
        nob.position(lcJoy.x + SIZE_R - (nob.width / 2), lcJoy.y + SIZE_R - (nob.height / 2), 0);

        initialDrag = false;

        CharController.setSpeed(0);
        CharController.setAction("none");
    }

    protected void onDragged(Motions.Point p){
        Vector2 pos = camera().screenToCamera((int)p.endPos.x, (int)p.endPos.y);
        CharController.setVelocity((float)angle(p));

        if(initialDrag) {

            if(joystick.center().distance(pos) > SIZE_R){
                nob.position((float)Math.cos(Math.toRadians(angle(p))) * SIZE_R + joystick.center().x - (nob.width / 2),
                        (float)Math.sin(Math.toRadians(angle(p))) * SIZE_R + joystick.center().y - (nob.height / 2), 0);
                CharController.setSpeed(1.0f);
            } else {
                nob.position(pos.x - SIZE_N, pos.y - SIZE_N, 0);
                CharController.setSpeed(joystick.center().distance(pos) / (SIZE_R * 2));
            }

            lastPoint.set(pos);

        } else {
            initialDrag = true;
            lastPoint =  pos;
        }
    }
}
