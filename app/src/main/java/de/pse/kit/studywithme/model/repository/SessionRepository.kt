package de.pse.kit.studywithme.model.repository

import android.content.Context
import android.util.Log
import de.pse.kit.studywithme.SingletonHolder
import de.pse.kit.studywithme.model.data.Session
import de.pse.kit.studywithme.model.data.SessionAttendee
import de.pse.kit.studywithme.model.database.AppDatabase
import de.pse.kit.studywithme.model.network.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class SessionRepository private constructor(context: Context) : SessionRepositoryInterface {
    private val sessionService = SessionService.instance
    private val sessionDao = AppDatabase.getInstance(context).sessionDao()
    private val auth = Authenticator
    // TODO: Local in key value speichern beim anmelden und hier abrufen
    private val uid: Int?  = null

    override fun getSessions(groupID: Int): Flow<List<Session>> {
        if (auth.firebaseUID == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        return channelFlow {
            val truthWasSend = AtomicBoolean(false)

            launch {
                val remoteSession = sessionService.getSessions(groupID)
                send(remoteSession)
                truthWasSend.set(true)
            }
            launch {
                val localSession = sessionDao.getSessions(groupID)
                if (!truthWasSend.get()) {
                    send(localSession)
                }
            }
        }.filterNotNull()
    }

    override fun newSession(session: Session): Boolean {
        if (auth.firebaseUID == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        return runBlocking {
            val remoteSession = sessionService.newSession(session)
            if (remoteSession != null) {
                Log.d(auth.TAG, "Remote Database Session Post:success")
                sessionDao.saveSession(remoteSession)
                return@runBlocking true
            } else {
                return@runBlocking false
            }
        }
    }

    override fun editSession(session: Session): Boolean {
        if (auth.firebaseUID == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        return runBlocking {
            val remoteSession = sessionService.editSession(session)

            if (remoteSession == session) {
                sessionDao.editSession(session)
                return@runBlocking true
            } else {
                return@runBlocking false
            }
        }
    }

    override fun removeSession(session: Session) {
        if (auth.firebaseUID == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        runBlocking {
            launch {
                sessionDao.removeSession(session)
            }
            launch {
                sessionService.removeSession(session.sessionID)
            }
        }
    }

    override fun newAttendee(sessionID: Int): Boolean {
        if (uid == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }
        
        return runBlocking {
            val remoteSessionAttendee = sessionService.newAttendee(uid, sessionID)
            if (remoteSessionAttendee != null) {
                Log.d(auth.TAG, "Remote Database Session Post:success")
                sessionDao.saveSessionAttendee(remoteSessionAttendee)
                return@runBlocking true
            } else {
                return@runBlocking false
            }
        }
    }

    override fun removeAttendee(sessionID: Int) {
        if (uid == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        runBlocking {
            launch {
                sessionService.removeAttendee(uid, sessionID)
            }
            launch {
                sessionDao.removeSessionAttendee(SessionAttendee(sessionID, uid, true))
            }
        }
    }

    override fun getAttendees(sessionID: Int): Flow<List<SessionAttendee>> {
        if (auth.firebaseUID == null) {
            // TODO: Explicit exception class
            throw Exception("Authentication Error: No local user signed in.")
        }

        return channelFlow {
            val truthWasSend = AtomicBoolean(false)

            launch {
                val remoteSessionAttendees = sessionService.getAttendees(sessionID)
                send(remoteSessionAttendees)
                truthWasSend.set(true)
            }
            launch {
                val localSessionAttendees = sessionDao.getSessionAttendees(sessionID)
                if (!truthWasSend.get()) {
                    send(localSessionAttendees)
                }
            }
        }.filterNotNull()
    }

    companion object : SingletonHolder<SessionRepository, Context>({ SessionRepository(it) })
}