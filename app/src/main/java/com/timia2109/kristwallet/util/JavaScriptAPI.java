package com.timia2109.kristwallet.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.timia2109.kristwallet.KristAPI;
import com.timia2109.kristwallet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 25.02.2016.
 */
public class JavaScriptAPI {
    Activity context;
    WebView webView;

    public JavaScriptAPI(Activity pContext) {
        context = pContext;
    }

    public JavaScriptAPI(Activity pContext, WebView pWebView) {
        context = pContext;
        webView = pWebView;
    }

    @JavascriptInterface
    public void sendKrist(String paddress) {
        final class IntHolder {
            public int hold = 0;
            public IntHolder(int p) {hold = p;}
        }

        class TransactionDataHolder {
            String address, metadata;
            long amount=1;
            boolean canEdit=true;
        }

        final TransactionDataHolder tdh = new TransactionDataHolder();
        if (paddress.charAt(0) == '{') {
            try {
                JSONObject data = new JSONObject(paddress);
                tdh.address = data.getString("address");
                if (data.has("amount")) {
                    tdh.amount = data.getLong("amount");
                    if (data.has("canEdit"))
                        tdh.canEdit = data.getBoolean("canEdit");
                }
                if (data.has("metadata"))
                    tdh.metadata = data.getString("metadata");
            }
            catch (JSONException e) {
                return;
            }
        }
        else {
            tdh.address = paddress;
        }

        Intent data = context.getIntent();
        final KristAPI[] apis = (KristAPI[]) data.getSerializableExtra("apis");
        final IntHolder apiPointer = new IntHolder(0);
        final List<CharSequence> apisLabels = new ArrayList<>();
        for (int i=0; i<apis.length; i++) {
            apisLabels.add(apis[i].getName());
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Send KST to "+tdh.address);

        View builderView = LayoutInflater.from(context).inflate(R.layout.send_alert,null);
        final EditText kristAns = (EditText) builderView.findViewById(R.id.kristAns);
        kristAns.setText(Long.toString(tdh.amount));
        final EditText kristMeta = (EditText) builderView.findViewById(R.id.kristMeta);
        kristMeta.setText(tdh.metadata);

        if (!tdh.canEdit) {
            kristAns.setFocusable(false);
            kristMeta.setFocusable(false);
            kristAns.setTextColor(0xFF6E6E6E);
            kristMeta.setTextColor(0xFF6E6E6E);
        }

        final Button chooseAPI = (Button) builderView.findViewById(R.id.walletChoose);
        chooseAPI.setText(apis[apiPointer.hold].getName());
        chooseAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Pick a Wallet");
                builder.setItems(apisLabels.toArray(new CharSequence[apisLabels.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        apiPointer.hold = which;
                        chooseAPI.setText(apis[apiPointer.hold].getName());
                    }
                });
                builder.show();
            }
        });

        builder.setView(builderView);
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long ans = 0;
                try {
                    ans = Long.parseLong(kristAns.getText().toString());
                    tdh.metadata = kristMeta.getText().toString();
                    if (tdh.metadata.equals(""))
                        tdh.metadata = null;
                    KristSender ks = new KristSender(ans, tdh.address, apis[apiPointer.hold], tdh.metadata, context);
                    ks.putRunnable(new SendKSTResult(true, tdh.address, webView));
                    ks.putView(webView);
                    ks.start();
                } catch (NumberFormatException e) {
                    Toast.makeText(context, context.getString(R.string.noNumber), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (webView != null)
                    webView.post(new SendKSTResult(false, null, webView));
            }
        });
        builder.show();
    }

    @JavascriptInterface
    public String getAddresses() {
        StringBuilder back = new StringBuilder("[");
        KristAPI[] apis = (KristAPI[]) context.getIntent().getSerializableExtra("apis");
        for (int i=0; i<apis.length; i++) {
            back.append("\"").append(apis[i].getAddress()).append("\"");
            if (i < apis.length-1)
                back.append(",");
        }
        back.append("]");
        return back.toString();
    }

    @JavascriptInterface
    public int getVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    public static class SendKSTResult implements Runnable {
        public boolean done;
        public WebView wv;
        public String receiver;

        public SendKSTResult(boolean done, String receiver, WebView wv) {
            this.done = done;
            this.receiver = receiver;
            this.wv = wv;
        }

        @Override
        public void run() {
            if (receiver != null)
                wv.loadUrl("javascript:try {window.onSendKST("+done+", '"+receiver+"');} catch (e) {}");
            else
                wv.loadUrl("javascript:try {window.onSendKST("+done+");} catch (e) {}");
        }
    }
}
