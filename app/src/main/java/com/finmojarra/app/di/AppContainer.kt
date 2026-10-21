package com.finmojarra.app.di

import android.content.Context
import com.finmojarra.app.data.auth.AuthRepository
import com.finmojarra.app.data.auth.FirebaseAuthRepository
import com.finmojarra.app.data.auth.GoogleTokenProvider
import com.finmojarra.app.data.auth.SessionHolder
import com.finmojarra.app.data.goals.FirestoreGoalsRepository
import com.finmojarra.app.data.goals.GoalsRepository
import com.finmojarra.app.data.profile.FirebaseProfileRepository
import com.finmojarra.app.data.profile.FirestoreProfileDetailsRepository
import com.finmojarra.app.data.profile.ProfileDetailsRepository
import com.finmojarra.app.data.profile.ProfileRepository

/**
 * Contenedor de dependencias manual (suficiente para esta fase).
 *
 * Aquí se conecta la app con Firebase: [FirebaseAuthRepository] es el único repositorio de
 * autenticación, [GoogleTokenProvider] pide el token de Google para "Continuar con Google" y
 * [FirestoreProfileDetailsRepository] guarda los datos extra del perfil en la nube (Firestore).
 * Para volver a guardarlos solo en el teléfono usa `LocalProfileDetailsRepository(context)`.
 */
class AppContainer(context: Context) {
    val sessionHolder = SessionHolder()
    val authRepository: AuthRepository = FirebaseAuthRepository()
    val googleTokenProvider = GoogleTokenProvider()
    val profileRepository: ProfileRepository = FirebaseProfileRepository()
    val profileDetailsRepository: ProfileDetailsRepository = FirestoreProfileDetailsRepository(context)
    val goalsRepository: GoalsRepository = FirestoreGoalsRepository(context)
}
