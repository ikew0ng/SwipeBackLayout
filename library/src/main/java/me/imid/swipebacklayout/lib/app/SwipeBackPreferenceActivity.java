
package me.imid.swipebacklayout.lib.app;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class SwipeBackPreferenceActivity extends PreferenceActivity {
    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getWindow().getDecorView().setBackgroundDrawable(null);
        mSwipeBackLayout = new SwipeBackLayout(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackLayout.attachToActivity(this);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mSwipeBackLayout != null) {
            v = mSwipeBackLayout.findViewById(id);
        }
        return v;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    public void setSwipeBackEnable(boolean enable) {
        mSwipeBackLayout.setEnableGesture(enable);
    }

    /**
     * Scroll out contentView and finish the activity
     */
    public void scrollToFinishActivity() {
        mSwipeBackLayout.scrollToFinishActivity();
    }
}
