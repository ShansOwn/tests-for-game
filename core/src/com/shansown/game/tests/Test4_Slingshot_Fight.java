package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.shansown.game.tests.slingshotfight.SlingshotFight;

public class Test4_Slingshot_Fight implements ApplicationListener {

    private Game game;

    @Override
    public void create() {
        game = new SlingshotFight();
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
