package info.izumin.android.droidux;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by izumin on 11/28/15.
 */
public class Dispatcher {
    public static final String TAG = Dispatcher.class.getSimpleName();

    private final List<Middleware> middlewares;
    private final List<StoreImpl> storeImpls;

    public Dispatcher(List<Middleware> middlewares, StoreImpl... storeImpls) {
        this.middlewares = middlewares;
        this.storeImpls = Arrays.asList(storeImpls);
    }

    public Observable<Action> dispatch(Action action) {
        return Observable.just(action)
                .flatMap(new Function<Action, Observable<Action>>() {
                    @Override
                    public Observable<Action> apply(@NonNull Action action) throws Exception {
                        return applyMiddlewaresBeforeDispatch(action);
                    }
                })
                .doOnNext(new Consumer<Action>() {
                    @Override
                    public void accept(@NonNull Action action) throws Exception {
                        for (StoreImpl store : storeImpls) {
                            store.dispatch(action);
                        }
                    }
                })
                .flatMap(new Function<Action, Observable<Action>>() {
                    @Override
                    public Observable<Action> apply(@NonNull Action action) throws Exception {
                        return applyMiddlewaresAfterDispatch(action);
                    }
                });
    }

    private Observable<Action> applyMiddlewaresBeforeDispatch(Action action) {
        Observable<Action> o = Observable.just(action);

        for (final Middleware<?> mw : middlewares) {
            o = o.flatMap(new Function<Action, Observable<Action>>() {
                @Override
                public Observable<Action> apply(@NonNull Action a) throws Exception {
                    return mw.beforeDispatch(a);
                }
            });
        }
        return o;
    }

    private Observable<Action> applyMiddlewaresAfterDispatch(Action action) {
        Observable<Action> o = Observable.just(action);
        ListIterator<Middleware> iterator = middlewares.listIterator(middlewares.size());
        while(iterator.hasPrevious()) {
            final Middleware<?> mw = iterator.previous();
            o = o.flatMap(new Function<Action, Observable<Action>>() {
                @Override
                public Observable<Action> apply(Action a) {
                    return mw.afterDispatch(a);
                }
            });
        }
        return o;
    }
}
