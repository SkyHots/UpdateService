package com.cheerslife.updateservice.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

interface IMQService {
    void connect(ConnectionFactory factory) throws IOException, TimeoutException;

    void disconnect() throws IOException, TimeoutException;

    default Connection getConnection(ConnectionFactory factory) throws IOException, TimeoutException {
        return factory.newConnection();
    }
}
