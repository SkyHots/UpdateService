package com.cheerslife.updateservice.rabbit;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.cheerslife.updateservice.rabbit.config.RabbitConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class ReceiveService implements IMQService {
    private ShutdownListener mShutdownListener = cause -> {
        Log.e(TAG, "ReceiveService: mConnection has shutdown");
        String hardError = "";
        String applInit = "";

        if (cause.isHardError()) {
            hardError = "connection";
        } else {
            hardError = "channel";
        }

        if (cause.isInitiatedByApplication()) {
            applInit = "application";
        } else {
            applInit = "broker";
        }
        String string = "Connectivity to MQ has failed.  It was caused by "
                + applInit + " at the " + hardError
                + " level.  Reason received " + cause.getReason();
        Log.e(TAG, string);
    };

    private static final String TAG = "ReceiveService";
    private Connection connection;
    private Channel mChannel;
    private String consumerTag = "";

    private RabbitConfig mqManager;

    ReceiveService(RabbitConfig mqManager) {
        this.mqManager = mqManager;
    }

    private void startSubscribe() {
        mqManager.getReceiveExecutor().submit(() ->
                subscribe(
                        mqManager.getExchange(),
                        mqManager.getType(),
                        mqManager.getRotingKeyArray()));
    }

    private void subscribe(String exchange, String type, List<String> routingKeyArray) {
        try {
            mChannel = connection.createChannel();
            mChannel.addShutdownListener(mShutdownListener);
            mChannel.basicQos(1);
            mChannel.exchangeDeclare(exchange, type, true);
            String queueName = mChannel.queueDeclare().getQueue();
            for (int i = 0; i < routingKeyArray.size(); i++) {
                mChannel.queueBind(queueName, exchange, routingKeyArray.get(i));
            }
            DefaultConsumer consumer = new ReceiveConsumer(mChannel, this);
            consumerTag = consumer.getConsumerTag();
            mChannel.basicConsume(queueName, false, consumer);
            Log.e(TAG, "connect mq success");
            ToastUtils.showLong("connect mq success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "connect mq fail" + e.getMessage());
            ToastUtils.showLong("connect mq fail");
        }
    }

    private void callback(String entity) {
        RabbitMQManager.getInstance().dispatchMessage(entity);
    }

    @Override
    public void connect(ConnectionFactory factory) throws IOException, TimeoutException {
        connection = getConnection(factory);
        connection.addShutdownListener(mShutdownListener);
        ((Recoverable) connection).addRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                if (recoverable instanceof Connection) {
                    Log.d(TAG, "Connection was recovered.");
                } else if (recoverable instanceof Channel) {
                    int channelNumber = ((Channel) recoverable).getChannelNumber();
                    Log.d(TAG, "Connection to channel #"
                            + channelNumber + " was recovered.");
                }
            }

            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                if (recoverable instanceof Connection) {
                    Log.d(TAG, "Connection was handleRecoveryStarted.");
                } else if (recoverable instanceof Channel) {
                    int channelNumber = ((Channel) recoverable).getChannelNumber();
                    Log.d(TAG, "Connection to channel #"
                            + channelNumber + " was handleRecoveryStarted.");
                }
            }
        });
        startSubscribe();
    }

    @Override
    public void disconnect() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            return;
        }
        if (mChannel != null && !TextUtils.isEmpty(consumerTag)) {
            mChannel.basicCancel(consumerTag);
            mChannel.removeShutdownListener(mShutdownListener);
            mChannel.abort();
            mChannel.close();
        }
        connection.removeShutdownListener(mShutdownListener);
        connection.abort();
        connection.close();
    }

    private static class ReceiveConsumer extends DefaultConsumer {
        private WeakReference<ReceiveService> mReference;

        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        ReceiveConsumer(Channel channel, ReceiveService service) {
            super(channel);
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            ReceiveService service = mReference.get();
            if (service == null) {
                Log.w(TAG, "handleDelivery: ReceiveService had been recycle");
                return;
            }
            try {
                service.mChannel.basicAck(envelope.getDeliveryTag(), false);
                String message = new String(body, StandardCharsets.UTF_8);
                service.callback(message);
                Log.e(TAG, "handleDelivery: " + message);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "handleDelivery: exception " + e.getMessage() + "  " + e.getCause());
            }
        }
    }

}
