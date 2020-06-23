package com.aliyun.alink.devicesdk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author glsite.com
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class MyDBOpenHelper extends SQLiteOpenHelper {

    // 第一个参数是上下文
    // 第二个参数是数据库名称
    // 第三个参数null表示使用默认的游标工厂
    // 第四个参数是数据库的版本号 数据库只能升级 不能降级，版本号只能增大不能变小
    public MyDBOpenHelper(Context context) {
        super(context, "glsite", null, 1);
    }

    /**
     * 当数据库第一次被创建的时候调用的方法，适合在这个方法当中把数据库的表结构定义出来
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("onCreate 数据库被创建了");
        db.execSQL("create table contactinfo (\n" +
                "id integer primary key autoincrement,\n" +
                "number char(20),\n" +
                "password varchar(20)\n" +
                ");");
    }

    /**
     * 当数据库更新的时候调用的方法
     * @param db
     *          数据库对象
     * @param oldVersion
     *          老版本
     * @param newVersion
     *          新版本
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("onUpgrade 数据库被更新了");
        db.execSQL("alter table contactinfo add addr varchar(20);");
    }
}
