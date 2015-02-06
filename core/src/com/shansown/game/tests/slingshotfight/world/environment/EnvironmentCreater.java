package com.shansown.game.tests.slingshotfight.world.environment;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

public abstract class EnvironmentCreater {

    public static Environment createBaseEnvironment(Vector3 sun, boolean shadows) {
        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, .3f, .55f, 1, 1));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, .2f, -0.6f, -.8f));
        environment.add(new PointLight().set(.3f, .3f, .3f, sun, 200));

        DirectionalLight sunLight = shadows ? new DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) : new DirectionalLight();
        sunLight.set(.8f, .8f, .8f,  -0.5f, -1f, 0.7f);
        environment.add(sunLight);

        if (shadows) {
            environment.shadowMap = (DirectionalShadowLight)sunLight;
        }
        return environment;
    }
}