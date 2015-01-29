package com.shansown.game.tests.bullet;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;

public class BulletMeshesTest extends BaseBulletTest {

    BulletEntity ground;
    btTriangleIndexVertexArray trexVertexArray;

    boolean loading;

    @Override
    public void create() {
        super.create();

        assets.load("data/T-rex.g3db", Model.class);
        assets.load("data/knight.g3db", Model.class);
        assets.load("data/Slingshot_guy.g3db", Model.class);
        loading = true;

        (ground = world.add("ground", 0f, 0f, 0f)).setColor(0.25f + 0.5f * (float) Math.random(),
                0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 1f);
    }

    @Override
    public void render() {
        super.render();
        if (loading && assets.update()) {
            loading = false;
            doneLoading();
        }
    }

    private void doneLoading() {
        final Model trexModel = assets.get("data/T-rex.g3db", Model.class);
        final Model knightModel = assets.get("data/knight.g3db", Model.class);
        final Model slingshotGuyModel = assets.get("data/Slingshot_guy.g3db", Model.class);

        trexVertexArray = new btTriangleIndexVertexArray(trexModel.meshParts);
        btGImpactMeshShape trexShape = new btGImpactMeshShape(trexVertexArray);
        trexShape.setLocalScaling(new Vector3(1f, 1f, 1f));
        trexShape.setMargin(0f);
        trexShape.updateBound();

        btCapsuleShape slingshotGuyShape = new btCapsuleShape(2f, 4f);

        world.addConstructor("T-RexGimpact", new BulletConstructor(trexModel, 1f, trexShape));
        world.add("T-RexGimpact", 0, 10, 0);

        world.addConstructor("T-RexHull", new BulletConstructor(trexModel, 1f, createConvexHullShape(trexModel, true, true)));
        world.add("T-RexHull", new Matrix4().setToTranslation(0, 10, 10).rotate(Vector3.Y, 180));

        world.addConstructor("knight", new BulletConstructor(knightModel, 1f, createConvexHullShape(knightModel, false, true)));
        world.add("knight", 0, 10, -10);

        world.addConstructor("slingshotGuy", new BulletConstructor(slingshotGuyModel, 1f, slingshotGuyShape));
        world.add("slingshotGuy", 10, 10, 0);
    }

    @Override
    public boolean tap (float x, float y, int count, int button) {
        shoot(x, y);
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (trexVertexArray != null) {
            trexVertexArray.dispose();
        }
        trexVertexArray = null;
        ground = null;
    }
}