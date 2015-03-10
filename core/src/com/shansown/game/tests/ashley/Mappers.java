package com.shansown.game.tests.ashley;

import com.badlogic.ashley.core.ComponentMapper;
import com.shansown.game.tests.ashley.components.*;
import com.shansown.game.tests.ashley.components.ai.StateMachineComponent;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.physics.DynamicComponent;
import com.shansown.game.tests.ashley.components.physics.KinematicComponent;
import com.shansown.game.tests.ashley.components.physics.StaticComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;

public class Mappers {
    // Models
    public static final ComponentMapper<IslandComponent> island = ComponentMapper.getFor(IslandComponent.class);
    public static final ComponentMapper<GuyComponent> guy = ComponentMapper.getFor(GuyComponent.class);
    public static final ComponentMapper<ShotStoneComponent> shotStone = ComponentMapper.getFor(ShotStoneComponent.class);

    // Physics
    public static final ComponentMapper<DynamicComponent> dynamic = ComponentMapper.getFor(DynamicComponent.class);
    public static final ComponentMapper<KinematicComponent> kinematic = ComponentMapper.getFor(KinematicComponent.class);
    public static final ComponentMapper<StaticComponent> statics = ComponentMapper.getFor(StaticComponent.class);

    // AI
    public static final ComponentMapper<StateMachineComponent> stateMachine = ComponentMapper.getFor(StateMachineComponent.class);

    // General
    public static final ComponentMapper<InputControlComponent> inputControl = ComponentMapper.getFor(InputControlComponent.class);
    public static final ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
    public static final ComponentMapper<RenderComponent> render = ComponentMapper.getFor(RenderComponent.class);
}