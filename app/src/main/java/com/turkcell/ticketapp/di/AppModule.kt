package com.turkcell.ticketapp.di


import com.turkcell.ticketapp.ui.login.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // LoginViewModel — AuthRepository'yi Koin'den otomatik alır
    viewModel { LoginViewModel(authRepository = get()) }
}
