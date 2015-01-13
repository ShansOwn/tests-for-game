package com.shansown.game.tests.android;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.shansown.game.tests.utils.TestsList;

public class AndroidApp extends com.badlogic.gdx.backends.android.AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		String testName = (String)extras.get("test");

		ApplicationListener test = TestsList.newTest(testName);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useWakelock = true;
		config.hideStatusBar = true;
		config.useImmersiveMode = true;

		initialize(test, config);
	}
}
