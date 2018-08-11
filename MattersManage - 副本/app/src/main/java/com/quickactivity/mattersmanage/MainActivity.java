package com.quickactivity.mattersmanage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements
        OnItemClickListener, OnItemLongClickListener {

    private Context mContext;
    private SimpleAdapter simp_adapter;
    private TextView tv_content;
    private NotesDB DB;
    private GridView gv;
    private SQLiteDatabase dbread;
    private List<Map<String, Object>> dataList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){   //标题栏按钮
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   //监听按钮
        switch (item.getItemId()) {
            case R.id.delete:
                //dbread.execSQL("DELETE FROM CUSTOMERS");
                //RefreshNotesList();
                //break;
                Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("删除全部日志");
                builder.setMessage("确认删除吗？！");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            //删除数据
                        dbread.execSQL("delete from table");
                        RefreshNotesList();
                        }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.add://监听菜单按钮
                SecondActivity.ENTER_STATE = 0;
                Intent intent = new Intent(mContext, SecondActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("info", "");
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   //标题栏设置按钮
        setContentView(R.layout.activity_main);

        tv_content = (TextView) findViewById(R.id.tv_content);
        gv= (GridView) findViewById(R.id.grid_view);
        dataList = new ArrayList<Map<String, Object>>();

        mContext = this;

        DB = new NotesDB(this);
        dbread = DB.getReadableDatabase();
        RefreshNotesList();
        gv.setOnItemClickListener(this);
        gv.setOnItemLongClickListener(this);
    }

    public void RefreshNotesList() {
            int size = dataList.size();
            if (size > 0) {
                dataList.removeAll(dataList);
                simp_adapter.notifyDataSetChanged();
                gv.setAdapter(simp_adapter);
            }

        simp_adapter = new SimpleAdapter(this, getData(), R.layout.item,
                new String[] { "tv_content", "tv_date" }, new int[] {
                R.id.tv_content, R.id.tv_date });
        gv.setAdapter(simp_adapter);
    }

    private List<Map<String, Object>> getData() {

        Cursor cursor = dbread.query("note", null, "content!=\"\"", null, null,
                null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tv_content", name);
            map.put("tv_date", date);
            dataList.add(map);
        }
        cursor.close();
        return dataList;

    }

    // 点击gridview中某一项的监听事件
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        SecondActivity.ENTER_STATE = 1;
        String content = gv.getItemAtPosition(arg2) + "";
        String content1 = content.substring(content.indexOf("=") + 1,
                content.indexOf(","));
        Log.d("CONTENT", content1);
        Cursor c = dbread.query("note", null,
                "content=" + "'" + content1 + "'", null, null, null, null);
        while (c.moveToNext()) {
            String No = c.getString(c.getColumnIndex("_id"));
            Log.d("TEXT", No);
            Intent myIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("info", content1);
            SecondActivity.id = Integer.parseInt(No);
            myIntent.putExtras(bundle);
            myIntent.setClass(MainActivity.this, SecondActivity.class);
            startActivityForResult(myIntent, 1);
        }
    }



    // 点击gridview中某一项长时间的点击事件
    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        final int n=arg2;
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除该日志");
        builder.setMessage("确认删除吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = gv.getItemAtPosition(n) + "";
                String content1 = content.substring(content.indexOf("=") + 1,
                        content.indexOf(","));
                Cursor c = dbread.query("note", null, "content=" + "'"
                        + content1 + "'", null, null, null, null);
                while (c.moveToNext()) {
                    String id = c.getString(c.getColumnIndex("_id"));
                    String sql_del = "update note set content='' where _id="
                            + id;
                    dbread.execSQL(sql_del);
                    RefreshNotesList();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create();
        builder.show();
        return true;
    }

    @Override
    // 接受上一个页面返回的数据，并刷新页面
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            RefreshNotesList();
        }
    }
}
