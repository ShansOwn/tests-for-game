package com.shansown.game.tests.ashley.managers;

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
import com.badlogic.gdx.utils.*;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.physics.KinematicComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;
import com.shansown.game.tests.ashley.systems.WorldSystem;
import com.shansown.game.tests.slingshotfight.reference.Models;

public class GuysManager extends Manager implements Disposable {

    private static final String TAG = GuysManager.class.getSimpleName();

    private Model model;

    private Array<Disposable> disposables = new Array<>();

    private btRigidBody.btRigidBodyConstructionInfo bodyInfo;
    private Pool<BulletBodyHolder> bulletBodyPool = new Pool<BulletBodyHolder>() {
        @Override
        protected BulletBodyHolder newObject() {
            return createRigidBody();
        }
    };

    public GuysManager(AssetManager assets, PooledEngine engine) {
        super(engine);
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
        RenderComponent render = obtainRenderComponent(modelInstance, GuyComponent.VISIBLE_RADIUS);
        TransformComponent transform = obtainTransformComponent(modelInstance);
        KinematicComponent kinematic = obtainKinematicComponent(entity, transform, isPlayer);

        entity.add(guy)
                .add(render)
                .add(transform)
                .add(kinematic);
        if (isPlayer) entity.add(obtainInputControlComponent());

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