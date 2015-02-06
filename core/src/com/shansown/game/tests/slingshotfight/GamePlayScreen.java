package com.shansown.game.tests.slingshotfight;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.shansown.game.tests.slingshotfight.world.GameWorld;
import com.shansown.game.tests.slingshotfight.world.camera.GameCamera;
import com.shansown.game.tests.slingshotfight.world.environment.EnvironmentCreater;

public class GamePlayScreen implements Screen, GestureDetector.GestureListener, InputProcessor {

    public static boolean shadows = false;
    public static boolean debug = true;

    private SlingshotFight game;
    private GameWorld world;

    private Stage hud;
    private Label fpsLabel;

    public StringBuilder performance = new StringBuilder();
    public PerformanceCounter performanceCounter = new PerformanceCounter("Bullet");
    public FloatCounter fpsCounter = new FloatCounter(5);

    public GamePlayScreen(SlingshotFight game) {
        this.game = game;
    }

    @Override
    public void show() {
        initScene();
        Gdx.input.setInputProcessor(new InputMultiplexer(this, new GestureDetector(this), new CameraInputController(world.camera)));
        initHud();
    }

    private void initScene() {
        world = GameWorld.createWorld(game, true);
    }

    private void initHud() {
        BitmapFont font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
        hud = new Stage();
        fpsLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        fpsLabel.setPosition(0, 0);
        hud.addActor(fpsLabel);
    }

    @Override
    public void render(float delta) {
        fpsCounter.put(Gdx.graphics.getFramesPerSecond());

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        updateWorld(delta);
        renderWorld();

        performance.setLength(0);
        performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
                .append((int)(performanceCounter.load.value * 100f)).append("%");
        performance.append(" visible: ").append(world.visibleEntities.size);
        fpsLabel.setText(performance);
        hud.act();
        hud.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.MENU) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            toggleDebugMode();
        }
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
//        world.tap(x, y, count, button);
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }



    private void updateWorld(float delta) {
        world.update();
    }

    private void renderWorld () {
        world.renderFrustumCulling();
    }

    private void toggleDebugMode() {
        world.toggleDebugMode();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        hud.dispose();
        world.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return world.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
       return world.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
       return world.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}