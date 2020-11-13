package com.cheerslife.updateservice.rabbit;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class SubscriberManager implements ISubscriber {
    private static final String TAG = "InternetThingsMessageSu";

    private List<IDelegateSubscriber> delegateSubscribers = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private SubscriberManager() {
    }

    public static SubscriberManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        public static final SubscriberManager INSTANCE = new SubscriberManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void dispatch(String message) {
        for (IDelegateSubscriber subscriber : delegateSubscribers) {
            if (subscriber.isAvailable(message)) {
                handler.post(() -> subscriber.dispatch(subscriber.format(message)));
                break;
            }
        }
    }

    @Override
    public void subscribe(IDelegateSubscriber subscriber) {
        if (subscriber != null && !delegateSubscribers.contains(subscriber)) {
            delegateSubscribers.add(subscriber);
        }
    }

    @Override
    public void unsubscribe(IDelegateSubscriber subscriber) {
        delegateSubscribers.remove(subscriber);
    }

}
