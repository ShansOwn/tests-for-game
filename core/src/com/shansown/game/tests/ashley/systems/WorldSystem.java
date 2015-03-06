package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.shansown.game.tests.ashley.AshleyGame;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.managers.GuysManager;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.managers.IslandsManager;
import com.shansown.game.tests.ashley.managers.ShotStonesManager;

public class WorldSystem extends EntitySystem {

    private static final String TAG = "WorldSystem";

    public static final float MIN_WORLD_VIEWPORT_SIZE = 10f;
    public static final short ALL_FLAGS = -1;
    public static final short PLAYER_FLAG = 1<<14;
    public static final short ENEMY_FLAG = 1<<13;
    public static final short STONE_FLAG = 1<<12;
    public static final short ISLAND_FLAG = 1<<11;

    public static final Vector3 OUT_WORLD_V = new Vector3(100, 100, 100);
    public static final Matrix4 OUT_WORLD_M = new Matrix4().translate(OUT_WORLD_V);

    private Array<Disposable> disposables = new Array<>();
    private ModelBuilder modelBuilder = new ModelBuilder();

    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();

    private AshleyGame game;
    private PooledEngine engine;

    private ShotStonesManager shotStonesManager;
    private GuysManager guysManager;
    private IslandsManager islandsManager;

    private PerformanceCounter performanceCounter;

    private int maxSubSteps = 5;
    private float fixedTimeStep = 1f / 300f;

    private final btCollisionConfiguration collisionConfiguration;
    private final btCollisionDispatcher dispatcher;
    private final btBroadphaseInterface broadphase;
    private final btConstraintSolver solver;
    private final btDynamicsWorld dynamicsWorld;
    private final Vector3 gravity;

    private ClosestRayResultCallback pickCB;
    private WorldContactListener contactListener;

    public WorldSystem(int priority, AshleyGame game) {
        super(priority);
        this.game = game;

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        gravity = new Vector3(0f, -9.8f, 0f);
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(gravity);

        pickCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
        // Needs only to be instantiated
        contactListener = new WorldContactListener();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = (PooledEngine) engine;
    }

    public void createWorld() {
        initWorldManagers();
        createIsland(tmpV.setZero());
        createPlayerEntities();
        createEnemyEntities();
    }

    private void initWorldManagers() {
        shotStonesManager = new ShotStonesManager(modelBuilder, engine);
        disposables.add(shotStonesManager);

        guysManager = new GuysManager(game.assets, engine);
        disposables.add(guysManager);

        islandsManager = new IslandsManager(game.assets, engine);
        disposables.add(islandsManager);
    }

    private Entity createIsland(Vector3 position) {
        return islandsManager.obtain(position);
    }

