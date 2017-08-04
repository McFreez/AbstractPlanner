package com.abstractplanner.adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import com.abstractplanner.data.NotificationsDataProvider;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.EditNotificationDialogFragment;
import com.abstractplanner.fragments.RescheduleNotificationDialogFragment;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SwipeableItemAdapter<NotificationsAdapter.MySwipeableViewHolder> {
    private static final String TAG = "NotificationsAdapter";

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    private AbstractDataProvider mProvider;
    private MainActivity mActivity;
    private int previousItemCount = 0;
    private NotificationsAdapter.EventListener mEventListener;
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
        public TextView mMessage;
        public TextView mDetails;

        public MySwipeableViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mMessage = (TextView) v.findViewById(R.id.notifications_list_item_message);
            mDetails = (TextView) v.findViewById(R.id.notifications_list_item_details);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    public static class MyStaticViewHolder extends RecyclerView.ViewHolder{

        public TextView mType;
        public MyStaticViewHolder(View itemView) {
            super(itemView);

            mType = (TextView) itemView.findViewById(R.id.list_item_header_text);
        }

        public void bind(String day){
            mType.setText(day);
        }
    }

    public NotificationsAdapter(AbstractDataProvider dataProvider, MainActivity activity) {
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

            String type = "Type: ";
            switch (((Notification)mProvider.getItem(position).getDataObject()).getType()){
                case Notification.TYPE_ONE_TIME_ID:
                    type += Notification.TYPE_ONE_TIME_NAME;
                    break;
                case Notification.TYPE_EVERY_DAY_ID:
                    type += Notification.TYPE_EVERY_DAY_NAME;
                    break;
            }

            final Calendar notificationDate = ((Notification)mProvider.getItem(position).getDataObject()).getDate();

            String details = "";

            switch (((Notification)mProvider.getItem(position).getDataObject()).getType()){
                case Notification.TYPE_EVERY_DAY_ID:
                    details = DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
                    break;
                case Notification.TYPE_ONE_TIME_ID:

                    Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                    Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                    if(notificationDate.after(previousYear) && notificationDate.before(nextYear))
                        details = DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE)
                                + " at "
                                + DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
                    else
                        details = DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                                + " at "
                                + DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
                    break;

            }

            String task = "";

            if(((Notification)mProvider.getItem(position).getDataObject()).getTask() != null){
                task = "For task: " + ((Notification)mProvider.getItem(position).getDataObject()).getTask().getName() + " - " + ((Notification)mProvider.getItem(position).getDataObject()).getTask().getArea().getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(details + "\n" + task + "\n" + type)
                    .setTitle(((Notification)mProvider.getItem(position).getDataObject()).getMessage())
                    .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                            EditNotificationDialogFragment newFragment = new EditNotificationDialogFragment();
                            newFragment.setAdapter(NotificationsAdapter.this);
                            newFragment.setNotificationToEdit(((Notification)mProvider.getItem(position).getDataObject()));
                            // The device is smaller, so show the fragment fullscreen
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            // For a little polish, specify a transition animation
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            // To make it fullscreen, use the 'content' root view as the container
                            // for the fragment, which is always the root view for the activity
                            transaction.add(/*android.R.id.content*/R.id.drawer_layout, newFragment)
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

    public void saveNotification(Notification notification){
        int position = getItemPositionById(notification.getId());

        if(position >= 0){
            if(((Notification)mProvider.getItem(position).getDataObject()).getType() == notification.getType()){
                mProvider.getItem(position).updateDataObject(notification);
                notifyItemChanged(position);
            }
            else {
                mProvider.getItem(position).updateDataObject(notification);
                mProvider.updateItem(position);
                notifyDataSetChanged();
            }
        } else {
            mProvider.refreshData();
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    private int getItemPositionById(long id){
        for(int i = 0; i < getItemCount(); i++){
            if(mProvider.getItem(i).getDataObject() != null)
                if(((Notification)mProvider.getItem(i).getDataObject()).getId() == id){
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
        if(viewType == NotificationsDataProvider.NotificationData.ITEM_HEADER) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.list_item_header, parent, false);
            return new NotificationsAdapter.MyStaticViewHolder(v);
        }
        else{
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View v = inflater.inflate(R.layout.notifications_list_item_normal, parent, false);
            return new NotificationsAdapter.MySwipeableViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        if(viewType == NotificationsDataProvider.NotificationData.ITEM_NORMAL){
            bindSwipableViewHolder((NotificationsAdapter.MySwipeableViewHolder)holder, position);
        } else
        if(viewType == NotificationsDataProvider.NotificationData.ITEM_HEADER){
            final AbstractDataProvider.Data item = mProvider.getItem(position);
            ((NotificationsAdapter.MyStaticViewHolder)holder).bind(item.getText());
        }
    }

    //@Override
    ///public void onBindViewHolder(MySwipeableViewHolder holder, int position) {
    public void bindSwipableViewHolder(NotificationsAdapter.MySwipeableViewHolder holder, int position) {
        final AbstractDataProvider.Data item = mProvider.getItem(position);

        // set listeners
        // (if the item is *pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        // (if the item is *not pinned*, click event comes to the mContainer)
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        holder.mContainer.setTag(position);

        holder.mMessage.setText(((Notification)item.getDataObject()).getMessage());

        final Calendar notificationDate = ((Notification)item.getDataObject()).getDate();

        switch (((Notification)item.getDataObject()).getType()){
            case Notification.TYPE_EVERY_DAY_ID:
                holder.mDetails.setText(DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));
                break;
            case Notification.TYPE_ONE_TIME_ID:

                Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                if(notificationDate.after(previousYear) && notificationDate.before(nextYear))
                    holder.mDetails.setText(DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE)
                            + " at "
                            + DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));
                else
                    holder.mDetails.setText(DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                            + " at "
                            + DateUtils.formatDateTime(getContext(), notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));
                break;

        }

        // set background resource (target view ID: container)
        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & NotificationsAdapter.Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & NotificationsAdapter.Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & NotificationsAdapter.Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }

        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(
                item.isPinned() ? NotificationsAdapter.Swipeable.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
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
    public int onGetSwipeReactionType(NotificationsAdapter.MySwipeableViewHolder holder, int position, int x, int y) {
        return NotificationsAdapter.Swipeable.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(NotificationsAdapter.MySwipeableViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case NotificationsAdapter.Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case NotificationsAdapter.Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left_reschedule;
                break;
            case NotificationsAdapter.Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right_delete;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(NotificationsAdapter.MySwipeableViewHolder holder, final int position, int result) {
        Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe right
            case NotificationsAdapter.Swipeable.RESULT_SWIPED_RIGHT:
                /*if (mProvider.getItem(position).isPinned()) {
                    // pinned --- back to default position
                    return new UnpinResultAction(this, position);
                } else {

                }*/
                // not pinned --- remove
                return new NotificationsAdapter.SwipeRightResultAction(this, position);

            // swipe left -- pin
            case NotificationsAdapter.Swipeable.RESULT_SWIPED_LEFT:
                return new NotificationsAdapter.SwipeLeftResultAction(this, position);
            // other --- do nothing
            case NotificationsAdapter.Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new NotificationsAdapter.UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public NotificationsAdapter.EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(NotificationsAdapter.EventListener eventListener) {
        mEventListener = eventListener;
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private NotificationsAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(NotificationsAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

/*        DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
*//*                item.getDataObject().getDate().set(Calendar.YEAR, year);
                item.getDataObject().getDate().set(Calendar.MONTH, monthOfYear);
                item.getDataObject().getDate().set(Calendar.DAY_OF_MONTH, dayOfMonth);*//*

                mAdapter.mProvider.updateItem(mPosition);
                mAdapter.notifyDataSetChanged();
            }
        };

        private void setDate(*//*View v*//*) {
*//*            final AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            DatePickerDialog datePickerDialog = new DatePickerDialog(mAdapter.getContext(), d,
                    item.getDataObject().getDate().get(Calendar.YEAR),
                    item.getDataObject().getDate().get(Calendar.MONTH),
                    item.getDataObject().getDate().get(Calendar.DAY_OF_MONTH));

            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, mAdapter.getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    item.setPinned(false);
                    mAdapter.notifyItemChanged(mPosition);
                    mSetPinned = false;
                }
            });
            datePickerDialog.show();*//*
        }*/

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            final AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            if (!item.isPinned()) {
                item.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
                mSetPinned = true;
            }
            //setDate();
            //showDeferDialog();
            RescheduleNotificationDialogFragment dialogFragment = new RescheduleNotificationDialogFragment();
            dialogFragment.setNotification((Notification) mAdapter.mProvider.getItem(mPosition).getDataObject());
            dialogFragment.setEventListener(new RescheduleNotificationDialogFragment.EventListener() {
                @Override
                public void onNotificationDefered() {
                    mAdapter.notifyItemChanged(mPosition);
                }

                @Override
                public void onDismissed() {
                    mSetPinned = false;
                    item.setPinned(false);
                    mAdapter.notifyItemChanged(mPosition);
                }
            });
            dialogFragment.show(mAdapter.getContext().getSupportFragmentManager(), "RescheduleNotification");

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
        private NotificationsAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(NotificationsAdapter adapter, int position) {
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
        private NotificationsAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(NotificationsAdapter adapter, int position) {
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
