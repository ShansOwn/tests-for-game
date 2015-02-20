package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class KinematicComponent extends Component {
    public boolean moving = false;
    public Vector3 position = new Vector3();
    public Vector3 offset = new Vector3();
    public btRigidBody body;
}