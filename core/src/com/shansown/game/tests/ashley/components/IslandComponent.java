package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class IslandComponent extends Component implements Pool.Poolable {

    private static final String TAG = IslandComponent.class.getSimpleName();

    public static final float VISIBLE_RADIUS = 10f;

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
    }
}
