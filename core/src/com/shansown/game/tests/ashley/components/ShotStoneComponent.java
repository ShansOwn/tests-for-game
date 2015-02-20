package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;

public class ShotStoneComponent extends Component {

    public static final float BBOX_SPHERE_RADIUS = .1f;
    public static final float MASS = .1f;

    public static final float VISIBLE_RADIUS = .1f;
    public static final float SAFE_VISIBLE_TIME = 5f;

    public State state = State.IDLE;
    public float safeTime;

    public enum State {
        IDLE, DANGEROUS, SAFE
    }
}