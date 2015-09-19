package com.timia2109.kristwallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static String bUrl = "http://ceriat.net/krist";
    static String updateGradle = "https://raw.githubusercontent.com/timia2109/KristWallet/master/app/build.gradle";
    static String updateURL = "https://timia2109.com/kristWallet.apk";

    private RecyclerView rv;
    private LinearLayoutManager mLayoutManager;
    private List<KristAPI> apis;
    public long lastUpdateCheck;
    public boolean hasUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);

        loadApis();

        Intent intent = new Intent(this, PushService.class);
        String put = "";
        int size = apis.size();
        for (int i = 0; i < size; i++) {
            put += apis.get(i).getAddress() + ";";
        }
        intent.putExtra("apis", put);
        startService(intent);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    protected void saveAll() {
        String put = ""; //Addresses
        String put2 = ""; //Passwords
        for (int i=0; i<apis.size();i++) {
            put += apis.get(i).getAddress()+";";
            put2 += apis.get(i).getRawKey()+";";
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("addresses", put);
        editor.putString("keys", put2);
        editor.putBoolean("hasUpdate", hasUpdate);
        editor.putLong("lastUpdate", lastUpdateCheck);
        editor.commit();
    }

    private void loadApis() {
        apis = new ArrayList<>();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        hasUpdate = sharedPref.getBoolean("hasUpdate", false);
        lastUpdateCheck = sharedPref.getLong("lastUpdate", 0);

        String load = sharedPref.getString("keys", "");
        if (load == "") {
            addWalletDialog();
        }
        else {
            String[] lU = load.split(";");
            for (int i = 0; i < lU.length; i++) {
                KristAPI k = new KristAPI(bUrl, lU[i]);
                //Add for User
                apis.add(k);
            }
        }
        showAPIs();
        update();
    }

    private void showAPIs() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView t = (TextView) v.findViewById(R.id.kristID);
                Intent wallet = new Intent(getActivity(), WalletActivity.class);

                for (int i=0;i<apis.size();i++) {
                    if (apis.get(i).getAddress().equals(t.getText().toString())) {
                        wallet.putExtra("api",apis.get(i));
                        wallet.putExtra("apis", (ArrayList<KristAPI>) apis);
                        break;
                    }
                }

                startActivity(wallet);
                getActivity().finish();
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Wallet");
                final TextView t = (TextView) v.findViewById(R.id.kristID);
                builder.setMessage("Delete Wallet: "+t.getText());
                builder.setPositiveButton("Delete!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeWallet(t.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return false;
            }
        };

        RVAdapter adapter = new RVAdapter(apis, onClickListener, onLongClickListener);
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.addItem:
                addWalletDialog();
                return true;
            case R.id.menu_delAll:
                apis = new ArrayList<>();
                showAPIs();
                saveAll();
                addWalletDialog();
                return true;
            case R.id.menu_about:
                //Show about
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public MainActivity getActivity() {
        return this;
    }

    public void addWallet(String password) {
        apis.add(new KristAPI(bUrl, password));
        saveAll();
    }

    public void addWallet(String password, boolean refreshUI) {
        addWallet(password);
        showAPIs();
    }

    public void removeWallet(String address) {
        int size = apis.size();
        for (int i=0;i<size;i++) {
            if (apis.get(i).getAddress().equals(address)) {
                apis.remove(i);
                saveAll();
                loadApis();
                break;
            }
        }
    }

    public void addWalletDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Krist Wallet");
        builder.setMessage("Insert your Krist Password or a Krist ID to connect to connect to a Krist Wallet");
        final EditText key = new EditText(this);
        key.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setView(key);
        builder.setPositiveButton("Add Wallet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //String val = id.getText().toString();
                addWallet(key.getText().toString(), true);
                alert("Wallet added!", Toast.LENGTH_SHORT);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (apis == null) {
                    onStop();
                }
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void alert(String message, int length) {
        //length = Toast.LENGTH_SHORT || ...LENGTH_LONG
        Toast toast = Toast.makeText(getApplicationContext(), message, length);
        toast.show();
    }

    private void update() {
        class LoadBudgetTask extends AsyncTask<String, Boolean, Boolean> {
            protected Boolean doInBackground(String... in) {
                try {
                    try {
                        String r = HTTP.readURL(updateGradle);
                        String seek = "versionCode";
                        int pos = r.indexOf(seek);
                        String v = "";
                        int i = pos+seek.length()+1;
                        while (r.charAt(i) != '\n' && r.charAt(i) != ' ') {
                            v+=r.charAt(i);
                            i++;
                        }
                        return (getPackageManager().getPackageInfo(getPackageName(),0).versionCode < Integer.parseInt(v));
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } catch (Exception e) {

                }
                return null;
            }

            protected void onPostExecute(Boolean result) {
                if (result != null) {
                    if (result.equals(true)) {
                        showUpdateModal();
                    }
                    hasUpdate = false;
                    lastUpdateCheck = System.currentTimeMillis() / 1000L;
                    saveAll();
                }
            }
        }

        if (hasUpdate)
            showUpdateModal();
        else if (lastUpdateCheck < (System.currentTimeMillis() / 1000L) + 60*60*48) {
            LoadBudgetTask loadBudgetTask = new LoadBudgetTask();
            loadBudgetTask.execute("");
        }
    }

    private void showUpdateModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update found!");
        builder.setMessage("A new version of this App available! Download now?");

        builder.setPositiveButton("Add Wallet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hasUpdate = false;
                saveAll();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(updateURL));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hasUpdate = true;
                saveAll();
                dialog.cancel();
            }
        });

        builder.show();
    }

}
