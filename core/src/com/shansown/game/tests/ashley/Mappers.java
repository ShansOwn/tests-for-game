package com.shansown.game.tests.ashley;

import com.badlogic.ashley.core.ComponentMapper;
import com.shansown.game.tests.ashley.components.*;

public class Mappers {
    public static final ComponentMapper<IslandComponent> island = ComponentMapper.getFor(IslandComponent.class);
    public static final ComponentMapper<GuyComponent> guy = ComponentMapper.getFor(GuyComponent.class);
    public static final ComponentMapper<ShotStoneComponent> shotStone = ComponentMapper.getFor(ShotStoneComponent.class);

    public static final ComponentMapper<DynamicComponent> dynamic = ComponentMapper.getFor(DynamicComponent.class);
    public static final ComponentMapper<KinematicComponent> kinematic = ComponentMapper.getFor(KinematicComponent.class);
    public static final ComponentMapper<StaticComponent> statics = ComponentMapper.getFor(StaticComponent.class);

    public static final ComponentMapper<InputControlComponent> inputControl = ComponentMapper.getFor(InputControlComponent.class);
    public static final ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
    public static final ComponentMapper<RenderComponent> render = ComponentMapper.getFor(RenderComponent.class);
}