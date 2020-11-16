package com.cheerslife.updateservice.rabbit;


import com.cheerslife.updateservice.rabbit.config.RabbitConfig;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RabbitMQManager {
    private static final String TAG = "RabbitMQManager";
    private RabbitConfig config;

    public RabbitConfig getConfig() {
        return config;
    }

    private ConnectionFactory mConnectionFactory = new ConnectionFactory();
    private SenderService mSenderService;
    private ReceiveService mReceiveService;

    private ExecutorService mDispatchService;
    private ExecutorService mConnectService =
            new ThreadPoolExecutor(1, 2, 1, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<>(), new NameThreadFactory("ConnectService"));


    private RabbitMQManager() {
    }

    public static RabbitMQManager getInstance() {
        return Holder.INSTANCE;
    }

    public void publish(Object message, String routingKey) {
        mSenderService.sendMessage(message, routingKey);
    }

    private static class Holder {
        public static final RabbitMQManager INSTANCE = new RabbitMQManager();
    }

    void dispatchMessage(String message) {
        mDispatchService.submit(() -> onDispatchMessage(message));
    }

    private void onDispatchMessage(String message) {
        formatMessage(message);
    }

    private void formatMessage(String message) {
        config.getSubscriber().dispatch(message);
    }

    private synchronized void connect() {
        try {
            mSenderService.connect(mConnectionFactory);
            mReceiveService.connect(mConnectionFactory);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private synchronized void disconnect() {
        try {
            mSenderService.disconnect();
            mReceiveService.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectMQ() {
        mConnectService.submit(() -> {
            synchronized (RabbitMQManager.this) {
                disconnect();
                connect();
            }
        });
    }

    public void init(RabbitConfig config) {
        this.config = config;
        mConnectionFactory.setUsername(config.getUserName());
        mConnectionFactory.setPassword(config.getPassword());
        mConnectionFactory.setHost(config.getHost());
        mConnectionFactory.setPort(config.getPort());
        mConnectionFactory.setRequestedHeartbeat(config.getHeartbeat());
        mConnectionFactory.setTopologyRecoveryEnabled(true);
        mConnectionFactory.setAutomaticRecoveryEnabled(true);
        mConnectionFactory.setNetworkRecoveryInterval(10 * 1000L);
        mDispatchService = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new NameThreadFactory("DispatchService"));
        mSenderService = new SenderService(config);
        mReceiveService = new ReceiveService(config);
    }
}
