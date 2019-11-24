package com.example.realfrontend;

import android.arch.lifecycle.ViewModel;
import android.util.Pair;

import com.example.data.Index;

import static android.util.Log.d;

public class MainViewModel extends ViewModel {

    private Index index = Index.EMPTY;

    private String selectedKey;
    private String selectedFakeName;


    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(String selectedKey) {
        this.selectedKey = selectedKey;
    }

    public String getSelectedFakeName() {
        return selectedFakeName;
    }

    public void setSelectedFakeName(String selectedFakeName) {
        this.selectedFakeName = selectedFakeName;
    }

    public Pair<Index.Book, Index.Fake> getSelectedBookAndFake() {
        return index.getBookAndFakeForNameAndKey(selectedFakeName, selectedKey);
    }
}
