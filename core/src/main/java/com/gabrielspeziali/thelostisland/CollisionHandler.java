package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class CollisionHandler {
    public static Array<Rectangle> getSolidRectangles(TiledMap map, String ground) {
        Array<Rectangle> rects = new Array<>();
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(ground);
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    rects.add(new Rectangle(x * tileWidth, y * tileHeight, tileWidth, tileHeight));
                }
            }
        }
        return rects;
    }
}
