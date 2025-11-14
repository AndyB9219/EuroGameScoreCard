package com.eurogame.scorecard.di

import androidx.room.Room
import com.eurogame.scorecard.data.local.GameDatabase
import com.eurogame.scorecard.data.repository.GameRepositoryImpl
import com.eurogame.scorecard.domain.repository.GameRepository
import com.eurogame.scorecard.presentation.gamesetup.GameSetupViewModel
import com.eurogame.scorecard.presentation.scorecard.ScorecardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GameDatabase::class.java,
            "game_database"
        ).build()
    }

    single { get<GameDatabase>().gameDao() }
    single { get<GameDatabase>().playerDao() }
    single { get<GameDatabase>().scoreCategoryDao() }
    single { get<GameDatabase>().playerScoreDao() }
}

val repositoryModule = module {
    single<GameRepository> {
        GameRepositoryImpl(
            gameDao = get(),
            playerDao = get(),
            scoreCategoryDao = get(),
            playerScoreDao = get()
        )
    }
}

val viewModelModule = module {
    viewModel { GameSetupViewModel(get()) }
    viewModel { ScorecardViewModel(get()) }
}

val appModules = listOf(
    databaseModule,
    repositoryModule,
    viewModelModule
)
