package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntMap;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.GuyComponent;
import com.shansown.game.tests.ashley.components.InputControlComponent;
import com.shansown.game.tests.ashley.components.physics.KinematicComponent;

public class InputSystem extends EntitySystem implements InputProcessor {

    private static final String TAG = InputSystem.class.getSimpleName();

    private final Vector3 tmpV = new Vector3();
    private Ray pickRay;

    private IntMap<Entity> pickedEntities = new IntMap<>(2);

    private WorldSystem world;

    private Camera camera;

    public InputSystem(int priority, Camera camera) {
        super(priority);
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        world = engine.getSystem(WorldSystem.class);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    public boolean checkUnpicking(Entity entity) {
        boolean result = false;
            if (pickedEntities.containsValue(entity, true)) {
                int pointer = pickedEntities.findKey(entity, true, -1);
                if (pointer > 0) {
                    processUnpicking(pointer);
                    result = true;
                }
            }
        return result;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log(TAG, "touchDown");
        boolean result = false;
        if (button == Input.Buttons.LEFT) {
            pickRay = camera.getPickRay(screenX, screenY);
            Vector3 rayFrom = pickRay.origin;
            Vector3 rayTo = tmpV.set(pickRay.direction).scl(50f).add(pickRay.origin); // 50 meters max from the origin

            Entity entity = world.bulletRayTest(rayFrom, rayTo);

            if (canPick(entity)) {
                processPicking(pointer, entity);
                result = true;
            }
        }
        return result;
    }

    private boolean canPick(Entity entity) {
        if (entity == null || !Mappers.kinematic.has(entity)) return false;
        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        boolean isPlayer = (WorldSystem.PLAYER_FLAG == (kinematic.body.getCollisionFlags() & WorldSystem.PLAYER_FLAG));
        return isPlayer
                && Mappers.inputControl.has(entity)
                && Mappers.inputControl.get(entity).canPick;
    }

    private void processPicking(int pointer, Entity entity) {
        Gdx.app.log(TAG, "Pick entity");
        Mappers.inputControl.get(entity).picked = true;
        pickedEntities.put(pointer, entity);
        if (Mappers.guy.has(entity)) {
            pickGuy(entity);
        }
    }

    private void pickGuy(Entity entity) {
        Gdx.app.log(TAG, "Pick guy");
        Mappers.guy.get(entity).state = GuyComponent.State.STRING;

        Vector3 pickPoint = world.getLatestHitWorld(tmpV);
        float distance = (pickPoint.y - pickRay.origin.y) / pickRay.direction.y;

        Mappers.transform.get(entity).transform.getTranslation(tmpV);
        Vector3 entityPosition = tmpV;

        Vector3 offset = Mappers.kinematic.get(entity).offset;
        offset.set(pickRay.direction).scl(distance).add(pickRay.origin);
        offset.x -= entityPosition.x;
        offset.z -= entityPosition.z;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Gdx.app.log(TAG, "touchDragged");
        boolean result = false;
        if (pickedEntities.containsKey(pointer)) {
            Entity pickedEntity = pickedEntities.get(pointer);
            dragEntity(pickedEntity, screenX, screenY);
            result = true;
        }
        return result;
    }

    private void dragEntity(Entity entity, int screenX, int screenY) {
        Gdx.app.log(TAG, "drag entity");
        Ray ray = camera.getPickRay(screenX, screenY);
        Gdx.app.log(TAG, "Ray: " + ray);

        KinematicComponent kinematic = Mappers.kinematic.get(entity);
        Vector3 offset = kinematic.offset;
        float distance = (offset.y - ray.origin.y) / ray.direction.y;

        Gdx.app.log(TAG, "distanceY: " + -ray.origin.y / ray.direction.y);
        Gdx.app.log(TAG, "offset: " + offset);
        Gdx.app.log(TAG, "distance: " + distance);
        tmpV.set(ray.direction).scl(distance).add(ray.origin);
        world.getTerrainY(tmpV);
        kinematic.position.set(tmpV);
        kinematic.moving = true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log(TAG, "touchUp");
        boolean result = false;
        if (pickedEntities.containsKey(pointer)) {
            processUnpicking(pointer);
            result = true;
        }
        return result;
    }

    private void processUnpicking(int pointer) {
        Entity entity = pickedEntities.get(pointer);
        pickedEntities.remove(pointer);
        Gdx.app.log(TAG, "Unpick entity");
        if (Mappers.guy.has(entity)) {
            unpickGuy(entity);
        }
    }

    private void unpickGuy(Entity entity) {
        Gdx.app.log(TAG, "Unpick guy");
        GuyComponent guy = Mappers.guy.get(entity);
        if (guy.state == GuyComponent.State.STRING) {
            guy.state = GuyComponent.State.SHOOT;
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}