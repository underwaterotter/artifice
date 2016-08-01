package com.underwaterotter.artifice.world.generation;

import com.underwaterotter.artifice.entities.MobMapper;
import com.underwaterotter.artifice.entities.items.ItemMapper;
import com.underwaterotter.artifice.world.AnimatedTilemap;
import com.underwaterotter.artifice.world.Terrain;
import com.underwaterotter.artifice.world.WorldTilemap;
import com.underwaterotter.ceto.Group;
import com.underwaterotter.utils.Block;
import com.underwaterotter.utils.Storable;

import java.util.Arrays;

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

    public int mapSizeW = 20;
    public int mapSizeH = 20;

    public int safeSizeW = mapSizeW;
    public int safeSizeH = mapSizeH + SAFE_OFFSET;

    public int mapLength = mapSizeW * mapSizeH;
    public int safeLength = safeSizeW * safeSizeH;

    //Surrounding cells index
    //0 1 2
    //3 C 4
    //5 6 7
    public int[] SURROUNDING_CELLS = {-safeSizeW - 1, -safeSizeW, -safeSizeW + 1,
                                             -1, + 1,
                                             safeSizeW - 1, safeSizeW, safeSizeW + 1 };

    public boolean isUnderground = false;

    private ItemMapper itemMapper;
    private MobMapper mobMapper;

    protected int[] map;
    protected int[] foremap;
    protected int[] watermap;

    Group tilemaps;
    protected AnimatedTilemap tilemap;
    protected AnimatedTilemap foretilemap;
    protected AnimatedTilemap watertilemap;

    Group controllers;

    private boolean[] explored;
    private boolean[] passable;
    private boolean[] climbable;
    private boolean[] flammable;
    private boolean[] unstable;

    public void init(){

        itemMapper = new ItemMapper();
        mobMapper = new MobMapper();

        map = new int[safeLength];
        foremap = new int[safeLength];
        watermap = new int[safeLength];

        explored = new boolean[safeLength];
        Arrays.fill(explored, false);

        passable = new boolean[safeLength];
        climbable = new boolean[safeLength];
        flammable = new boolean[safeLength];
        unstable = new boolean[safeLength];

        tilemaps = new Group();
        add(tilemaps);

        tilemaps.add(watertilemap);
        tilemaps.add(tilemap);
        tilemaps.add(foretilemap);

        controllers = new Group();
        add(controllers);

        controllers.add(itemMapper);
        controllers.add(mobMapper);

        generate();
        decorate();

        prespawnMobs();
        prespawnItems();
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
        mobMapper.destroyMob();
        //clear all arrays
    }

    public int[] getMapData(WorldTilemap.TILEMAP type){
        if(type == WorldTilemap.TILEMAP.WATER)
            return watermap;
        else if(type == WorldTilemap.TILEMAP.LAND)
            return map;
        else
            return foremap;
    }

    public boolean isPassable(int index){
        return passable[index];
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
