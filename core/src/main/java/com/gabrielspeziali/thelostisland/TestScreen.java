package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TestScreen implements Screen{
    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Array<Rectangle> solidRects;
    private SpriteBatch batch;

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/test/test_level.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        solidRects = CollisionHandler.getSolidRectangles(map, "ground");

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        camera.position.set(400, 240, 0);

        player = new Player(100,200);
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f); // clean the screen before draw

        player.update(delta, solidRects);

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        TextureRegion frame = player.getCurrentFrame();

        float drawX = player.position.x + player.width / 2f - frame.getRegionWidth() / 2f;
        float drawY = player.position.y;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(frame, drawX, drawY);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        batch.dispose();
        player.dispose();
    }
}
