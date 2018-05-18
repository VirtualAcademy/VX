package com.ndicson.vxplayerpro;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;



public class PlayListActivity extends ListActivity {
    // Video list
    public ArrayList<HashMap<String, String>> videoList = new ArrayList<HashMap<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        ArrayList<HashMap<String, String>> videoListData = new ArrayList<HashMap<String, String>>();

        VideoManager plm = new VideoManager();
        // get all video from sdcard
        this.videoList = plm.getPlayList();

        // looping through playlist
        for (int i = 0; i < videoList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> video = videoList.get(i);

            // adding HashList to ArrayList
            videoListData.add(video);
        }

        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter(this, videoListData,
                R.layout.playlist_item, new String[] { "videoTitle" }, new int[] {
                R.id.videoTitle });

        setListAdapter(adapter);

        // selecting single ListView item
        ListView lv = getListView();
        // listening to single listitem click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                int videoIndex = position;

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        VideoPlayerActivity.class);
                // Sending videoIndex to PlayerActivity
                in.putExtra("videoIndex", videoIndex);
                setResult(100, in);
                // Closing PlayListView
                finish();
            }
        });

    }
}
