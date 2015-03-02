package com.shansown.game.tests.ashley.creators;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.systems.WorldSystem;

public class BulletBodyHolder implements Pool.Poolable {

    private static final String TAG = BulletBodyHolder.class.getSimpleName();

    private static final short DEFAULT_DYNAMIC_FILTER_GROUP = btBroadphaseProxy.CollisionFilterGroups.DefaultFilter;
    private static final short DEFAULT_DYNAMIC_FILTER_MASK = btBroadphaseProxy.CollisionFilterGroups.AllFilter;

    private static final short DEFAULT_STATIC_FILTER_GROUP = btBroadphaseProxy.CollisionFilterGroups.StaticFilter;
    private static final short DEFAULT_STATIC_FILTER_MASK = ~btBroadphaseProxy.CollisionFilterGroups.StaticFilter;

    public btCollisionObject body;

    public BulletBodyHolder(btCollisionObject body) {
        this.body = body;
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset bullet body: " + body.getCPointer());
        body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        body.setActivationState(0);
        body.setWorldTransform(WorldSystem.OUT_WORLD_M);

        short filterGroup;
        short filterMask;
        if (body instanceof btRigidBody) {
            filterGroup = DEFAULT_DYNAMIC_FILTER_GROUP;
            filterMask = DEFAULT_DYNAMIC_FILTER_MASK;
        } else {
            filterGroup = DEFAULT_STATIC_FILTER_GROUP;
            filterMask = DEFAULT_STATIC_FILTER_MASK;
        }
        body.getBroadphaseHandle().setCollisionFilterGroup(filterGroup);
        body.getBroadphaseHandle().setCollisionFilterMask(filterMask);

        body.userData = null;
    }
}