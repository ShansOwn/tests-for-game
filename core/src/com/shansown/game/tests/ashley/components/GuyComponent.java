package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class GuyComponent extends Component implements Pool.Poolable {

    private static final String TAG = GuyComponent.class.getSimpleName();

    public static final float BBOX_CAPSULE_RADIUS = .4f;
    public static final float BBOX_CAPSULE_HEIGHT = 1f;
    public static final float OFFSET_Y = 2 * BBOX_CAPSULE_RADIUS;
    public static final float SHOOT_OFFSET = .25f;

    public static final float VISIBLE_RADIUS = 1f;
    public static final float SHOOT_IMPULSE_MAX = 10f;
    public static final float SHOOT_IMPULSE_MIN = 2f;
    public static final float DAMAGE_TIME = 1f;
    public static final float MASS = 0f;

//    public State state = State.IDLE;
    public boolean isPlayer;
    public float stateTime;
//    public float stringTime;
//    public float damageTime;

    /*public enum State {
        IDLE, STRING, SHOOT, DAMAGE, DEAD
    }*/

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
//        state = State.DEAD;
        stateTime = 0f;
//        stringTime = 0f;
//        damageTime = 0f;
    }
}