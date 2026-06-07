package com.turkcell.ticketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.ticketapp.screen.AdminHomeScreen
import com.turkcell.ticketapp.screen.CheckinScreen
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyTicketsScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.StaffHomeScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject(),
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val currentRole by authRepository.currentRole.collectAsStateWithLifecycle(initialValue = null)

    when (isLoggedIn) {
        null -> SplashScreen()
        false -> UnAuthedNavHost(navController)
        true -> {
            // Eski oturumlar DataStore'da role anahtarı taşımaz → USER default'u
            val role = currentRole ?: UserRole.USER
            when (role) {
                UserRole.ADMIN -> AdminNavHost(navController)
                UserRole.STAFF -> StaffNavHost(navController)
                UserRole.USER -> AuthedNavHost(navController)
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onEventClick = { eventId -> navController.navigate(EventDetail(eventId = eventId)) },
                onTicketClick = { ticketId -> navController.navigate(TicketDetail(ticketId = ticketId)) },
                onNavigateToMyTickets = { navController.navigate(MyTickets) },
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMyTickets = {
                    navController.navigate(MyTickets) {
                        popUpTo(Home) { inclusive = false }
                    }
                },
            )
        }
        composable<MyTickets> {
            MyTicketsScreen(
                onBack = { navController.popBackStack() },
                onTicketClick = { ticketId -> navController.navigate(TicketDetail(ticketId = ticketId)) },
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun AdminNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AdminHome) {
        composable<AdminHome> {
            AdminHomeScreen()
        }
        // Admin, User ekranlarına da erişebilir
        composable<Home> {
            HomeScreen(
                onEventClick = { eventId -> navController.navigate(EventDetail(eventId = eventId)) },
                onTicketClick = { ticketId -> navController.navigate(TicketDetail(ticketId = ticketId)) },
                onNavigateToMyTickets = { navController.navigate(MyTickets) },
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMyTickets = {
                    navController.navigate(MyTickets) {
                        popUpTo(AdminHome) { inclusive = false }
                    }
                },
            )
        }
        composable<MyTickets> {
            MyTicketsScreen(
                onBack = { navController.popBackStack() },
                onTicketClick = { ticketId -> navController.navigate(TicketDetail(ticketId = ticketId)) },
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<StaffHome> {
            StaffHomeScreen(
                onNavigateToCheckin = { navController.navigate(Checkin) },
            )
        }
        composable<Checkin> {
            CheckinScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun StaffNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = StaffHome) {
        composable<StaffHome> {
            StaffHomeScreen(
                onNavigateToCheckin = { navController.navigate(Checkin) },
            )
        }
        composable<Checkin> {
            CheckinScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) },
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
    }
}