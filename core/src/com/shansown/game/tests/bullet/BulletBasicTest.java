package com.shansown.game.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.Array;

public class BulletBasicTest extends BaseBulletTest {

    final static short GROUND_FLAG = 1<<8;
    final static short SHAPE_FLAG = 1<<9;
    final static short ALL_FLAG = -1;

    final static String[] shapes = {"sphere", "box", "cone", "capsule", "cylinder"};

    class TestContactListener extends ContactListener {
        public Array<BulletEntity> entities;

        @Override
        public boolean onContactAdded (int userValue0, int partId0, int index0, boolean match0, int userValue1, int partId1, int index1, boolean match1) {
            if (match0) {
                final BulletEntity entity = entities.get(userValue0);
                entity.setColor(Color.WHITE);
            }

            if (match1) {
                final BulletEntity entity = entities.get(userValue1);
                entity.setColor(Color.WHITE);
            }
            return true;
        }
    }

    float spawnTimer;

    BulletEntity ground;

    TestContactListener contactListener;
    public static float time;

    @Override
    public void create () {
        super.create();

        final long attributes = Usage.Position | Usage.Normal;

        final Model groundModel = modelBuilder.createBox(5f, 1f, 5f, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes);
        disposables.add(groundModel);
        world.addConstructor("ground", new BulletConstructor(groundModel, 0f, new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));

        final Model sphereModel = modelBuilder.createSphere(1f, 1f, 1f, 10, 10, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes);
        disposables.add(sphereModel);
        world.addConstructor("sphere", new BulletConstructor(sphereModel, 1f, new btSphereShape(0.5f)));

        final Model boxModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes);
        disposables.add(boxModel);
        world.addConstructor("box", new BulletConstructor(boxModel, 1f, new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));

        final Model coneModel = modelBuilder.createCone(1f, 2f, 1f, 10, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes);
        disposables.add(coneModel);
        world.addConstructor("cone", new BulletConstructor(coneModel, 1f, new btConeShape(0.5f, 2f)));

        final Model capsuleModel = modelBuilder.createCapsule(0.5f, 2f, 10, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes);
        disposables.add(capsuleModel);
        world.addConstructor("capsule", new BulletConstructor(capsuleModel, 1f, new btCapsuleShape(.5f, 1f)));

        final Model cylinderModel = modelBuilder.createCylinder(1f, 2f, 1f, 10, new Material(ColorAttribute.createDiffuse(Color.MAGENTA)), attributes);
        disposables.add(cylinderModel);
        world.addConstructor("cylinder", new BulletConstructor(cylinderModel, 1f, new btCylinderShape(new Vector3(.5f, 1f, .5f))));

        ground = world.add("ground", 0, 0, 0);
        ground.body.setCollisionFlags(ground.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        ground.body.setContactCallbackFlag(GROUND_FLAG);
        ground.body.setContactCallbackFilter(0);
        ground.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        contactListener = new TestContactListener();
        contactListener.entities = world.entities;

        time = 0;
    }

    public void spawn () {
        BulletEntity shape = world.add(shapes[1 + MathUtils.random(shapes.length - 2)],
                MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f),
                MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        shape.body.setCollisionFlags(shape.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        shape.body.setContactCallbackFlag(SHAPE_FLAG);
        shape.body.setContactCallbackFilter(GROUND_FLAG);
        Gdx.app.log("Test", "Object spawned! " + world.entities.size  + " objects in world!");
    }

    float angle, speed = 90f;
    @Override
    public void render () {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        angle = (angle + delta * speed) % 360f;
        ground.transform.setTranslation(0, MathUtils.sinDeg(angle) * 2.5f, 0f);

        if ((spawnTimer -= delta) < 0) {
            spawn();
            spawnTimer = 1.5f;
        }
        super.render();
    }

    @Override
    public void dispose () {
        if (contactListener != null) {
            contactListener.dispose();
        }
    }

    @Override
    public void pause () {
    }

    @Override
    public void resume () {
    }

    @Override
    public void resize (int width, int height) {
    }
}