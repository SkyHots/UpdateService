package com.cheerslife.updateservice.rabbit.config;

import com.cheerslife.updateservice.rabbit.ISubscriber;
import com.cheerslife.updateservice.rabbit.NameThreadFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RabbitConfig {
    private String host;
    private int port;
    private String userName;
    private String password;
    private int heartbeat;
    private List<String> rotingKeyArray;
    private String type;
    private String heartbeatRoutingKey;
    private String callControlRoutingKey;
    private String exchange;
    private ExecutorService receiveExecutor;
    private ExecutorService sendExecutor;
    private ISubscriber subscriber;

    private RabbitConfig(Builder builder) {
        setHost(builder.host);
        setPort(builder.port);
        setUserName(builder.userName);
        setPassword(builder.password);
        setHeartbeat(builder.heartbeat);
        setRotingKeyArray(builder.rotingKeyArray);
        setType(builder.type);
        setHeartbeatRoutingKey(builder.heartbeatRoutingKey);
        setCallControlRoutingKey(builder.callControlRoutingKey);
        setExchange(builder.exchange);
        setReceiveExecutor(builder.receiveExecutor);
        setSendExecutor(builder.sendExecutor);
        setSubscriber(builder.subscriber);
    }

    public ISubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(ISubscriber subscriber) {
        this.subscriber = subscriber;
    }

    public ExecutorService getReceiveExecutor() {
        return receiveExecutor;
    }

    private void setReceiveExecutor(ExecutorService receiveExecutor) {
        this.receiveExecutor = receiveExecutor;
    }

    public ExecutorService getSendExecutor() {
        return sendExecutor;
    }

    private void setSendExecutor(ExecutorService sendExecutor) {
        this.sendExecutor = sendExecutor;
    }

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    private void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public List<String> getRotingKeyArray() {
        return rotingKeyArray;
    }

    private void setRotingKeyArray(List<String> rotingKeyArray) {
        this.rotingKeyArray = rotingKeyArray;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getHeartbeatRoutingKey() {
        return heartbeatRoutingKey;
    }

    private void setHeartbeatRoutingKey(String heartbeatRoutingKey) {
        this.heartbeatRoutingKey = heartbeatRoutingKey;
    }

    public String getCallControlRoutingKey() {
        return callControlRoutingKey;
    }

    private void setCallControlRoutingKey(String callControlRoutingKey) {
        this.callControlRoutingKey = callControlRoutingKey;
    }

    public String getExchange() {
        return exchange;
    }

    private void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public static final class Builder {

        static final int DEFAULT_MQ_HEARTBEAT = 40;

        private String host;
        private int port;
        private String userName;
        private String password;
        private int heartbeat = DEFAULT_MQ_HEARTBEAT;
        private List<String> rotingKeyArray;
        private String type;
        private String heartbeatRoutingKey;
        private String callControlRoutingKey;
        private String exchange;
        private ExecutorService receiveExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new NameThreadFactory("ReceiveService"));
        private ExecutorService sendExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new NameThreadFactory("SendService"));
        private ISubscriber subscriber;

        public Builder() {
        }

        public Builder host(String val) {
            host = val;
            return this;
        }

        public Builder port(int val) {
            port = val;
            return this;
        }

        public Builder userName(String val) {
            userName = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public Builder heartbeat(int val) {
            heartbeat = val;
            return this;
        }

        public Builder rotingKeyArray(List<String> val) {
            rotingKeyArray = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder heartbeatRoutingKey(String val) {
            heartbeatRoutingKey = val;
            return this;
        }

        public Builder callControlRoutingKey(String val) {
            callControlRoutingKey = val;
            return this;
        }

        public Builder exchange(String val) {
            exchange = val;
            return this;
        }

        public Builder receiveExecutor(ExecutorService val) {
            receiveExecutor = val;
            return this;
        }

        public Builder sendExecutor(ExecutorService val) {
            sendExecutor = val;
            return this;
        }

        public Builder subscriber(ISubscriber val) {
            subscriber = val;
            return this;
        }

        public RabbitConfig build() {
            return new RabbitConfig(this);
        }
    }
}
