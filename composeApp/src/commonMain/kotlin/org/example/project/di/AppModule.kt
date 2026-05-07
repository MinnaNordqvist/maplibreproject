package org.example.project.di

import org.example.project.BusViewModel
import org.example.project.data.SiriApi
import org.example.project.data.SiriApiImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


val koinConfig: KoinAppDeclaration = {
    modules(appModule)
}
val appModule = module {
    single<SiriApi> { SiriApiImpl() }
    viewModel { BusViewModel()}

}