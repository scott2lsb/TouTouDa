package com.quanliren.quan_two.custom.emoji;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.quanliren.quan_two.activity.R;
import com.quanliren.quan_two.adapter.EmoteLargeAdapter;
import com.quanliren.quan_two.bean.emoticon.EmoticonActivityListBean.EmoticonZip.EmoticonImageBean;
import com.quanliren.quan_two.custom.emoji.EmoteGridView.EmoticonListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@EFragment
public class EmoticonFragmentLarge extends Fragment {

    AtomicBoolean init = new AtomicBoolean(false);

    @ViewById
    EmoteGridView gridview;

    @FragmentArg
    ArrayList<EmoticonImageBean> emoticon;

    EmoteLargeAdapter adapter;

    View view;

    EmoticonListener listener;

    public void setListener(EmoticonListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.emoticon_gridview_large, null);
        } else {
            ViewParent parent = view.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(view);
            }
        }
        return view;
    }

    @AfterViews
    public void refresh() {
        if (getActivity() != null && init.compareAndSet(false, true)) {
            adapter = new EmoteLargeAdapter(getActivity(), emoticon);
            gridview.setListener(listener);
            gridview.setAdapter(adapter);
        }

    }

}
