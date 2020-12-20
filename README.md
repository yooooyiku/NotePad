# 基于NotePad应用做功能扩展 

学号：116052018115

姓名：吴伟为

##### 项目结构：

[![rdw4m9.png](https://s3.ax1x.com/2020/12/21/rdw4m9.png)](https://imgchr.com/i/rdw4m9)



##### 基本要求：NoteList中显示条目增加时间戳显示

布局文件用线性布局，并添加一个TextView

```
<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="match_parent"
    android:background="@drawable/bk1"
tools:context=".NotesList"
android:orientation="horizontal">
    <TextView
        android:id="@android:id/text1"
        android:layout_width="298dp"
        android:layout_height="83dp"
        android:gravity="center|left"
        android:layout_marginLeft="5dp"
        android:singleLine="true"
        android:textAppearance="?android:attr/textAppearanceLarge"></TextView>
    <TextView
        android:gravity="center"
        android:layout_width="match_parent"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:layout_margin="3dp"
        android:textSize="12sp"
        android:id="@android:id/text2"
        >
    </TextView>
</LinearLayout>
```

NotesList.java中，将NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE添加到PROJECTION中

```
private static final String[] PROJECTION = new String[] {
        NotePad.Notes._ID, // 0
        NotePad.Notes.COLUMN_NAME_TITLE, // 1
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//2
};
```

在dataColumns，viewIDs中补充时间戳的部分

```
String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;
```

```
int[] viewIDs = { android.R.id.text1 ,android.R.id.text2};
```

还需要修改时间的格式，不然显示出来的是一长串的数字

在NotePadProvider的Insert方法中，将时间插入更改为以下代码

```
//获取当前时间
Long now = Long.valueOf(System.currentTimeMillis());
Date date=new Date(now);
//以年-月-日 时-分的格式显示时间，使用SimpleDateFormat
SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
String dateFormat = simpleDateFormat.format(date);
// If the values map doesn't contain the creation date, sets the value to the current time.
if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, dateFormat);
}

// If the values map doesn't contain the modification date, sets the value to the current
// time.
if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateFormat);
}
```

在NoteEditor中也要做相应更改，将笔记更新时间也格式化

updateNote方法：

```
ContentValues values = new ContentValues();
//获取当前时间
long now = System.currentTimeMillis();
Date date = new Date(now);
SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
String dateFormat = simpleDateFormat.format(date);
values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateFormat);
```

效果图：

[![rdwtW8.png](https://s3.ax1x.com/2020/12/21/rdwtW8.png)](https://imgchr.com/i/rdwtW8)

##### 添加笔记查询功能（根据标题查询） 

线在主页面添加一个搜索按钮

在list_options_menu.xml中 添加以下代码

```
<item android:id="@+id/menu_search"
      android:icon="@drawable/search"//使用search.png图标
    android:title="Search"
    android:alphabeticShortcut='a'
    android:showAsAction="always" />
```

在NotesList的onOptionsItemSelected中添加选择事件，进行跳转

```
case R.id.menu_search:
      startActivity(new Intent(this,NoteSearch.class));
      return true;
```

新建NoteSearch，布局文件如下，包含一个SearchView和一个ListView

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/bk2">
    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false">
     </SearchView>
   <ListView
    android:id="@+id/list_view"
    android:layout_width="match_parent"
   android:layout_height="wrap_content"
    >
   </ListView>

</LinearLayout>
```

在AndroidManifest.xml中添加：

```
<activity
    android:name=".NoteSearch"
    android:label="NotesSearch"
    >

    <intent-filter>
        <action android:name="android.intent.action.NoteSearch" />
        <action android:name="android.intent.action.SEARCH" />
        <action android:name="android.intent.action.SEARCH_LONG_PRESS" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="vnd.android.cursor.dir/vnd.google.note" />
    </intent-filter>
</activity
```

首先，在NoteSearch中定义一个数组存储数据，定义ListView和SQLite

```
private static final String[] PROJECTION = new String[]{
        NotePad.Notes._ID, // 0
        NotePad.Notes.COLUMN_NAME_TITLE, // 1
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//2
};
ListView listView;
SQLiteDatabase sqLiteDatabase;
```

onCreate代码

```
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //引入布局文件
    setContentView(R.layout.activity_notes_search);
    SearchView searchView=findViewById(R.id.search_view);
    //获取intent
    Intent intent=getIntent();
    if (intent.getData()==null){
        intent.setData(NotePad.Notes.CONTENT_URI);
    }
    listView=findViewById(R.id.list_view);
    sqLiteDatabase=new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
    //显示搜索按钮
    searchView.setSubmitButtonEnabled(true);
    searchView.setOnQueryTextListener(NoteSearch.this);
}
```

实现查询功能

```
public boolean onQueryTextChange(String string){
    String selection1 = NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?";
    String[] selection2 = {"%"+string+"%","%"+string+"%"};
    //读取数据
    Cursor cursor=sqLiteDatabase.query(
            NotePad.Notes.TABLE_NAME,
            PROJECTION,
            selection1,
            selection2,
            null,
            null,
            NotePad.Notes.DEFAULT_SORT_ORDER
    );
    // 参照NotesList中的显示方法
    String[] dataColumns = {
            NotePad.Notes.COLUMN_NAME_TITLE,//标题
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE //时间
    } ;
    int[] viewIDs = {
            android.R.id.text1,
            android.R.id.text2
    };
    //将数据装填入adapter
    SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
            this,                            
            R.layout.noteslist_item,         
            cursor,                           // The cursor to get items from
            dataColumns,
            viewIDs
    );
    listView.setAdapter(adapter);
    return true;
}
```

效果图：

[![rddAC8.png](https://s3.ax1x.com/2020/12/21/rddAC8.png)](https://imgchr.com/i/rddAC8)

[![rddF4f.png](https://s3.ax1x.com/2020/12/21/rddF4f.png)](https://imgchr.com/i/rddF4f)

##### 拓展功能：

##### UI美化

插入背景图片，将适用于ListView的小图bk1.png及适用于其他Activity的大图bk2.png放入drawable



在noteslist_item.xml的LinearLayout标签中添加android:background="@drawable/bk1"

在note_editor.xml、activity_notes_search.xml中添加android:background-"@drawable/bk2"

效果图：

[![rdwtW8.png](https://s3.ax1x.com/2020/12/21/rdwtW8.png)](https://imgchr.com/i/rdwtW8)

[![rddE8S.png](https://s3.ax1x.com/2020/12/21/rddE8S.png)](https://imgchr.com/i/rddE8S)

[![rddAC8.png](https://s3.ax1x.com/2020/12/21/rddAC8.png)](https://imgchr.com/i/rddAC8)