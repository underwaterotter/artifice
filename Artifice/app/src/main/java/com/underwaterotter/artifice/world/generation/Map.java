package com.underwaterotter.artifice.world.generation;

import com.underwaterotter.artifice.Artifice;
import com.underwaterotter.artifice.world.Terrain;

import java.util.Arrays;

public class Map {

    //-------DEFAULT CONSTANTS-----------
    public static final double F1 = 4.0;
    public static final double F2 = 2.0;
    public static final double F3 = 1.0;
    //Elevation octaves
    public static final double EOCT1 = 1.0;
    public static final double EOCT2 = 0.5;
    public static final double EOCT3 = 0.25;

    public static final double AMP = 0.15;
    public static final double RISE = 1.30;
    public static final double DROP = 2.60;

    public static final double REDIS = 3.65;
    public static final int SMOOTH = 5;

    private static final double INTERMEDIATE_TILE = 100d;
    //------------------------------------
    private Noise hmapnoise;
    private Noise vmapnoise;

    private double[][] heightmap;
    private double[][] moisturemap;

    private int[] md;
    private int[] wm;
    private int[] hm;

    private int xsize, ysize;

    double amp;           //amplitude = 0.0 - 1.0
    double rise;          //rise = 0.0 - 2.0
    double drop;          //drop = 0.0 - 10.0
    double f1, f2, f3;    //frequencies
    double eo1, eo2, eo3; //elevation octaves
    int smooth;        //average sample size
    double redis;         //redistribution

    public Map(int w, int h, long seed,
               double amp, double rise, double drop,
               double f1, double f2, double f3,
               double eo1, double eo2, double eo3,
               int smooth, double redis){
        xsize = w;
        ysize = h;

        hmapnoise = new Noise(w, h, seed);
        vmapnoise = new Noise(w, h, seed);

        heightmap = new double[h][w];
        moisturemap = new double[h][w];

        this.amp = amp;
        this.rise = rise;
        this.drop = drop;

        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;

        this.eo1 = eo1;
        this.eo2 = eo2;
        this.eo3 = eo3;

        this.smooth = smooth;
        this.redis = redis;
    }

    public void build(){
        md = new int[xsize * (ysize + Level.SAFE_OFFSET)]; //map data
        Arrays.fill(md, Terrain.SOLID_BED);
        wm = new int[xsize * (ysize + Level.SAFE_OFFSET)]; //water
        Arrays.fill(wm, Terrain.SWATER_1);
        hm = new int[xsize * (ysize + Level.SAFE_OFFSET)]; //height data

        genHeightmap();
        normalizeHeightmap();
        genDispMap();
        cleanWaterTiles();
        cleanGrassTiles();
        cleanWallTiles();
    }

    public int[][] getDisplayMaps(){
        return new int[][] {md, wm};
    }

    public double[][] getHeightmap(){
        return heightmap;
    }

    public double[][] getMoisturemap(){
        return moisturemap;
    }

    private int getHeight(double e){
        if(e < 0.1)
            return 0;
        else if(e < 0.2)
            return 1;
        else if(e < 0.3)
            return 2;
        else if(e < 0.4)
            return 3;
        else if(e < 0.5)
            return 4;
        else
            return 5;
    }

    private int getTile(double e){
        //take data from moisture and elevation
        if(e < 0.1)
            return Terrain.SWATER_1;

        //do rest of biomes here
        return Terrain.EMPTY;
    }
    public int getVegetation(double m){
        if(m < 0.1) return Terrain.BUSH;
        //do rest of vegetation here
        return Terrain.EMPTY;
    }

    public void setFrequencies(double f1, double f2, double f3){
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;

        build();
    }

    public void setElevationOctaves(double eo1, double eo2, double eo3){
        this.eo1 = eo1;
        this.eo2 = eo2;
        this.eo3 = eo3;

        build();
    }

    public void setAmp(double amp){
        this.amp = amp;

        build();
    }

    public void setRise(double rise){
        this.rise = rise;

        build();
    }

    public void setDrop(double drop){
        this.drop = drop;

        build(); //cache results for faster rebuilding???
    }

    public void setRedistribution(double redis){
        this.redis = redis;

        build();
    }

    public void setSmooth(int smooth){
        this.smooth = smooth;

        build();
    }

    private void genHeightmap(){
        for(int y = 0; y < ysize; y++){
            for (int x = 0; x < xsize; x++){
                double e = eo1 * hmapnoise.smoothNoise(x / f1, y / f1)
                        + eo2 * hmapnoise.smoothNoise(x / f2, y / f2)
                        + eo3 * hmapnoise.smoothNoise(x / f3, y / f3);
                e /= (eo1 + eo2 + eo3);
                e = Math.pow(e, redis);

                //Manhattan Distance
                double d = 2 * Math.max(Math.abs((double)x / xsize - 0.5),
                        Math.abs((double)y / ysize - 0.5));

                heightmap[y][x] = (e + amp) * (1 - rise * Math.pow(d, drop));
            }
        }

        for(int y = 0; y < ysize; y++){
            for (int x = 0; x < xsize; x++){
                moisturemap[y][x] = vmapnoise.turbulence(x, y, 64); //64 is size of texture 64x64
            }
        }
    }

