
package me.imid.swipebacklayout.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by Issac on 8/11/13.
 */
public class TestActivity extends SwipeBackActivity {
    private View mContainer;

    private int[] mBgColors = new int[] {
            Color.BLACK, Color.BLUE, Color.GRAY, Color.RED, Color.YELLOW
    };

    private static int mBgIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestActivity.this, TestActivity.class));
            }
        });
        mContainer = findViewById(R.id.container);
        mContainer.setBackgroundColor(mBgColors[mBgIndex]);
        mBgIndex++;
        if (mBgIndex >= mBgColors.length) {
            mBgIndex = 0;
        }
    }

}
