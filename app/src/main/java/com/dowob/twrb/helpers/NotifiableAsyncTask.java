package com.dowob.twrb.helpers;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutionException;

public abstract class NotifiableAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private OnPostExecuteListener onPostExecuteListener = null;

    public void setOnPostExecuteListener(OnPostExecuteListener onPostExecuteListener) {
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Nullable
    public Result getResult() {
        while (true)
            try {
                return get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (this.onPostExecuteListener != null)
            this.onPostExecuteListener.onPostExecute(this);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(NotifiableAsyncTask notifiableAsyncTask);
    }
}
