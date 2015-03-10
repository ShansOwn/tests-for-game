package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.ai.StateMachineComponent;

public class StateMachineSystem extends IteratingSystem {

    public StateMachineSystem() {
        super(Family.all(StateMachineComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
        stateMachine.stateTime += deltaTime;
        stateMachine.stateMachine.update();
    }
}
