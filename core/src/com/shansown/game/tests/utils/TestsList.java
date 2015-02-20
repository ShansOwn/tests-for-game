package com.shansown.game.tests.utils;

import com.badlogic.gdx.ApplicationListener;
import com.shansown.game.tests.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestsList {

    //Add new tests here
    public static final List<Class<? extends ApplicationListener>> tests = new ArrayList<Class<? extends ApplicationListener>>(Arrays.asList(
            Test0_Bullet_Tests.class,
            Test1_SceneLoading.class,
            Test2_Animation.class,
            Test3_TRex_Animation.class,
            Test4_Slingshot_Fight.class,
            Test5_Ashley_Test.class
    ));

    public static List<String> getNames () {
        List<String> names = new ArrayList<String>(tests.size());
        for (Class clazz : tests)
            names.add(clazz.getSimpleName());
        Collections.sort(names);
        return names;
    }

    private static Class<? extends ApplicationListener> forName (String name) {
        for (Class clazz : tests)
            if (clazz.getSimpleName().equals(name)) return clazz;
        return null;
    }

    public static ApplicationListener newTest (String testName) {
        try {
            return forName(testName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
