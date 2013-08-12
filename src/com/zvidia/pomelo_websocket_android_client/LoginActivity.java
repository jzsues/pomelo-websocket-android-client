package com.zvidia.pomelo_websocket_android_client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protocol.PomeloMessage;
import com.zvidia.pomelo.websocket.OnDataHandler;
import com.zvidia.pomelo.websocket.OnErrorHandler;
import com.zvidia.pomelo.websocket.OnHandshakeSuccessHandler;
import com.zvidia.pomelo.websocket.PomeloClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class LoginActivity extends Activity {

    String LOG_TAG = getClass().getSimpleName();

    EditText m_username;

    EditText m_channel;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        m_username = (EditText) findViewById(R.id.username);
        m_channel = (EditText) findViewById(R.id.channel);
    }

    String host = null;
    String port = null;

    OnHandshakeSuccessHandler onConnectorHandshakeSuccessHandler = new OnHandshakeSuccessHandler() {
        @Override
        public void onSuccess(PomeloClient client, JSONObject jsonObject) {
            try {
                JSONObject connectorJson = new JSONObject();
                String username = m_username.getText().toString();
                String rid = m_channel.getText().toString();
                connectorJson.put("username", username);
                connectorJson.put("rid", rid);
                client.request("connector.entryHandler.enter", connectorJson.toString(), new OnDataHandler() {

                    @Override
                    public void onData(PomeloMessage.Message message) {
                        Log.d(LOG_TAG, message.toString());
                    }
                });
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } catch (PomeloException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    };

    OnHandshakeSuccessHandler onGateHandshakeSuccessHandler = new OnHandshakeSuccessHandler() {
        @Override
        public void onSuccess(final PomeloClient client, JSONObject jsonObject) {
            Log.d(LOG_TAG, "on gate handshake success!" + jsonObject.toString());
            try {
                JSONObject gateJson = new JSONObject();
                String username = m_username.getText().toString();
                String channel = m_channel.getText().toString();
                gateJson.put("uid", username);
                client.request("gate.gateHandler.queryEntry", gateJson.toString(), new OnDataHandler() {
                    @Override
                    public void onData(PomeloMessage.Message message) {
                        JSONObject bodyJson = message.getBodyJson();
                        int code = 0;
                        try {
                            code = bodyJson.getInt(PomeloClient.HANDSHAKE_RES_CODE_KEY);
                            if (code == 200) {
                                host = bodyJson.getString(PomeloClient.HANDSHAKE_RES_HOST_KEY);
                                port = bodyJson.getString(PomeloClient.HANDSHAKE_RES_PORT_KEY);
                                client.close();
                                PomeloClient connector = new PomeloClient(new URI("ws://" + host + ":" + port));
                                connector.setOnHandshakeSuccessHandler(onConnectorHandshakeSuccessHandler);
                                connector.setOnErrorHandler(onErrorHandler);
                                connector.connect();
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        } catch (URISyntaxException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } catch (PomeloException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    };

    OnErrorHandler onErrorHandler = new OnErrorHandler() {
        @Override
        public void onError(Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    };

    public void doLogin(View view) {
        try {
            Log.d(LOG_TAG, "start connector to pomelo server");
            PomeloClient gateConnector = new PomeloClient(new URI("ws://192.168.1.5:3014"));
            gateConnector.setOnHandshakeSuccessHandler(onGateHandshakeSuccessHandler);
            gateConnector.setOnErrorHandler(onErrorHandler);
            gateConnector.connect();
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
