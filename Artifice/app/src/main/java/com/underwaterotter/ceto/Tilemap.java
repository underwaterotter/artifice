package com.underwaterotter.ceto;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.underwaterotter.glesutils.TextureAtlas;
import com.underwaterotter.glesutils.VertexQuad;

import java.nio.FloatBuffer;

public class Tilemap extends Overlay {

    protected boolean[] flipData;
    protected int[] mapData;
    protected int mapCellsW;
    protected int mapCellsH;

    protected int cellW;
    protected int cellH;

    private TextureAtlas src;

    private float[] vertices;
    private float[] stVertices;

    private FloatBuffer vertexBuffer;
    private FloatBuffer stVertexBuffer;

    boolean dirty;
    int mapCells;

    public Tilemap(Object id, int cellW, int cellH){
        super();

        this.cellW = cellW;
        this.cellH = cellH;

        src = new TextureAtlas(id);
        src.populateFixedCells(cellW, cellH);

        vertices = new float[8];
        stVertices = new float[8];
    }
    
    public void readMapData(int[] data, boolean[] flipData, int horizontalCells){

        this.mapData = data;
        this.flipData = flipData;

        mapCellsW = horizontalCells;
        mapCellsH = data.length / horizontalCells;

        mapCells = mapCellsW * mapCellsH;

        vertexBuffer = VertexQuad.genBuffer(mapCells);
        stVertexBuffer = VertexQuad.genBuffer(mapCells);

        dirty = true;
    }

    protected void updateAllVertices(){

        Rect viewBoundry = Camera.main.getScreenBoundries();
        int cellStart_y = Math.max(0, Math.round(viewBoundry.top / cellH));
        int cellEnd_y = Math.min(mapCellsH, Math.round(viewBoundry.bottom / cellH) + 1);

        int cellStart_x = Math.max(0, Math.round(viewBoundry.left / cellW));
        int cellEnd_x = Math.min(mapCellsW, Math.round(viewBoundry.right / cellW) + 1);

        for(int y = cellStart_y; y < cellEnd_y; y++){
            for(int x = cellStart_x; x < cellEnd_x; x++){
                if (Camera.main.inScreenView(x * cellW, y * cellH)) {
                    //update regular vertices
                    vertices[0] = x * cellW;
                    vertices[1] = y * cellH;

                    vertices[2] = x * cellW;
                    vertices[3] = y * cellH + cellH;

                    vertices[4] = x * cellW + cellW;
                    vertices[5] = y * cellH;

                    vertices[6] = x * cellW + cellW;
                    vertices[7] = y * cellH + cellH;

                    vertexBuffer.put(vertices);

                    //retrieve texture id and return a rect
                    RectF tileRect = src.get(mapData[y * mapCellsW + x]);
                    boolean flipHorizontal = flipData[y * mapCellsW + x];
                    //now update texture vertices

                    if (!flipHorizontal) {
                        stVertices[0] = tileRect.left;
                        stVertices[1] = tileRect.top;

                        stVertices[2] = tileRect.left;
                        stVertices[3] = tileRect.bottom;

                        stVertices[4] = tileRect.right;
                        stVertices[5] = tileRect.top;

                        stVertices[6] = tileRect.right;
                        stVertices[7] = tileRect.bottom;
                    } else {
                        stVertices[0] = tileRect.right;
                        stVertices[1] = tileRect.top;

                        stVertices[2] = tileRect.right;
                        stVertices[3] = tileRect.bottom;

                        stVertices[4] = tileRect.left;
                        stVertices[5] = tileRect.top;

                        stVertices[6] = tileRect.left;
                        stVertices[7] = tileRect.bottom;
                    }

                    stVertexBuffer.put(stVertices);
                }
            }
        }

        dirty = false;
    }

    public void draw(){
        super.draw();

        Renderer renderer = Renderer.get();

        src.bind();

        renderer.changeCamera(camera());

        renderer.uModel.valueMat4(modelMatrix);
        renderer.mColor.value4f(mr, mg, mb, ma);
        renderer.aColor.value4f(ar, ag, ab, aa);

        if(dirty){
            updateAllVertices();
        }

        renderer.drawVQuads(vertexBuffer, stVertexBuffer, mapCells);
    }
}
