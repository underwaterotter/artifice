package com.underwaterotter.artifice.world.generation;

import com.underwaterotter.artifice.entities.mobs.Mob;
import com.underwaterotter.artifice.entities.mobs.MobMapper;
import com.underwaterotter.artifice.entities.items.Item;
import com.underwaterotter.artifice.entities.items.ItemMapper;
import com.underwaterotter.artifice.world.AnimatedTilemap;
import com.underwaterotter.artifice.world.Terrain;
import com.underwaterotter.artifice.world.WorldTilemap;
import com.underwaterotter.ceto.Group;
import com.underwaterotter.math.Vector3;
import com.underwaterotter.utils.Block;
import com.underwaterotter.utils.Storable;

import java.util.Arrays;
import java.util.HashSet;

public abstract class Level extends Group implements Storable {
    public static final int SAFE_OFFSET = 10;

    private static final String UNDERGROUND = "underground";
    private static final String OVERWORLD_MAP = "overworld";
    private static final String OVERWORLD_SUCCESS = "overworld_gen";
    private static final String MAP = "map";
    private static final String EXPLORED = "explored";
    private static final String PASSABLE = "passable";
    private static final String CLIMBABLE = "climbable";
    private static final String FLAMMABLE = "flammable";
    private static final String UNSTABLE = "unstable";
    private static final String HIDDEN = "hidden";

    private static boolean overworldGenerated = false;

    public static boolean isUnderground = false;

    protected int mapWidth = 30;
    protected int mapHeight = 30;

    //Surrounding cells index
    //0 1 2
    //3 C 4
    //5 6 7
    final int[] s_cells = {-mapWidth - 1, -mapWidth, -mapWidth + 1,
            -1,                  + 1,
            mapWidth - 1, mapWidth, mapWidth + 1 };


    protected Group tilemaps;
    protected AnimatedTilemap tilemap;
    protected AnimatedTilemap foretilemap;
    protected AnimatedTilemap watertilemap;

    protected int[] map;
    protected int[] foremap;
    protected int[] watermap;
    protected int[] heightmap;

    private int sfMapW = mapWidth;
    private int sfMapH = mapHeight + SAFE_OFFSET;

    private int mapLength = mapWidth * mapHeight;
    private int sfLength = sfMapW * sfMapH;

    private ItemMapper itemMapper;
    private MobMapper mobMapper;

    private HashSet<Item> items;
    private HashSet<Mob> mobs;

    private boolean[] explored;
    private boolean[] passable;
    private boolean[] climbable;
    private boolean[] flammable;
    private boolean[] unstable;

    Group controllers;
    enum MapType {WATERMAP, TILEMAP}

    public void init(){

        itemMapper = new ItemMapper();
        mobMapper = new MobMapper();

        map = new int[sfLength];
        foremap = new int[sfLength];
        watermap = new int[sfLength];
        heightmap = new int[sfLength];

        explored = new boolean[sfLength];
        Arrays.fill(explored, false);

        passable = new boolean[sfLength];
        climbable = new boolean[sfLength];
        flammable = new boolean[sfLength];
        unstable = new boolean[sfLength];

        tilemaps = new Group();
        add(tilemaps);

        //add the tilemaps when they have been generated

        controllers = new Group();
        add(controllers);

        controllers.add(itemMapper);
        controllers.add(mobMapper);
    }

    @Override
    public void saveToBlock(Block block){
        itemMapper.saveToBlock(block);
        mobMapper.saveToBlock(block);

        block.put(UNDERGROUND, isUnderground);

        block.put(OVERWORLD_SUCCESS, overworldGenerated);
        if(!isUnderground)
            block.put(OVERWORLD_MAP, map);
        else
            block.put(MAP, map);

        block.put(EXPLORED, explored);
        block.put(PASSABLE, passable);
        block.put(CLIMBABLE, climbable);
        block.put(FLAMMABLE, flammable);
        block.put(UNSTABLE, unstable);
    }

    @Override
    public void loadFromBlock(Block block){
        itemMapper.loadFromBlock(block);
        mobMapper.loadFromBlock(block);

        isUnderground = block.getBoolean(UNDERGROUND);

        overworldGenerated = block.getBoolean(OVERWORLD_SUCCESS);
        if(!isUnderground)
            map = block.getIntArray(OVERWORLD_MAP);
        else if(overworldGenerated)
            map = block.getIntArray(MAP);

        explored = block.getBooleanArray(EXPLORED);
        passable = block.getBooleanArray(PASSABLE);
        climbable = block.getBooleanArray(CLIMBABLE);
        flammable = block.getBooleanArray(FLAMMABLE);
        unstable = block.getBooleanArray(UNSTABLE);
    }

    public void destroy(){
        itemMapper.destroy();
        mobMapper.destroy();
        //clear all arrays
    }

    public int getSfLength(){
        return sfLength;
    }

    public int getSfMapW(){
        return sfMapW;
    }

    public int[] getMapData(WorldTilemap.TILEMAP type){
        if(type == WorldTilemap.TILEMAP.WATER)
            return watermap;
        else if(type == WorldTilemap.TILEMAP.LAND)
            return map;
        else
            return foremap;
    }

    public boolean isPassable(int index) {
        return passable[index];
    }

    public int getElevation(int index) {
        return heightmap[index];
    }

    public MobMapper getMobMapper() {
        return mobMapper;
    }

    public ItemMapper getItemMapper() {
        return itemMapper;
    }

    public void addMob(Mob mob){
        mobMapper.addMob(mob);
        mob.visible = mob.isVisible();
    }

    public void addItem(Item item){
        itemMapper.addItem(item);
        item.getSprite().visible = item.isVisible();
    }

    public int worldToCell(Vector3 pos) {
        return tilemap.worldToCell(pos);
    }

    public int worldToCell(int x, int y){
        return tilemap.worldToCell(x, y);
    }

    public void updateFlipData(int index, boolean setting, MapType type){
        if(type == MapType.TILEMAP)
            tilemap.updateFlipData(index, setting);
        else
            watertilemap.updateFlipData(index, setting);
    }

    public abstract void generate();

    public abstract void decorate();

    protected abstract void prespawnMobs();

    protected abstract void prespawnItems();

    public void buildFlags(){
        for(int i = 0; i < mapLength; i++){
            int flags = Terrain.flags[map[i]];
            passable[i] = (flags & Terrain.PASSABLE) != 0;
            climbable[i] = (flags & Terrain.CLIMBABLE) != 0;
            flammable[i] = (flags & Terrain.FLAMMABLE) != 0;
            unstable[i] = (flags & Terrain.UNSTABLE) != 0;
        }
    }
}
