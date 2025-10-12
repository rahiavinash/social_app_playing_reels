package com.wedo8020.zelpyy.SocketProvider


import android.os.Handler
import android.os.Looper
import android.util.Log
import com.wedo8020.app.BuildConfig
import com.wedo8020.app.MainApplication
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.SocketEmitConstant
import com.wedo8020.app.utils.SocketListenerConstant
import com.wedo8020.app.utils.Utils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import java.net.URI


class SocketIOImpl : SocketIO {
    private var socket: Socket? = null

    override fun connect() {
        println("CallProcess->Connect Socket")

        try {

            socket = IO.socket(URI(BuildConfig.SOCKET_URL), IO.Options().apply {
                println("CallProcess->SocketToken-> ${Prefs.init().accessToken} ")
              //  query = "auth=Bearer " + Prefs.init().accessToken
                extraHeaders = mapOf("auth" to listOf("Bearer "+Prefs.init().accessToken))

//                callFactory = clientBuilder.build()
//                callFactory = clientBuilder.build()
                transports = listOf(io.socket.engineio.client.transports.WebSocket.NAME).toTypedArray()
            })

        } catch (e: Exception) {
            Utils.printLog(tag = "SocketERROR", msg = "${e.message}")
            e.printStackTrace()
        }
        setupSocketListeners()
        socket?.connect()
    }

    override fun isConnected(): Boolean {
        return if (socket?.connected() != null) {
            socket?.connected()!!
        } else {
            false
        }
    }

    override fun disconnect() {
        socket?.disconnect()
        socket?.close()
        socket = null
    }

    override fun listenOn(key: String, value: Emitter.Listener) {
        socket?.on(key, value)

    }

    override fun listenOff(key: String) {
        socket?.off(key)
    }

    override fun <T> emitMessage(key: String, value: T, onResult: (key: String, value: T) -> Unit) {
        try {
            socket?.emit(key, value)
            println("CallProcess->EmitMessage ${key} ${socket?.connected()}")
        } catch (e: JSONException) {
            println("CallProcess->EmitMessage${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupSocketListeners() {
        listenOff(Socket.EVENT_CONNECT)
        listenOff(Socket.EVENT_CONNECT_ERROR)
        listenOn(
            Socket.EVENT_CONNECT
        ) { args ->
            Handler(Looper.getMainLooper()).post{
                Utils.printLog(LogTypeEnum.DEBUG,tag = "EVENT CONNECTED",isConnected().toString())
                if (Prefs.init().accessToken.isEmpty()){
                    EventBus.getDefault().post(isSocketConnected(isConnected()))
                }else{
                    listenLogin()
                    Utils.runWithDelay(1000) {
                        emitLogin()
                    }

                }
            }
        }
        listenOn(
            Socket.EVENT_CONNECT_ERROR
        ) {
                args ->
            Utils.printLog(LogTypeEnum.DEBUG,tag = "EVENT CONNECTED",args[0].toString())
            Handler(Looper.getMainLooper()).post{

            }
        }
    }
    private fun listenLogin(){
        MainApplication.get().socket.listenOff(SocketListenerConstant.LOGIN_SUCCESS)
        MainApplication.get().socket.listenOn(SocketListenerConstant.LOGIN_SUCCESS){
                _->
            CoroutineScope(Dispatchers.Main).launch {
                Utils.printLog(LogTypeEnum.DEBUG,tag = "Gson_toJson login",isConnected().toString())

                EventBus.getDefault().post(isSocketConnected(isConnected()))
            }

        }
    }
    private fun emitLogin() {
        if (Prefs.init().accessToken.isNotEmpty()){
            CoroutineScope(Dispatchers.Main).launch {
                val obj = Utils.toJsonObject(
                    "token" to "Bearer " + Prefs.init()
                        .accessToken,
                    "country" to Utils.getCountryNameFromCode(),

                    )
                if (MainApplication.get().socket.isConnected()) {
                    MainApplication.get().socket.emitMessage(SocketEmitConstant.LOGIN, obj) { _, _ ->
                    }
                } else {
                    Log.e("emitMessage", "socket not connect4ed")
                }
            }
        }
    }
}