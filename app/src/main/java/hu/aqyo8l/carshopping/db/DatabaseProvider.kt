package hu.aqyo8l.carshopping.db

import android.content.Context

object DatabaseProvider {
    @Volatile
    private var dbHelper: AppDbHelper? = null

    fun init(context: Context) {
        if (dbHelper == null) {
            synchronized(this) {
                if (dbHelper == null) {
                    dbHelper = AppDbHelper(context.applicationContext)
                }
            }
        }
    }

    fun db(): AppDbHelper {
        return checkNotNull(dbHelper) { "DatabaseProvider is not initialized." }
    }
}
