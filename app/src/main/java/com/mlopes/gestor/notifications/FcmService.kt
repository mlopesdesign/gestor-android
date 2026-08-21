package com.mlopes.gestor.notifications

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mlopes.gestor.data.remote.FcmTokenStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Servico Firebase Cloud Messaging.
 *
 * - onNewToken(): token FCM novo/refreshed -> persiste e envia pro WP
 * - onMessageReceived(): push chegou -> mostra notificacao
 *
 * FIX v0.1.5: criado pra teste de FCM (F6 Notificacoes).
 */
@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var tokenStorage: FcmTokenStorage
    @Inject lateinit var notifier: Notifier

    override fun onNewToken(token: String) {
        tokenStorage.atualizarToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val titulo = message.notification?.title
            ?: message.data["titulo"]
            ?: "Gestor de Demandas"
        val corpo = message.notification?.body
            ?: message.data["corpo"]
            ?: message.data["body"]
            ?: ""
        val tag = message.data["tag"] ?: "fcm_default"
        notifier.show(titulo = titulo, corpo = corpo, tag = tag)
    }
}