    private void normalizeHeightmap(){
        for(int y = 0; y < ysize; y++){
            for(int x = 0; x < xsize; x++){
                hm[(y * xsize) + x] = getHeight(heightmap[y][x]);
            }
        }
    }

    private void genDispMap(){
        int[] shiftY = new int[xsize];

        for(int y = ysize - 2; y >= 0; y--){
            for(int x = xsize - 1; x >= 0; x--) {
                int h1 = hm[(y * xsize) + x];
                int h2 = hm[((y + 1) * xsize) + x];
                int y1 = y + Level.SAFE_OFFSET;

                if (h1 == 0) {
                    wm[y1 * xsize + x] = Terrain.SWATER_1;
                    md[y1 * xsize + x] = Terrain.SOLID_BED;
                    hm[y1 * xsize + x] = h1;
                } else if(h2 == 0 && h1 == 1) {
                    md[y1 * xsize + x] = Terrain.TGRASS;
                    wm[y1 * xsize + x] = Terrain.EMPTY;
                    hm[y1 * xsize + x] = h1;
                    md[(y1 - 1) * xsize + x] = Terrain.SGRASS;
                    wm[(y1 - 1) * xsize + x] = Terrain.EMPTY;
                    hm[(y1 - 1) * xsize + x] = h1;
                } else if (h1 > h2) {
                    shiftY[x] = h1 - h2;
                    md[(y1 - shiftY[x]) * xsize + x] = Terrain.TGRASS;
                    wm[(y1 - shiftY[x]) * xsize + x] = Terrain.EMPTY;
                    hm[(y1 - shiftY[x]) * xsize + x] = h1;
                    md[(y1 - shiftY[x] - 1) * xsize + x] = Terrain.SGRASS;
                    wm[(y1 - shiftY[x] - 1) * xsize + x] = Terrain.EMPTY;
                    hm[(y1 - shiftY[x] - 1) * xsize + x] = h1;
                    for (int i = 1; i <= shiftY[x]; i++) {
                        if (i == h1 - h2){
                            md[(y1 - shiftY[x] + i) * xsize + x] = Terrain.ELEVATED_END;
                            hm[(y1 - shiftY[x] + i) * xsize + x] = h1 - i;
                        } else {
                            md[(y1 - shiftY[x] + i) * xsize + x] = Terrain.STONE;
                            hm[(y1 - shiftY[x] + i) * xsize + x] = h1 - i;
                        }
                        wm[(y1 - shiftY[x] + i) * xsize + x] = Terrain.EMPTY;
                    }
                } else if (h1 == h2) {
                    if (md[y1 * xsize + x] == Terrain.SOLID_BED) {
                        md[y1 * xsize + x] = Terrain.SGRASS;
                        wm[y1 * xsize + x] = Terrain.EMPTY;
                        hm[y1 * xsize + x] = h1;
                    }
                } else { //h1 < h2 ignore lower heights
                    if (md[y1 * xsize + x] == Terrain.SOLID_BED) {
                        md[y1 * xsize + x] = Terrain.SOLID_BED; //adjust for invisible lower levels than foreground
                        wm[y1 * xsize + x] = Terrain.EMPTY;
                        hm[y1 * xsize + x] = h1;
                    }
                }
            }
        }
    }

