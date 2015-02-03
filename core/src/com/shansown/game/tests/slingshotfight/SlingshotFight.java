package com.shansown.game.tests.slingshotfight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

public class SlingshotFight extends Game {

    public AssetManager assets;

    @Override
    public void create() {
        assets = new AssetManager();
        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
