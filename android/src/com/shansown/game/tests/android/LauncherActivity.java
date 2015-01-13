package com.shansown.game.tests.android;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.shansown.game.tests.utils.TestsList;

import java.util.List;

public class LauncherActivity extends ListActivity {
    SharedPreferences prefs;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> testNames = TestsList.getNames();
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testNames));

        prefs = getSharedPreferences("libgdx-tests", Context.MODE_PRIVATE);
        getListView().setSelectionFromTop(prefs.getInt("index", 0), prefs.getInt("top", 0));
    }

    protected void onListItemClick (ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("index", listView.getFirstVisiblePosition());
        editor.putInt("top", listView.getChildAt(0) == null ? 0 : listView.getChildAt(0).getTop());
        editor.commit();

        Object o = this.getListAdapter().getItem(position);
        String testName = o.toString();

        Bundle bundle = new Bundle();
        bundle.putString("test", testName);
        Intent intent = new Intent(this, AndroidApp.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }

}
