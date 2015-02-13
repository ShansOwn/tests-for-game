package com.shansown.game.tests.slingshotfight.world.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.slingshotfight.world.GameWorld;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletEntity;

public class ShotStone extends GameEntity implements Pool.Poolable {

    private static final String TAG = "Stone";

    public static final float BBOX_SPHERE_RADIUS = .1f;
    public static final float MASS = .1f;

    private static final float VISIBLE_RADIUS = .1f;
    private static final float SAFE_VISIBLE_TIME = 5f;

    public State state = State.IDLE;

    private float safeTime;

    public enum State {
        IDLE, DANGEROUS, SAFE
    }

    public ShotStone(GameWorld world, BulletEntity copyEntity) {
        super(copyEntity.modelInstance, copyEntity.body);
        this.world = world;
        visibleRadius = VISIBLE_RADIUS;
    }

    public void init(Vector3 position, boolean forPlayer) {
        Gdx.app.log(TAG, "init: " + body.getUserValue());
        state = State.DANGEROUS;

        short group = forPlayer ? GameWorld.PLAYER_FLAG : GameWorld.ENEMY_FLAG;
        group |= GameWorld.STONE_FLAG;
        short mask = forPlayer ? GameWorld.PLAYER_FLAG :  GameWorld.ENEMY_FLAG;
        mask ^= GameWorld.ALL_FLAGS;

        body.getBroadphaseHandle().setCollisionFilterGroup(group);
        body.getBroadphaseHandle().setCollisionFilterMask(mask);

        body.setContactCallbackFlag(group);
        body.setContactCallbackFilter(mask);

        setColor(0.5f + 0.5f * (float) Math.random(), 0.5f + 0.5f * (float) Math.random(),
                0.5f + 0.5f * (float) Math.random(), 1f);

        transform.setTranslation(position);
        body.setWorldTransform(transform);
        body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        body.activate();
    }

    public void deactivate() {
        state = State.SAFE;
        body.setContactCallbackFilter(0);
    }

    @Override
    public void update(float delta) {
        switch (state) {
            case DANGEROUS:
                if (transform.getTranslation(tmpV).y < 0) {
                    state = State.SAFE;
                }
                break;
            case SAFE:
                safeTime += delta;
                if (safeTime >= SAFE_VISIBLE_TIME) {
                    state = State.IDLE;
                    safeTime = 0;
                }
                break;
        }
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset: " + body.getUserValue());
        body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        body.setActivationState(0);
        transform.setTranslation(GameWorld.OUT_WORLD);
        body.setWorldTransform(transform);
    }
}