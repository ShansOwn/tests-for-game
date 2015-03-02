package com.shansown.game.tests.ashley.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.systems.WorldSystem;
import com.shansown.game.tests.slingshotfight.reference.Models;

public class GuyCreator implements Disposable {

    private static final String TAG = GuyCreator.class.getSimpleName();

    private Model model;
    private PooledEngine engine;

    private Array<Disposable> disposables = new Array<>();

    private btRigidBody.btRigidBodyConstructionInfo bodyInfo;
    private Pool<BulletBodyHolder> bulletBodyPool = new Pool<BulletBodyHolder>() {
        @Override
        protected BulletBodyHolder newObject() {
            return createRigidBody();
        }
    };

    public GuyCreator(AssetManager assets, PooledEngine engine) {
        this.engine = engine;
        // Model loaded from assets, so we shouldn't dispose it manually
        model = assets.get(Models.SLINGSHOT_GUY, Model.class);
        btCapsuleShape shape = new btCapsuleShape(GuyComponent.BBOX_CAPSULE_RADIUS, GuyComponent.BBOX_CAPSULE_HEIGHT);
        bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(GuyComponent.MASS, null, shape, Vector3.Zero);
    }

    public Entity obtain(Matrix4 trans, boolean isPlayer) {
        Entity entity = engine.createEntity();
        trans.trn(0, GuyComponent.OFFSET_Y, 0);
        ModelInstance modelInstance = new ModelInstance(model, trans.cpy());

        GuyComponent guy = obtainGuyComponent(isPlayer);
        RenderComponent render = obtainRenderComponent(modelInstance);
        TransformComponent transform = obtainTransformComponent(modelInstance);
        KinematicComponent kinematic = obtainKinematicComponent(entity, transform, isPlayer);
        InputControlComponent inputControl = obtainInputControlComponent(isPlayer);

        entity.add(render)
                .add(transform)
                .add(kinematic)
                .add(inputControl)
                .add(guy);
        engine.addEntity(entity);
        return entity;
    }

    public boolean free(Entity entity) {
        if (!Mappers.guy.has(entity)) return false;
        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        bulletBodyPool.free(kinematic.getBodyHolder());
        engine.removeEntity(entity);
        return true;
    }

    private GuyComponent obtainGuyComponent(boolean isPlayer) {
        GuyComponent guy = engine.createComponent(GuyComponent.class);
        guy.isPlayer = isPlayer;
        guy.state = GuyComponent.State.IDLE;
        return guy;
    }

    private RenderComponent obtainRenderComponent(ModelInstance modelInstance) {
        RenderComponent render = engine.createComponent(RenderComponent.class);
        render.modelInstance = modelInstance;
        render.visibleRadius = GuyComponent.VISIBLE_RADIUS;
        return render;
    }

    private TransformComponent obtainTransformComponent(ModelInstance modelInstance) {
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.transform = modelInstance.transform;
        return transform;
    }

    private KinematicComponent obtainKinematicComponent(Entity entity, TransformComponent transform, boolean isPlayer) {
        KinematicComponent kinematic = engine.createComponent(KinematicComponent.class);
        kinematic.init(bulletBodyPool.obtain());
        kinematic.body.setWorldTransform(transform.transform);
        kinematic.body.userData = entity;

        int collisionFlags = kinematic.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT;
        if (isPlayer) {
            collisionFlags |= WorldSystem.PLAYER_FLAG;
        } else {
            collisionFlags |= WorldSystem.ENEMY_FLAG;
        }
        kinematic.body.setCollisionFlags(collisionFlags);

        short collisionFilterGroup;
        short collisionFilterMask = WorldSystem.ALL_FLAGS;
        if (isPlayer) {
            collisionFilterGroup = WorldSystem.PLAYER_FLAG;
        } else {
            collisionFilterGroup = WorldSystem.ENEMY_FLAG;
        }
        kinematic.filterGroup = collisionFilterGroup;
        kinematic.filterMask = collisionFilterMask;

        kinematic.body.getBroadphaseHandle().setCollisionFilterGroup(kinematic.filterGroup);
        kinematic.body.getBroadphaseHandle().setCollisionFilterMask(kinematic.filterMask);

        return kinematic;
    }

    private InputControlComponent obtainInputControlComponent(boolean isPlayer) {
        InputControlComponent inputControl = engine.createComponent(InputControlComponent.class);
        inputControl.canPick = isPlayer;
        return inputControl;
    }

    private BulletBodyHolder createRigidBody() {
        btRigidBody body = new btRigidBody(bodyInfo);
        disposables.add(body);
        engine.getSystem(WorldSystem.class).addRigidBody(body);
        return new BulletBodyHolder(body);
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        bulletBodyPool.clear();
        bodyInfo.dispose();
    }
}