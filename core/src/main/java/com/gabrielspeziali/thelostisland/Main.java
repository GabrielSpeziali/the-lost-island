package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.Game;

/** implementation shared by all platforms. */
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new TestScreen());
    }
}
