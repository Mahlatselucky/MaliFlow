package com.example.maliflow.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.maliflow.data.dao.*
import com.example.maliflow.data.model.*

@Database
    (
    entities = [User::class, Category::class, ExpenseEntry::class, Goal::class, FlowPoints::class],
    version = 2,
    exportSchema = false
    )
abstract class AppDatabase : RoomDatabase()
{

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao
    abstract fun goalDao(): GoalDao
    abstract fun flowPointsDao(): FlowPointsDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2 - adds new columns to categories and creates flow_points table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to categories table
                database.execSQL("ALTER TABLE categories ADD COLUMN emoji TEXT NOT NULL DEFAULT '💰'")
                database.execSQL("ALTER TABLE categories ADD COLUMN budgetLimit REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE categories ADD COLUMN rolloverEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE categories ADD COLUMN rolloverBalance REAL NOT NULL DEFAULT 0.0")

                // Create the flow_points table for gamification
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS flow_points (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        points INTEGER NOT NULL DEFAULT 0,
                        streak INTEGER NOT NULL DEFAULT 0,
                        lastLogDate TEXT NOT NULL DEFAULT '',
                        totalExpensesLogged INTEGER NOT NULL DEFAULT 0,
                        goalsMetCount INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maliflow_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}