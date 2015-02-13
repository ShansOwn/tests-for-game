package com.shansown.game.tests.slingshotfight.world.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.shansown.game.tests.slingshotfight.world.GameWorld;

public abstract class CameraCreator {

    public static GameCamera createGameCamera() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        if (width > height) {
            width = GameWorld.MIN_WORLD_VIEWPORT_SIZE * width / height;
            height = GameWorld.MIN_WORLD_VIEWPORT_SIZE;
        } else {
            height = GameWorld.MIN_WORLD_VIEWPORT_SIZE * height / width;
            width = GameWorld.MIN_WORLD_VIEWPORT_SIZE;
        }
        return new GameCamera(1, 100, width, height, new Vector3(7, 7, 9), new Vector3(0, -5, 0));
    }

    public static PerspectiveCamera createPerspectiveCamera(float near, float far, Vector3 position, Vector3 lookAt) {
        return createPerspectiveCamera(near, far, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), position, lookAt);
    }

    public static PerspectiveCamera createPerspectiveCamera(float near, float far, float viewportWidth, float viewportHeight,
                                                            Vector3 position, Vector3 lookAt) {
        PerspectiveCamera camera = new PerspectiveCamera(67, viewportWidth, viewportHeight);
        camera.position.set(position);
        camera.lookAt(lookAt);
        camera.near = near;
        camera.far = far;
        camera.update();
        return camera;
    }
}
