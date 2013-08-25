SwipeBackLayout
===

An Android library that help you to build app with swipe back gesture.


![image](https://github.com/Issacw0ng/SwipeBackLayout/blob/master/art/screenshot.png?raw=true)


Requirement
===
The latest android-support-v4.jar should be referenced by your project.

Usage
===
1. Add SwipeBackLayout as a dependency to your existing project.
2. To enable SwipeBackLayout, you can simply make your `Activity` extend `SwipeBackActivity`:
	* In `onCreate` method, `setContentView()` should be called as usual.
	* You will have access to the `getSwipeBackLayout()` method so you can customize the `SwipeBackLayout`. 
3. Make window translucent by adding `<item name="android:windowIsTranslucent">true</item>` to your theme.

Simple Example
===
```
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
```



Pull Requests
===
I will gladly accept pull requests for fixes and feature enhancements but please do them in the develop branch.

License
===

   Copyright 2013 Issac Wong

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
