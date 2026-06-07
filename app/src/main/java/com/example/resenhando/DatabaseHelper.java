package com.example.resenhando;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "resenhando.db";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "id";

    // Tabela Resenhas
    public static final String TABLE_ONE_NAME = "resenhas";
    public static final String COLUMN_TITLE = "titulo";
    public static final String COLUMN_QTY_STARS = "qnt_estrelas";
    public static final String COLUMN_DESCRIPTION = "descricao";
    public static final String COLUMN_IMAGE_URL = "caminho_imagem";
    public static final String COLUMN_USER_ID = "usuario_id";

    // Tabela Usuários

    public static final String TABLE_TWO_NAME = "usuarios";
    public static final String COLUMN_NAME = "nome";
    public static final String COLUMN_BIRTHDATE = "data_nascimento";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "senha";

    // SQL Tabela Resenhas
    private static final String CREATE_TABLE_RESENHAS =
            "CREATE TABLE " + TABLE_ONE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_QTY_STARS + " REAL ," +
                    COLUMN_DESCRIPTION + " TEXT ," +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_USER_ID + " INTEGER);";

    // SQL Tabela Usuários
    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_TWO_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_BIRTHDATE + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT);";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RESENHAS);
        db.execSQL(CREATE_TABLE_USUARIOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWO_NAME);
        onCreate(db);
    }
}
