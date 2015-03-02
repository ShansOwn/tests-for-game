package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
import com.shansown.game.tests.ashley.systems.WorldSystem;

public class TransformComponent extends Component implements Pool.Poolable {

    private static final String TAG = TransformComponent.class.getSimpleName();

    public Matrix4 transform = new Matrix4();

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        transform.set(WorldSystem.OUT_WORLD_M);
    }
}