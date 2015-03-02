package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class ShotStoneComponent extends Component implements Pool.Poolable {

    private static final String TAG = ShotStoneComponent.class.getSimpleName();

    public static final float BBOX_SPHERE_RADIUS = .1f;
    public static final float MASS = .1f;

    public static final float VISIBLE_RADIUS = .1f;
    public static final float SAFE_VISIBLE_TIME = 5f;

    public State state = State.IDLE;
    public float safeTime;

    public enum State {
        IDLE, DANGEROUS, SAFE
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        state = State.SAFE;
        safeTime = 0f;
    }
}