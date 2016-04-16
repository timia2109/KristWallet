package com.timia2109.kristwallet.KLottery;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.timia2109.kristwallet.KristAPI;
import com.timia2109.kristwallet.R;
import com.timia2109.kristwallet.util.JavaScriptAPI;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class KLotteryFragment extends Fragment {
    WebSocketClient mWebSocketClient;
    Handler handler;
    Button sendKrist;
    View view;
    TextView potValue, countdown;
    RecyclerView recyclerView;
    String addr;
    MyCount count;


    public KLotteryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_klottery, container, false);
        sendKrist = (Button) view.findViewById(R.id.sendButton);
        potValue = (TextView) view.findViewById(R.id.potValue);
        countdown = (TextView) view.findViewById(R.id.countdownTFFORTHIS);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        sendKrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JavaScriptAPI jsa = new JavaScriptAPI(getActivity());
                jsa.sendKrist(addr);
            }
        });
        if (mWebSocketClient == null)
            connectWebSocket();

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (count != null) {
            count.end = -1;
            count = null;
        }
        if (mWebSocketClient != null)
            mWebSocketClient.close();
    }

    @Override
    public void onDestroy() {
        onDetach();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://klottery.sci4me.com/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake x) {

            }

            @Override
            public void onMessage(final String s) {
                final JSONObject msg;
                try {
                    msg = new JSONObject(s);
                    String type = msg.getString("type");
                    if (type.equals("purchase_address")) {
                        addr = msg.getString("address");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendKrist.setText(getString(R.string.klotterySend) + "        (" + addr + ")");
                            }
                        });
                    } else if (type.equals("next_payout")) {
                        final Date endOn = parseDate(msg.getString("time"));
                        long mills = endOn.getTime() - System.currentTimeMillis();
                        count = new KLotteryFragment.MyCount(mills, 1000, handler);
                        count.start();

                    } else if (type.equals("update_users")) {
                        final JSONObject userz = msg.getJSONObject("users");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(new KLotteryUsersAdapter(userz));
                                recyclerView.invalidate();
                            }
                        });
                    } else if (type.equals("pot_value")) {
                        final String val = msg.getString("value");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                potValue.setText(val+KristAPI.currency);
                            }
                        });
                    }
                } catch (Exception e) {
                    Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Snackbar.make(view, "Connection closed", Snackbar.LENGTH_SHORT);
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT);
            }
        };
        mWebSocketClient.connect();
    }

    public class MyCount extends Thread {
        Handler handler;
        long end, interval;
        public MyCount(long millisInFuture, long countDownInterval, Handler h) {
            end = millisInFuture;
            interval = countDownInterval;
            handler = h;
        }

        public void run() {
            while (end > 0) {
                onTick(end);
                end -= interval;
                try {
                    Thread.sleep(interval);
                } catch (Exception ignored) {}
            }
        }

        public void onTick(long millisUntilFinished) {
            millisUntilFinished = millisUntilFinished/1000;
            final int mins = (int) millisUntilFinished / 60;
            final int secs = (int) millisUntilFinished%60;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    countdown.setText(mins+" minutes and "+secs+" secounds");
                }
            });
        }
    }

    public Date parseDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat(KristAPI.DATE_FORMAT, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format.parse(date);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public class KLotteryUsersAdapter extends RecyclerView.Adapter<UserHolder>{
        List<JSONObject> users;

        public KLotteryUsersAdapter(JSONObject usersRaw){
            users = new ArrayList<>();
            Iterator<String> keys = usersRaw.keys();
            while ( keys.hasNext() ) {
                String key = keys.next();
                try {
                    JSONObject put = new JSONObject();
                    put.put("name", key);
                    put.put("val", usersRaw.getLong(key));
                    users.add(put);
                } catch (JSONException e) {
                    Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT);
                }
            }
            Collections.sort(users, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    try {
                        if (lhs.getLong("val") < rhs.getLong("val"))
                            return -1;
                        else if ((lhs.getLong("val") > rhs.getLong("val")))
                            return 1;
                    } catch (JSONException ignored) {}
                    return 0;
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_cardview, viewGroup, false);
            UserHolder pvh = new UserHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(final UserHolder holder, final int i) {
            JSONObject c = users.get(i);
            try {
                holder.address.setText( c.getString("name") );
                holder.tickets.setText( Long.toString( c.getLong("val") )+KristAPI.currency );
            } catch (Exception ignored) {}
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        public TextView address, tickets;

        UserHolder(View itemView) {
            super(itemView);
            address = (TextView) itemView.findViewById(R.id.kristID);
            tickets = (TextView) itemView.findViewById(R.id.kristStateTV);
        }
    }

}
