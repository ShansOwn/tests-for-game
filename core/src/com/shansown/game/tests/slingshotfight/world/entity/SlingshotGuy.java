package com.shansown.game.tests.slingshotfight.world.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.shansown.game.tests.slingshotfight.world.GameWorld;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletConstructor;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletEntity;
import com.shansown.game.tests.slingshotfight.world.bullet.BulletWorld;

public class SlingshotGuy extends GameEntity {

    protected SlingshotGuy(Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, float x, float y, float z) {
        super(model, bodyInfo, x, y, z);
    }

    protected SlingshotGuy(Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, Matrix4 transform) {
        super(model, bodyInfo, transform);
    }

    protected SlingshotGuy(Model model, btCollisionObject body, float x, float y, float z) {
        super(model, body, x, y, z);
    }

    protected SlingshotGuy(Model model, btCollisionObject body, Matrix4 transform) {
        super(model, body, transform);
    }

    protected SlingshotGuy(ModelInstance modelInstance, btCollisionObject body) {
        super(modelInstance, body);
    }

    @Override
    public void update(GameWorld world, float delta) {

    }

    public class Constructor extends BulletConstructor {

        public Constructor(Model model, float mass, btCollisionShape shape) {
            super(model, mass, shape);
        }

        public Constructor(Model model, btCollisionShape shape) {
            this(model, -1f, shape);
        }

        public Constructor(Model model, float mass, float width, float height, float depth) {
            super(model, mass, width, height, depth);
        }

        public Constructor(Model model, float width, float height, float depth) {
            this(model, -1f, width, height, depth);
        }

        public Constructor(Model model, float mass) {
            super(model, mass);
        }

        public Constructor(Model model) {
            this(model, -1f);
        }



        @Override
        public SlingshotGuy construct (float x, float y, float z) {
            if (bodyInfo == null && shape != null) {
                btCollisionObject obj = new btCollisionObject();
                obj.setCollisionShape(shape);
                return new SlingshotGuy(model, obj, x, y, z);
            } else
                return new SlingshotGuy(model, bodyInfo, x, y, z);
        }

        @Override
        public SlingshotGuy construct (final Matrix4 transform) {
            if (bodyInfo == null && shape != null) {
                btCollisionObject obj = new btCollisionObject();
                obj.setCollisionShape(shape);
                return new SlingshotGuy(model, obj, transform);
            } else
                return new SlingshotGuy(model, bodyInfo, transform);
        }
    }
}
