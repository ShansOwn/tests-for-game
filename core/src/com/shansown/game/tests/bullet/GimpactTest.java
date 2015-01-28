package com.shansown.game.tests.bullet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;

public class GimpactTest extends BaseBulletTest {

    BulletEntity ground;
    btTriangleIndexVertexArray chassisVertexArray;

    boolean loading;

    @Override
    public void create () {
        super.create();

        assets.load("data/car.obj", Model.class);
        loading = true;

        (ground = world.add("ground", 0f, 0f, 0f)).setColor(0.25f + 0.5f * (float) Math.random(),
                0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 1f);
    }

    private void doneLoading() {
        final Model chassisModel = assets.get("data/car.obj");

        chassisModel.materials.get(0).clear();
        chassisModel.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createSpecular(Color.WHITE));

        chassisVertexArray = new btTriangleIndexVertexArray(chassisModel.meshParts);
        btGImpactMeshShape chassisShape = new btGImpactMeshShape(chassisVertexArray);
        chassisShape.setLocalScaling(new Vector3(1f, 1f, 1f));
        chassisShape.setMargin(0f);
        chassisShape.updateBound();

        world.addConstructor("chassis", new BulletConstructor(chassisModel, 1f, chassisShape));
        world.add("chassis", 3f, 10f, 3f);
    }

    @Override
    public void render() {
        super.render();
        if (loading && assets.update()) {
            loading = false;
            doneLoading();
        }
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
        shoot(x, y);
        return true;
    }

    @Override
    public void dispose () {
        super.dispose();
        chassisVertexArray.dispose();
        chassisVertexArray = null;
        ground = null;
    }
}
