package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Vector3;

public class TestScreen implements Screen{
    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Array<Rectangle> solidRects;
    private SpriteBatch batch;
    private float mapWidthPixels, mapHeightPixels;
    private float cameraGroundY; // vertical focus, only updated while the player is grounded
    private Hud hud;
    private Vector3 mouseWorldPos = new Vector3();

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/level_1/level_1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        solidRects = CollisionHandler.getSolidRectangles(map, "ground");

        MapProperties mapProps = map.getProperties();
        int mapWidthTiles = mapProps.get("width", Integer.class);
        int mapHeightTiles = mapProps.get("height", Integer.class);
        int tileSize = mapProps.get("tilewidth", Integer.class);

        mapWidthPixels = mapWidthTiles * tileSize;
        mapHeightPixels = mapHeightTiles * tileSize;

        camera = new OrthographicCamera();
        viewport = new FitViewport(960, 540, camera);
        camera.position.set(400, 240, 0);

        player = new Player(150,200);
        batch = new SpriteBatch();

        cameraGroundY = player.position.y + player.height / 2f;

        hud = new Hud(960, 540);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f); // clean the screen before draw

        mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldPos);

        player.update(delta, solidRects, mouseWorldPos.x);

        // Clamp the player inside the map bounds (temporary safety net until real level walls exist)
        player.position.x = MathUtils.clamp(player.position.x, 0, mapWidthPixels - player.width);
        player.position.y = MathUtils.clamp(player.position.y, 0, mapHeightPixels - player.height);

        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;

        float cameraX = MathUtils.clamp(player.position.x, halfViewportWidth, mapWidthPixels - halfViewportWidth);

        // Camera Y only updates while grounded — jumps don't move the camera at all,
        // it only repositions once the player lands (same height or a new one)
        if (player.onGround) {
            cameraGroundY = player.position.y + player.height / 2f;
        }
        float cameraY = MathUtils.clamp(cameraGroundY, halfViewportHeight, mapHeightPixels - halfViewportHeight);

        camera.position.set(cameraX, cameraY, 0);
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

        hud.render(batch, player.currentLife, Player.MAX_LIFE);

        if (player.isDead()) {
            ((Game) Gdx.app.getApplicationListener()).setScreen(new TestScreen());
        }
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
        hud.dispose();
    }
}
