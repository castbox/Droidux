package info.izumin.android.droidux;

import io.reactivex.Observable;

/**
 * Created by izumin on 12/6/15.
 */
public interface BaseStore {
    Observable<Action> dispatch(Action action);
}
