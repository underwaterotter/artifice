package com.underwaterotter.artifice.scenes;

import android.util.Log;

import com.underwaterotter.artifice.AnimatedTilemap;
import com.underwaterotter.artifice.Artifice;
import com.underwaterotter.artifice.Joystick;
import com.underwaterotter.artifice.world.Assets;
import com.underwaterotter.artifice.world.Terrain;
import com.underwaterotter.ceto.Group;
import com.underwaterotter.artifice.WorldTilemap;
import com.underwaterotter.artifice.entities.Mob;
import com.underwaterotter.artifice.entities.items.Item;
import com.underwaterotter.artifice.entities.main.Char;
import com.underwaterotter.ceto.Image;
import com.underwaterotter.ceto.Text;
import com.underwaterotter.ceto.ui.Button;

public class GameScene extends UIScene {

    public static GameScene scene;
    public AnimatedTilemap watermap;
    public WorldTilemap tilemap;

    public Char player;

    private Group world;
    private Group liquid;
    private Group weather;
    private Group mobs;
    private Group items;
    private Group fauna;
    private Group pouches;

    public void create(){
        super.create();

        scene = this;
        watermap = new AnimatedTilemap(Artifice.level.tiles(), 2) {

            @Override
            protected void setTileAnimations() {
                int[] dwater = {Terrain.DWATER_1, Terrain.DWATER_2, Terrain.DWATER_3, Terrain.DWATER_2};
                tileAnimations.put(Terrain.DWATER_1, dwater);
                tileAnimations.put(Terrain.DWATER_2, dwater);
                tileAnimations.put(Terrain.DWATER_3, dwater);

                int[] twater = {Terrain.TWATER_1, Terrain.TWATER_2, Terrain.TWATER_3, Terrain.TWATER_2};
                tileAnimations.put(Terrain.TWATER_1, twater);
                tileAnimations.put(Terrain.TWATER_2, twater);
                tileAnimations.put(Terrain.TWATER_3, twater);

                frames = 4;
            }

        };

        tilemap = new AnimatedTilemap(Artifice.level.tiles(), 2){

            @Override
            protected  void setTileAnimations(){
            }
        };


        //heightmap = new WorldTilemap(Artifice.level.heightmap);

        player = new Char();
        //pre-init level setup
        if(Artifice.depth < 0){
            Artifice.level.isUnderground = true;
        }
        Artifice.level.init();
        watermap.setMap(Artifice.level.watermap);
        tilemap.setMap(Artifice.level.map);

        Artifice.level.mm.add(player);

        world = new Group();
        add(world);

        liquid = new Group();
        add(liquid);

        weather = new Group();
        add(weather);

        world.add(tilemap);
        world.add(watermap);
        world.add(liquid);
        world.add(weather);

        mobs = new Group();

        for(Mob m : Artifice.level.mm.mobs.values()){
            addMob(m);
        }
        add(mobs);

        items = new Group();

        for(Item i : Artifice.level.im.items.values()){
            addItem(i);
        }
        add(items);

        fauna = new Group();
        add(fauna);

        pouches = new Group();
        add(pouches);

        Joystick joy = new Joystick();
        joy.camera = uiCamera;
        joy.position(Artifice.settings.getInt(Joystick.JOY_X, 20),
                Artifice.settings.getInt(Joystick.JOY_Y, 130));
        add(joy);
    }

    @Override
    public void destroy(){

        scene = null;

        super.destroy();
    }

    @Override
    public synchronized void update(){
        super.update();

        watermap.setMap(Artifice.level.watermap);
        tilemap.setMap(Artifice.level.map);
        //heightmap.setMap(Artifice.level.heightmap);

        mobs.update();
        items.update();
    }

    public void addMob(Mob mob){
        mobs.add(mob);
        mob.sprite.visible = mob.isVisible();
        mob.sprite.setMob(mob);
    }

    public void addItem(Item item){
        items.add(item);
        item.sprite.visible = item.isVisible();
    }

    public static void add(Mob mob){
        Artifice.level.mm.add(mob);
        scene.addMob(mob);
    }

    public static void add(Item item){
        Artifice.level.im.add(item);
        scene.addItem(item);
    }

    public static void exploreCell(int cell){
        if(scene != null){
            scene.tilemap.explore(cell);
        }
    }


    private static class DebugButton extends Button {

        public static final int SIZE_W = 64;
        public static final int SIZE_H = 20;
        public static final int LEFT_PADDING = 5;

        private Image image;
        private Text label;

        public DebugButton(String text){
            super();

            image.textureRect(0, 0, SIZE_W, SIZE_H);

            this.label = createText(text, 12f); //12font
            add(label);

            resize(SIZE_W, SIZE_H);
        }

        @Override
        public void createContent(){
            super.createContent();

            image = new Image(Assets.SELECTORS);
            add(image);
        }

        @Override
        public void updateHitbox(){
            super.updateHitbox();

            label.pos.x = x + LEFT_PADDING;
            label.pos.y = y - 4 + (image.height() / 2);

            image.pos.x = x;
            image.pos.y = y;
        }

        protected void onTouch() {
            Log.v("DEBUG_TOUCH", "Lightened button.");
        }
    }
}
