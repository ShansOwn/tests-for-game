package com.shansown.game.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/**
 * Only for test. For this task maybe kinematic objects are more appropriate.
 */
public class RayPickTest extends BaseBulletTest {

    final static short PLAYER_FLAG = 1<<8;

    IntMap<PlayerCharacter> characters = new IntMap<PlayerCharacter>();
    BulletEntity ground;

    PlayerCharacter pickedCharacter;

    btGhostPairCallback ghostPairCallback;
    ClosestRayResultCallback rayTestCB;
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
        loading = true;

        (ground = world.add("ground", 0f, 0f, 0f)).setColor(0.25f + 0.5f * (float) Math.random(),
                0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 1f);

        rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
    }

    @Override
    public void render() {
        super.render();
        if (loading && assets.update()) {
            loading = false;
            doneLoading();
        }
    }

    @Override
    public void update() {
        for (PlayerCharacter character : characters.values()) {
            character.update();
        }
        super.update();
    }

    private void doneLoading() {
        final Model characterModel = assets.get("data/Slingshot_guy2.g3db", Model.class);

        BoundingBox bbox = new BoundingBox();
        characterModel.calculateBoundingBox(bbox);
        world.addConstructor("character", new BulletConstructor(characterModel, null));

        createCharacter(5, -bbox.min.y, 5);
        createCharacter(3, -bbox.min.y, 5);
        createCharacter(1, -bbox.min.y, 5);
    }

    private void createCharacter(float x, float y, float z) {
        BulletEntity character = world.add("character", x, y, z);
        int id = characters.size;
        characters.put(id, new PlayerCharacter(id ,character, world));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("Test", "touchDown");
        boolean result = false;
        if (button == Input.Buttons.LEFT) {
            Ray ray = camera.getPickRay(screenX, screenY);
            Vector3 rayFrom = ray.origin;
            Vector3 rayTo = tmpV.set(ray.direction).scl(50f).add(ray.origin); // 50 meters max from the origin

            // Because we reuse the ClosestRayResultCallback, we need reset it's values
            rayTestCB.setCollisionObject(null);
            rayTestCB.setClosestHitFraction(1f);
            rayTestCB.setRayFromWorld(rayFrom);
            rayTestCB.setRayToWorld(rayTo);

            world.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB);

            if (rayTestCB.hasHit()) {
                Gdx.app.log("Test", "hasHit");
                final btCollisionObject obj = rayTestCB.getCollisionObject();
                boolean canPick = PLAYER_FLAG == (obj.getCollisionFlags() & PLAYER_FLAG);
                if (canPick) {
                    Gdx.app.log("Test", "Pick");
                    pickedCharacter = characters.get(obj.getUserValue());
                    result = true;
                }
            }
        }
        return result || super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Gdx.app.log("Test", "touchDragged");
        boolean result = false;
        if (pickedCharacter != null) {
            Ray ray = camera.getPickRay(screenX, screenY);
            Gdx.app.log("Test", "Ray: " + ray);
            final float distance = -ray.origin.y / ray.direction.y;
            tmpV.set(ray.direction).scl(distance).add(ray.origin).y = 1;
            pickedCharacter.moveTo(tmpV);
            result = true;
        }
        return result || super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("Test", "touchUp");
        boolean result = false;
        if (pickedCharacter != null) {
            pickedCharacter = null;
            result = true;
        }
        return result || super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
        shoot(x, y);
        return true;
    }

    @Override
    public void dispose() {
        Gdx.app.log("Test", "RayPickTest dispose");
        for (PlayerCharacter character : characters.values()) {
            character.dispose();
        }
        characters.clear();

        if (rayTestCB != null) {
            rayTestCB.dispose();
            rayTestCB = null;
        }

        super.dispose();
        ghostPairCallback.dispose();
        ground = null;
    }

    private static class PlayerCharacter implements Disposable {
        int id;
        BulletWorld world;
        BulletEntity bulletEntity;
        btPairCachingGhostObject ghostObject;
        btConvexShape ghostShape;
        btKinematicCharacterController characterController;

        Vector3 tmpV = new Vector3();

        public PlayerCharacter(int id, BulletEntity bulletEntity, BulletWorld world) {
            this.id = id;
            this.world = world;
            this.bulletEntity = bulletEntity;
            // Create the physics representation of the character
            ghostObject = new btPairCachingGhostObject();
            ghostObject.setUserValue(id);
            ghostObject.setWorldTransform(bulletEntity.transform);
            ghostShape = new btCapsuleShape(.3f, 1f);
            ghostObject.setCollisionShape(ghostShape);
            ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT | PLAYER_FLAG);
            characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);

            // And add it to the physics world
            world.collisionWorld.addCollisionObject(ghostObject,
                    (short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
                    (short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter
                            | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
            ((btDiscreteDynamicsWorld)(world.collisionWorld)).addAction(characterController);
        }

        public void update() {
            // And fetch the new transformation of the character (this will make the model be rendered correctly)
            ghostObject.getWorldTransform(bulletEntity.transform);
        }

        public void moveTo(Vector3 point) {
            Gdx.app.log("Test", "moveTo: " + point);
            bulletEntity.transform.getTranslation(tmpV);
            point.y = tmpV.y;
            // Clamp by ground size with some margin
            point.x = MathUtils.clamp(point.x, -19, 19);
            point.z = MathUtils.clamp(point.z, -19, 19);
            bulletEntity.transform.setTranslation(point);
            ghostObject.setWorldTransform(bulletEntity.transform);
        }

        @Override
        public void dispose() {
            Gdx.app.log("Test", "PlayerCharacter #"  + id + " dispose");
            ((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
            world.collisionWorld.removeCollisionObject(ghostObject);
            characterController.dispose();
            ghostObject.dispose();
            ghostShape.dispose();
            world = null;
            bulletEntity = null;
        }
    }
}