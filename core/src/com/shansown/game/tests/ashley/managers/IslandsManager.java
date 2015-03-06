package com.shansown.game.tests.ashley.managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.physics.StaticComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;
import com.shansown.game.tests.ashley.systems.WorldSystem;
import com.shansown.game.tests.slingshotfight.reference.Models;

public class IslandsManager extends Manager implements Disposable {

    private static final String TAG = IslandsManager.class.getSimpleName();

    private Model model;

    private Array<Disposable> disposables = new Array<>();

    private btCollisionShape collisionShape;
    private Pool<BulletBodyHolder> bulletBodyPool = new Pool<BulletBodyHolder>() {
        @Override
        protected BulletBodyHolder newObject() {
            return createCollisionObject();
        }
    };

    public IslandsManager(AssetManager assets, PooledEngine engine) {
        super(engine);
        // Model loaded from assets, so we shouldn't dispose it manually
        model = assets.get(Models.ISLAND, Model.class);
        collisionShape = Bullet.obtainStaticNodeShape(model.nodes);
    }

    public Entity obtain(Vector3 position) {
        Entity entity = engine.createEntity();
        ModelInstance modelInstance = new ModelInstance(model, position);

        IslandComponent island = obtainIslandComponent();
        RenderComponent render = obtainRenderComponent(modelInstance, IslandComponent.VISIBLE_RADIUS);
        TransformComponent transform = obtainTransformComponent(modelInstance);
        StaticComponent statics = obtainStaticComponent(entity, modelInstance);

        entity.add(island)
                .add(render)
                .add(transform)
                .add(statics);
        engine.addEntity(entity);
        return entity;
    }

    public boolean free(Entity entity) {
        if (!Mappers.island.has(entity)) return false;
        StaticComponent statics = Mappers.statics.get(entity);
        bulletBodyPool.free(statics.getBodyHolder());
        engine.removeEntity(entity);
        return true;
    }

    private IslandComponent obtainIslandComponent() {
        return engine.createComponent(IslandComponent.class);
    }

    private StaticComponent obtainStaticComponent(Entity entity, ModelInstance modelInstance) {
        StaticComponent statics = engine.createComponent(StaticComponent.class);
        statics.init(bulletBodyPool.obtain());
        statics.object.userData = entity;
        statics.object.setWorldTransform(modelInstance.transform);
        statics.object.setFriction(10);
        statics.object.setContactCallbackFlag(WorldSystem.ISLAND_FLAG);
        statics.object.setContactCallbackFilter(0);
        return statics;
    }

    private BulletBodyHolder createCollisionObject() {
        btCollisionObject collisionObject = new btCollisionObject();
        disposables.add(collisionObject);
        collisionObject.setCollisionShape(collisionShape);
        engine.getSystem(WorldSystem.class).addCollisionObject(collisionObject);
        return new BulletBodyHolder(collisionObject);
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        collisionShape.dispose();
    }
}