package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.GuyComponent;
import com.shansown.game.tests.ashley.components.KinematicComponent;
import com.shansown.game.tests.ashley.components.RenderComponent;
import com.shansown.game.tests.ashley.components.TransformComponent;
import com.shansown.game.tests.slingshotfight.world.GameWorld;

public class GuySystem extends IteratingSystem {

    private static final String TAG = GuySystem.class.getSimpleName();

    private final Vector3 tmpV = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    private WorldSystem world;

    public GuySystem(int priority) {
        super(Family.getFor(GuyComponent.class, KinematicComponent.class), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        world = engine.getSystem(WorldSystem.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        GuyComponent guy = Mappers.guy.get(entity);
        switch (guy.state) {
            case IDLE:
                break;
            case STRING:
                guy.stringTime += deltaTime;
                break;
            case SHOOT:
                shoot(entity);
                guy.state = GuyComponent.State.IDLE;
                break;
            case DAMAGE:
                guy.damageTime += deltaTime;
                if (guy.damageTime >= GuyComponent.DAMAGE_TIME) {
                    guy.state = GuyComponent.State.IDLE;
                    KinematicComponent kinematic = Mappers.kinematic.get(entity);
                    short group = (short) (kinematic.body.getBroadphaseHandle().getCollisionFilterGroup()
                            | (guy.isPlayer ? WorldSystem.PLAYER_FLAG : WorldSystem.ENEMY_FLAG));
                    kinematic.body.getBroadphaseHandle().setCollisionFilterGroup(group);
                    guy.damageTime = 0;
                }
                break;
            case DEAD:
                break;
        }
    }

    private Entity shoot (Entity entity) {
        GuyComponent guy = Mappers.guy.get(entity);
        TransformComponent transform = Mappers.transform.get(entity);

        float shootImpulse = guy.stringTime * GuyComponent.SHOOT_IMPULSE_MAX;
        shootImpulse = MathUtils.clamp(shootImpulse, GuyComponent.SHOOT_IMPULSE_MIN, GuyComponent.SHOOT_IMPULSE_MAX);

        Gdx.app.log(TAG, "Shoot impulse: " + shootImpulse);
        guy.state = GuyComponent.State.IDLE;
        guy.stringTime = 0f;

        Vector3 direction = tmpV.set(0, 0, -1).rot(transform.transform).nor();
        Vector3 origin = transform.transform.getTranslation(tmpV2);
        origin.y += GuyComponent.SHOOT_OFFSET * origin.y;
        origin.add(direction.scl(GuyComponent.BBOX_CAPSULE_RADIUS));

        Entity shotStoneEntity = world.obtainShotStone(origin, guy.isPlayer);
        Mappers.dynamic.get(shotStoneEntity).body.applyCentralImpulse(direction.scl(shootImpulse));

        return shotStoneEntity;
    }

    public int damage(Entity entity) {
        int result = -1;
        if (!Mappers.guy.has(entity)) return result;

        GuyComponent guy = Mappers.guy.get(entity);
        guy.state = GuyComponent.State.DAMAGE;

        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        short group = (short) (kinematic.body.getBroadphaseHandle().getCollisionFilterGroup()
                ^ (guy.isPlayer ? WorldSystem.PLAYER_FLAG : GameWorld.ENEMY_FLAG));
        kinematic.body.getBroadphaseHandle().setCollisionFilterGroup(group);

        RenderComponent render = Mappers.render.get(entity);
        render.setColor(0.5f + 0.5f * (float) Math.random(), 0.5f + 0.5f * (float) Math.random(),
                0.5f + 0.5f * (float) Math.random(), 1f);

        //TODO
        result = 0;
        return result;
    }
}