package com.shansown.game.tests.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;

public class StaticComponent extends Component {
    public btCollisionObject object = new btCollisionObject();
}
