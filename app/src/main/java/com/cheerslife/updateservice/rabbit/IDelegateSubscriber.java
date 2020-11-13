package com.cheerslife.updateservice.rabbit;

public interface IDelegateSubscriber<T> {
    boolean isAvailable(String message);

    void dispatch(T entities);

    T format(String message);
}
