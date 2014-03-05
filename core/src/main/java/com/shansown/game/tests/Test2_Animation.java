package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class Test2_Animation implements ApplicationListener, InputProcessor {

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
    AnimationController animation;

    final AnimationController.Transform trTmp = new AnimationController.Transform();
    final AnimationController.Transform trForward = new AnimationController.Transform();
    final AnimationController.Transform trSneakForward = new AnimationController.Transform();
    final AnimationController.Transform trBackward = new AnimationController.Transform();
    final AnimationController.Transform trSneakBackward = new AnimationController.Transform();
    final AnimationController.Transform trRight = new AnimationController.Transform();
    final AnimationController.Transform trLeft = new AnimationController.Transform();
    final Matrix4 tmpMatrix = new Matrix4();

    AnimationState state;
    int damageCount = 0;
    boolean rightKey, leftKey, upKey, downKey, shiftKey, spaceKey, enterKey;
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
        camInputController.rotateLeftKey = camInputController.rotateRightKey
                = camInputController.forwardKey = camInputController.backwardKey = 0;
        Gdx.input.setInputProcessor(new InputMultiplexer(this, camInputController));

        trForward.translation.set(0,0,8f);
        trSneakForward.translation.set(0,0,5);
        trBackward.translation.set(0,0,-8f);
        trSneakBackward.translation.set(0,0,-5);
        trLeft.rotation.setFromAxis(Vector3.Y, 90);
        trRight.rotation.setFromAxis(Vector3.Y, -90);
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
    public void render() {
        if (character != null) {
            animation.update(Gdx.graphics.getDeltaTime());
            if (state != AnimationState.DIE) {
                if (upKey) {
                    if (shiftKey) {
                        if (!animation.inAction) {
                            trTmp.idt().lerp(trSneakForward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                            character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                        }
                        if (state != AnimationState.SNEAK_FORWARD) {
                            animation.animate("Sneak", -1, 1f, null, 0.2f);
                            state = AnimationState.SNEAK_FORWARD;
                        }
                    } else {
                        if (!animation.inAction) {
                            trTmp.idt().lerp(trForward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                            character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                        }
                        if (state != AnimationState.WALK) {
                            animation.animate("Walk", -1, 1f, null, 0.2f);
                            state = AnimationState.WALK;
                        }
                    }
                } else if (downKey) {
                    if (shiftKey) {
                        if (!animation.inAction) {
                            trTmp.idt().lerp(trSneakBackward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                            character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                        }
                        if (state != AnimationState.SNEAK_BACK) {
                            animation.animate("Sneak", -1, -1f, null, 0.2f);
                            state = AnimationState.SNEAK_BACK;
                        }
                    } else {
                        if (!animation.inAction) {
                            trTmp.idt().lerp(trBackward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                            character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                        }
                        if (state != AnimationState.BACK) {
                            animation.animate("Walk", -1, -1f, null, 0.2f);
                            state = AnimationState.BACK;
                        }
                    }
                } else if (state != AnimationState.IDLE) {
                    animation.animate("Idle", -1, 1f, null, 0.2f);
                    state = AnimationState.IDLE;
                }
                if (rightKey && (state == AnimationState.WALK
                        || state == AnimationState.BACK || state == AnimationState.SNEAK_FORWARD
                        || state == AnimationState.SNEAK_BACK) && !animation.inAction) {
                    trTmp.idt().lerp(trRight, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                    character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                } else if (leftKey && (state == AnimationState.WALK
                        || state == AnimationState.BACK || state == AnimationState.SNEAK_FORWARD
                        || state == AnimationState.SNEAK_BACK) && !animation.inAction) {
                    trTmp.idt().lerp(trLeft, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
                    character.transform.mul(trTmp.toMatrix4(tmpMatrix));
                }
                if (spaceKey && !animation.inAction) {
                    animation.action("Attack", 1, 1f, null, 0.2f);
                    state = AnimationState.ATTACK;
                }
                if (enterKey && state != AnimationState.DAMAGED) {
                    if (damageCount < 3) {
                        animation.action("Damaged", 1, 1f, null, 0.2f);
                        state = AnimationState.DAMAGED;
                    } else {
                        animation.action("Die", 1, 1f, new DieAnimationListener(), 0.2f);
                        state = AnimationState.DIE;
                    }
                }
            }
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

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }

    private void doneLoading() {
        if (skydome == null) {
            skydome = new ModelInstance(assets.get("data/skydome.g3db", Model.class));
            floorModel.materials.get(0).set(TextureAttribute.createDiffuse(assets.get("data/concrete.png", Texture.class)));
            instances.add(new ModelInstance(floorModel));
            assets.load("data/knight.g3db", Model.class);
            loading = true;
        } else if (character == null) {
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

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A)
            leftKey = true;
        if (keycode == Input.Keys.D)
            rightKey = true;
        if (keycode == Input.Keys.W)
            upKey = true;
        if (keycode == Input.Keys.S)
            downKey = true;
        if (keycode == Input.Keys.SHIFT_LEFT)
            shiftKey = true;
        if (keycode == Input.Keys.ENTER) {
            enterKey = true;
            damageCount++;
        }
        if (keycode == Input.Keys.SPACE)
            spaceKey = true;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A)
            leftKey = false;
        if (keycode == Input.Keys.D)
            rightKey = false;
        if (keycode == Input.Keys.W)
            upKey = false;
        if (keycode == Input.Keys.S)
            downKey = false;
        if (keycode == Input.Keys.SHIFT_LEFT)
            shiftKey = false;
        if (keycode == Input.Keys.ENTER)
            enterKey = false;
        if (keycode == Input.Keys.SPACE)
            spaceKey = false;
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

    private enum AnimationState {
        IDLE, WALK, BACK, ATTACK, DAMAGED, SNEAK_FORWARD, SNEAK_BACK, DIE
    }

    private class DieAnimationListener implements AnimationController.AnimationListener {

        @Override
        public void onEnd(AnimationController.AnimationDesc anim) {
            animation.paused = true;
        }

        @Override
        public void onLoop(AnimationController.AnimationDesc animation) {

        }
    }
}
