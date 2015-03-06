package com.shansown.game.tests.ashley.managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.physics.DynamicComponent;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.ShotStoneComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;
import com.shansown.game.tests.ashley.systems.WorldSystem;

public class ShotStonesManager extends Manager implements Disposable {

    private static final String TAG = ShotStonesManager.class.getSimpleName();

    private Model model;

    private Array<Disposable> disposables = new Array<>();

    private btRigidBody.btRigidBodyConstructionInfo bodyInfo;
    private Pool<BulletBodyHolder> bulletBodyPool = new Pool<BulletBodyHolder>() {
        @Override
        protected BulletBodyHolder newObject() {
            return createRigidBody();
        }
    };

    public ShotStonesManager(ModelBuilder modelBuilder, PooledEngine engine) {
        super(engine);

        // Model created manually, so we should dispose it manually to
        model = modelBuilder.createSphere(ShotStoneComponent.BBOX_SPHERE_RADIUS,
                ShotStoneComponent.BBOX_SPHERE_RADIUS,
                ShotStoneComponent.BBOX_SPHERE_RADIUS,
                5, 5, new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(model);

        final BoundingBox boundingBox = new BoundingBox();
        model.calculateBoundingBox(boundingBox);
        Vector3 dimensions = new Vector3();
        boundingBox.getDimensions(dimensions);
        Vector3 tmpV = new Vector3();
        btBoxShape shape = new btBoxShape(tmpV.set(dimensions.x * 0.5f, dimensions.y * 0.5f, dimensions.z * 0.5f));
        shape.calculateLocalInertia(ShotStoneComponent.MASS, tmpV);
        bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(ShotStoneComponent.MASS, null, shape, tmpV);
    }

    public Entity obtain(Vector3 position, boolean forPlayer) {
        Entity entity = engine.createEntity();
        ModelInstance modelInstance = new ModelInstance(model, position);

        ShotStoneComponent shotStone = obtainShotStoneComponent();
        RenderComponent render = obtainRenderComponent(modelInstance, ShotStoneComponent.VISIBLE_RADIUS);
        TransformComponent transform = obtainTransformComponent(modelInstance);
        DynamicComponent dynamic = obtainDynamicComponent(entity, transform, forPlayer);

        entity.add(shotStone)
                .add(render)
                .add(transform)
                .add(dynamic);
        engine.addEntity(entity);
        return entity;
    }

    public boolean free(Entity entity) {
        if (!Mappers.shotStone.has(entity)) return false;
        DynamicComponent dynamic = Mappers.dynamic.get(entity);
        bulletBodyPool.free(dynamic.getBodyHolder());
        engine.removeEntity(entity);
        return true;
    }

    private ShotStoneComponent obtainShotStoneComponent() {
        ShotStoneComponent shotStone = engine.createComponent(ShotStoneComponent.class);
        shotStone.state = ShotStoneComponent.State.DANGEROUS;
        return shotStone;
    }

    private DynamicComponent obtainDynamicComponent(Entity entity, TransformComponent transform, boolean forPlayer) {
        DynamicComponent dynamic = engine.createComponent(DynamicComponent.class);
        dynamic.init(bulletBodyPool.obtain());
        dynamic.body.setWorldTransform(transform.transform);
        dynamic.body.userData = entity;

        short group = forPlayer ? WorldSystem.PLAYER_FLAG : WorldSystem.ENEMY_FLAG;
        group |= WorldSystem.STONE_FLAG;
        short mask = forPlayer ? WorldSystem.PLAYER_FLAG :  WorldSystem.ENEMY_FLAG;
        mask ^= WorldSystem.ALL_FLAGS;
        dynamic.filterGroup = group;
        dynamic.filterMask = mask;

        dynamic.body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        dynamic.body.setContactCallbackFlag(dynamic.filterGroup);
        dynamic.body.setContactCallbackFilter(dynamic.filterMask);
        dynamic.body.getBroadphaseHandle().setCollisionFilterGroup(dynamic.filterGroup);
        dynamic.body.getBroadphaseHandle().setCollisionFilterMask(dynamic.filterMask);

        dynamic.body.activate();
        Gdx.app.log(TAG, "obtain bullet body: " + dynamic.body.getCPointer());
        return dynamic;
    }

    private BulletBodyHolder createRigidBody() {
        btRigidBody body = new btRigidBody(bodyInfo);
        Gdx.app.log(TAG, "create bullet body: " + body.getCPointer());
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