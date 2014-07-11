package net.fastfourier.something;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ProfileActivity extends SomeActivity {
    private ProfileFragment profileView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ActionBar ab = getActionBar();
        profileView = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profile_fragment);

        if(ab != null){
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("userid")){
            profileView.loadProfile(intent.getIntExtra("userid",0));
        }
    }
}
