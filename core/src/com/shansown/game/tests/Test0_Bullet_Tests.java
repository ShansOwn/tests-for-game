package com.shansown.game.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.shansown.game.tests.bullet.BulletBasicTest;
import com.shansown.game.tests.bullet.BulletTest;

public class Test0_Bullet_Tests implements ApplicationListener {

    protected final BulletTest[] tests = {new BulletBasicTest()};

    protected int testIndex = 0;

    private Application app = null;

    private Stage hud;
    private Label fpsLabel;
    private Label titleLabel;
    private CameraInputController cameraController;

    @Override
    public void create() {
        if (app == null) {
            app = Gdx.app;
            tests[testIndex].create();
        }

        BitmapFont font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
        hud = new Stage();
        fpsLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        fpsLabel.setPosition(0, 0);
        hud.addActor(fpsLabel);
        titleLabel = new Label(tests[testIndex].getClass().getSimpleName(), new Label.LabelStyle(font, Color.WHITE));
        titleLabel.setY(hud.getHeight() - titleLabel.getHeight());
        hud.addActor(titleLabel);
        Label nextLabel = new Label("next >> ", new Label.LabelStyle(font, Color.WHITE));
        nextLabel.setPosition(hud.getWidth() - nextLabel.getWidth(), hud.getHeight() / 2);
        nextLabel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadTest(true);
                return true;
            }
        });
        hud.addActor(nextLabel);
        Label previousLabel = new Label(" << previous", new Label.LabelStyle(font, Color.WHITE));
        previousLabel.setPosition(0, hud.getHeight() / 2);
        previousLabel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadTest(false);
                return true;
            }
        });
        hud.addActor(previousLabel);

        if (testIndex == 0) {
            previousLabel.setVisible(false);
        }
        if (testIndex == tests.length - 1) {
            nextLabel.setVisible(false);
        }

        cameraController = new CameraInputController(tests[testIndex].camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(hud, cameraController));
    }

    @Override
    public void render() {
        tests[testIndex].render();
        fpsLabel.setText(tests[testIndex].performance);
        hud.draw();
    }

    public void loadTest(boolean next) {
        app.log("Test", "disposing test '" + tests[testIndex].getClass().getName() + "'");
        tests[testIndex].dispose();
        // This would be a good time for GC to kick in.
        System.gc();
        if (next) {
            testIndex++;
        } else {
            testIndex--;
        }
        tests[testIndex].create();
        cameraController.camera = tests[testIndex].camera;
        app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName() + "'");
        titleLabel.setText(tests[testIndex].getClass().getSimpleName());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int width, int height) {
        hud.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        tests[testIndex].dispose();
        app = null;
    }
}