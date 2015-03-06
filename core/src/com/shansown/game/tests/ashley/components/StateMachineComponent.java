package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;

public class StateMachineComponent extends Component {

    public DefaultStateMachine<Entity> stateMachine;
}
