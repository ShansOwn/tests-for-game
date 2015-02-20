package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.DynamicComponent;
import com.shansown.game.tests.ashley.components.ShotStoneComponent;
import com.shansown.game.tests.ashley.components.TransformComponent;

public class ShotStoneSystem extends IteratingSystem {

    private static final String TAG = "ShotStoneSystem";

    private final Vector3 tmpV = new Vector3();

    public ShotStoneSystem(int priority) {
        super(Family.getFor(ShotStoneComponent.class), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ShotStoneComponent shotStone = Mappers.shotStone.get(entity);
        TransformComponent transform = Mappers.transform.get(entity);
        switch (shotStone.state) {
            case DANGEROUS:
                if (transform.transform.getTranslation(tmpV).y < 0) {
                    shotStone.state = ShotStoneComponent.State.SAFE;
                }
                break;
            case SAFE:
                shotStone.safeTime += deltaTime;
                if (shotStone.safeTime >= ShotStoneComponent.SAFE_VISIBLE_TIME) {
                    shotStone.state = ShotStoneComponent.State.IDLE;
                    shotStone.safeTime = 0;
                }
                break;
        }
    }

    public void deactivate(Entity entity) {
        if (!Mappers.shotStone.has(entity)) return;
        ShotStoneComponent shotStone = Mappers.shotStone.get(entity);
        DynamicComponent dynamic = Mappers.dynamic.get(entity);
        shotStone.state = ShotStoneComponent.State.SAFE;
        dynamic.body.setContactCallbackFilter(0);
    }
}