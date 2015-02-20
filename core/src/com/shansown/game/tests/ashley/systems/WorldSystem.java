package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.shansown.game.tests.ashley.AshleyGame;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.reference.Models;

public class WorldSystem extends EntitySystem {

    private static final String TAG = "WorldSystem";

    public static final float MIN_WORLD_VIEWPORT_SIZE = 10f;
    public static final short ALL_FLAGS = -1;
    public static final short PLAYER_FLAG = 1<<14;
    public static final short ENEMY_FLAG = 1<<13;
    public static final short STONE_FLAG = 1<<12;
    public static final short ISLAND_FLAG = 1<<11;

    private Array<Disposable> disposables = new Array<>();
    private ModelBuilder modelBuilder = new ModelBuilder();

    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();

    private AshleyGame game;
    private Engine engine;

    public PerformanceCounter performanceCounter;

    private int maxSubSteps = 10;
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
        this.engine = engine;
    }

    public void createWorld() {
        createIsland(tmpV.setZero());
        createPlayerEntities();
        createEnemyEntities();
    }

    private Entity createIsland(Vector3 position) {
        Entity entity = new Entity();

        Model model = game.assets.get(Models.ISLAND, Model.class);
        ModelInstance modelInstance = new ModelInstance(model, position);
        btCollisionShape shape = Bullet.obtainStaticNodeShape(model.nodes);

        RenderComponent render = new RenderComponent();
        render.modelInstance = modelInstance;

        TransformComponent transform = new TransformComponent();
        transform.transform = modelInstance.transform;
        transform.transform.rotate(Vector3.Y, 30);

        StaticComponent statics = new StaticComponent();
        statics.object.userData = entity;
        statics.object.setCollisionShape(shape);
        statics.object.setWorldTransform(modelInstance.transform);
        statics.object.setFriction(10);
        statics.object.setContactCallbackFlag(ISLAND_FLAG);
        statics.object.setContactCallbackFilter(0);

        addCollisionObject(statics.object);

        entity.add(render);
        entity.add(transform);
        entity.add(statics);

        engine.addEntity(entity);
        return entity;
    }

    private void createPlayerEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, 3));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createGuy(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(7, 0, -1));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createGuy(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, -5));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createGuy(tmpM, true);
    }

    private void createEnemyEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, 3));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createGuy(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-7, 0, -1));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createGuy(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, -5));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createGuy(tmpM, false);
    }

    private Entity createGuy(Matrix4 position, boolean isPlayer) {
        position.trn(0, GuyComponent.OFFSET_Y, 0);
        Entity entity = new Entity();

        Model model = game.assets.get(Models.SLINGSHOT_GUY, Model.class);
        ModelInstance modelInstance = new ModelInstance(model, position.cpy());
        btCapsuleShape shape = new btCapsuleShape(GuyComponent.BBOX_CAPSULE_RADIUS, GuyComponent.BBOX_CAPSULE_HEIGHT);
        btRigidBody.btRigidBodyConstructionInfo bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(GuyComponent.MASS, null, shape, Vector3.Zero);

        RenderComponent render = new RenderComponent();
        render.modelInstance = modelInstance;
        render.visibleRadius = GuyComponent.VISIBLE_RADIUS;

        TransformComponent transform = new TransformComponent();
        transform.transform = modelInstance.transform;

        KinematicComponent kinematic = new KinematicComponent();
        kinematic.body = new btRigidBody(bodyInfo);
        kinematic.body.setWorldTransform(transform.transform);
        kinematic.body.userData = entity;

        int collisionFlags = kinematic.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT;
        if (isPlayer) {
            collisionFlags |= PLAYER_FLAG;
        } else {
            collisionFlags |= ENEMY_FLAG;
        }
        kinematic.body.setCollisionFlags(collisionFlags);

        InputControlComponent inputControl = new InputControlComponent();
        inputControl.canPick = isPlayer;

        GuyComponent guy = new GuyComponent();
        guy.isPlayer = isPlayer;

        short collisionFilterGroup;
        short collisionFilterMask = ALL_FLAGS;

        if (isPlayer) {
            collisionFilterGroup = PLAYER_FLAG;
        } else {
            collisionFilterGroup = ENEMY_FLAG;
        }

        addRigidBody(kinematic.body, collisionFilterGroup, collisionFilterMask);

        entity.add(render)
                .add(transform)
                .add(kinematic)
                .add(inputControl)
                .add(guy);

        engine.addEntity(entity);
        return entity;
    }

    public Entity obtainShotStone(Vector3 position, boolean forPlayer) {
        return createShotStone(position, forPlayer);


//        ShotStone shotStone = shotStonesPool.obtain();
//        shotStone.init(position, forPlayer);
//        activeShotStones.add(shotStone);
    }

    private Entity createShotStone(Vector3 position, boolean forPlayer) {
        Entity entity = new Entity();

        final Model model = modelBuilder.createSphere(ShotStoneComponent.BBOX_SPHERE_RADIUS,
                ShotStoneComponent.BBOX_SPHERE_RADIUS,
                ShotStoneComponent.BBOX_SPHERE_RADIUS,
                5, 5, new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        ModelInstance modelInstance = new ModelInstance(model, position);
        final BoundingBox boundingBox = new BoundingBox();
        model.calculateBoundingBox(boundingBox);
        Vector3 dimensions = new Vector3();
        boundingBox.getDimensions(dimensions);
        btBoxShape shape = new btBoxShape(tmpV.set(dimensions.x * 0.5f, dimensions.y * 0.5f, dimensions.z * 0.5f));
        shape.calculateLocalInertia(ShotStoneComponent.MASS, tmpV);
        Vector3 localInertia = tmpV;
        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(ShotStoneComponent.MASS, null, shape, localInertia);

        ShotStoneComponent shotStone = new ShotStoneComponent();
        shotStone.state = ShotStoneComponent.State.DANGEROUS;

        RenderComponent render = new RenderComponent();
        render.modelInstance = modelInstance;
        render.visibleRadius = ShotStoneComponent.VISIBLE_RADIUS;
        render.setColor(0.5f + 0.5f * (float) Math.random(), 0.5f + 0.5f * (float) Math.random(),
                0.5f + 0.5f * (float) Math.random(), 1f);

        TransformComponent transform = new TransformComponent();
        transform.transform = modelInstance.transform;

        DynamicComponent dynamic = new DynamicComponent();
        dynamic.body = new btRigidBody(bodyInfo);
        dynamic.body.setWorldTransform(transform.transform);
        dynamic.body.userData = entity;

        short group = forPlayer ? PLAYER_FLAG : ENEMY_FLAG;
        group |= STONE_FLAG;
        short mask = forPlayer ? PLAYER_FLAG :  ENEMY_FLAG;
        mask ^= ALL_FLAGS;

        dynamic.body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);


        dynamic.body.setContactCallbackFlag(group);
        dynamic.body.setContactCallbackFilter(mask);

        /*transform.setTranslation(position);
        body.setWorldTransform(transform);
        body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        body.activate();*/

        addRigidBody(dynamic.body, group, mask);

//        dynamic.body.getBroadphaseHandle().setCollisionFilterGroup(group);
//        dynamic.body.getBroadphaseHandle().setCollisionFilterMask(mask);

        entity.add(shotStone)
                .add(render)
                .add(transform)
                .add(dynamic);

        engine.addEntity(entity);
        return entity;
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

    private void addCollisionObject(btCollisionObject object) {
        dynamicsWorld.addCollisionObject(object);
    }

    private void addCollisionObject(btCollisionObject object, short group, short mask) {
        dynamicsWorld.addCollisionObject(object, group, mask);
    }

    private void addRigidBody(btRigidBody body) {
        dynamicsWorld.addRigidBody(body);
    }

    private void addRigidBody(btRigidBody body, short group, short mask) {
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