package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.GuyComponent;
import com.shansown.game.tests.ashley.components.physics.KinematicComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;

public class KinematicSystem extends IteratingSystem {

    public KinematicSystem(int priority) {
        super(Family.all(KinematicComponent.class, TransformComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        if (kinematic.moving) {
            moveEntity(entity);
            kinematic.moving = false;
        }
    }

    private void moveEntity(Entity entity) {
        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        TransformComponent transform = Mappers.transform.get(entity);

        Vector3 position = kinematic.position;
        Vector3 offset = kinematic.offset;
        position.x -= offset.x;
        position.z -= offset.z;

        transform.transform.setTranslation(adjustPosition(entity, position));
        kinematic.body.setWorldTransform(transform.transform);
        kinematic.body.activate();
    }

    private Vector3 adjustPosition(Entity entity, Vector3 position) {
        if (Mappers.guy.has(entity)) {
            position.y += GuyComponent.OFFSET_Y;
        }
        return position;
    }
}