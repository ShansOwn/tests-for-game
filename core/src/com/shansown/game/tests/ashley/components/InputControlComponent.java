package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class InputControlComponent extends Component implements Pool.Poolable {

    private static final String TAG = InputControlComponent.class.getSimpleName();

    public boolean picked;
    public boolean canPick;

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        picked = false;
        canPick = false;
    }
}
