package com.shansown.game.tests.slingshotfight.world.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.shansown.game.tests.slingshotfight.world.GameWorld;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletEntity;

public class SlingshotGuy extends GameEntity {

    private static final String TAG = "SlingshotGuy";

    public static final float BBOX_CAPSULE_RADIUS = .4f;
    public static final float BBOX_CAPSULE_HEIGHT = 1f;
    public static final float OFFSET = 2 * BBOX_CAPSULE_RADIUS;

    private static final float VISIBLE_RADIUS = 1f;
    private static final float SHOOT_IMPULSE_MAX = 10f;
    private static final float SHOOT_IMPULSE_MIN = 2f;
    private static final float DAMAGE_TIME = 1f;

    public State state = State.IDLE;
    public boolean isPlayer;

    private float stringTime;
    private float damageTime;

    public enum State {
        IDLE, STRING, SHOOT, DAMAGE, DEAD
    }

    public SlingshotGuy(GameWorld world, BulletEntity copyEntity, boolean isPlayer) {
        super(copyEntity.modelInstance, copyEntity.body);
        this.world = world;
        visibleRadius = VISIBLE_RADIUS;
        this.isPlayer = isPlayer;
    }

    public void string() {
        state = State.STRING;
    }

    public BulletEntity shoot () {
        float shootImpulse = stringTime * SHOOT_IMPULSE_MAX;
        shootImpulse = MathUtils.clamp(shootImpulse, SHOOT_IMPULSE_MIN, SHOOT_IMPULSE_MAX);
        return shoot(shootImpulse);
    }

    private BulletEntity shoot (final float impulse) {
        Gdx.app.log(TAG, "Shoot impulse: " + impulse);
        state = State.SHOOT;
        stringTime = 0.0f;

        Vector3 direction = tmpV.set(0, 0, -1).rot(transform).nor();
        Vector3 origin = transform.getTranslation(tmpV2);
        origin.y += .25f * origin.y;
        origin.add(direction.scl(BBOX_CAPSULE_RADIUS));

        ShotStone shotStone = world.obtainShotStone(origin, isPlayer);

        ((btRigidBody) shotStone.body).applyCentralImpulse(direction.scl(impulse));
        state = State.IDLE;
        return shotStone;
    }

    public void moveTo(final Vector3 target, final Vector3 offset) {
        Gdx.app.log(TAG, "moveTo: " + target);
        target.x -= offset.x;
        target.y += OFFSET;
        target.z -= offset.z;
        // Clamp by ground size with some margin
//        target.x = MathUtils.clamp(target.x, -19, 19);
//        target.z = MathUtils.clamp(target.z, -19, 19);
        Gdx.app.log(TAG, "adjusted moveTo: " + target);
        transform.setTranslation(target);
        body.setWorldTransform(transform);
        // We don't disable deactivation for characters
        // (we don't do this: body.setActivationState(Collision.DISABLE_DEACTIVATION);)
        // so we should activate it manually when move it!
        body.activate();
    }

    public int damage() {
        state = State.DAMAGE;
        short group = (short) (body.getBroadphaseHandle().getCollisionFilterGroup()
                ^ (isPlayer ? GameWorld.PLAYER_FLAG : GameWorld.ENEMY_FLAG));
        body.getBroadphaseHandle().setCollisionFilterGroup(group);
        return 0;
    }

    @Override
    public void update(float delta) {
        switch (state) {
            case IDLE:
                break;
            case STRING:
                stringTime += delta;
                break;
            case SHOOT:
                break;
            case DAMAGE:
                damageTime += delta;
                if (damageTime >= DAMAGE_TIME) {
                    state = State.IDLE;
                    short group = (short) (body.getBroadphaseHandle().getCollisionFilterGroup()
                            | (isPlayer ? GameWorld.PLAYER_FLAG : GameWorld.ENEMY_FLAG));
                    body.getBroadphaseHandle().setCollisionFilterGroup(group);
                    damageTime = 0;
                }
                break;
            case DEAD:
                break;
        }
    }
}