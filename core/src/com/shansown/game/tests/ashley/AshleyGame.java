package com.shansown.game.tests.ashley;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.shansown.game.tests.ashley.screens.SplashScreen;

public class AshleyGame extends Game {

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
