package com.example.personallevelingsystem.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.personallevelingsystem.model.*

@Database(
    entities = [
        Water::class,
        Sleep::class,
        FlexibilityTraining::class,
        EnduranceTraining::class,
        User::class,
        Meal::class,
        Program::class,
        Session::class,
        Exercise::class,
        TrainingSession::class,
        TrainingSet::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun programDao(): ProgramDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun mealDao(): MealDao
    abstract fun userDao(): UserDao
    abstract fun flexibilityTrainingDao(): FlexibilityTrainingDao
    abstract fun enduranceTrainingDao(): EnduranceTrainingDao
    abstract fun SleepTimeDao(): SleepTimeDao
    abstract fun WaterDao(): WaterDao
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingSetDao(): TrainingSetDao






    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_levelingsystem.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
