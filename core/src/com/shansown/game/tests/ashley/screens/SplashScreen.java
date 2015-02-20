package com.shansown.game.tests.ashley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.shansown.game.tests.ashley.AshleyGame;
import com.shansown.game.tests.ashley.reference.Models;
import com.shansown.game.tests.ashley.reference.Textures;

public class SplashScreen implements Screen {

    private AshleyGame game;
    private Stage hud;

    private Image splashImage;
    private Action splashAction;
    private boolean loaded = false, animationFinished = false;

    public SplashScreen(AshleyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        loadAssets();
        Bullet.init();
        hud = new Stage();
        splashImage = new Image(new Texture(Gdx.files.internal(Textures.SPLASH)));
        splashImage.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);
        splashImage.setOrigin(splashImage.getWidth() / 2, splashImage.getHeight() / 2);
        hud.addActor(splashImage);
        addSplashAnimation();
    }

    private void loadAssets() {
        game.assets.load(Models.SLINGSHOT_GUY, Model.class);
        game.assets.load(Models.ISLAND, Model.class);
    }

    private void addSplashAnimation() {
        splashAction =  Actions.sequence(
                Actions.parallel(
                        Actions.fadeOut(0),
                        Actions.scaleTo(.1f, .1f)),
                Actions.parallel(
                        Actions.fadeIn(1f),
                        Actions.rotateTo(3 * 360, 1f),
                        Actions.scaleTo(1f, 1f, 1f)),
                Actions.delay(.2f),
                Actions.scaleTo(.5f, .5f, .5f),
                Actions.delay(.2f),
                Actions.moveTo(0, Gdx.graphics.getHeight() / 2 - splashImage.getHeight() / 2, 1f, Interpolation.elasticOut),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        animationFinished = true;
                    }
                }));
        splashImage.addAction(splashAction);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        drawHud();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            exitApp();
        }

        if (!loaded && game.assets.update()) {
            loaded = true;
        }

        if (loaded && animationFinished) {
            game.setScreen(new GameScreen(game));
        }
    }

    private void drawHud() {
        hud.act();
        hud.draw();
    }

    private void exitApp() {
        if (splashAction != null && !animationFinished) {
            splashImage.removeAction(splashAction);
        }
        Gdx.app.exit();
    }

    @Override
    public void resize(int width, int height) {
        hud.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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
    }
}