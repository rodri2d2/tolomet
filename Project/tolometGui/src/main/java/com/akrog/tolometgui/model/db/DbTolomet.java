package com.akrog.tolometgui.model.db;

import android.database.sqlite.SQLiteDatabase;

import com.akrog.tolomet.utils.Utils;
import com.akrog.tolomet.providers.SpotProviderType;
import com.akrog.tolomet.providers.WindProviderType;
import com.akrog.tolometgui.Tolomet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = DbTolomet.VERSION, entities = {StationEntity.class, SpotEntity.class})
public abstract class DbTolomet extends RoomDatabase {
    public static final int VERSION = 20;
    public static final String NAME = "Tolomet.db";
    public static final String ASSET = "databases/Tolomet.db";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DbTolomet instance;

    public abstract StationDao stationDao();

    public abstract SpotDao spotDao();

    public abstract StatsDao statsDao();

    synchronized public static DbTolomet getInstance() {
        if( instance == null ) {
            //updVersion();
            if( getVersion() != VERSION )
                if( !overrideDb() )
                    return null;
            instance = Room
                .databaseBuilder(Tolomet.getAppContext(), DbTolomet.class, NAME)
                //.createFromAsset(ASSET)
                .allowMainThreadQueries()
                .build();
        }
        return instance;
    }

    private static void updVersion() {
        File file = Tolomet.getAppContext().getDatabasePath(NAME);
        try(SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE)) {
            db.setVersion(VERSION);
            db.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getVersion() {
        File file = Tolomet.getAppContext().getDatabasePath(NAME);
        try(SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
            return db.getVersion();
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean overrideDb() {
        File db = Tolomet.getAppContext().getDatabasePath(NAME);
        db.getParentFile().mkdirs();
        try(
            InputStream is = Tolomet.getAppContext().getAssets().open(DbTolomet.ASSET);
            OutputStream os = new FileOutputStream(db);
        ) {
            Utils.copy(is, os);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static class ProviderInfo {
        private int count;
        private Date date;
        private String provider;
        private WindProviderType windProviderType;
        private SpotProviderType spotProviderType;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public WindProviderType getWindProviderType() {
            return windProviderType;
        }

        public void setWindProviderType(WindProviderType windProviderType) {
            this.windProviderType = windProviderType;
        }

        public SpotProviderType getSpotProviderType() {
            return spotProviderType;
        }

        public void setSpotProviderType(SpotProviderType spotProviderType) {
            this.spotProviderType = spotProviderType;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }
}
