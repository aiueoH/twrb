package linkedspinner;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
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
    private List<Item> leftItems;
    private List<Item> rightItems;
    private LeftItemAdapter leftItemAdapter;
    private RightItemAdapter rightItemAdapter;
    private MyView myView;

    private Item rightSelectedItem, leftSelectedItem;

    public LinkedSpinner(Context context, List<Item> leftItems) {
        this.mContext = context;
        this.leftItems = leftItems;
        leftItemAdapter = new LeftItemAdapter(mContext, leftItems);
        rightItems = new ArrayList<>();
        for (Item item : leftItems)
            rightItems.addAll(item.getSubItems());
        rightItemAdapter = new RightItemAdapter(mContext, rightItems);
        setRightSelectedItem(0);
    }

    public void show() {
        setLeftSelectedItem(rightSelectedItem.getSuperItem());
        myView = new MyView(mContext);
        myView.show();
    }

    public Item getRightSelectedItem() {
        return rightSelectedItem;
    }

    public void setRightSelectedItem(Item item) {
        rightSelectedItem = item;
        setLeftSelectedItem(item.getSuperItem());
    }

    public void setRightSelectedItem(int index) {
        setRightSelectedItem(rightItems.get(index));
    }

    private void setLeftSelectedItem(Item item) {
        leftSelectedItem = item;
        leftItemAdapter.setSelected(leftItems.indexOf(leftSelectedItem));
    }

    private void onLeftItemClick(int position) {
        Item item = leftItems.get(position);
        setLeftSelectedItem(item);
        if (item.getSubItems().size() > 0) {
            Item subItem = (Item) item.getSubItems().get(0);
            int index = rightItems.indexOf(subItem);
            if (index >= 0)
                myView.rightSmoothScrollToPosition(index);
        }
    }

    private void onRightItemClick(int position) {
        setRightSelectedItem(position);
        myView.dismiss();
        EventBus.getDefault().post(new OnSelectedEvent(rightSelectedItem));
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

        private boolean isRightAutoScrolling = false;
        private boolean isLeftAutoScrolling = false;
        private int leftAutoScrollingTarget;
        private LinearLayoutManagerWithSmoothScroller leftLayoutManager, rightLayoutManager;

        public MyView(Context context) {
            super(context, R.style.linkedSpinner);
            leftLayoutManager = new LinearLayoutManagerWithSmoothScroller(context);
            rightLayoutManager = new LinearLayoutManagerWithSmoothScroller(context);
            rightLayoutManager.setOnStartHook(new Runnable() {
                @Override
                public void run() {
                    isRightAutoScrolling = true;
                }
            });
        }

        public void leftSmoothScrollToPosition(int position, int snapTo) {
            if (isLeftAutoScrolling && position == leftAutoScrollingTarget)
                return;
            isLeftAutoScrolling = true;
            leftAutoScrollingTarget = position;
            leftLayoutManager.smoothScrollToPosition(left_recyclerView, null, position, snapTo);
        }

        public void rightSmoothScrollToPosition(int position) {
            rightLayoutManager.smoothScrollToPosition(right_recyclerView, null, position);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.linkedspinner);
            ButterKnife.bind(this);
            left_recyclerView.setLayoutManager(leftLayoutManager);
            left_recyclerView.setAdapter(leftItemAdapter);
            left_recyclerView.scrollToPosition(leftItems.indexOf(rightSelectedItem.getSuperItem()));
            left_recyclerView.addOnScrollListener(new LeftOnScrollListener());
            right_recyclerView.setLayoutManager(rightLayoutManager);
            right_recyclerView.setAdapter(rightItemAdapter);
            right_recyclerView.scrollToPosition(rightItems.indexOf(rightSelectedItem));
            right_recyclerView.addOnScrollListener(new RightOnScrollListener());
            leftAutoScrollingTarget = leftItems.indexOf(rightSelectedItem.getSuperItem());
        }

        class LeftOnScrollListener extends RecyclerView.OnScrollListener {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isLeftAutoScrolling = false;
                }
            }
        }

        class RightOnScrollListener extends RecyclerView.OnScrollListener {
            private int scrollState = RecyclerView.SCROLL_STATE_IDLE;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                scrollState = newState;
                if (isRightAutoScrolling && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isRightAutoScrolling = false;
                }
                if (!isRightAutoScrolling &&
                        (scrollState == RecyclerView.SCROLL_STATE_IDLE ||
                                scrollState == RecyclerView.SCROLL_STATE_DRAGGING)) {
                    int rightFirstIndex = rightLayoutManager.findFirstCompletelyVisibleItemPosition();
                    int rightLastIndex = rightLayoutManager.findLastCompletelyVisibleItemPosition();
                    Item newLeftItem = getLeftItemOfFocusRightItem(rightFirstIndex, rightLastIndex);
                    setLeftSelectedItem(newLeftItem);
                    scrollToLeftItemIfInvisible();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int rightFirstIndex = rightLayoutManager.findFirstCompletelyVisibleItemPosition();
                int rightLastIndex = rightLayoutManager.findLastCompletelyVisibleItemPosition();
                if (isRightAutoScrolling) {
                    Item firstSubItem = (Item) leftSelectedItem.getSubItems().get(0);
                    if (firstSubItem != null && rightItems.indexOf(firstSubItem) == rightFirstIndex)
                        isRightAutoScrolling = false;
                }
                if (!isRightAutoScrolling) {
                    Item newLeftItem = getLeftItemOfFocusRightItem(rightFirstIndex, rightLastIndex);
                    setLeftSelectedItem(newLeftItem);
                    scrollToLeftItemIfInvisible();
                }
            }

            @NonNull
            private Item getLeftItemOfFocusRightItem(int rightFirstIndex, int rightLastIndex) {
                if (rightLastIndex == rightItems.size() - 1)
                    return rightItems.get(rightLastIndex).getSuperItem();
                if (rightFirstIndex == 0)
                    return rightItems.get(rightFirstIndex).getSuperItem();
                boolean b = false;
                for (int i = rightFirstIndex; i <= rightLastIndex; i++)
                    b = leftSelectedItem == rightItems.get(i).getSuperItem();
                if (!b)
                    return rightItems.get(rightFirstIndex).getSuperItem();
                return leftSelectedItem;
            }

            private void scrollToLeftItemIfInvisible() {
                int snapTo = isLeftItemVisible(leftSelectedItem);
                if (snapTo != 0) {
                    leftSmoothScrollToPosition(leftItems.indexOf(leftSelectedItem), snapTo);
                }
            }

            private int isLeftItemVisible(Item leftItem) {
                int f = leftLayoutManager.findFirstCompletelyVisibleItemPosition();
                int l = leftLayoutManager.findLastCompletelyVisibleItemPosition();
                int i = leftItems.indexOf(leftItem);
                if (!(i >= f && i <= l)) {
                    if (i > f)
                        return LinearSmoothScroller.SNAP_TO_END;
                    else
                        return LinearSmoothScroller.SNAP_TO_START;
                }
                return 0;
            }
        }

        class LinearLayoutManagerWithSmoothScroller extends LinearLayoutManager {
            private Runnable onStartHook;

            public LinearLayoutManagerWithSmoothScroller(Context context) {
                super(context, VERTICAL, false);
            }

            public void setOnStartHook(Runnable onStartHook) {
                this.onStartHook = onStartHook;
            }

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                smoothScrollToPosition(recyclerView, state, position, LinearSmoothScroller.SNAP_TO_START);
            }

            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position, int snapTo) {
                RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext(), snapTo);
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }

            private class TopSnappedSmoothScroller extends LinearSmoothScroller {
                private int snapTo;

                public TopSnappedSmoothScroller(Context context, int snapTo) {
                    super(context);
                    this.snapTo = snapTo;
                }

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return LinearLayoutManagerWithSmoothScroller.this
                            .computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected int getVerticalSnapPreference() {
                    return snapTo;
                }

                @Override
                protected void onStart() {
                    super.onStart();
                    if (onStartHook != null)
                        onStartHook.run();
                }
            }
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
            holder.box.setOnClickListener(new View.OnClickListener() {
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
            holder.box.setOnClickListener(new View.OnClickListener() {
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
}
