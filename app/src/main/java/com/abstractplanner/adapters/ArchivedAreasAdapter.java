package com.abstractplanner.adapters;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractDataProvider;
import com.abstractplanner.data.TasksDataProvider;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.AddQuickTaskDialogFragment;
import com.abstractplanner.fragments.AddTaskNotificationDialogFragment;
import com.abstractplanner.fragments.EditAreaDialogFragment;
import com.abstractplanner.fragments.EditTaskDialogFragment;
import com.abstractplanner.utils.DateTimeUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Calendar;

public class ArchivedAreasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SwipeableItemAdapter<ArchivedAreasAdapter.MySwipeableViewHolder> {
    private static final String TAG = "ArchivedAreasAdapter";

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    private AbstractDataProvider mProvider;
    private MainActivity mActivity;
    private int previousItemCount = 0;
    private ArchivedAreasAdapter.EventListener mEventListener;
    private View.OnClickListener mItemViewOnClickListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemPinned(int position);

        void onDatasetEmpty();

        void onDatasetFilled();

        void onItemViewClicked(View v, boolean pinned);
    }

    public static class MySwipeableViewHolder extends AbstractSwipeableItemViewHolder {
        public FrameLayout mContainer;
        public TextView mAreaName;
        public TextView mAreaDescription;

        public MySwipeableViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mAreaName = (TextView) v.findViewById(R.id.archived_areas_list_area_name);
            mAreaDescription = (TextView) v.findViewById(R.id.archived_areas_list_area_description);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    public ArchivedAreasAdapter(AbstractDataProvider dataProvider, MainActivity activity) {
        mProvider = dataProvider;
        mActivity = activity;
        mItemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };

        // SwipeableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    public AppCompatActivity getContext(){
        return mActivity;
    }

    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(v, true); // true --- pinned
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if(v.getTag() != null) {
            final int position = (int) v.getTag();

            final Area area = (Area) mProvider.getItem(position).getDataObject();


            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(area.getDescription())
                    .setTitle(area.getName())
                    .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                            EditAreaDialogFragment newFragment = new EditAreaDialogFragment();
                            newFragment.setPrevoisArea(area);
                            newFragment.setAreasAdapter(ArchivedAreasAdapter.this);
                            // The device is smaller, so show the fragment fullscreen
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            // For a little polish, specify a transition animation
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            // To make it fullscreen, use the 'content' root view as the container
                            // for the fragment, which is always the root view for the activity
                            transaction.add(R.id.drawer_layout, newFragment)
                                    .addToBackStack(null).commit();
                        }
                    })
                    .setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ((Area) mProvider.getItem(position).getDataObject()).setArchived(false);

                            mProvider.updateItem(position);
                            notifyItemRemoved(position);
                        }
                    })
                    .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProvider.removeItem(position, true);
                            notifyItemRemoved(position);
                        }
                    });

            builder.show();
        }

        if (mEventListener != null) {
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }

    public void saveEditedArea(Area area){
        int position = getItemPositionById(area.getId());

        if(position >= 0){
            mProvider.getItem(position).updateDataObject(area);
            notifyItemChanged(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    private int getItemPositionById(long id){
        for(int i = 0; i < getItemCount(); i++){
            if(mProvider.getItem(i).getDataObject() != null)
                if(((Area)mProvider.getItem(i).getDataObject()).getId() == id){
                    return i;
                }
        }
        return - 1;
    }

    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*if(viewType == TasksDataProvider.TaskData.ITEM_HEADER) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.list_item_header, parent, false);
            return new ArchivedAreasAdapter.MyStaticViewHolder(v);
        }
        else{*/
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.archived_areas_list_item_normal, parent, false);
            return new MySwipeableViewHolder(v);
        /*}*/
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        /*if(viewType == TasksDataProvider.TaskData.ITEM_NORMAL){*/
            bindSwipableViewHolder((MySwipeableViewHolder)holder, position);
        /*} else
        if(viewType == TasksDataProvider.TaskData.ITEM_HEADER){
            final AbstractDataProvider.Data item = mProvider.getItem(position);
            ((ArchivedAreasAdapter.MyStaticViewHolder)holder).bind(item.getText());
        }*/
    }

    //@Override
    ///public void onBindViewHolder(MySwipeableViewHolder holder, int position) {
    public void bindSwipableViewHolder(MySwipeableViewHolder holder, int position) {
        final AbstractDataProvider.Data item = mProvider.getItem(position);
        Area area = (Area) item.getDataObject();

        // set listeners
        // (if the item is *pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        // (if the item is *not pinned*, click event comes to the mContainer)
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        holder.mContainer.setTag(position);

        // set text
        holder.mAreaName.setText(area.getName());

        if(area.getDescription() != null && !area.getDescription().equals("")) {
            holder.mAreaDescription.setVisibility(View.VISIBLE);
            holder.mAreaDescription.setText(area.getDescription());
        }
        else
            holder.mAreaDescription.setVisibility(View.GONE);

        // set background resource (target view ID: container)
        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & ArchivedAreasAdapter.Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & ArchivedAreasAdapter.Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & ArchivedAreasAdapter.Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }

        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(
                item.isPinned() ? ArchivedAreasAdapter.Swipeable.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
    }

    @Override
    public int getItemCount() {
        int itemCount = mProvider.getCount();
        if(previousItemCount != itemCount){
            if(itemCount == 0){
                mEventListener.onDatasetEmpty();
            } else
            if(previousItemCount == 0 && itemCount > 0){
                mEventListener.onDatasetFilled();
            }
        }
        previousItemCount = itemCount;

        return itemCount;
    }

    @Override
    public int onGetSwipeReactionType(MySwipeableViewHolder holder, int position, int x, int y) {
        return ArchivedAreasAdapter.Swipeable.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(MySwipeableViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case ArchivedAreasAdapter.Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case ArchivedAreasAdapter.Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left_delete;
                break;
            case ArchivedAreasAdapter.Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right_restore;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(MySwipeableViewHolder holder, final int position, int result) {
        Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe right
            case ArchivedAreasAdapter.Swipeable.RESULT_SWIPED_RIGHT:
                /*if (mProvider.getItem(position).isPinned()) {
                    // pinned --- back to default position
                    return new UnpinResultAction(this, position);
                } else {

                }*/
                // not pinned --- remove
                return new ArchivedAreasAdapter.SwipeRightResultAction(this, position);

            // swipe left -- pin
            case ArchivedAreasAdapter.Swipeable.RESULT_SWIPED_LEFT:
                return new ArchivedAreasAdapter.SwipeLeftResultAction(this, position);
            // other --- do nothing
            case ArchivedAreasAdapter.Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new ArchivedAreasAdapter.UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public ArchivedAreasAdapter.EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(ArchivedAreasAdapter.EventListener eventListener) {
        mEventListener = eventListener;
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private ArchivedAreasAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(ArchivedAreasAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            mAdapter.mProvider.removeItem(mPosition, true);
            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

/*            if (mSetPinned && mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemPinned(mPosition);
            }*/
            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            //mAdapter = null;
        }
    }

    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private ArchivedAreasAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(ArchivedAreasAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            ((Area) mAdapter.mProvider.getItem(mPosition).getDataObject()).setArchived(false);

            mAdapter.mProvider.updateItem(mPosition);
            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        private ArchivedAreasAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(ArchivedAreasAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            if (item.isPinned()) {
                item.setPinned(false);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

}