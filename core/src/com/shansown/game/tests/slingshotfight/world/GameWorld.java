package com.shansown.game.tests.slingshotfight.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.shansown.game.tests.slingshotfight.SlingshotFight;
import com.shansown.game.tests.slingshotfight.world.camera.CameraCreator;
import com.shansown.game.tests.slingshotfight.world.camera.GameCamera;
import com.shansown.game.tests.slingshotfight.world.entity.GameEntity;
import com.shansown.game.tests.slingshotfight.world.environment.EnvironmentCreater;
import com.shansown.game.tests.slingshotfight.reference.Entities;
import com.shansown.game.tests.slingshotfight.reference.Models;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletConstructor;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletEntity;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletWorld;

public class GameWorld extends BulletWorld implements InputProcessor, GestureDetector.GestureListener {

    private static final String TAG = "GameWorld";

    public static final float MIN_GAME_VIEWPORT_SIZE = 10f;
    public static final short PLAYER_FLAG = 1<<8;
    private static final float CHARACTER_OFFSET = .8f;

    //do not work properly!
    public static boolean shadows = false;
    public static boolean useBulletFrustumCulling = false;

    protected SlingshotFight game;
    public GameCamera camera;
    protected ModelBatch modelBatch;
    protected Environment environment;
    protected ModelBatch shadowBatch;
    public ModelBuilder modelBuilder = new ModelBuilder();

    public Array<Disposable> disposables = new Array<>();
    public Array<BulletEntity> visibleEntities = new Array<>();
    public Array<GameEntity> braves = new Array<>();
    public BulletEntity pickedCharacter;

    private BulletEntity frustumEntity;
    private BulletEntity island;

    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 tmpV2 = new Vector3();
    private Vector3 terrainVector = new Vector3();
    private Ray tmpRay = new Ray(Vector3.Zero, Vector3.Z);
    private Vector3 offset = new Vector3();

    private ClosestRayResultCallback terrainCB;
    private ClosestRayResultCallback pickCB;

    public static GameWorld createWorld(SlingshotFight game, boolean grid) {
        // No need to use dynamics for this game
        /*btDbvtBroadphase broadphase = new btDbvtBroadphase();
        btDefaultCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
        btCollisionWorld collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
        GameWorld world = new GameWorld(collisionConfig, dispatcher, broadphase, null, collisionWorld);*/
        GameWorld world = new GameWorld();

        world.game = game;
        world.camera = CameraCreator.createGameCamera();
        world.environment = EnvironmentCreater.createBaseEnvironment(new Vector3(-6, 14, 6), shadows);
        world.modelBatch = new ModelBatch();
        world.shadowBatch = new ModelBatch(new DepthShaderProvider());

        world.initWorldConstructors();
        world.createEntities(grid);
        return world;
    }

    protected void initWorldConstructors() {
        final Model slingshotGuyModel = game.assets.get(Models.SLINGSHOT_GUY, Model.class);
        btCapsuleShape slingshotGuyShape = new btCapsuleShape(.3f, 1f);
        addConstructor(Entities.SLINGSHOT_GUY, new BulletConstructor(slingshotGuyModel, 0, slingshotGuyShape));

        Model islandModel = game.assets.get(Models.ISLAND, Model.class);
        btCollisionShape collisionShape = Bullet.obtainStaticNodeShape(islandModel.nodes.first(), true);
        addConstructor(Entities.ISLAND, new BulletConstructor(islandModel, 0, collisionShape));

        final Model boxModel = modelBuilder.createBox(.1f, .1f, .1f, new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(boxModel);

        addConstructor(Entities.BOX, new BulletConstructor(boxModel, 1f));
    }

    protected void createEntities(boolean grid) {
        if (grid) {
            add(new BulletEntity(createAxis(-10, 10, 1), null));
        }

        createStaticIsland(new Vector3());

        terrainCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
        pickCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);

        createPlayerEntities();
        createEnemyEntities();
        if (useBulletFrustumCulling) {
            createFrustumEntity();
        }
    }

