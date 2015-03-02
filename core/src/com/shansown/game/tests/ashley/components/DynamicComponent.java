package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.creators.BulletBodyHolder;

public class DynamicComponent extends Component implements Pool.Poolable {

    private static final String TAG = DynamicComponent.class.getSimpleName();

    private static final short DEFAULT_FILTER_GROUP = btBroadphaseProxy.CollisionFilterGroups.DefaultFilter;
    private static final short DEFAULT_FILTER_MASK = btBroadphaseProxy.CollisionFilterGroups.AllFilter;

    public short filterGroup = DEFAULT_FILTER_GROUP;
    public short filterMask = DEFAULT_FILTER_MASK;

    public btRigidBody body;
    private BulletBodyHolder bodyHolder;

    public void init(BulletBodyHolder bodyHolder) {
        this.bodyHolder = bodyHolder;
        body = (btRigidBody) bodyHolder.body;
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        bodyHolder = null;
        body = null;
    }

    public BulletBodyHolder getBodyHolder() {
        return bodyHolder;
    }
}