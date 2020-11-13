package com.cheerslife.updateservice.rabbit;

import android.util.Log;

import com.cheerslife.updateservice.rabbit.config.RabbitConfig;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class SenderService implements IMQService {
    private static final String TAG = "SenderService";
    private ShutdownListener mConnectionListener = cause -> {
        Log.w(TAG, "SenderService: mConnection has shutdown");
    };

    private Connection connection;
    private RabbitConfig mqManager;
    private Gson gson = new Gson();

    SenderService(RabbitConfig mqManager) {
        this.mqManager = mqManager;
    }

    void sendMessage(Object message, String routingKey) {
        mqManager.getSendExecutor().submit(() ->
                send(message, routingKey, mqManager.getExchange(), mqManager.getType()));
    }

    private void send(Object message, String routingKey, String exchange, String type) {
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchange, type, true);
            String body = gson.toJson(message);
            Log.i(TAG, "send: " + body);
            channel.basicPublish(exchange, routingKey, null, body.getBytes());
            channel.abort();
            channel.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(ConnectionFactory factory) throws IOException, TimeoutException {
        connection = getConnection(factory);
        connection.addShutdownListener(mConnectionListener);
    }

    @Override
    public void disconnect() throws IOException {
        if (connection == null || !connection.isOpen()) {
            return;
        }
        connection.removeShutdownListener(mConnectionListener);
        connection.abort();
        connection.close();
    }
}
