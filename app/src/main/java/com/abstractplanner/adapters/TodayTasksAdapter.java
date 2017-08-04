/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.abstractplanner.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractDataProvider;
import com.abstractplanner.data.TasksDataProvider;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.EditNotificationDialogFragment;
import com.abstractplanner.fragments.EditTaskDialogFragment;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import java.util.Calendar;

public class TodayTasksAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SwipeableItemAdapter<TodayTasksAdapter.MySwipeableViewHolder> {
    private static final String TAG = "TodayTasksAdapter";

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    private AbstractDataProvider mProvider;
    private MainActivity mActivity;
    private int previousItemCount = 0;
    private EventListener mEventListener;
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
        public TextView mTaskName;
        public TextView mTaskAreaName;
        public TextView mTaskDescription;

        public MySwipeableViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mTaskName = (TextView) v.findViewById(R.id.today_list_task_name);
            mTaskAreaName = (TextView) v.findViewById(R.id.today_list_task_area);
            mTaskDescription = (TextView) v.findViewById(R.id.today_list_task_description);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    public static class MyStaticViewHolder extends RecyclerView.ViewHolder{

        public TextView mDay;
        public MyStaticViewHolder(View itemView) {
            super(itemView);

            mDay = (TextView) itemView.findViewById(R.id.list_item_header_text);
        }

        public void bind(String day){
            mDay.setText(day);
        }
    }

    public TodayTasksAdapter(AbstractDataProvider dataProvider, MainActivity activity) {
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
        if(v.getTag() != null){
            final int position = (int) v.getTag();

            final Task task = (Task) mProvider.getItem(position).getDataObject();

            String status;
            if(task.isDone())
                status = "Status: Done";
            else
                status = "Status: Undone";
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(task.getDescription() + "\n\n" + status)
                    .setTitle(task.getName())
                    .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                            EditTaskDialogFragment newFragment = new EditTaskDialogFragment();
                            newFragment.setTask(task);
                            newFragment.setTaskDate(task.getDate());
                            newFragment.setAdapter(TodayTasksAdapter.this);
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
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

            builder.show();

        }

        if (mEventListener != null) {
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }

    public void saveEditedTask(Task task){
        int position = getItemPositionById(task.getId());

        if(position >= 0){
            if(task.isDone()){
                mProvider.removeItem(position, task.isDone());
                notifyItemRemoved(position);
            }else
                if(((Task)mProvider.getItem(position).getDataObject()).getDate().compareTo(task.getDate()) == 0){
                    mProvider.getItem(position).updateDataObject(task);
                    notifyItemChanged(position);
                } else {
                    // int position = mProvider
                    mProvider.getItem(position).updateDataObject(task);
                    mProvider.updateItem(position);
                    notifyDataSetChanged();
                }
        }
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    private int getItemPositionById(long id){
        for(int i = 0; i < getItemCount(); i++){
            if(mProvider.getItem(i).getDataObject() != null)
                if(((Task)mProvider.getItem(i).getDataObject()).getId() == id){
                    return i;
                }
        }
        return - 1;
    }

/*    public Task getItemByPosition(int position){
        return mTasks.get(position);
    }

    public void removeByPosition(int position){
        mTasks.remove(position);
        //notifyDataSetChanged();
    }*/
    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TasksDataProvider.TaskData.ITEM_HEADER) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.list_item_header, parent, false);
            return new MyStaticViewHolder(v);
        }
        else{
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.today_tasks_list_item_normal, parent, false);
            return new MySwipeableViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        if(viewType == TasksDataProvider.TaskData.ITEM_NORMAL){
            bindSwipableViewHolder((MySwipeableViewHolder)holder, position);
        } else
            if(viewType == TasksDataProvider.TaskData.ITEM_HEADER){
                final AbstractDataProvider.Data item = mProvider.getItem(position);
                ((MyStaticViewHolder)holder).bind(item.getText());
            }
    }

    //@Override
    ///public void onBindViewHolder(MySwipeableViewHolder holder, int position) {
    public void bindSwipableViewHolder(MySwipeableViewHolder holder, int position) {
        final AbstractDataProvider.Data item = mProvider.getItem(position);
        Task task = (Task) item.getDataObject();

        // set listeners
        // (if the item is *pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        // (if the item is *not pinned*, click event comes to the mContainer)
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        holder.mContainer.setTag(position);

        // set text
        holder.mTaskName.setText(task.getName());

        holder.mTaskAreaName.setText(task.getArea().getName());

        holder.mTaskDescription.setText(task.getDescription());

        // set background resource (target view ID: container)
        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }

        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(
                item.isPinned() ? Swipeable.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
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
        return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(MySwipeableViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left_notify;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right_done;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(MySwipeableViewHolder holder, final int position, int result) {
        Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe right
            case Swipeable.RESULT_SWIPED_RIGHT:
                /*if (mProvider.getItem(position).isPinned()) {
                    // pinned --- back to default position
                    return new UnpinResultAction(this, position);
                } else {

                }*/
                // not pinned --- remove
                return new SwipeRightResultAction(this, position);

                // swipe left -- pin
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position);
            // other --- do nothing
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private TodayTasksAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(TodayTasksAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

/*        DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
                ((Task)item.getDataObject()).getDate().set(Calendar.YEAR, year);
                ((Task)item.getDataObject()).getDate().set(Calendar.MONTH, monthOfYear);
                ((Task)item.getDataObject()).getDate().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mAdapter.mProvider.updateItem(mPosition);
                mAdapter.notifyDataSetChanged();
            }
        };

        private void setDate(*//*View v*//*) {
            final AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            DatePickerDialog datePickerDialog = new DatePickerDialog(mAdapter.getContext(), d,
                    ((Task)item.getDataObject()).getDate().get(Calendar.YEAR),
                    ((Task)item.getDataObject()).getDate().get(Calendar.MONTH),
                    ((Task)item.getDataObject()).getDate().get(Calendar.DAY_OF_MONTH));

            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, mAdapter.getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    item.setPinned(false);
                    mAdapter.notifyItemChanged(mPosition);
                    mSetPinned = false;
                }
            });
            datePickerDialog.show();
        }*/

        private void createNotification(){
            FragmentManager fragmentManager = mAdapter.getContext().getSupportFragmentManager();
            EditNotificationDialogFragment newFragment = new EditNotificationDialogFragment();
            //newFragment.setAdapter(NotificationsAdapter.this);
            //newFragment.setNotificationToEdit(((Notification)mProvider.getItem(position).getDataObject()));
            newFragment.setNotificationTask((Task)mAdapter.mProvider.getItem(mPosition).getDataObject());
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(/*android.R.id.content*/R.id.drawer_layout, newFragment)
                    .addToBackStack(null).commit();
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

/*            AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            if (!item.isPinned()) {
                item.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
                mSetPinned = true;
            }*/
            //setDate();

            createNotification();

            /*mAdapter.mProvider.removeItem(mPosition);
            mAdapter.notifyItemRemoved(mPosition);*/
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
        private TodayTasksAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(TodayTasksAdapter adapter, int position) {
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
        private TodayTasksAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(TodayTasksAdapter adapter, int position) {
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