    private void createPlayerEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, 3));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(7, 0, -1));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, true);

        tmpM.idt();
        getTerrainY(tmpV.set(6, 0, -5));
        tmpM.rotate(Vector3.Y, 90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, true);
    }

    private void createEnemyEntities() {
        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, 3));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-7, 0, -1));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, false);

        tmpM.idt();
        getTerrainY(tmpV.set(-6, 0, -5));
        tmpM.rotate(Vector3.Y, -90).setTranslation(tmpV);
        createSlingshotGuyEntity(tmpM, false);
    }

    private Vector3 getTerrainY(Vector3 out) {
        Gdx.app.log("Test", "getTerrainY start: " + out);
        //TODO: think up with implementation because method below isn't relevant
        /*tmpRay.set(out.x, 10, out.z, 0, -1, 0);
        Vector3 rayFrom = tmpRay.origin;
        Vector3 rayTo = terrainVector.set(tmpRay.direction).scl(50f).add(tmpRay.origin); // 50 meters max from the origin
        // Because we reuse the ClosestRayResultCallback, we need reset it's values
        terrainCB.setCollisionObject(null);
        terrainCB.setClosestHitFraction(1f);
        terrainCB.setRayFromWorld(rayFrom);
        terrainCB.setRayToWorld(rayTo);

        collisionWorld.rayTest(rayFrom, rayTo, terrainCB);

        if (terrainCB.hasHit()) {
            terrainCB.getHitPointWorld(terrainVector);
            Gdx.app.log("Test", "got height map point: " + terrainVector);
            out.y = terrainVector.y;
        }*/

        out.y = 0;
        Gdx.app.log("Test", "getTerrainY end: " + out);
        return out;
    }

    private GameWorld (final btCollisionConfiguration collisionConfiguration,
                      final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase,
                      final btConstraintSolver solver, final btCollisionWorld world, final Vector3 gravity) {
        super(collisionConfiguration, dispatcher, broadphase, solver, world, gravity);
    }

    private GameWorld (final btCollisionConfiguration collisionConfiguration,
                      final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase,
                      final btConstraintSolver solver, final btCollisionWorld world) {
        this(collisionConfiguration, dispatcher, broadphase, solver, world, new Vector3(0, -10, 0));
    }

    private GameWorld (final Vector3 gravity) {
        super(gravity);
    }

    private GameWorld () {
        this(new Vector3(0, -10, 0));
    }

    @Override
    public void update() {
        super.update();
        collisionWorld.performDiscreteCollisionDetection();
        for (GameEntity brave : braves) {
            brave.update(this, Gdx.graphics.getDeltaTime());
        }
    }

    public void renderFrustumCulling() {
        if (performanceCounter != null) performanceCounter.start();
        if (useBulletFrustumCulling) {
            getEntitiesCollidingWithObject((camera).frustumObject, visibleEntities);
        } else {
            visibleEntities.clear();
            for (int i = 0; i < entities.size; i++) {
                final BulletEntity e = entities.get(i);
                e.modelInstance.transform.getTranslation(tmpV);
                if (camera.frustum.sphereInFrustum(tmpV, 1)) visibleEntities.add(e);
            }
        }
        if (performanceCounter != null) performanceCounter.stop();

        if (shadows) {
            ((DirectionalShadowLight)environment.shadowMap).begin(Vector3.Zero, camera.direction);
            shadowBatch.begin(((DirectionalShadowLight)environment.shadowMap).getCamera());
            super.render(shadowBatch, null, visibleEntities);
            shadowBatch.end();
            ((DirectionalShadowLight)environment.shadowMap).end();
        }

        modelBatch.begin(camera);
        super.render(modelBatch, environment, visibleEntities);
        modelBatch.end();
    }

    public int toggleDebugMode() {
        Gdx.app.log(TAG, "toggle debug mode");
        if (getDebugMode() == btIDebugDraw.DebugDrawModes.DBG_NoDebug) {
            setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
                    | btIDebugDraw.DebugDrawModes.DBG_DrawFeaturesText
                    | btIDebugDraw.DebugDrawModes.DBG_DrawText
                    | btIDebugDraw.DebugDrawModes.DBG_DrawContactPoints);
        } else if (renderMeshes) {
            renderMeshes = false;
        } else {
            renderMeshes = true;
            setDebugMode(btIDebugDraw.DebugDrawModes.DBG_NoDebug);
        }
        return getDebugMode();
    }

    private BulletEntity createStaticIsland(Vector3 position) {
        island = add(Entities.ISLAND, position.x, position.y, position.z);
        island.transform.rotate(Vector3.Y, 30);
        island.body.setWorldTransform(island.transform);
        island.body.setCollisionFlags(island.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
        return island;
    }

    private BulletEntity createFrustumEntity() {
        final Model frustumModel = createFrustumModel(camera.frustum.planePoints);
        disposables.add(frustumModel);
        Vector3 position = camera.position;
        frustumEntity = new BulletEntity(frustumModel, camera.frustumObject, position.x, position.y, position.z);
        frustumEntity.setColor(Color.BLUE);
        add(frustumEntity);
        return frustumEntity;
    }

    private BulletEntity createSlingshotGuyEntity(Matrix4 transform, boolean player) {
        transform.trn(0, CHARACTER_OFFSET, 0);
        BulletEntity slingshotGuy = add(Entities.SLINGSHOT_GUY, transform);

        int collisionFlags = slingshotGuy.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT;
        if (player) {
            collisionFlags |= PLAYER_FLAG;
        }

        slingshotGuy.body.setCollisionFlags(collisionFlags);
        return slingshotGuy;
    }

    private ModelInstance createAxis(final int gridMin, final int gridMax, final int gridStep) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", 1, 3, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = gridMin; t <= gridMax; t += gridStep) {
            builder.line(t, 0, gridMin, t, 0, gridMax);
            builder.line(gridMin, 0, t, gridMax, 0, t);
        }
        builder = modelBuilder.part("axes", 1, 3, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 100, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 100, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 100);
        Model axesModel = modelBuilder.end();
        return new ModelInstance(axesModel);
    }

    private Model createFrustumModel (final Vector3... p) {
        return ModelBuilder.createFromMesh(
                new float[]{
                        p[0].x, p[0].y, p[0].z, 0, 0, 1, p[1].x, p[1].y, p[1].z, 0, 0, 1,
                        p[2].x, p[2].y, p[2].z, 0, 0, 1, p[3].x, p[3].y, p[3].z, 0, 0, 1, // near
                        p[4].x, p[4].y, p[4].z, 0, 0, -1, p[5].x, p[5].y, p[5].z, 0, 0, -1,
                        p[6].x, p[6].y, p[6].z, 0, 0, -1, p[7].x, p[7].y, p[7].z, 0, 0, -1},// far
                new VertexAttribute[] {
                        new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                        new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")},
                new short[] {0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7},
                GL20.GL_LINES, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)));
    }

    public BulletEntity shoot (final float x, final float y) {
        return shoot(x, y, 30f);
    }

    public BulletEntity shoot (final float x, final float y, final float impulse) {
        return shoot(Entities.BOX, x, y, impulse);
    }

    public BulletEntity shoot (final String what, final float x, final float y, final float impulse) {
        // Shoot a box
        BulletEntity entity = null;
        if (pickedCharacter != null) {
            Vector3 direction = tmpV2.set(0, 0, -1).rot(pickedCharacter.transform).nor();
            Vector3 origin = pickedCharacter.transform.getTranslation(tmpV);
            origin.add(direction);
            entity = add(what, origin.x, origin.y, origin.z);
            entity.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
                    0.5f + 0.5f * (float)Math.random(), 1f);
            ((btRigidBody) entity.body).applyCentralImpulse(direction.scl(impulse));
        }
        return entity;
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }

        if (pickCB != null) {
            pickCB.dispose();
            pickCB = null;
        }

        if (terrainCB != null) {
            terrainCB.dispose();
            terrainCB = null;
        }

        modelBatch.dispose();
        modelBatch = null;

        if (environment.shadowMap != null) {
            ((DirectionalShadowLight) environment.shadowMap).dispose();
        }

        shadowBatch.dispose();
        shadowBatch = null;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        shoot(x, y);
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
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
        Gdx.app.log("Test", "touchDown");
        boolean result = false;
        if (button == Input.Buttons.LEFT) {
            Ray pickRay = camera.getPickRay(screenX, screenY);
            Vector3 rayFrom = pickRay.origin;
            Vector3 rayTo = tmpV.set(pickRay.direction).scl(50f).add(pickRay.origin); // 50 meters max from the origin

            // Because we reuse the ClosestRayResultCallback, we need reset it's values
            pickCB.setCollisionObject(null);
            pickCB.setClosestHitFraction(1f);
            pickCB.setRayFromWorld(rayFrom);
            pickCB.setRayToWorld(rayTo);
            collisionWorld.rayTest(rayFrom, rayTo, pickCB);

            if (pickCB.hasHit()) {
                Gdx.app.log("Test", "hasHit");
                final btCollisionObject obj = pickCB.getCollisionObject();
                boolean canPick = PLAYER_FLAG == (obj.getCollisionFlags() & PLAYER_FLAG);
                if (canPick) {
                    Gdx.app.log("Test", "Pick");
                    pickedCharacter = entities.get(obj.getUserValue());

                    pickCB.getHitPointWorld(tmpV);
                    Vector3 characterHitPoint = tmpV;

                    float distance = (characterHitPoint.y - pickRay.origin.y) / pickRay.direction.y;

                    pickedCharacter.transform.getTranslation(tmpV);
                    Vector3 characterPosition = tmpV;

                    offset.set(pickRay.direction).scl(distance).add(pickRay.origin);
                    offset.x -= characterPosition.x;
                    offset.z -= characterPosition.z;

                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Gdx.app.log("Test", "touchDragged");
        boolean result = false;
        if (pickedCharacter != null) {
            Ray ray = camera.getPickRay(screenX, screenY);
            Gdx.app.log("Test", "Ray: " + ray);

            float distance = (offset.y - ray.origin.y) / ray.direction.y;

            Gdx.app.log("Test", "distanceY: " + -ray.origin.y / ray.direction.y);
            Gdx.app.log("Test", "offset: " + offset);
            Gdx.app.log("Test", "distance: " + distance);
            tmpV.set(ray.direction).scl(distance).add(ray.origin);
            getTerrainY(tmpV);
            movePickedCharacter(tmpV);
            result = true;
        }
        return result;
    }

    private void movePickedCharacter(Vector3 point) {
        Gdx.app.log("Test", "moveTo: " + point);
        point.x -= offset.x;
        point.y += CHARACTER_OFFSET;
        point.z -= offset.z;
        // Clamp by ground size with some margin
//        point.x = MathUtils.clamp(point.x, -19, 19);
//        point.z = MathUtils.clamp(point.z, -19, 19);
        Gdx.app.log("Test", "adjusted moveTo: " + point);
        pickedCharacter.transform.setTranslation(point);
        // We don't disable deactivation for characters
        // (we don't do this: slingshotGuy.body.setActivationState(Collision.DISABLE_DEACTIVATION);)
        // so we should activate it manually when move it!
        pickedCharacter.body.setWorldTransform(pickedCharacter.transform);
        pickedCharacter.body.activate();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("Test", "touchUp");
        boolean result = false;
        if (pickedCharacter != null) {
            pickedCharacter = null;
            result = true;
        }
        return result;
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