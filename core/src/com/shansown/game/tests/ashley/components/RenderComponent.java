package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

public class RenderComponent extends Component {
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
}
