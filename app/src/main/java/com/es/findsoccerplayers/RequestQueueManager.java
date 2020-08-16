package com.es.findsoccerplayers;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;

public class RequestQueueManager {

    private static RequestQueueManager instance;
    private RequestQueue requestQueue;
    private WeakReference<Context> ctx;

    private RequestQueueManager(Context context) {
        ctx = new WeakReference<>(context);
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestQueueManager(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.get().getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
