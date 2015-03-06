package com.shansown.game.tests.ashley.components.graphics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Pool;

public class RenderComponent extends Component implements Pool.Poolable {

    private static final String TAG = RenderComponent.class.getSimpleName();

    private final Color tmpC = new Color();

    public float visibleRadius = 1f;
    public ModelInstance modelInstance;

    public void setColor (Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setColor (float r, float g, float b, float a) {
        tmpC.set(r, g, b, a);
        if (modelInstance != null) {
            for (Material m : modelInstance.materials) {
                ColorAttribute ca = (ColorAttribute)m.get(ColorAttribute.Diffuse);
                if (ca != null) ca.color.set(r, g, b, a);
            }
        }
    }

    @Override
    public void reset() {
        Gdx.app.log(TAG, "reset!");
        visibleRadius = 1f;
        modelInstance = null;
    }
}
