package com.shansown.game.tests.slingshotfight.world.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;

public class GameCamera extends PerspectiveCamera {

    private static final String TAG = "GameCamera";

    private static final boolean USE_COMPOUND = true;

    public btPairCachingGhostObject frustumObject;
    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();

    public GameCamera(float near, float far, float viewportWidth, float viewportHeight,
                      Vector3 position, Vector3 lookAtPoint) {
        super(67, viewportWidth, viewportHeight);
        this.position.set(position);
        lookAt(lookAtPoint);
        this.near = near;
        this.far = far;
        update();
        frustumObject = createFrustumObject(frustum.planePoints);
    }

    private btPairCachingGhostObject createFrustumObject (final Vector3... points) {
        final btPairCachingGhostObject result = new FrustumPairCachingGhostObject();
        // Using a compound shape is not necessary, but it's good practice to create shapes around the center.
        if (USE_COMPOUND) {
            final Vector3 centerNear = new Vector3(points[2]).sub(points[0]).scl(0.5f).add(points[0]);
            final Vector3 centerFar = new Vector3(points[6]).sub(points[4]).scl(0.5f).add(points[4]);
            final Vector3 center = new Vector3(centerFar).sub(centerNear).scl(0.5f).add(centerNear);
            final btConvexHullShape hullShape = new btConvexHullShape();
            for (Vector3 point : points) {
                hullShape.addPoint(tmpV.set(point).sub(center));
            }
            final btCompoundShape shape = new btCompoundShape();
            shape.addChildShape(tmpM.setToTranslation(center), hullShape);
            result.setCollisionShape(shape);
        } else {
            final btConvexHullShape shape = new btConvexHullShape();
            for (Vector3 point : points) {
                shape.addPoint(point);
            }
            result.setCollisionShape(shape);
        }
        result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        return result;
    }

    // Simple helper class to keep a reference to the collision shape
    private static class FrustumPairCachingGhostObject extends btPairCachingGhostObject {
        public btCollisionShape shape;

        @Override
        public void setCollisionShape (btCollisionShape collisionShape) {
            shape = collisionShape;
            super.setCollisionShape(collisionShape);
        }

        @Override
        public void dispose () {
            super.dispose();
            if (shape != null) shape.dispose();
            shape = null;
        }
    }
}