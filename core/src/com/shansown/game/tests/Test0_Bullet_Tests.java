package com.shansown.game.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.shansown.game.tests.bullet.*;

public class Test0_Bullet_Tests implements ApplicationListener, InputProcessor, GestureDetector.GestureListener {

    protected final BulletTest[] tests = {new BulletBasicTest(), new BulletMeshesTest(), new GimpactTest(),
            new ConvexHullTest(), new CharacterTest(), new RayPickTest()};

    protected int testIndex = 0;

    private Application app = null;

    private Stage hud;
    private Label fpsLabel;
    private Label titleLabel;
    private Label previousLabel;
    private Label nextLabel;
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
        nextLabel = new Label("next >> ", new Label.LabelStyle(font, Color.WHITE));
        nextLabel.setPosition(hud.getWidth() - nextLabel.getWidth(), hud.getHeight() / 2);
        nextLabel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadTest(true);
                return true;
            }
        });
        hud.addActor(nextLabel);
        previousLabel = new Label(" << previous", new Label.LabelStyle(font, Color.WHITE));
        previousLabel.setPosition(0, hud.getHeight() / 2);
        previousLabel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadTest(false);
                return true;
            }
        });
        hud.addActor(previousLabel);

        checkTestNavigation();

        cameraController = new CameraInputController(tests[testIndex].camera);
        cameraController.activateKey = Input.Keys.CONTROL_LEFT;
        Gdx.input.setInputProcessor(new InputMultiplexer(hud, cameraController, this, new GestureDetector(this)));
    }

    private void checkTestNavigation() {
        if (testIndex == 0) {
            previousLabel.setVisible(false);
        } else {
            previousLabel.setVisible(true);
        }
        if (testIndex == tests.length - 1) {
            nextLabel.setVisible(false);
        } else {
            nextLabel.setVisible(true);
        }
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
        checkTestNavigation();
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

    @Override
    public boolean keyDown (int keycode) {
        return tests[testIndex].keyDown(keycode);
    }

    @Override
    public boolean keyTyped (char character) {
        return tests[testIndex].keyTyped(character);
    }

    @Override
    public boolean keyUp (int keycode) {
        return tests[testIndex].keyUp(keycode);
    }

    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {
        return tests[testIndex].touchDown(x, y, pointer, button);
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        return tests[testIndex].touchDragged(x, y, pointer);
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        return tests[testIndex].touchUp(x, y, pointer, button);
    }

    @Override
    public boolean mouseMoved (int x, int y) {
        return tests[testIndex].mouseMoved(x, y);
    }

    @Override
    public boolean scrolled (int amount) {
        return tests[testIndex].scrolled(amount);
    }

    @Override
    public boolean touchDown (float x, float y, int pointer, int button) {
        return tests[testIndex].touchDown(x, y, pointer, button);
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
        return tests[testIndex].tap(x, y, count, button);
    }

    @Override
    public boolean longPress (float x, float y) {
        return tests[testIndex].longPress(x, y);
    }

    @Override
    public boolean fling (float velocityX, float velocityY, int button) {
        tests[testIndex].fling(velocityX, velocityY, button);
        return true;
    }

    @Override
    public boolean pan (float x, float y, float deltaX, float deltaY) {
        return tests[testIndex].pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop (float x, float y, int pointer, int button) {
        return tests[testIndex].panStop(x, y, pointer, button);
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance) {
        return tests[testIndex].zoom(originalDistance, currentDistance);
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
        return tests[testIndex].pinch(initialFirstPointer, initialSecondPointer, firstPointer, secondPointer);
    }
}