package com.underwaterotter.artifice.scenes;

import com.underwaterotter.artifice.uibuttons.game.CharBscButton;
import com.underwaterotter.artifice.uibuttons.game.CharSpcButton;
import com.underwaterotter.artifice.Artifice;
import com.underwaterotter.artifice.uibuttons.game.Joystick;
import com.underwaterotter.artifice.uibuttons.game.PauseButton;
import com.underwaterotter.artifice.windows.Window;
import com.underwaterotter.artifice.world.generation.Level;
import com.underwaterotter.ceto.Group;
import com.underwaterotter.artifice.entities.mobs.main.Char;

public class GameScene extends UIScene {

    public static GameScene scene;

    private Level currentLevel;

    private Char player;

    private Group world;
    private Group weather;
    private Group fauna;
    private Group pouches;

    public void create(){
        super.create();

        scene = this;
        currentLevel = Artifice.getLevel();

        player = new Char();
        //pre-init currentLevel setup
        if(Artifice.getDepth() < 0){
            currentLevel.isUnderground = true;
        }
        currentLevel.init();
        currentLevel.addMob(player);

        world = new Group();
        add(world);

        weather = new Group();
        add(weather);

        world.add(currentLevel);
        world.add(weather);

        fauna = new Group();
        add(fauna);

        pouches = new Group();
        add(pouches);

        Joystick joy = new Joystick();
        joy.setCamera(uiCamera);
        joy.position(Artifice.settings.getInt(Joystick.JOY_X, 20),
                Artifice.settings.getInt(Joystick.JOY_Y, 130));
        add(joy);

        CharBscButton bscButton = new CharBscButton();
        bscButton.setCamera(uiCamera);
        bscButton.position(240, 130);
        add(bscButton);

        CharSpcButton spcButton = new CharSpcButton();
        spcButton.setCamera(uiCamera);
        spcButton.position(255, 100);
        add(spcButton);

        PauseButton pauseButton = new PauseButton();
        pauseButton.setCamera(uiCamera);
        pauseButton.position(270, 4);
        add(pauseButton);
    }

    @Override
    public void destroy(){
        super.destroy();
        scene = null;
    }

    @Override
    public synchronized void update(){
        super.update();
    }

    public Char getPlayer(){
        return player;
    }

    public void openWindow(Window window) {
        add(window);
    }

    public void closeWindow(Window window) {
        remove(window);
    }
}
