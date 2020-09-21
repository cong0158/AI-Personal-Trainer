package im.zego.video.talk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;

import im.zego.video.talk.R;
import im.zego.video.talk.adapter.MyExpandableListView;

public class CourseMenuUI extends Activity {
    public static int RESULT = 1;
    public static int groupId;
    public static int childId;

    private ExpandableListView expandableListView;

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, CourseMenuUI.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_menu);
        initView();
    }

    private void initView() {
        expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
        //#TODO 去掉自带箭头，在一级列表中动态添加
        expandableListView.setGroupIndicator(null);
        expandableListView.setAdapter(new MyExpandableListView(CourseMenuUI.this));
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView childAt = (TextView)((LinearLayout) v).getChildAt(1);//获得点击列表中TextView的值，需要强转一下，否则找不到getChildAt方法
                Toast.makeText(CourseMenuUI.this, "点击了 "+childAt.getText()+" 列表", Toast.LENGTH_SHORT).show();
                groupId = groupPosition;
                childId = childPosition;
                setResult(RESULT);
                finish();
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
