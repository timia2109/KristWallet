package com.timia2109.kristwallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

import com.timia2109.kristwallet.KLottery.KLotteryFragment;

public class WalletActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    Fragment startNow = null;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    KristAPI useAPI;
    KristAPI[] apis;
    Saver saver;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        //Get KristAPI
        intent = getIntent();

        useAPI = (KristAPI) intent.getSerializableExtra("api");
        apis = (KristAPI[]) intent.getSerializableExtra("apis");
        saver = (Saver) intent.getSerializableExtra("saver");
        if (saver == null) {
            saver = Saver.load(this);
            apis = saver.apis;
        }

        if (useAPI == null) onBackPressed();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (intent.hasExtra("sendTo")) {
            SendKristFragrament.preTo = intent.getStringExtra("sendTo");
            onNavigationDrawerItemSelected(2);
        }
        else
            onNavigationDrawerItemSelected(0);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        boolean handled = false;
        switch (position) {
            case 0:
                TransactionsFragrament u = new TransactionsFragrament();
                u.appendAPI(useAPI);
                u.appendSaver(saver);
                startNow = u;
                break;
            case 1:
                EconomiconFragrament u2 = new EconomiconFragrament();
                u2.appendAPI(useAPI);
                u2.appendSaver(saver);
                startNow = u2;
                break;
            case 2:
                if (useAPI.isFullAPI()) {
                    SendKristFragrament u3 = new SendKristFragrament();
                    u3.setAPIs(apis, useAPI);
                    startNow = u3;
                }
                break;
            case 3:
                KSTNameFragment u4 = new KSTNameFragment();
                u4.appendAPI(useAPI);
                startNow = u4;
                break;
            case 4:
                startNow = new KLotteryFragment();
                break;
            case 5:
                handled = webApp();
                break;
            default:
                if (position > 5) {
                    handled = openWebApp( saver.webApps[position-6] );
                }
                else
                    startNow = new TransactionsFragrament();
                break;
        }

        if (!handled) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, startNow);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.wallet, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        //return MainActivity.optionsItemSelected(item, this);
    }

    public void onBackPressed() {
        startActivity( new Intent(this, MainActivity.class) );
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (saver != null)
            saver.save(this);
        else if (Saver.load() != null)
            Saver.load().save(this);
    }

    private boolean openWebApp(String url) {
        Intent sintent = new Intent(this, WebViewActivty.class);
        sintent.putExtras(getIntent().getExtras());
        sintent.putExtra("url", url);
        startActivity(sintent);
        return true;
    }

    private boolean webApp() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.webApp);
        builder.setMessage(R.string.webAppInfo);
        final EditText url = new EditText(this);
        url.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        url.setText(saver.lastWebApp);
        builder.setView(url);
        builder.setPositiveButton(R.string.webAppOpen, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saver.lastWebApp = url.getText().toString();
                saver.nosave();
                openWebApp(url.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        return true;
    }
}
