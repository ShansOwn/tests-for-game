package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.shansown.game.tests.ashley.AshleyGame;

public class Test5_Ashley_Test implements ApplicationListener {

    AshleyGame game;

    @Override
    public void create() {
        game = new AshleyGame();
        game.create();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void render() {
        game.render();
    }

    @Override
    public void pause() {
        game.pause();
    }

    @Override
    public void resume() {
        game.resume();
    }

    @Override
    public void dispose() {
        game.dispose();
        game = null;
    }
}
