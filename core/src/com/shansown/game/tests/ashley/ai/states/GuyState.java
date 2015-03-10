package com.shansown.game.tests.ashley.ai.states;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.GuyComponent;
import com.shansown.game.tests.ashley.components.ai.StateMachineComponent;
import com.shansown.game.tests.ashley.components.physics.KinematicComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;
import com.shansown.game.tests.ashley.systems.WorldSystem;

public enum GuyState implements State<Entity> {

    IDLE,
    STRING,
    SHOOT,
    DAMAGE {
        @Override
        public void update(Entity entity) {
            GuyComponent guy = Mappers.guy.get(entity);
            if (guy.stateTime >= GuyComponent.DAMAGE_TIME) {
                KinematicComponent kinematic = Mappers.kinematic.get(entity);
                short group = (short) (kinematic.body.getBroadphaseHandle().getCollisionFilterGroup()
                        | (guy.isPlayer ? WorldSystem.PLAYER_FLAG : WorldSystem.ENEMY_FLAG));
                kinematic.body.getBroadphaseHandle().setCollisionFilterGroup(group);

                StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
                stateMachine.stateMachine.changeState(GuyState.IDLE);
            }
        }
    },
    DEAD;

    private static final String TAG = GuyState.class.getSimpleName();

    private final Vector3 tmpV = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    @Override
    public void enter(Entity entity) {

    }

    @Override
    public void update(Entity entity) {

    }

    @Override
    public void exit(Entity entity) {
        GuyComponent guy = Mappers.guy.get(entity);
        guy.stateTime = 0;
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }

    private Entity shoot (Entity entity) {
        GuyComponent guy = Mappers.guy.get(entity);
        TransformComponent transform = Mappers.transform.get(entity);

        float shootImpulse = guy.stateTime * GuyComponent.SHOOT_IMPULSE_MAX;
        shootImpulse = MathUtils.clamp(shootImpulse, GuyComponent.SHOOT_IMPULSE_MIN, GuyComponent.SHOOT_IMPULSE_MAX);

        Gdx.app.log(TAG, "Shoot impulse: " + shootImpulse);

        Vector3 direction = tmpV.set(0, 0, -1).rot(transform.transform).nor();
        Vector3 origin = transform.transform.getTranslation(tmpV2);
        origin.y += GuyComponent.SHOOT_OFFSET * origin.y;
        origin.add(direction.scl(GuyComponent.BBOX_CAPSULE_RADIUS));

        Entity shotStoneEntity = world.obtainShotStone(origin, guy.isPlayer);
        Mappers.dynamic.get(shotStoneEntity).body.applyCentralImpulse(direction.scl(shootImpulse));

        return shotStoneEntity;
    }
}