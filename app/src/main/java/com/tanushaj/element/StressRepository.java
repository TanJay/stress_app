package com.tanushaj.element;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class StressRepository {

    private StressItemDAO stressItemDAO;
    private LiveData<List<StressItem>> listLiveData;

    StressRepository(Application application){
        StressDatabase stressDatabase = StressDatabase.getDatabase(application);
        stressItemDAO = stressDatabase.stressItemDAO();
        listLiveData = stressItemDAO.getStresItems();
    }

    public LiveData<List<StressItem>> getListLiveData() {
        return listLiveData;
    }

    public void insert (StressItem stressItem) {
        new insertAsyncTask(stressItemDAO).execute(stressItem);
    }

    public void deleteAll(){
        stressItemDAO.deleteAll();
    }

    private static class insertAsyncTask extends AsyncTask<StressItem, Void, Void> {

        private StressItemDAO mAsyncTaskDao;

        insertAsyncTask(StressItemDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final StressItem... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
