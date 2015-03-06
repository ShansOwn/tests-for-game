package com.shansown.game.tests.ashley.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.shansown.game.tests.ashley.Mappers;
import com.shansown.game.tests.ashley.components.graphics.RenderComponent;
import com.shansown.game.tests.ashley.components.physics.TransformComponent;

public class RenderSystem extends IteratingSystem {

    private static final String TAG = RenderSystem.class.getSimpleName();

    private static final boolean SHADOWS = false;

    private final Vector3 tmpV = new Vector3();

    private PerspectiveCamera camera;
    private Environment environment;
    private ModelBatch modelBatch;
    private SpriteBatch spriteBatch;
    private DebugDrawer debugDrawer;

    private WorldSystem world;

    private Array<Entity> visibleEntities = new Array<>();
    private boolean renderMeshes = true;

    public RenderSystem(int priority, PerspectiveCamera camera) {
        super(Family.all(RenderComponent.class, TransformComponent.class).get(), priority);
        this.camera = camera;
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        environment = createEnvironment();
    }

    private Environment createEnvironment() {
        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, .3f, .55f, 1, 1));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, .2f, -0.6f, -.8f));
        environment.add(new PointLight().set(.3f, .3f, .3f, -6, 14, 6, 200));

        DirectionalLight sunLight = SHADOWS ? new DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) : new DirectionalLight();
        sunLight.set(.8f, .8f, .8f,  -0.5f, -1f, 0.7f);
        environment.add(sunLight);

        if (SHADOWS) {
            environment.shadowMap = (DirectionalShadowLight)sunLight;
        }
        return environment;
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

    private int getDebugMode () {
        return (debugDrawer == null) ? 0 : debugDrawer.getDebugMode();
    }

    private void setDebugMode (final int mode) {
        if (mode == btIDebugDraw.DebugDrawModes.DBG_NoDebug && debugDrawer == null) return;
        if (debugDrawer == null) {
            world.setDebugDrawer(debugDrawer = new DebugDrawer());
        }
        debugDrawer.setDebugMode(mode);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        world = engine.getSystem(WorldSystem.class);
    }

    @Override
    public void update(float deltaTime) {
        modelBatch.begin(camera);
        if (renderMeshes) {
            super.update(deltaTime);
            for (Entity entity : visibleEntities) {
                RenderComponent render = Mappers.render.get(entity);
                modelBatch.render(render.modelInstance, environment);
            }
            visibleEntities.clear();
        }

        if (debugDrawer != null && debugDrawer.getDebugMode() > 0) {
            modelBatch.flush();
            debugDrawer.begin(modelBatch.getCamera());
            world.debugDrawWorld();
            debugDrawer.end();
        }
        modelBatch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (isVisible(entity)) {
            visibleEntities.add(entity);
        }
    }

    private boolean isVisible(Entity entity) {
        TransformComponent transform = Mappers.transform.get(entity);
        RenderComponent render = Mappers.render.get(entity);
        transform.transform.getTranslation(tmpV);
        return camera.frustum.sphereInFrustum(tmpV, render.visibleRadius);
    }

    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        modelBatch.dispose();
        spriteBatch.dispose();
    }
}