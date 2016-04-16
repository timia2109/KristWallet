package com.timia2109.kristwallet;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.timia2109.kristwallet.util.PostData;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rv;
    private LinearLayoutManager mLayoutManager;
    private Saver saver;
    private int versionCode;
    private android.support.v7.app.ActionBar actionbar;
    private CharSequence[] longClickOpts;
    private final int longClickOptsStrings[] = new int[] {
            R.string.delWallet,
            R.string.setNameWallet,
    };
    public static final DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        longClickOpts = new CharSequence[longClickOptsStrings.length];
        for (int i=0; i<longClickOptsStrings.length;i++) {
            longClickOpts[i] = getString(longClickOptsStrings[i]);
        }

        //Debug ID:
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            //Never happen, but Java is a diva...
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionbar = getSupportActionBar();
        if (actionbar != null)
            actionbar.setSubtitle("by timia2109");

        rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);

        if (!HTTP.isOnline(this))
            alert(getString(R.string.offline), Toast.LENGTH_LONG);
        else if (saver != null)
            showAPIs();
        else {
            loadApis();
            if (saver.apis != null && saver.apis.length != 0)
                sendPush();
        }
    }

    @Override
    public void onBackPressed() {
        saver.save(this);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!saver.isSaved)
            saver.save(this);
    }

    private void loadApis() {
        if (Saver.hasSaver(getApplicationContext())) {
            saver = Saver.load(getApplicationContext());
            if (saver == null) {
                alert("Error LOADING DATA");
                System.exit(0);
            }

            if (saver.lastVCode != versionCode) {
                afterUpdate();
                saver.lastVCode = versionCode;
                saver.nosave();
            }
        }
        else
            updateData();

        showAPIs();
        update();
    }

    private void showAPIs() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) v.findViewById(R.id.kristID);
                    Intent wallet = new Intent(getActivity(), WalletActivity.class);
                    for (int i=0;i<saver.apis.length;i++) {
                        if (saver.apis[i].getName().equals(t.getText().toString())) {
                            wallet.putExtra("api", saver.apis[i]);
                            break;
                        }
                    }
                    wallet.putExtra("apis", saver.apis);
                    wallet.putExtra("saver", saver);
                    startActivity(wallet);
                    getActivity().finish();
                }
            };

            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    TextView t = (TextView) v.findViewById(R.id.kristID);

                    final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.actionFor, t.getText()));
                    builder.setItems(longClickOpts, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder=null;
                            final String address = ((TextView) v.findViewById(R.id.kristID)).getText().toString();
                            switch (which) {
                                case 0:
                                    builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle(R.string.delWallet);
                                    builder.setMessage(getString(R.string.delWallet) + ": " +address);
                                    builder.setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            saver.removeAPI(address);
                                            showAPIs();
                                        }
                                    });
                                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    break;
                                case 1:
                                    builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle(R.string.delWallet);
                                    builder.setMessage(getString(R.string.dialogWalletName, address));
                                    final EditText input = new EditText(getActivity());
                                    builder.setView(input);
                                    builder.setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String inputString = input.getText().toString();
                                            for (int i=0; i<saver.apis.length; i++) {
                                                if (saver.apis[i].getName().equals(address)) {
                                                    if (inputString.equals(""))
                                                        saver.apis[i].setAlias();
                                                    else
                                                        saver.apis[i].setAlias(inputString);
                                                    saver.isSaved = false;
                                                    Snackbar.make(v, getString(R.string.nameSaved), Snackbar.LENGTH_SHORT).show();
                                                    break;
                                                }
                                            }
                                            showAPIs();
                                            sendPush();
                                        }
                                    });
                                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    break;
                            }
                            if (builder != null)
                                builder.show();
                        }
                    });
                    builder.show();

                    return false;
                }
            };

            RVAdapter adapter = new RVAdapter(saver.apis, onClickListener, onLongClickListener, actionbar);
            rv.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsItemSelected(item,this);
    }

    public MainActivity getActivity() {
        return this;
    }

    public void addWallet(String password, boolean refreshUI) {
        saver.appendAPI(new KristAPI(password));
        //saveAll();
        showAPIs();
        sendPush();
    }

    public void addWalletDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.addKWallet);
        builder.setMessage(R.string.addDialogMessage);
        final EditText key = new EditText(this);
        key.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        key.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setView(key);
        builder.setPositiveButton(R.string.addWallet, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //String val = id.getText().toString();
                addWallet(key.getText().toString(), true);
                alert(getString(R.string.walletAdd), Toast.LENGTH_SHORT);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (saver.apis == null) {
                    onStop();
                }
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void alert(String message, int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }

    public void alert(String message) {
        alert(message, Toast.LENGTH_SHORT);
    }

    private void update() {
        final Handler handler = new Handler();
        final PostData pd = new PostData();
        if (saver.allowStatics) {
            pd.put("sdkV", Integer.toString(Build.VERSION.SDK_INT))
                    .put("did", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HTTP.postURL(HTTP.UPDATE_URL+"?appV="+versionCode, pd);
                    JSONObject resp = new JSONObject(response);
                    saver.setWebApps(resp.getJSONArray("webApps"));
                    if (resp.getBoolean("update")) {
                        saver.hasUpdate = true;
                        final String toVersion = resp.getString("toVersion");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showUpdateModal(toVersion);
                            }
                        });
                    }
                    saver.lastUpdate = System.currentTimeMillis() / 1000L;
                    saver.nosave();
                }
                catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            alert(getString(R.string.failUpdate, e.getMessage()));
                        }
                    });
                }
            }
        }).start();
    }

    private void showUpdateModal(final String toVersion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.updateFound));
        builder.setMessage(getString(R.string.updateMessage));

        builder.setPositiveButton("Download v"+toVersion+" APK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saver.hasUpdate = false;
                saver.nosave();
                //saveAll();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(HTTP.getUpdateURL(toVersion)));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), cancelClickListener);

        builder.show();
    }

    public static void updateNotes(final Context context) {
        final android.os.Handler h = new android.os.Handler();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final String notes = HTTP.getUpdateNotes();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Update Notes");
                        builder.setMessage(notes);
                        builder.show();
                    }
                });
            }
        });
        t.start();
    }

    private void afterUpdate() {
        updateNotes(this);
    }

    private void sendPush() {
        Intent intent = new Intent(this, PushService.class);
        intent.putExtra("saver", saver);
        startService(intent);
    }

    private void updateData() {
        //System.out.println("Dev Dev: \t"+Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        android.support.v7.app.AlertDialog.Builder welcomeMessage = new android.support.v7.app.AlertDialog.Builder(this);
        welcomeMessage.setTitle(R.string.welcomeTitle);
        final LinearLayout welcomeView = (LinearLayout) LayoutInflater.from(welcomeMessage.getContext()).inflate(R.layout.welcome_alert, null);
        welcomeMessage.setView(welcomeView);
        welcomeMessage.setPositiveButton(R.string.welcomeGo,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                saver.allowStatics = ((CheckBox) welcomeView.findViewById(R.id.staticsBox)).isChecked();
                dialog.cancel();
                addWalletDialog();
            }
        });
        welcomeMessage.show();

        saver = new Saver();
        saver.hasUpdate  = false;
        saver.lastUpdate = 0;
        saver.lastVCode = 0;
        saver.nosave();
    }

    public static boolean optionsItemSelected(MenuItem item,final Activity ac) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.addItem:
                ((MainActivity)ac).addWalletDialog();
                return true;
            case R.id.menu_delAll:
                ((MainActivity)ac).saver.apis = new KristAPI[0];
                ((MainActivity)ac).showAPIs();
                //saveAll();
                ((MainActivity)ac).addWalletDialog();
                return true;
            case R.id.menu_googleplus:
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse( "https://plus.google.com/u/0/communities/110417735196001201854" ));
                ac.startActivity(i);
                return true;
            case R.id.menu_updateNotes:
                MainActivity.updateNotes(ac);
                return true;
            case R.id.menu_about:
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse( "https://github.com/timia2109/KristWallet/blob/master/README.md" ));
                ac.startActivity(i);
                return true;
            case R.id.menu_dateFormat:
                AlertDialog.Builder builder = new AlertDialog.Builder(ac);
                builder.setTitle(R.string.editDateFormat);
                LayoutInflater inflater = LayoutInflater.from(ac);
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dateformat_alert, null);
                ((TextView)layout.findViewById(R.id.textViewDateFormat)).setText(R.string.editDateDesc);
                ((TextView)layout.findViewById(R.id.textViewDateFormat)).setMovementMethod(LinkMovementMethod.getInstance());
                final EditText input = (EditText) layout.findViewById(R.id.dateformat);
                input.setText(((MainActivity)ac).saver.dateFormat);
                builder.setView(layout);
                builder.setPositiveButton(R.string.editDateFormat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity)ac).saver.dateFormat = input.getText().toString();
                        ((MainActivity)ac).saver.nosave();
                    }
                });
                builder.setNegativeButton(R.string.cancel, cancelClickListener);
                builder.show();
                break;
        }
        return false;
    }
}
