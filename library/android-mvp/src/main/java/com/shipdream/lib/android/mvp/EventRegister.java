/*
 * Copyright 2016 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvp;

import android.os.Handler;
import android.os.Looper;

import com.shipdream.lib.android.mvp.event.BaseEventV;
import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

class EventRegister {
    @Inject
    @EventBusV
    private EventBus eventBusV;

    private static Handler handler;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Object androidComponent;
    private boolean eventsRegistered = false;

    EventRegister(Object androidComponent) {
        this.androidComponent = androidComponent;
    }

    /**
     * Register event bus for views.
     */
    void registerEventBuses() {
        if (!eventsRegistered) {
            eventBusV.register(androidComponent);
            eventsRegistered = true;
            logger.trace("+Event2V bus registered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        } else {
            logger.trace("!Event2V bus already registered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    /**
     * Unregister event bus for views.
     */
    void unregisterEventBuses() {
        if (eventsRegistered) {
            eventBusV.unregister(androidComponent);
            eventsRegistered = false;
            logger.trace("-Event2V bus unregistered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
        } else {
            logger.trace("!Event2V bus already unregistered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    void onCreate() {
        AndroidMvp.graph().inject(this);
    }

    void onDestroy() {
        AndroidMvp.graph().release(this);
    }

    void postEvent2V(final BaseEventV event) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            eventBusV.post(event);
        } else {
            //Android handler is presented, posting to the main thread on Android.
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    eventBusV.post(event);
                }
            });
        }
    }

}