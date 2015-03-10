package com.shansown.game.tests.ashley.components.ai;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;

public class StateMachineComponent extends Component implements Telegraph {

    public DefaultStateMachine<Entity> stateMachine;
    public float stateTime = 0f;

    /** Can only be created by PooledEngine */
    private StateMachineComponent() {
        // private constructor
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return stateMachine != null && stateMachine.handleMessage(msg);
    }
}