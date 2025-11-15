package com.eurogame.scorecard.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eurogame.scorecard.data.local.GameDatabase
import com.eurogame.scorecard.data.repository.GameRepositoryImpl
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.domain.repository.GameRepository
import com.eurogame.scorecard.presentation.gamesetup.GameSetupViewModel
import com.eurogame.scorecard.presentation.scorecard.ScorecardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns to games table
        database.execSQL("ALTER TABLE games ADD COLUMN subtitle TEXT")
        database.execSQL("ALTER TABLE games ADD COLUMN designer TEXT")
        database.execSQL("ALTER TABLE games ADD COLUMN studio TEXT")
        database.execSQL("ALTER TABLE games ADD COLUMN minPlayers INTEGER")
        database.execSQL("ALTER TABLE games ADD COLUMN maxPlayers INTEGER")
        database.execSQL("ALTER TABLE games ADD COLUMN backgroundImageUrl TEXT")
        database.execSQL("ALTER TABLE games ADD COLUMN publishYear INTEGER")
        database.execSQL("ALTER TABLE games ADD COLUMN templateId TEXT")

        // Add new columns to score_categories table
        database.execSQL("ALTER TABLE score_categories ADD COLUMN description TEXT")
        database.execSQL("ALTER TABLE score_categories ADD COLUMN iconUrl TEXT")
        database.execSQL("ALTER TABLE score_categories ADD COLUMN backgroundImageUrl TEXT")
        database.execSQL("ALTER TABLE score_categories ADD COLUMN scoringRuleType TEXT")
        database.execSQL("ALTER TABLE score_categories ADD COLUMN scoreIndex INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add optional category support to score_categories table
        database.execSQL("ALTER TABLE score_categories ADD COLUMN isOptional INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE score_categories ADD COLUMN isEnabled INTEGER NOT NULL DEFAULT 1")
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GameDatabase::class.java,
            "game_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<GameDatabase>().gameDao() }
    single { get<GameDatabase>().playerDao() }
    single { get<GameDatabase>().scoreCategoryDao() }
    single { get<GameDatabase>().playerScoreDao() }
}

val storageModule = module {
    single { TemplateStorageManager(androidContext()) }
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
    viewModel { GameSetupViewModel(get(), get()) }
    viewModel { ScorecardViewModel(get()) }
    viewModel { com.eurogame.scorecard.presentation.templatebuilder.TemplateBuilderViewModel(get()) }
    viewModel { com.eurogame.scorecard.presentation.templatebrowser.TemplateBrowserViewModel(get()) }
}

val appModules = listOf(
    databaseModule,
    storageModule,
    repositoryModule,
    viewModelModule
)
