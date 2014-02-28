package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
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
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class Test2_Animation implements ApplicationListener {

    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Environment environment;
    public boolean loading;

    ModelInstance skydome;
    Model floorModel;
    ModelInstance character;
    AnimationController animation;

    AnimationState state;
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

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        assets = new AssetManager();
        assets.load("data/skydome.g3db", Model.class);
        assets.load("data/concrete.png", Texture.class);
        loading = true;

        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder part = builder.part("floor", GL10.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal, new Material());
        for (float x = -200f; x < 200f; x += 10f) {
            for (float z = -200f; z < 200f; z += 10f) {
                part.rect(x, 0, z+10f, x+10f, 0, z+10f, x+10f, 0, z, x, 0, z, 0, 1, 0);
            }
        }
        floorModel = builder.end();
    }



    @Override
    public void render() {




        if (character != null) {
            animation.update(Gdx.graphics.getDeltaTime());
            /*if (upKey) {
                if (!animation.inAction) {
                    trTmp.idt().lerp(trForward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                    character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                }
                if (status != walk) {
                    animation.animate("Walk", -1, 1f, null, 0.2f);
                    status = walk;
                }
            } else if (downKey) {
                if (!animation.inAction) {
                    trTmp.idt().lerp(trBackward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                    character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                }
                if (status != back) {
                    animation.animate("Walk", -1, -1f, null, 0.2f);
                    status = back;
                }
            } else if (status != idle) {*/
                animation.animate("Idle", -1, 1f, null, 0.2f);
                /*status = idle;
            }
            if (rightKey && (status == walk || status == back) && !animation.inAction) {
                trTmp.idt().lerp(trRight, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                character.transform.mul(trTmp.toMatrix4(tmpMatrix));
            } else if (leftKey && (status == walk || status == back) && !animation.inAction) {
                trTmp.idt().lerp(trLeft, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                character.transform.mul(trTmp.toMatrix4(tmpMatrix));
            }
            if (spaceKey && !animation.inAction) {
                animation.action("Attack", 1, 1f, null, 0.2f);
            }*/
        }

        /*if (character != null) {
            shadowLight.begin(character.transform.getTranslation(tmpVector), cam.direction);
            shadowBatch.begin(shadowLight.getCamera());
            if (character != null)
                shadowBatch.render(character);
            shadowBatch.end();
            shadowLight.end();

             }*/

        if (loading && assets.update()) {
            loading = false;
            Gdx.app.log("Test", "onLoaded");
            doneLoading();
        }

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        if (instances != null)
            modelBatch.render(instances, environment);
        if (skydome != null)
            modelBatch.render(skydome);
        modelBatch.end();
//        fpsLogger.log();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }

    private void doneLoading() {
        if (skydome == null) {
            Gdx.app.log("Test", "DoneLoading skydome == null");
            skydome = new ModelInstance(assets.get("data/skydome.g3db", Model.class));
            floorModel.materials.get(0).set(TextureAttribute.createDiffuse(assets.get("data/concrete.png", Texture.class)));
            instances.add(new ModelInstance(floorModel));
            assets.load("data/knight.g3db", Model.class);
            loading = true;
        } else if (character == null) {
            Gdx.app.log("Test", "DoneLoading character == null");
            character = new ModelInstance(assets.get("data/knight.g3db", Model.class));
            BoundingBox bbox = new BoundingBox();
            character.calculateBoundingBox(bbox);
            character.transform.setToRotation(Vector3.Y, 180).trn(0, -bbox.min.y, 0);
            instances.add(character);
            animation = new AnimationController(character);
            animation.animate("Idle", -1, 1f, null, 0.2f);
            state = AnimationState.IDLE;
            for (Animation anim : character.animations)
                Gdx.app.log("Test", anim.id);
        }
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

    private enum AnimationState {
        IDLE, WALK, BACK, ATTACK
    }
}
