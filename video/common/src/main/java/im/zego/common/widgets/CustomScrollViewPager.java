package im.zego.common.widgets;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zego on 2019/4/26.
 */

public class CustomScrollViewPager extends ViewPager {

    // false 禁止ViewPager左右滑动。
    private boolean scrollable = false;

    public CustomScrollViewPager(Context context) {
       this(context, null);
    }

    public CustomScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return scrollable;
    }

}
