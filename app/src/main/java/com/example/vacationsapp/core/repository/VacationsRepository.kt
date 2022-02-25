package com.example.vacationsapp.core.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.vacationsapp.core.entity.DesiredVacation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Vacations repository for the application
class VacationsRepository private constructor(context: Context) :
    SQLiteOpenHelper(context, "vacations_app_database.sqlite", null, 1) {

    // Database creation queries
    override fun onCreate(db: SQLiteDatabase?) {
        val database = db ?: return
        database.execSQL(
            "create table if not exists $vacationsTableName\n" +
                    "(\n" +
                    "   $vacationsId          integer primary key autoincrement,\n" +
                    "   $vacationsName        text    not null    unique,\n" +
                    "   $vacationsHotelName   text    not null,\n" +
                    "   $vacationsLocation    text    not null,\n" +
                    "   $vacationsCost        integer not null,\n" +
                    "   $vacationsDescription text    not null,\n" +
                    "   $vacationsImagePath   text    not null \n" +
                    ")"
        )
        println("Created database")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    // Insert a new vacation in the database
    suspend fun vacationInsert(vacation: DesiredVacation): Long {
        lock.withLock {
            val writeableDb = try {
                writableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return -1
            }

            val input = ContentValues()
            input.put(vacationsName, vacation.name)
            input.put(vacationsHotelName, vacation.hotelName)
            input.put(vacationsLocation, vacation.location)
            input.put(vacationsCost, vacation.cost)
            input.put(vacationsDescription, vacation.description)
            input.put(vacationsImagePath, vacation.imagePath)

            val inserted = writeableDb.insert(
                vacationsTableName,
                null,
                input
            )
            writeableDb.close()
            return inserted
        }
    }

    // Deletes a vacation from the database
    suspend fun vacationDelete(id: Int): Int {
        lock.withLock {
            val writeableDb = try {
                writableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return 0
            }
            val whereClause = "$vacationsId like ?"
            val params = arrayOf(id.toString())
            val deleted = writeableDb.delete(vacationsTableName, whereClause, params)
            writeableDb.close()
            return deleted
        }
    }

    // Update a vacation from the database
    suspend fun vacationUpdate(vacation: DesiredVacation): Int {
        lock.withLock {
            val writeableDb = try {
                writableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return 0
            }

            val input = ContentValues()
            input.put(vacationsName, vacation.name)
            input.put(vacationsHotelName, vacation.hotelName)
            input.put(vacationsLocation, vacation.location)
            input.put(vacationsCost, vacation.cost)
            input.put(vacationsDescription, vacation.description)
            input.put(vacationsImagePath, vacation.imagePath)

            val whereClause = "$vacationsId like ?"
            val params = arrayOf(vacation.id.toString())
            val updated = writeableDb.update(vacationsTableName, input, whereClause, params)

            writeableDb.close()
            return updated
        }
    }

    // Fetches a single vacation from the database
    suspend fun vacationFetch(id: Int): DesiredVacation? {
        lock.withLock {
            val readableDb = try {
                readableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return null
            }
            val params = arrayOf(id.toString())
            val cursor = readableDb.rawQuery(
                "Select * from $vacationsTableName where $vacationsId like ? LIMIT 1",
                params
            )

            var output: DesiredVacation? = null

            if (cursor.moveToFirst()) {

                val vacationId = cursor.getInt(0)
                val name = cursor.getString(1)
                val hotelName = cursor.getString(2)
                val location = cursor.getString(3)
                val cost = cursor.getInt(4)
                val description = cursor.getString(5)
                val imageName = cursor.getString(6)
                output = DesiredVacation(
                    vacationId,
                    name,
                    hotelName,
                    location,
                    cost,
                    description,
                    imageName
                )
            }

            cursor.close()
            readableDb.close()

            return output
        }
    }

    // Fetches all vacations from the database
    suspend fun vacationsFetch(): List<DesiredVacation> {
        lock.withLock {
            val readableDb = try {
                readableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return listOf()
            }

            val cursor =
                readableDb.rawQuery("Select * from $vacationsTableName", null)

            val output = ArrayList<DesiredVacation>(cursor.count)

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(0)
                    val name = cursor.getString(1)
                    val hotelName = cursor.getString(2)
                    val location = cursor.getString(3)
                    val cost = cursor.getInt(4)
                    val description = cursor.getString(5)
                    val imageName = cursor.getString(6)
                    val vacation =
                        DesiredVacation(id, name, hotelName, location, cost, description, imageName)
                    output.add(vacation)
                } while (cursor.moveToNext())
            }

            cursor.close()
            readableDb.close()
            return output
        }
    }

    // Static variables
    companion object {

        // Vacations table variables
        private const val vacationsTableName = "Vacations"
        private const val vacationsId = "Id"
        private const val vacationsName = "Name"
        private const val vacationsHotelName = "Hotel_name"
        private const val vacationsLocation = "Location"
        private const val vacationsCost = "Cost"
        private const val vacationsDescription = "Description"
        private const val vacationsImagePath = "Image_path"

        // Static mutex ensuring the thread-safe access of the repository at any time
        private val lock = Mutex()


        // Making the repository a singleton
        lateinit var instance: VacationsRepository

        fun initInstance(context: Context) {
            instance = VacationsRepository(context)
        }
    }
}