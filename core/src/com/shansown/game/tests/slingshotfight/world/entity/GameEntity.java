package com.shansown.game.tests.slingshotfight.world.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.shansown.game.tests.slingshotfight.world.GameWorld;
import com.shansown.game.tests.slingshotfight.world.bullet.BaseEntity;
import com.shansown.game.tests.slingshotfight.world.bullet.BaseWorld;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletEntity;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletWorld;

public abstract class GameEntity extends BulletEntity {

    public int id;
    public float visibleRadius;

    protected GameEntity(Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, float x, float y, float z) {
        this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), x, y, z);
    }

    protected GameEntity(Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, Matrix4 transform) {
        this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), transform);
    }

    protected GameEntity(Model model, btCollisionObject body, float x, float y, float z) {
        this(model, body, tmpM.setToTranslation(x, y, z));
    }

    protected GameEntity(Model model, btCollisionObject body, Matrix4 transform) {
        this(new ModelInstance(model, transform.cpy()), body);
    }

    protected GameEntity(ModelInstance modelInstance, btCollisionObject body) {
        super(modelInstance, body);
    }


    public abstract void update(final GameWorld world, float delta);
}
