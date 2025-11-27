package com.simon.harmonichackernews.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.simon.harmonichackernews.R;
import com.simon.harmonichackernews.data.Story;
import com.simon.harmonichackernews.utils.SwipePreferences;
import com.simon.harmonichackernews.utils.SwipePreferences.SwipeAction;
import com.simon.harmonichackernews.utils.Utils;

import java.util.List;
import java.util.Locale;

/**
 * ItemTouchHelper.Callback that handles swipe gestures on story items
 * and displays visual feedback (text labels) while swiping.
 * Inspired by materialistic's PeekabooTouchHelperCallback.
 */
public abstract class StorySwipeTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private final Paint mPaint = new Paint();
    private final int mPadding;
    private final Context mContext;
    
    private SwipeAction mLeftAction;
    private SwipeAction mRightAction;
    
    // Colors for different actions
    private int mSaveColor;
    private int mVoteColor;
    private int mShareColor;
    private int mMarkReadColor;
    
    // Current item state (for dynamic text like Save/Unsave)
    protected boolean mIsCurrentItemSaved = false;

    public StorySwipeTouchHelperCallback(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mContext = context;
        
        mPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.swipe_text_size));
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.swipe_text_padding);
        
        // Initialize colors
        mSaveColor = ContextCompat.getColor(context, R.color.swipe_save);
        mVoteColor = ContextCompat.getColor(context, R.color.swipe_vote);
        mShareColor = ContextCompat.getColor(context, R.color.swipe_share);
        mMarkReadColor = ContextCompat.getColor(context, R.color.swipe_mark_read);
        
        // Load preferences
        refreshSwipePreferences(context);
    }

    public void refreshSwipePreferences(Context context) {
        SwipeAction[] prefs = SwipePreferences.getSwipePreferences(context);
        mLeftAction = prefs[0];
        mRightAction = prefs[1];
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support drag & drop
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
        // Only allow swiping on story items (not headers)
        if (!(viewHolder instanceof StoryRecyclerViewAdapter.StoryViewHolder)) {
            return 0;
        }
        
        StoryRecyclerViewAdapter.StoryViewHolder storyHolder = 
                (StoryRecyclerViewAdapter.StoryViewHolder) viewHolder;
        Story story = storyHolder.story;
        
        if (story == null || !story.loaded) {
            return 0;
        }

        // Update saved state for dynamic text
        mIsCurrentItemSaved = Utils.isBookmarked(mContext, story.id);

        int swipeDirs = 0;
        
        // Check left swipe
        if (canPerformAction(mLeftAction, story)) {
            swipeDirs |= ItemTouchHelper.LEFT;
        }
        
        // Check right swipe
        if (canPerformAction(mRightAction, story)) {
            swipeDirs |= ItemTouchHelper.RIGHT;
        }

        return swipeDirs;
    }

    private boolean canPerformAction(SwipeAction action, Story story) {
        switch (action) {
            case None:
                return false;
            case Vote:
                // Allow vote if not already voted (we don't track this currently, so always allow)
                return true;
            case Save:
            case Share:
            case MarkRead:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            drawSwipeText(c, viewHolder, dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void drawSwipeText(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        boolean swipeRight = dX > 0;
        
        SwipeAction action = swipeRight ? mRightAction : mLeftAction;
        if (action == SwipeAction.None) return;
        
        String text = getActionText(action);
        int color = getActionColor(action);
        
        Rect rect = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), rect);
        float textWidth = rect.right - rect.left;
        float textHeight = rect.bottom - rect.top;
        float width = itemView.getWidth();
        float paddingY = (itemView.getHeight() - textHeight) / 2f;
        
        mPaint.setColor(color);
        // Calculate alpha based on swipe progress
        float progress = Math.abs(dX) / width;
        int alpha = Math.min(255, (int) (255 * progress / getSwipeThreshold(viewHolder)));
        mPaint.setAlpha(alpha);
        
        float textX;
        if (swipeRight) {
            textX = itemView.getLeft() + mPadding;
        } else {
            textX = itemView.getRight() - mPadding - textWidth;
        }
        
        canvas.drawText(text.toUpperCase(Locale.getDefault()),
                textX,
                itemView.getTop() + itemView.getHeight() - paddingY,
                mPaint);
    }

    private String getActionText(SwipeAction action) {
        switch (action) {
            case Save:
                return mIsCurrentItemSaved ? "UNSAVE" : "SAVE";
            case Vote:
                return "UPVOTE";
            case Share:
                return "SHARE";
            case MarkRead:
                return "MARK READ";
            default:
                return "";
        }
    }

    private int getActionColor(SwipeAction action) {
        switch (action) {
            case Save:
                return mSaveColor;
            case Vote:
                return mVoteColor;
            case Share:
                return mShareColor;
            case MarkRead:
                return mMarkReadColor;
            default:
                return mSaveColor;
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.33f; // 1/3 of the item width
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE; // Require full swipe, not velocity-based
    }

    public SwipeAction getLeftSwipeAction() {
        return mLeftAction;
    }

    public SwipeAction getRightSwipeAction() {
        return mRightAction;
    }
}
