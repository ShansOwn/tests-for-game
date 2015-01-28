package com.shansown.game.tests.bullet;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class BaseBulletTest extends BulletTest {

    // Set this to the path of the lib to use it on desktop instead of default lib.
    private final static String customDesktopLib = null;//"C:\\Xoppa\\code\\libgdx\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";

    private static boolean initialized = false;

    public static boolean shadows = true;

    public static void init () {
        if (initialized) return;
        // Need to initialize bullet before using it.
        if (Gdx.app.getType() == Application.ApplicationType.Desktop && customDesktopLib != null) {
            System.load(customDesktopLib);
        } else
            Bullet.init();
        Gdx.app.log("Bullet", "Version = " + LinearMath.btGetVersion());
        initialized = true;
    }

    public Environment environment;
    public DirectionalLight light;
    public ModelBatch shadowBatch;

    public BulletWorld world;
    public AssetManager assets;
    public ObjLoader objLoader = new ObjLoader();
    public ModelBuilder modelBuilder = new ModelBuilder();
    public ModelBatch modelBatch;
    public Array<Disposable> disposables = new Array<Disposable>();
    private int debugMode = btIDebugDraw.DebugDrawModes.DBG_NoDebug;

    public BulletWorld createWorld () {
        return new BulletWorld();
    }

    @Override
    public void create() {
        init();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        light = shadows ? new DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) : new DirectionalLight();
        light.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, 0.7f);
        environment.add(light);
        if (shadows)
            environment.shadowMap = (DirectionalShadowLight)light;
        shadowBatch = new ModelBatch(new DepthShaderProvider());

        modelBatch = new ModelBatch();
        assets = new AssetManager();

        world = createWorld();
        world.performanceCounter = performanceCounter;

        final float width = Gdx.graphics.getWidth();
        final float height = Gdx.graphics.getHeight();
        if (width > height)
            camera = new PerspectiveCamera(67f, 3f * width / height, 3f);
        else
            camera = new PerspectiveCamera(67f, 3f, 3f * height / width);
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.update();

        // Create some simple models
        final Model groundModel = modelBuilder.createRect(
                20f,
                0f,
                -20f,
                -20f,
                0f,
                -20f,
                -20f,
                0f,
                20f,
                20f,
                0f,
                20f,
                0,
                1,
                0,
                new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE),
                        FloatAttribute.createShininess(16f)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(groundModel);
        final Model boxModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.WHITE),
                ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(boxModel);

        // Add the constructors
        world.addConstructor("ground", new BulletConstructor(groundModel, 0f)); // mass = 0: static body
        world.addConstructor("box", new BulletConstructor(boxModel, 1f)); // mass = 1kg: dynamic body
        world.addConstructor("staticbox", new BulletConstructor(boxModel, 0f)); // mass = 0: static body
    }

    @Override
    public void dispose() {
        world.dispose();
        world = null;

        assets.dispose();
        assets = null;

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();

        modelBatch.dispose();
        modelBatch = null;

        shadowBatch.dispose();
        shadowBatch = null;

        if (shadows) {
            ((DirectionalShadowLight) light).dispose();
        }
        light = null;

        super.dispose();
    }

    @Override
    public void render() {
        fpsCounter.put(Gdx.graphics.getFramesPerSecond());

        update();

        beginRender();
        renderWorld();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        if (debugMode != btIDebugDraw.DebugDrawModes.DBG_NoDebug) world.setDebugMode(debugMode);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        performance.setLength(0);
        performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
                .append((int)(performanceCounter.load.value * 100f)).append("%");
    }

    protected void beginRender () {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
    }

    protected void renderWorld () {
        if (shadows) {
            ((DirectionalShadowLight)light).begin(Vector3.Zero, camera.direction);
            shadowBatch.begin(((DirectionalShadowLight)light).getCamera());
            world.render(shadowBatch, null);
            shadowBatch.end();
            ((DirectionalShadowLight)light).end();
        }

        modelBatch.begin(camera);
        world.render(modelBatch, environment);
        modelBatch.end();
    }

    public void update () {
        world.update();
    }

    protected btConvexHullShape createConvexHullShape (final Model model, boolean optimize, boolean needRotation) {
        final Mesh meshOrigin = model.meshes.get(0);
        Mesh mesh = meshOrigin.copy(true);
        if (needRotation) {
            mesh.transform(new Matrix4().rotate(Vector3.X, -90));
        } else {
            mesh = meshOrigin;
        }
        final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        mesh.dispose();
        return result;
    }

    public BulletEntity shoot (final float x, final float y) {
        return shoot(x, y, 30f);
    }

    public BulletEntity shoot (final float x, final float y, final float impulse) {
        return shoot("box", x, y, impulse);
    }

    public BulletEntity shoot (final String what, final float x, final float y, final float impulse) {
        // Shoot a box
        Ray ray = camera.getPickRay(x, y);
        BulletEntity entity = world.add(what, ray.origin.x, ray.origin.y, ray.origin.z);
        entity.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
                1f);
        ((btRigidBody)entity.body).applyCentralImpulse(ray.direction.scl(impulse));
        return entity;
    }

    public void setDebugMode (final int mode) {
        world.setDebugMode(debugMode = mode);
    }

    public void toggleDebugMode () {
        Gdx.app.log("Test", "toggle debug mode");
        if (world.getDebugMode() == btIDebugDraw.DebugDrawModes.DBG_NoDebug) {
            setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
                    | btIDebugDraw.DebugDrawModes.DBG_DrawFeaturesText
                    | btIDebugDraw.DebugDrawModes.DBG_DrawText
                    | btIDebugDraw.DebugDrawModes.DBG_DrawContactPoints);
        } else if (world.renderMeshes) {
            world.renderMeshes = false;
        } else {
            world.renderMeshes = true;
            setDebugMode(btIDebugDraw.DebugDrawModes.DBG_NoDebug);
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.MENU) {
            toggleDebugMode();
            return  true;
        }
        return false;
    }
}