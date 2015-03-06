package com.shansown.game.tests.ashley.screens;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.shansown.game.tests.ashley.AshleyGame;
import com.shansown.game.tests.ashley.systems.*;

public class GameScreen implements Screen {

    private static final String TAG = GameScreen.class.getSimpleName();

    private AshleyGame game;
    private PerspectiveCamera camera;

    private PooledEngine engine;

    private Stage hud;
    private Label fpsLabel;

    private PerformanceCounter performanceCounter;
    public StringBuilder performance = new StringBuilder();
    public FloatCounter fpsCounter = new FloatCounter(5);

    public GameScreen(AshleyGame game) {
        this.game = game;
        camera = createCamera();
    }

    @Override
    public void show() {
        initHud();
        initEngine();
        initInputProcessor();
    }

    private PerspectiveCamera createCamera() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        if (width > height) {
            width = WorldSystem.MIN_WORLD_VIEWPORT_SIZE * width / height;
            height = WorldSystem.MIN_WORLD_VIEWPORT_SIZE;
        } else {
            height = WorldSystem.MIN_WORLD_VIEWPORT_SIZE * height / width;
            width = WorldSystem.MIN_WORLD_VIEWPORT_SIZE;
        }
        PerspectiveCamera camera = new PerspectiveCamera(67, width, height);
        camera.position.set(new Vector3(7, 7, 9));
        camera.lookAt(new Vector3(0, -5, 0));
        camera.near = 1;
        camera.far = 100;
        camera.update();
        return camera;
    }

    private void initHud() {
        BitmapFont font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
        hud = new Stage();
        fpsLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        fpsLabel.setPosition(0, 0);
        hud.addActor(fpsLabel);
    }

    private void initEngine() {
        engine = new PooledEngine();

        WorldSystem worldSystem = new WorldSystem(10, game);
        performanceCounter = worldSystem.initPerformanceCounter();

        InputSystem inputSystem = new InputSystem(1, camera);
        GuySystem guySystem = new GuySystem(30);
        ShotStoneSystem shotStoneSystem = new ShotStoneSystem(40);
        KinematicSystem kinematicSystem = new KinematicSystem(50);
        RenderSystem renderSystem = new RenderSystem(50, camera);
        DynamicSystem dynamicSystem = new DynamicSystem(20);

        // Change order carefully!
        engine.addSystem(worldSystem);      // priority - 10
        engine.addSystem(inputSystem);      // priority - 1
        engine.addSystem(dynamicSystem);    // priority - 20
        engine.addSystem(guySystem);        // priority - 30
        engine.addSystem(shotStoneSystem);  // priority - 40
        engine.addSystem(kinematicSystem);  // priority - 50
        engine.addSystem(renderSystem);     // priority - 60

        worldSystem.createWorld();
    }

    private void initInputProcessor() {
        CameraInputController cameraController = new CameraInputController(camera);
        cameraController.activateKey = Input.Keys.CONTROL_LEFT;
        InputProcessor inputProcessor = engine.getSystem(InputSystem.class);
        Gdx.input.setInputProcessor(new InputMultiplexer(hud, cameraController, inputProcessor));
    }

    @Override
    public void render(float delta) {
        if (delta > 0.1f) delta = 0.1f;

        fpsCounter.put(Gdx.graphics.getFramesPerSecond());

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        performance.setLength(0);
        performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
                .append((int) (performanceCounter.load.value * 100f)).append("%");
        fpsLabel.setText(performance);
        hud.act();
        hud.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.MENU) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            toggleDebugMode();
        }
    }

    private void toggleDebugMode() {
        engine.getSystem(RenderSystem.class).toggleDebugMode();
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
        Gdx.app.log(TAG, "dispose");
        hud.dispose();
        engine.removeAllEntities();
        engine.clearPools();
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disableable) {
                ((Disposable) system).dispose();
            }
            engine.removeSystem(system);
        }
    }
}