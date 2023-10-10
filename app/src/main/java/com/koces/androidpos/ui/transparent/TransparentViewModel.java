package com.jiw.androidpos.ui.transparent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransparentViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TransparentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is reflow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}