/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quanliren.quan_two.pull;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.quanliren.quan_two.pull.lib.EnvironmentDelegate;
import com.quanliren.quan_two.pull.lib.HeaderTransformer;
import com.quanliren.quan_two.pull.lib.Options;
import com.quanliren.quan_two.pull.lib.PullToRefreshAttacher;

class AbcPullToRefreshAttacher extends PullToRefreshAttacher {

    private FrameLayout mHeaderViewWrapper;

    protected AbcPullToRefreshAttacher(Activity activity, Options options) {
        super(activity, options);
    }

    @Override
    protected void addHeaderViewToActivity(View headerView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            super.addHeaderViewToActivity(headerView);
        } else {
            // On older devices we need to wrap the HeaderView in a FrameLayout otherwise
            // visibility changes do not take effect
            mHeaderViewWrapper = new FrameLayout(getAttachedActivity());
            mHeaderViewWrapper.addView(headerView);
            super.addHeaderViewToActivity(mHeaderViewWrapper);
        }
    }

    @Override
    protected void updateHeaderViewPosition(View headerView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            super.updateHeaderViewPosition(headerView);
        } else {
            super.updateHeaderViewPosition(mHeaderViewWrapper);
        }
    }

    @Override
    protected void removeHeaderViewFromActivity(View headerView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            super.removeHeaderViewFromActivity(headerView);
        } else if (mHeaderViewWrapper != null) {
            super.removeHeaderViewFromActivity(mHeaderViewWrapper);
            mHeaderViewWrapper = null;
        }
    }

    @Override
    protected EnvironmentDelegate createDefaultEnvironmentDelegate() {
        return new AbcEnvironmentDelegate();
    }

    @Override
    protected HeaderTransformer createDefaultHeaderTransformer() {
        return new AbcDefaultHeaderTransformer();
    }

    public static class AbcEnvironmentDelegate implements EnvironmentDelegate {
        /**
         * @return Context which should be used for inflating the header layout
         */
        public Context getContextForInflater(Activity activity) {
            Context context = null;
            ActionBar ab = ((ActionBarActivity) activity).getSupportActionBar();
            if (ab != null) {
                context = ab.getThemedContext();
            }
            if (context == null) {
                context = activity;
            }
            return context;
        }
    }
}
