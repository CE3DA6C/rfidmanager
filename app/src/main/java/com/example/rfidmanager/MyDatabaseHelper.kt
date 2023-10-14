package com.example.rfidmanager

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class MyDatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val context: Context? = null
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_REF + " TEXT, " +
                COLUMN_UID + " TEXT, " +
                COLUMN_OWNER + " TEXT);"
        sqLiteDatabase.execSQL(query)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    fun addNew(ref: String?, uid: String?, ownerInfo: String?) {
        var sqLiteDatabase: SQLiteDatabase? = null
        try {
            sqLiteDatabase = this.writableDatabase
            val contentValues = ContentValues()
            if(ref!=null)
                contentValues.put(COLUMN_REF, ref)
            else
                contentValues.put(COLUMN_REF, "")
            if(uid!=null)
                contentValues.put(COLUMN_UID, uid)
            else
                contentValues.put(COLUMN_UID, "")
            if(ownerInfo!=null)
                contentValues.put(COLUMN_OWNER, ownerInfo)
            else
                contentValues.put(COLUMN_OWNER, "")

            val result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues)
            if (result == -1L) {
                //Toast.makeText(context, "Adding key failed", Toast.LENGTH_SHORT)
            } else {
                //Toast.makeText(context, "Succesfully added new key", Toast.LENGTH_SHORT)
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
        }
    }

    fun readAllData(): Cursor? {
        val query = "SELECT * FROM " + TABLE_NAME
        val sqLiteDatabase = this.readableDatabase
        var cursor: Cursor? = null
        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.rawQuery(query, null)
        }

        return cursor
    }

    val all: ArrayList<TagViewModel>
        get() {
            val alltags = ArrayList<TagViewModel>()
            val cursor = readAllData()
            if (cursor!!.count == 0) {
            } else {
                while (cursor.moveToNext()) {
                    var reference_id = cursor.getString(1)
                    var u_id = cursor.getString(2)
                    var owner_info = cursor.getString(3)

                    if(reference_id==null)
                        reference_id=""
                    if(u_id==null)
                        u_id=""
                    if(owner_info==null)
                        owner_info=""

                    alltags.add(TagViewModel(R.drawable.rfid_card,cursor.getInt(0),
                        reference_id, u_id, owner_info))
                }
            }
            cursor.close()
            return alltags
        }

    fun updateTag(id: Int, reference_id: String, u_id: String, owner_info: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_REF, reference_id)
            put(COLUMN_UID, u_id)
            put(COLUMN_OWNER, owner_info)
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close()
    }

    fun getTagById(id: Int): TagViewModel{
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $id"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        var reference_id = cursor.getString(1)
        var u_id = cursor.getString(2)
        var owner_info = cursor.getString(3)
        if(reference_id==null)
            reference_id=""
        if(u_id==null)
            u_id=""
        if(owner_info==null)
            owner_info=""
        cursor.close()
        db.close()

        return TagViewModel(R.drawable.rfid_card,id,
            reference_id, u_id, owner_info)
    }

    fun checkTag(reference_id: String): Boolean{
        var result: String
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_REF = $reference_id"

        try {
            val cursor = db.rawQuery(query, null)
            result = cursor.getString(1)
        }catch(e: Exception){

            db.close()
            return false
        }
        if(result==null) {
            db.close()
            return false
        }
        db.close()

        return true
    }

    fun deleteTag(id: Int) :Int{
        val db = writableDatabase

        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        val result = db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "mydb"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "mytable"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_REF = "reference"
        private const val COLUMN_UID = "uid"
        private const val COLUMN_OWNER = "owner"
    }
}