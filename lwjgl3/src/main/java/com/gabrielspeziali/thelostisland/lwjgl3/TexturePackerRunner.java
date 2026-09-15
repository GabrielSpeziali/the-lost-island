package com.gabrielspeziali.thelostisland.lwjgl3;


import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class TexturePackerRunner {
    public static void main(String[] args) {
        TexturePacker.process(
            "assets/sprites/player/raw_frames",
            "assets/sprites/player",
            "player"
        );

        TexturePacker.process(
            "assets/sprites/ui/raw_frames",
            "assets/sprites/ui",
            "life"
        );
    }
}
