package com.wedo8020.app.eventBus

import org.greenrobot.eventbus.EventBus

class GlobalBus {

    companion object{
        private var bus: EventBus?=null
        fun getBus(): EventBus? {
            if (bus == null) bus = EventBus()
            return bus
        }
    }

}