    private void cleanWaterTiles(){
        Level lv = Artifice.getLevel();
        for (int i = 0; i < wm.length; i++) {
            //clean tiles that are adjacent to water tiles
            if (wm[i] == Terrain.EMPTY) {
                if (wm[i + lv.s_cells[6]] == Terrain.SWATER_1) {
                    //clean bottom corner tiles
                    if (wm[i + lv.s_cells[4]] == Terrain.SWATER_1 &&
                            wm[i + lv.s_cells[3]] != Terrain.SWATER_1) {
                                md[i] = Terrain.CONV_GRASS;
                                md[i + lv.s_cells[1]] = Terrain.D1GRASS;
                                wm[i] = Terrain.DWATER_1;
                                lv.getTilemap().updateFlipData(i, false);
                                lv.getTilemap().updateFlipData(i + lv.s_cells[1], false);
                                lv.getWatertilemap().updateFlipData(i, false);
                    } else if (wm[i + lv.s_cells[3]] == Terrain.SWATER_1 &&
                            wm[i + lv.s_cells[4]] != Terrain.SWATER_1){
                                md[i] = Terrain.CONV_GRASS;
                                md[i + lv.s_cells[1]] = Terrain.D1GRASS;
                                wm[i] = Terrain.DWATER_1;
                                lv.getTilemap().updateFlipData(i, true);
                                lv.getTilemap().updateFlipData(i + lv.s_cells[1], true);
                                lv.getWatertilemap().updateFlipData(i, true);
                    //clean bottom flat tiles
                    } else if (wm[i + lv.s_cells[4]] != Terrain.SWATER_1 &&
                            wm[i + lv.s_cells[3]] != Terrain.SWATER_1) {
                        wm[i + lv.s_cells[6]] = Terrain.TWATER_1;
                    }
                } else if (wm[i + lv.s_cells[1]] == Terrain.SWATER_1) {
                    //clean top corner tiles
                    if (wm[i + lv.s_cells[4]] == Terrain.SWATER_1 &&
                            wm[i + lv.s_cells[3]] != Terrain.SWATER_1) {
                        md[i] = Terrain.CONV_CGRASS;
                        wm[i] = Terrain.SWATER_2;
                        lv.getTilemap().updateFlipData(i, false);
                        lv.getTilemap().updateFlipData(i + lv.s_cells[1], false);
                        lv.getWatertilemap().updateFlipData(i, false);
                    } else if (wm[i + lv.s_cells[3]] == Terrain.SWATER_1 &&
                            wm[i + lv.s_cells[4]] != Terrain.SWATER_1) {
                        md[i] = Terrain.CONV_CGRASS;
                        wm[i] = Terrain.SWATER_2;
                        lv.getTilemap().updateFlipData(i, true);
                        lv.getTilemap().updateFlipData(i + lv.s_cells[1], true);
                        lv.getWatertilemap().updateFlipData(i, true);
                    //clean top flat tiles
                    } else {
                        md[i] = Terrain.CONV_EGRASS;
                        wm[i] = Terrain.SWATER_3;
                    }
                }
            }
        }
    }

    private void cleanGrassTiles() {
        Level lv = Artifice.getLevel();
        //clean tiles based on height differences
        for (int e = 5; e > 1; e--) {
            for (int i = 0; i < hm.length; i++) {
                if (hm[i] == e && hm[i + lv.s_cells[1]] == e
                        && (hm[i + lv.s_cells[3]] == e || hm[i + lv.s_cells[4]] == e)) {
                    if (md[i + lv.s_cells[1]] == Terrain.SGRASS && md[i] == Terrain.TGRASS) {
                        if (md[i + lv.s_cells[4]] == Terrain.TGRASS) {
                            md[i] = Terrain.CONV_GRASS;
                            md[i + lv.s_cells[1]] = Terrain.D1GRASS;
                            lv.getTilemap().updateFlipData(i, true);
                            lv.getTilemap().updateFlipData(i + lv.s_cells[1], true);
                        } else if (md[i + lv.s_cells[3]] == Terrain.TGRASS) {
                            md[i] = Terrain.CONV_GRASS;
                            md[i + lv.s_cells[1]] = Terrain.D1GRASS;
                            lv.getTilemap().updateFlipData(i, false);
                            lv.getTilemap().updateFlipData(i + lv.s_cells[1], false);
                        }
                    }
                }
            }
        }
    }

    private void cleanWallTiles(){
        Level lv = Artifice.getLevel();

        for (int i = 0; i < wm.length; i++) {
            if (md[i] == Terrain.ELEVATED_END){
                if (md[i + lv.s_cells[1]] == Terrain.CONV_GRASS &&
                        md[i + lv.s_cells[0]] == Terrain.TGRASS &&
                        md[i + lv.s_cells[6]] != Terrain.TGRASS) {
                    md[i] = Terrain.DSTONE;
                    lv.getTilemap().updateFlipData(i, false);
                } else if (md[i + lv.s_cells[1]] == Terrain.CONV_GRASS &&
                        md[i + lv.s_cells[2]] == Terrain.TGRASS &&
                        md[i + lv.s_cells[6]] != Terrain.TGRASS){
                    md[i] = Terrain.DSTONE;
                    lv.getTilemap().updateFlipData(i, true);
                } else {
                    md[i] = Terrain.STONE;
                }
            }
        }
    }

    private void smoothMap(double[][] map, int sampleSize){
        double averages = 0;
        for(int y = 0; y < ysize; y++){
            for(int x = 0; x < xsize; x++){
                for(int iy = 0; iy < sampleSize; iy++){
                    for(int ix = 0; ix < sampleSize; ix++){
                        averages += map[y + iy][x + ix];
                    }
                }
                map[y][x] = averages / (sampleSize * sampleSize);
                averages = 0;
            }
        }
    }
}
