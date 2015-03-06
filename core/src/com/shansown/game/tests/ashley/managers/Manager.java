package com.shansown.game.tests.ashley.managers;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.shansown.game.tests.ashley.components.InputControlComponent;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;

abstract class Manager {

    protected PooledEngine engine;

    public Manager(PooledEngine engine) {
        this.engine = engine;
    }

    protected RenderComponent obtainRenderComponent(ModelInstance modelInstance, float visibleRadius) {
        RenderComponent render = engine.createComponent(RenderComponent.class);
        render.modelInstance = modelInstance;
        render.visibleRadius = visibleRadius;
        return render;
    }

    protected TransformComponent obtainTransformComponent(ModelInstance modelInstance) {
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.transform = modelInstance.transform;
        return transform;
    }

    protected InputControlComponent obtainInputControlComponent() {
        InputControlComponent inputControl = engine.createComponent(InputControlComponent.class);
        inputControl.canPick = true;
        return inputControl;
    }
}