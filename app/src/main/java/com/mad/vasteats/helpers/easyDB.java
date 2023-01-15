package com.mad.vasteats.helpers;
import android.content.Context;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class easyDB {
    public static EasyDB easyDB;
    public static void init(Context context) {
        easyDB = EasyDB.init(context, "customer_cart")
                .setTableName("tbl_cart_items")
                .addColumn(new Column("foodId", new String[]{"text", "not null"}))
                .addColumn(new Column("foodName", new String[]{"text", "not null"}))
                .addColumn(new Column("foodImage", new String[]{"text", "not null"}))
                .addColumn(new Column("cartFoodQty", new String[]{"text", "not null"}))
                .addColumn(new Column("cartFoodRate", new String[]{"text", "not null"}))
                .addColumn(new Column("cartFoodTotal", new String[]{"text", "not null"}))
                .doneTableColumn();
    }
}
