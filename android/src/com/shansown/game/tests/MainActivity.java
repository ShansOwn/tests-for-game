
package com.shansown.game.tests;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL20 = true;
        config.useWakelock = true;
        config.hideStatusBar = true;
        config.useGLSurfaceViewAPI18 = true;
        config.useImmersiveMode = true;

		initialize(new FirstTest(), config);
	}
}
