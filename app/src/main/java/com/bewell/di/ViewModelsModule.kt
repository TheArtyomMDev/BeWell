package com.bewell.di

import com.bewell.viewmodels.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var viewModelsModule = module {
    viewModel {
        StartMeasureViewModel(get(), get())
    }
    viewModel {
        MeasureResultViewModel(get())
    }
    viewModel {
        AskToGivePermissionsViewModel()
    }
    viewModel {
        HRVMeasureViewModel(get(), get())
    }
    viewModel {
        LoginViewModel(get(), get())
    }
    viewModel {
        SignupViewModel(get(), get())
    }
}