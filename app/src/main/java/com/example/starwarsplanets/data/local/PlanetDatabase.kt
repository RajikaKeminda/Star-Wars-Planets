package com.example.starwarsplanets.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.starwarsplanets.data.models.Planet
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "planets")
data class PlanetEntity(
    @PrimaryKey val url: String,
    val name: String,
    val climate: String,
    val gravity: String,
    val imageUrl: String
)

fun Planet.toEntity(): PlanetEntity {
    return PlanetEntity(
        url = this.url,
        name = this.name,
        climate = this.climate,
        gravity = this.gravity,
        imageUrl = this.imageUrl
    )
}

fun PlanetEntity.toDomain(): Planet {
    return Planet(
        url = this.url,
        name = this.name,
        climate = this.climate,
        gravity = this.gravity,
        imageUrl = this.imageUrl
    )
}

@Dao
interface PlanetDao {
    @Query("SELECT * FROM planets")
    fun getAllPlanets(): Flow<List<PlanetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanets(planets: List<PlanetEntity>)

    @Query("DELETE FROM planets")
    suspend fun clearAllPlanets()
}

@Database(entities = [PlanetEntity::class], version = 1, exportSchema = false)
abstract class PlanetDatabase : RoomDatabase() {
    abstract fun planetDao(): PlanetDao

    companion object {
        @Volatile
        private var INSTANCE: PlanetDatabase? = null

        fun getDatabase(context: Context): PlanetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanetDatabase::class.java,
                    "planet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}