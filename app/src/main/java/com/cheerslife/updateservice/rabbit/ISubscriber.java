package com.cheerslife.updateservice.rabbit;

public interface ISubscriber {
    void dispatch(String message);

    void subscribe(IDelegateSubscriber callback);

    void unsubscribe(IDelegateSubscriber callback);
}
