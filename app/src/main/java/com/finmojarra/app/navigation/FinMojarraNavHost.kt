package com.finmojarra.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.finmojarra.app.ui.goals.CreateGoalScreen
import com.finmojarra.app.ui.goals.CreateGoalViewModel
import com.finmojarra.app.ui.goals.GoalDetailScreen
import com.finmojarra.app.ui.goals.GoalDetailViewModel
import com.finmojarra.app.ui.goals.GoalsScreen
import com.finmojarra.app.ui.goals.GoalsViewModel
import com.finmojarra.app.ui.home.HomeScreen
import com.finmojarra.app.data.learn.CreditLevelContent
import com.finmojarra.app.ui.learn.LearnProgressViewModel
import com.finmojarra.app.ui.learn.LearnScreen
import com.finmojarra.app.ui.learn.LevelScreen
import com.finmojarra.app.ui.learn.PillDetailScreen
import com.finmojarra.app.ui.learn.PillsScreen
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
    const val METAS_NUEVA = "metas/nueva"
    const val METAS_DETALLE = "meta"
    const val METAS_DETALLE_PATTERN = "meta/{goalId}"
    const val APRENDER = "aprender"
    const val APRENDER_NIVEL = "aprender/nivel"
    const val APRENDER_PILDORAS = "aprender/pildoras"
    const val APRENDER_PILDORA = "aprender/pildora"
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
            val viewModel: GoalsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { GoalsViewModel(container.goalsRepository) }
                }
            )
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            HomeScreen(
                viewModel = viewModel,
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenGoals = { navController.navigate(Routes.METAS) },
                onOpenLearn = { navController.navigate(Routes.APRENDER) },
                onCreateGoal = { navController.navigate(Routes.METAS_NUEVA) }
            )
        }

        composable(Routes.METAS) {
            val viewModel: GoalsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { GoalsViewModel(container.goalsRepository) }
                }
            )
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            GoalsScreen(
                viewModel = viewModel,
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenLearn = { navController.goToTab(Routes.APRENDER) },
                onOpenGoal = { id -> navController.navigate("${Routes.METAS_DETALLE}/$id") },
                onCreateGoal = { navController.navigate(Routes.METAS_NUEVA) }
            )
        }

        composable(
            route = Routes.METAS_DETALLE_PATTERN,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId").orEmpty()
            val viewModel: GoalDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { GoalDetailViewModel(container.goalsRepository, goalId) }
                }
            )
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            GoalDetailScreen(
                viewModel = viewModel,
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenLearn = { navController.goToTab(Routes.APRENDER) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Routes.METAS_NUEVA) {
            val viewModel: CreateGoalViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { CreateGoalViewModel(container.goalsRepository) }
                }
            )
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            CreateGoalScreen(
                viewModel = viewModel,
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onCreated = { navController.popBackStack() }
            )
        }

        composable(Routes.APRENDER) {
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            val progressViewModel = learnProgressViewModel(container)
            val steps by progressViewModel.steps.collectAsStateWithLifecycle()
            LearnScreen(
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenGoals = { navController.goToTab(Routes.METAS) },
                completedSteps = steps,
                onOpenPath = { navController.navigate(Routes.APRENDER_NIVEL) }
            )
        }

        composable(Routes.APRENDER_NIVEL) {
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            val progressViewModel = learnProgressViewModel(container)
            val steps by progressViewModel.steps.collectAsStateWithLifecycle()
            LevelScreen(
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenGoals = { navController.goToTab(Routes.METAS) },
                completedSteps = steps,
                onWatchVideo = {
                    progressViewModel.complete(CreditLevelContent.STEP_VIDEO)
                    navController.navigate(Routes.APRENDER_PILDORAS)
                }
            )
        }

        composable(Routes.APRENDER_PILDORAS) {
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            val progressViewModel = learnProgressViewModel(container)
            val steps by progressViewModel.steps.collectAsStateWithLifecycle()
            PillsScreen(
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenGoals = { navController.goToTab(Routes.METAS) },
                onLearn = { navController.navigate(Routes.APRENDER_PILDORA) },
                quizDone = CreditLevelContent.STEP_QUIZ in steps,
                onCorrectAnswer = { progressViewModel.complete(CreditLevelContent.STEP_QUIZ) }
            )
        }

        composable(Routes.APRENDER_PILDORA) {
            val details by container.profileDetailsRepository.details.collectAsStateWithLifecycle()
            val progressViewModel = learnProgressViewModel(container)
            PillDetailScreen(
                photoPath = details.photoPath,
                photoStamp = details.photoStamp,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PERFIL) },
                onOpenSavings = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenGoals = { navController.goToTab(Routes.METAS) },
                onViewed = { progressViewModel.complete(CreditLevelContent.STEP_PILL) }
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

/** Cambia de pestaña (Metas / Aprender) sin apilar pantallas: siempre queda Home debajo. */
private fun NavHostController.goToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME)
        launchSingleTop = true
    }
}

/** Avance del curso "Tarjeta de crédito sin miedo" (uno por pantalla; todos leen el mismo dato). */
@Composable
private fun learnProgressViewModel(container: AppContainer): LearnProgressViewModel =
    viewModel(
        factory = viewModelFactory {
            initializer {
                LearnProgressViewModel(container.learningProgressRepository, CreditLevelContent.PATH_ID)
            }
        }
    )
