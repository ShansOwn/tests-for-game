package com.shansown.game.tests.bullet;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
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
    }

    @Override
    public void dispose() {
        world.dispose();
        world = null;

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

    public void setDebugMode (final int mode) {
        world.setDebugMode(debugMode = mode);
    }
}