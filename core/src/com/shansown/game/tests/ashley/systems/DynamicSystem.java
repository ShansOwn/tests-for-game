package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.DynamicComponent;
import com.shansown.game.tests.ashley.components.TransformComponent;

public class DynamicSystem extends IteratingSystem {

    private static final String TAG = "RigidBodySystem";

    public DynamicSystem(int priority) {
        super(Family.getFor(DynamicComponent.class, TransformComponent.class), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = Mappers.transform.get(entity);
        DynamicComponent dynamic = Mappers.dynamic.get(entity);
        dynamic.body.getWorldTransform(transform.transform);
    }
}