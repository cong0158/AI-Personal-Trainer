package im.zego.video.talk.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import im.zego.video.talk.R;

public class MyExpandableListView extends BaseExpandableListAdapter {
    //一级列表数据源
    public static String[] groups = {"网球", "太极拳", "物理康复"};
    //二级列表数据源
    public static String[][] childs={{"发球","传统正手","现代正手"},{"起手式","左右野马分鬃","白鹤亮翅"},{"伸肘肌群训练","靠墙静蹲","肘关节稳定性"}};

    private Activity mContext;

    public MyExpandableListView(Activity context){
        super();
        this.mContext = context;
    }

    /*一级列表个数*/
    @Override
    public int getGroupCount() {
        return groups.length;
    }

    /*每个二级列表的个数*/
    @Override
    public int getChildrenCount(int groupPosition) {
        return childs[groupPosition].length;
    }

    /*一级列表中单个item*/
    @Override
    public Object getGroup(int groupPosition) {
        return groups[groupPosition];
    }

    /*二级列表中单个item*/
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /*每个item的id是否固定，一般为true*/
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /*#TODO 填充一级列表
     * isExpanded 是否已经展开
     * */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {

            convertView = this.mContext.getLayoutInflater().inflate(R.layout.course_menu_1,null);
        }
        TextView tv_group = (TextView) convertView.findViewById(R.id.tv_group);
        ImageView iv_group = (ImageView) convertView.findViewById(R.id.iv_group);
        tv_group.setText(groups[groupPosition]);
        //控制是否展开图标
        if (isExpanded) {
            iv_group.setImageResource(R.drawable.down);
        } else {
            iv_group.setImageResource(R.drawable.up);
        }
        return convertView;
    }

    /*#TODO 填充二级列表*/
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mContext.getLayoutInflater().inflate(R.layout.course_menu_2,null);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.iv_child);
        TextView tv = (TextView) convertView.findViewById(R.id.tv_child);
        tv.setText(childs[groupPosition][childPosition]);
        return convertView;
    }

    /*二级列表中每个能否被选中，如果有点击事件一定要设为true*/
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
