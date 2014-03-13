package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class Test3_TRex_Animation implements ApplicationListener, InputProcessor {

    public PerspectiveCamera cam;
    public CameraInputController camInputController;
    public ModelBatch modelBatch;
    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Environment environment;
    public boolean loading;

    ModelInstance skydome;
    Model floorModel;
    ModelInstance character;

    FPSLogger fpsLogger = new FPSLogger();

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(25f, 25f, 25f);
        cam.lookAt(0,0,0);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();

        camInputController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, camInputController));

        assets = new AssetManager();
        assets.load("data/skydome.g3db", Model.class);
        assets.load("data/concrete.png", Texture.class);
        loading = true;

        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder part = builder.part("floor", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal, new Material());
        for (float x = -200f; x < 200f; x += 10f) {
            for (float z = -200f; z < 200f; z += 10f) {
                part.rect(x, 0, z+10f, x+10f, 0, z+10f, x+10f, 0, z, x, 0, z, 0, 1, 0);
            }
        }
        floorModel = builder.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        if (loading && assets.update()) {
            loading = false;
            doneLoading();
        }

        camInputController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        if (instances != null)
            modelBatch.render(instances, environment);
        if (skydome != null)
            modelBatch.render(skydome);
        modelBatch.end();
        fpsLogger.log();
    }

    private void doneLoading() {
        if (skydome == null) {
            skydome = new ModelInstance(assets.get("data/skydome.g3db", Model.class));
            floorModel.materials.get(0).set(TextureAttribute.createDiffuse(assets.get("data/concrete.png", Texture.class)));
            instances.add(new ModelInstance(floorModel));
            assets.load("data/T-rex.g3db", Model.class);
            loading = true;
        } else if (character == null) {
            character = new ModelInstance(assets.get("data/T-rex.g3db", Model.class));
            BoundingBox bbox = new BoundingBox();
            character.calculateBoundingBox(bbox);
            character.transform.setToRotation(Vector3.Y, 180).trn(0, -bbox.min.y, 0);
            instances.add(character);
        }
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
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
