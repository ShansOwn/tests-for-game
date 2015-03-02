package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.creators.BulletBodyHolder;

public class StaticComponent extends Component implements Pool.Poolable {

    private static final String TAG = "StaticComponent";

    private static final short DEFAULT_FILTER_GROUP = btBroadphaseProxy.CollisionFilterGroups.StaticFilter;
    private static final short DEFAULT_FILTER_MASK = ~btBroadphaseProxy.CollisionFilterGroups.StaticFilter;

    public short filterGroup = DEFAULT_FILTER_GROUP;
    public short filterMask = DEFAULT_FILTER_MASK;

    public btCollisionObject object;
    private BulletBodyHolder bodyHolder;

    public void init(BulletBodyHolder bodyHolder) {
        this.bodyHolder = bodyHolder;
        object = bodyHolder.body;
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        object = null;
    }

    public BulletBodyHolder getBodyHolder() {
        return bodyHolder;
    }
}