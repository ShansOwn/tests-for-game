package com.shansown.game.tests.bullet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.utils.PerformanceCounter;

public class BulletTest implements ApplicationListener {

    public StringBuilder performance = new StringBuilder();
    public PerformanceCounter performanceCounter = new PerformanceCounter(this.getClass().getSimpleName());
    public FloatCounter fpsCounter = new FloatCounter(5);
    public PerspectiveCamera camera;

    @Override
    public void create() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
