package linkedspinner;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ah.twrbtest.R;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class LinkedSpinner {
    private Context mContext;
    private List<Item> items;
    private List<Item> subItems;
    private LeftItemAdapter leftItemAdapter;
    private RightItemAdapter rightItemAdapter;
    private MyView myView;

    private Item selectedRightItem;

    public LinkedSpinner(Context context, List<Item> items) {
        this.mContext = context;
        this.items = items;
        leftItemAdapter = new LeftItemAdapter(mContext, items);
        subItems = new ArrayList<>();
        for (Item item : items)
            subItems.addAll(item.getSubItems());
        rightItemAdapter = new RightItemAdapter(mContext, subItems);
        setSelectedSubItem(0);
    }

    public void show() {
        updateSuperItem();
        myView = new MyView(mContext);
        myView.show();
    }

    public Item getSelectedSubItem() {
        return selectedRightItem;
    }

    public void setSelectedSubItem(Item item) {
        selectedRightItem = item;
        updateSuperItem();
    }

    public void setSelectedSubItem(int index) {
        setSelectedSubItem(subItems.get(index));
    }

    private void updateSuperItem() {
        int superIndex = items.indexOf(selectedRightItem.getSuperItem());
        leftItemAdapter.setSelected(superIndex);
    }

    private void onLeftItemClick(int position) {
        Item item = items.get(position);
        if (item.getSubItems().size() > 0) {
            Item subItem = (Item) item.getSubItems().get(0);
            int index = subItems.indexOf(subItem);
            if (index >= 0) {
                myView.right_recyclerView.scrollToPosition(index);
            }
        }
    }

    private void onRightItemClick(int position) {
        setSelectedSubItem(position);
        myView.dismiss();
        EventBus.getDefault().post(new OnSelectedEvent(selectedRightItem));
    }

    public static class OnSelectedEvent {
        private Item selectedItem;

        public OnSelectedEvent(Item selectedItem) {
            this.selectedItem = selectedItem;
        }

        public Item getSelectedItem() {
            return selectedItem;
        }
    }

    class MyView extends Dialog {
        @Bind(R.id.recyclerView_left)
        RecyclerView left_recyclerView;
        @Bind(R.id.recyclerView_right)
        RecyclerView right_recyclerView;

        public MyView(Context context) {
            super(context, R.style.linkedSpinner);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.linkedspinner);
            ButterKnife.bind(this);
            left_recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            left_recyclerView.setAdapter(leftItemAdapter);
            left_recyclerView.scrollToPosition(items.indexOf(selectedRightItem.getSuperItem()));
            right_recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            right_recyclerView.setAdapter(rightItemAdapter);
            right_recyclerView.scrollToPosition(subItems.indexOf(selectedRightItem));
            right_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }
    }

    class LeftItemAdapter extends RecyclerView.Adapter<LeftItemAdapter.ViewHolder> {
        private Context mContext;
        private List<Item> items;
        private int selected = 0;

        public LeftItemAdapter(Context mContext, List<Item> items) {
            this.mContext = mContext;
            this.items = items;
        }

        public int getSelected() {
            return selected;
        }

        public void setSelected(int selected) {
            notifyItemChanged(this.selected);
            this.selected = selected;
            notifyItemChanged(this.selected);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_linkedspinner, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Item item = items.get(position);
            holder.textView.setText(item.getName().toString());
            if (position == selected) {
                holder.box.setBackgroundColor(Color.parseColor("#009587"));
                holder.textView.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                holder.box.setBackgroundColor(Color.parseColor("#FFFFFF"));
                holder.textView.setTextColor(Color.parseColor("#6d6d6d"));
            }
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int lastSelected = selected;
                    selected = position;
                    notifyItemChanged(lastSelected);
                    notifyItemChanged(selected);
                    onLeftItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.linearLayout_box)
            LinearLayout box;
            @Bind(R.id.textView)
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    class RightItemAdapter extends RecyclerView.Adapter<RightItemAdapter.ViewHolder> {
        private Context mContext;
        private List<Item> items;

        public RightItemAdapter(Context context, List<Item> items) {
            this.mContext = context;
            this.items = items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_linkedspinner, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Item item = items.get(position);
            holder.textView.setText(item.getName().toString());
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRightItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.textView)
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
