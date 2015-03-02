package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.creators.BulletBodyHolder;
import com.shansown.game.tests.ashley.systems.WorldSystem;

public class KinematicComponent extends Component implements Pool.Poolable {

    private static final String TAG = KinematicComponent.class.getSimpleName();

    private static final short DEFAULT_FILTER_GROUP = btBroadphaseProxy.CollisionFilterGroups.StaticFilter;
    private static final short DEFAULT_FILTER_MASK = ~btBroadphaseProxy.CollisionFilterGroups.StaticFilter;

    public short filterGroup = DEFAULT_FILTER_GROUP;
    public short filterMask = DEFAULT_FILTER_MASK;

    public boolean moving = false;
    public Vector3 position = new Vector3();
    public Vector3 offset = new Vector3();
    public btRigidBody body;
    private BulletBodyHolder bodyHolder;

    public void init(BulletBodyHolder bodyHolder) {
        this.bodyHolder = bodyHolder;
        body = (btRigidBody) bodyHolder.body;
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        moving = false;
        position.set(WorldSystem.OUT_WORLD_V);
        offset.setZero();

        body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        body.setActivationState(0);
        body.setWorldTransform(WorldSystem.OUT_WORLD_M);
        body.userData = null;
    }

    public BulletBodyHolder getBodyHolder() {
        return bodyHolder;
    }
}