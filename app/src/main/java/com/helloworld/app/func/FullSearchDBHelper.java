package com.helloworld.app.func;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * sqlite全文检索
 */
public class FullSearchDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fts_note.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME_FTS_4 = "fts4_note";
    private static final String COL_CONTENT = "content";

    private static FullSearchDBHelper noteDBHelper;

    public static FullSearchDBHelper getInstance(Context context) {
        if (noteDBHelper == null) {
            noteDBHelper = new FullSearchDBHelper(context.getApplicationContext());
        }
        return noteDBHelper;
    }

    private FullSearchDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE VIRTUAL TABLE " + TABLE_NAME_FTS_4 + " USING fts4 ( " + COL_CONTENT + " )");
            Log.d("FullSearch","onCreate...");
        }catch (SQLException e){
            Log.d("FullSearch","Exception=" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //删除数据
    public void deleteData() {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME_FTS_4, null, null);
        database.close();
    }

    //删除数据
    public void deleteData(NoteEntity noteEntity){
        if(noteEntity == null){
            return;
        }

        deleteData(noteEntity.id);
    }

    //删除数据
    public void deleteData(long id){
        try {
            SQLiteDatabase database = getWritableDatabase();
            String sql = String.format("delete from %s where rowid=%d",id);
            database.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //更新数据
    public void updateData(NoteEntity noteEntity){
        if(noteEntity == null){
            return;
        }

        try {
            SQLiteDatabase database = getWritableDatabase();
            String sql = String.format("update %s set %s=%s where rowid=%d",TABLE_NAME_FTS_4,COL_CONTENT,noteEntity.content,noteEntity.id);
            database.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //插入数据
    public void insertData(NoteEntity noteEntity){
        if(noteEntity == null){
            return;
        }

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid",noteEntity.id);
        values.put(COL_CONTENT, noteEntity.content);
        database.insert(TABLE_NAME_FTS_4, null, values);
    }

    public void insertData(List<NoteEntity> noteList) {
        if (noteList == null || noteList.isEmpty()) {
            return;
        }

        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            for (NoteEntity entity : noteList) {
                ContentValues values = new ContentValues();
                values.put("rowid",entity.id);
                values.put(COL_CONTENT, entity.content);
                long r = database.insert(TABLE_NAME_FTS_4, null, values);

                Log.d("FullSearch","r=" + r);
            }
            database.setTransactionSuccessful(); //TRANSACTION SUCCESSFUL
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction(); // END Transaction
        }
    }

    //搜索
    @SuppressLint("Range")
    public List<NoteEntity> search(String word) {
        List<NoteEntity> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME_FTS_4, new String[]{"rowid", COL_CONTENT},  "content MATCH ?", new String[]{word + "*"}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                NoteEntity entity = new NoteEntity();
                entity.id = cursor.getLong(cursor.getColumnIndex("rowid"));
                entity.content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));
                list.add(entity);
                cursor.moveToNext();
            }
        }
        return list;
    }
}
