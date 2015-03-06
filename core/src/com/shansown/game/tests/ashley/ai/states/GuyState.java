package com.shansown.game.tests.ashley.ai.states;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

public enum GuyState implements State<Entity> {

    IDLE, STRING, SHOOT, DAMAGE, DEAD;

    @Override
    public void enter(Entity entity) {

    }

    @Override
    public void update(Entity entity) {

    }

    @Override
    public void exit(Entity entity) {

    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}