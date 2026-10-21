package com.finmojarra.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finmojarra.app.di.AppContainer
import com.finmojarra.app.ui.auth.InicioScreen
import com.finmojarra.app.ui.auth.InicioViewModel
import com.finmojarra.app.ui.auth.LoginScreen
import com.finmojarra.app.ui.auth.LoginViewModel
import com.finmojarra.app.ui.auth.RecoverScreen
import com.finmojarra.app.ui.auth.RecoverViewModel
import com.finmojarra.app.ui.auth.RegisterScreen
import com.finmojarra.app.ui.auth.RegisterViewModel
import com.finmojarra.app.ui.goals.GoalsScreen
import com.finmojarra.app.ui.goals.GoalsViewModel
import com.finmojarra.app.ui.home.HomeScreen
import com.finmojarra.app.ui.profile.AddressScreen
import com.finmojarra.app.ui.profile.DemographicsScreen
import com.finmojarra.app.ui.profile.DocumentScreen
import com.finmojarra.app.ui.profile.EditPhotoScreen
import com.finmojarra.app.ui.profile.PhoneScreen
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel
import com.finmojarra.app.ui.profile.ProfileScreen
import com.finmojarra.app.ui.profile.ProfileViewModel

object Routes {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val REGISTRO_PATTERN = "registro?email={email}"
    const val RECUPERAR = "recuperar"
    const val HOME = "home"
    const val METAS = "metas"
    const val PERFIL = "perfil"
    const val PERFIL_FOTO = "perfil/foto"
    const val PERFIL_DEMOGRAFICA = "perfil/demografica"
    const val PERFIL_CELULAR = "perfil/celular"
    const val PERFIL_DIRECCION = "perfil/direccion"
    const val PERFIL_DOCUMENTO = "perfil/documento"
}

@Composable
fun FinMojarraNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.INICIO) {

        composable(Routes.INICIO) {
            val viewModel: InicioViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { InicioViewModel(container.authRepository, container.sessionHolder) }
                }
            )
            InicioScreen(
                viewModel = viewModel,
                googleTokenProvider = container.googleTokenProvider,
                onRegister = { email ->
                    navController.navigate("${Routes.REGISTRO}?email=${Uri.encode(email)}")
                },
                onLogin = { navController.navigate(Routes.LOGIN) },
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { LoginViewModel(container.authRepository, container.sessionHolder) }
                }
            )
            LoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onForgot = { navController.navigate(Routes.RECUPERAR) },
                onRegister = { navController.navigate(Routes.REGISTRO) },
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.REGISTRO_PATTERN,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val initialEmail = backStackEntry.arguments?.getString("email").orEmpty()
            val viewModel: RegisterViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        RegisterViewModel(
                            repository = container.authRepository,
                            sessionHolder = container.sessionHolder,
                            initialEmail = initialEmail,
                            detailsRepository = container.profileDetailsRepository
                        )
                    }
                }
            )
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RECUPERAR) {
            val viewModel: RecoverViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { RecoverViewModel(container.authRepository) }
                }
            )
            RecoverScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenGoals = { navController.navigate(Routes.METAS) }
            )
        }

        composable(Routes.METAS) {
            val viewModel: GoalsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { GoalsViewModel(container.goalsRepository) }
                }
            )
            GoalsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFIL) {
            val viewModel: ProfileViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ProfileViewModel(
                            container.profileRepository,
                            container.sessionHolder,
                            container.profileDetailsRepository
                        )
                    }
                }
            )
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.INICIO) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onEditPhoto = { navController.navigate(Routes.PERFIL_FOTO) },
                onOpenDemographics = { navController.navigate(Routes.PERFIL_DEMOGRAFICA) },
                onOpenPhone = { navController.navigate(Routes.PERFIL_CELULAR) },
                onOpenAddress = { navController.navigate(Routes.PERFIL_DIRECCION) },
                onOpenDocument = { navController.navigate(Routes.PERFIL_DOCUMENTO) }
            )
        }

        composable(Routes.PERFIL_FOTO) {
            EditPhotoScreen(
                viewModel = profileDetailsViewModel(container),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFIL_DEMOGRAFICA) {
            DemographicsScreen(
                viewModel = profileDetailsViewModel(container),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFIL_CELULAR) {
            PhoneScreen(
                viewModel = profileDetailsViewModel(container),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFIL_DIRECCION) {
            AddressScreen(
                viewModel = profileDetailsViewModel(container),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFIL_DOCUMENTO) {
            DocumentScreen(
                viewModel = profileDetailsViewModel(container),
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/** ViewModel de las pantallas de cambio del perfil (uno por pantalla). */
@Composable
private fun profileDetailsViewModel(container: AppContainer): ProfileDetailsViewModel =
    viewModel(
        factory = viewModelFactory {
            initializer { ProfileDetailsViewModel(container.profileDetailsRepository) }
        }
    )
