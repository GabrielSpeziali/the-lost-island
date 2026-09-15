package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Hud {
    private OrthographicCamera camera;
    private TextureAtlas lifeAtlas;
    private Array<TextureAtlas.AtlasRegion> lifeFrames;

    public Hud(float viewportWidth, float viewportHeight) {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);

        lifeAtlas = new TextureAtlas(Gdx.files.internal("sprites/ui/life.atlas"));
        lifeFrames = lifeAtlas.findRegions("life"); // life_0 (full) to life_5 (empty)
    }

    public void render(SpriteBatch batch, int currentLife, int maxLife) {
        int missingHearts = maxLife - currentLife;
        TextureRegion heartFrame = lifeFrames.get(missingHearts);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(heartFrame, 20, camera.viewportHeight - 64 - 20);
        batch.end();
    }

    public void dispose() {
        lifeAtlas.dispose();
    }
}