    private void createPlayerEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, 3));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        obtainGuy(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(7, 0, -1));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        obtainGuy(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, -5));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        obtainGuy(tmpM, true);
    }

    private void createEnemyEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, 3));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        obtainGuy(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-7, 0, -1));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        obtainGuy(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, -5));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        obtainGuy(tmpM, false);
    }

    private Entity obtainGuy(Matrix4 transform, boolean isPlayer) {
        return guysManager.obtain(transform, isPlayer);
    }

    public Entity obtainShotStone(Vector3 position, boolean forPlayer) {
        return shotStonesManager.obtain(position, forPlayer);
    }

    public Vector3 getTerrainY(Vector3 out) {
        Gdx.app.log(TAG, "getTerrainY start: " + out);
        //TODO: think up with implementation because method below isn't relevant
        /*tmpRay.set(out.x, 10, out.z, 0, -1, 0);
        Vector3 rayFrom = tmpRay.origin;
        Vector3 rayTo = terrainVector.set(tmpRay.direction).scl(50f).add(tmpRay.origin); // 50 meters max from the origin
        // Because we reuse the ClosestRayResultCallback, we need reset it's values
        terrainCB.setCollisionObject(null);
        terrainCB.setClosestHitFraction(1f);
        terrainCB.setRayFromWorld(rayFrom);
        terrainCB.setRayToWorld(rayTo);

        collisionWorld.rayTest(rayFrom, rayTo, terrainCB);

        if (terrainCB.hasHit()) {
            terrainCB.getHitPointWorld(terrainVector);
            Gdx.app.log("Test", "got height map point: " + terrainVector);
            out.y = terrainVector.y;
        }*/

        out.y = 0;
        Gdx.app.log(TAG, "getTerrainY end: " + out);
        return out;
    }

    public PerformanceCounter initPerformanceCounter() {
        performanceCounter = new PerformanceCounter(getClass().getSimpleName());
        return performanceCounter;
    }

    public void addCollisionObject(btCollisionObject object) {
        dynamicsWorld.addCollisionObject(object);
    }

    public void addCollisionObject(btCollisionObject object, short group, short mask) {
        dynamicsWorld.addCollisionObject(object, group, mask);
    }

    public void addRigidBody(btRigidBody body) {
        dynamicsWorld.addRigidBody(body);
    }

    public void addRigidBody(btRigidBody body, short group, short mask) {
        dynamicsWorld.addRigidBody(body, group, mask);
    }

    public Entity bulletRayTest(Vector3 rayFrom, Vector3 rayTo) {
        // Because we reuse the ClosestRayResultCallback, we need reset it's values
        pickCB.setCollisionObject(null);
        pickCB.setClosestHitFraction(1f);
        pickCB.setRayFromWorld(rayFrom);
        pickCB.setRayToWorld(rayTo);

        Entity result = null;
        dynamicsWorld.rayTest(rayFrom, rayTo, pickCB);
        if (pickCB.hasHit()) {
            Gdx.app.log(TAG, "hasHit");
            result = (Entity) pickCB.getCollisionObject().userData;
        }
        return result;
    }

    public Vector3 getLatestHitWorld(Vector3 out) {
        pickCB.getHitPointWorld(out);
        return out;
    }

    public void setDebugDrawer(DebugDrawer debugDrawer) {
        dynamicsWorld.setDebugDrawer(debugDrawer);
    }

    public void debugDrawWorld() {
        dynamicsWorld.debugDrawWorld();
    }

    public void freeEntity(Entity entity) {
        if (Mappers.shotStone.has(entity)) {
            shotStonesManager.free(entity);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (performanceCounter != null) {
            performanceCounter.tick();
            performanceCounter.start();
        }
        dynamicsWorld.stepSimulation(deltaTime, maxSubSteps, fixedTimeStep);
        if (performanceCounter != null) performanceCounter.stop();
    }

    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        if (pickCB != null) {
            pickCB.dispose();
            pickCB = null;
        }
        dynamicsWorld.dispose();
        collisionConfiguration.dispose();
        dispatcher.dispose();
        broadphase.dispose();
        solver.dispose();
    }

    private class WorldContactListener extends ContactListener {
        @Override
        public void onContactStarted (btPersistentManifold manifold, boolean match0, boolean match1) {
            if (match0) {
                Gdx.app.log("WorldContactListener", "match0");
                final Entity shotStoneEntity = (Entity)(manifold.getBody0().userData);
                final Entity entity = (Entity) manifold.getBody1().userData;
                processCollision(shotStoneEntity, entity);
            }
            if (match1) {
                Gdx.app.log("WorldContactListener", "match1");
                final Entity shotStoneEntity = (Entity)(manifold.getBody1().userData);
                final Entity entity = (Entity) manifold.getBody0().userData;
                processCollision(shotStoneEntity, entity);
            }
        }

        private void processCollision(Entity shotStoneEntity, Entity entity) {
            if (Mappers.guy.has(entity)) {
                processGuyCollision(shotStoneEntity, entity);
            } else {
                processIslandCollision(shotStoneEntity, entity);
            }
        }

        private void processGuyCollision(Entity shotStoneEntity, Entity guyEntity) {
            ShotStoneComponent shotStone = Mappers.shotStone.get(shotStoneEntity);
            if (shotStone.state == ShotStoneComponent.State.DANGEROUS) {
                engine.getSystem(ShotStoneSystem.class).deactivate(shotStoneEntity);
                engine.getSystem(GuySystem.class).damage(guyEntity);
                engine.getSystem(InputSystem.class).checkUnpicking(guyEntity);
            }
        }

        private void processIslandCollision(Entity shotStoneEntity, Entity islandEntity) {
            ShotStoneComponent shotStone = Mappers.shotStone.get(shotStoneEntity);
            if (shotStone.state == ShotStoneComponent.State.DANGEROUS) {
                engine.getSystem(ShotStoneSystem.class).deactivate(shotStoneEntity);
            }
        }
    }
}