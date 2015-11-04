package info.izumin.android.droidux.sample;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import info.izumin.android.droidux.sample.action.CompleteTodoAction;
import info.izumin.android.droidux.sample.databinding.ListItemTodoBinding;
import info.izumin.android.droidux.sample.entity.TodoList;
import info.izumin.android.droidux.sample.reducer.DroiduxRootStore;
import rx.subjects.PublishSubject;

/**
 * Created by izumin on 11/4/15.
 */
public class TodoListAdapter extends BaseAdapter {
    public static final String TAG = TodoListAdapter.class.getSimpleName();

    private static final int LAYOUT_RES_ID = R.layout.list_item_todo;

    private final LayoutInflater inflater;
    private final DroiduxRootStore store;

    public TodoListAdapter(Context context) {
        super();
        this.inflater = LayoutInflater.from(context);
        this.store = ((App) context.getApplicationContext()).getStore();
        this.store.getTodoListStore().observe().subscribe(todoList -> notifyDataSetChanged());
    }

    @Override
    public int getCount() {
        return store.getTodoListStore().getState().getTodoList().size();
    }

    @Override
    public TodoList.Todo getItem(int position) {
        return store.getTodoListStore().getState().getTodoList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemTodoBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, LAYOUT_RES_ID, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ListItemTodoBinding) convertView.getTag();
        }

        binding.checkboxTodo.setOnCheckedChangeListener(null);

        TodoList.Todo todo = getItem(position);
        binding.setTodo(todo);

        PublishSubject<Boolean> checkBoxSubject = PublishSubject.create();
        binding.checkboxTodo.setOnCheckedChangeListener((v, isChecked) -> checkBoxSubject.onNext(isChecked));
        checkBoxSubject
                .flatMap(isCompleted -> store.dispatch(new CompleteTodoAction(todo.getId(), isCompleted)))
                .subscribe();

        return convertView;
    }
}
