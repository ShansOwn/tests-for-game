package com.shansown.game.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

public class CharacterTest extends BaseBulletTest {

    final int BOXCOUNT_X = 5;
    final int BOXCOUNT_Y = 5;
    final int BOXCOUNT_Z = 1;

    final float BOXOFFSET_X = -2.5f;
    final float BOXOFFSET_Y = 0.5f;
    final float BOXOFFSET_Z = 0f;

    BulletEntity ground;
    BulletEntity character;

    AnimationController animation;
    AnimationState state;

    btGhostPairCallback ghostPairCallback;
    btPairCachingGhostObject ghostObject;
    btConvexShape ghostShape;
    btKinematicCharacterController characterController;
    Vector3 characterDirection = new Vector3();
    Vector3 walkDirection = new Vector3();

    Vector3 tmpV = new Vector3();
    boolean loading;

    @Override
    public BulletWorld createWorld() {
        // We create the world using an axis sweep broadphase for this test
        btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
        btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
        btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
        btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
        ghostPairCallback = new btGhostPairCallback();
        sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
        return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
    }

    @Override
    public void create() {
        super.create();

        assets.load("data/Slingshot_guy2.g3db", Model.class);
        assets.load("data/Slingshot_guy.png", Texture.class);
        loading = true;

        (ground = world.add("ground", 0f, 0f, 0f)).setColor(0.25f + 0.5f * (float) Math.random(),
                0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 1f);

        // Create some boxes to play with
        for (int x = 0; x < BOXCOUNT_X; x++) {
            for (int y = 0; y < BOXCOUNT_Y; y++) {
                for (int z = 0; z < BOXCOUNT_Z; z++) {
                    world.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)
                            .setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
                }
            }
        }
    }

    @Override
    public void render() {
        super.render();
        if (loading && assets.update()) {
            loading = false;
            doneLoading();
        }
    }

    private void doneLoading() {
        final Model characterModel = assets.get("data/Slingshot_guy2.g3db", Model.class);

        world.addConstructor("character", new BulletConstructor(characterModel, null));
        character = world.add("character", 5, 3, 5);

        // Create the physics representation of the character
        ghostObject = new btPairCachingGhostObject();
        ghostObject.setWorldTransform(character.transform);
        ghostShape = new btCapsuleShape(.3f, 1f);
        ghostObject.setCollisionShape(ghostShape);
        ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);

        // And add it to the physics world
        world.collisionWorld.addCollisionObject(ghostObject,
                (short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
                (short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
        ((btDiscreteDynamicsWorld)(world.collisionWorld)).addAction(characterController);

        animation = new AnimationController(character.modelInstance);
        animation.animate("Armature|Idle", -1, 1f, null, 0.2f);
        state = AnimationState.IDLE;
        for (Animation anim : character.modelInstance.animations)
            Gdx.app.log("Test", anim.id);
    }

    @Override
    public void update () {
        if (character != null) {
            animation.update(Gdx.graphics.getDeltaTime());
            if (state != AnimationState.DIE) {
                // If the left or right key is pressed, rotate the character and update its physics update accordingly.
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    character.transform.rotate(0, 1, 0, 3f);
                    ghostObject.setWorldTransform(character.transform);
                }
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    character.transform.rotate(0, 1, 0, -3f);
                    ghostObject.setWorldTransform(character.transform);
                }
                // Fetch which direction the character is facing now
                characterDirection.set(0,0,-1).rot(character.transform).nor();
                // Set the walking direction accordingly (either forward or backward)
                walkDirection.set(0,0,0);
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    if (state != AnimationState.WALK) {
                        animation.animate("Armature|Walk", -1, 2f, null, 0.2f);
                        state = AnimationState.WALK;
                    }
                    walkDirection.add(characterDirection);
                } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    if (state != AnimationState.WALK) {
                        animation.animate("Armature|Walk", -1, -2f, null, 0.2f);
                        state = AnimationState.BACK;
                    }
                    walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
                } else if (state != AnimationState.IDLE) {
                    animation.animate("Armature|Idle", -1, 1f, null, 0.2f);
                    state = AnimationState.IDLE;
                }
            }

            walkDirection.scl(3f * Gdx.graphics.getDeltaTime());
            // And update the character controller
            characterController.setWalkDirection(walkDirection);

            character.transform.getTranslation(camera.position);
            tmpV.set(camera.position).sub(5, 0, 5).y = 0f;
            camera.position.add(tmpV.nor().scl(-6f)).y = 4f;
            character.transform.getTranslation(tmpV);
            camera.lookAt(tmpV);
            camera.up.set(Vector3.Y);
            camera.update();

        }
        // Now we can update the world as normally
        super.update();
        // And fetch the new transformation of the character (this will make the model be rendered correctly)
        if (character != null) {
            ghostObject.getWorldTransform(character.transform);
        }
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
        shoot(x, y);
        return true;
    }

    @Override
    public void dispose() {
        ((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
        world.collisionWorld.removeCollisionObject(ghostObject);
        super.dispose();
        characterController.dispose();
        ghostObject.dispose();
        ghostShape.dispose();
        ghostPairCallback.dispose();
        ground = null;
    }

    private enum AnimationState {
        IDLE, WALK, BACK, ATTACK, DAMAGED, SNEAK_FORWARD, SNEAK_BACK, DIE
    }
}