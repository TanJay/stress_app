package com.tanushaj.element;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class StressViewModel extends AndroidViewModel {

    private StressRepository mRepository;

    private LiveData<List<StressItem>> mAllWords;

    public StressViewModel (Application application) {
        super(application);
        mRepository = new StressRepository(application);
        mAllWords = mRepository.getListLiveData();
    }

    LiveData<List<StressItem>> getAllWords() { return mAllWords; }

    public void insert(StressItem word) { mRepository.insert(word); }

    public void deleteAll() { mRepository.deleteAll(); }

}
