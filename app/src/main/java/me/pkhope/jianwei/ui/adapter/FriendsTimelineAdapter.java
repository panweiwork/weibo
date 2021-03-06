package me.pkhope.jianwei.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;
import java.util.List;

import me.pkhope.jianwei.R;
import me.pkhope.jianwei.ui.activity.ImageDetailActivity;
import me.pkhope.jianwei.ui.activity.ReplyActivity;
import me.pkhope.jianwei.ui.activity.UserActivity;
import me.pkhope.jianwei.ui.activity.WeiboDetailActivity;
import me.pkhope.jianwei.utils.AdditionText;
import me.pkhope.jianwei.utils.TimeConverter;
import me.pkhope.jianwei.utils.ToastUtils;
import me.pkhope.jianwei.widget.emojitextview.EmojiTextView;

/**
 * Created by pkhope on 2016/6/8.
 */
public class FriendsTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_TEXT_ITEM = 0;
    private static final int TYPE_IMAGE_ITEM = 1;
    private static final int TYPE_RETWEET_ITEM = 2;

    private List<Status> statusList = new ArrayList<>();

    private Context context;

    public FriendsTimelineAdapter(Context context, List<Status> list){
        this.context = context;
        statusList = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_RETWEET_ITEM){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weibo_with_retweet_item,parent,false);
            return new RetweetViewHolder(view);
        }
        else if (viewType == TYPE_TEXT_ITEM){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weibo_text_item,parent,false);
            return new TextViewHolder(view);

        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weibo_image_item,parent,false);
            return new ImageViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Status status = statusList.get(position);
        ((TextViewHolder)holder).status = status;
        ((TextViewHolder)holder).content.setText(status.text);
        ((TextViewHolder)holder).nickname.setText(status.user.name);
        ((TextViewHolder)holder).time.setText(TimeConverter.convert(status.created_at));

        Glide.with(context)
                .load(status.user.profile_image_url)
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
//                .override(holder.avatar.getMeasuredWidth(), holder.avatar.getMeasuredHeight())
                .placeholder(R.drawable.default_image)
                .into(((TextViewHolder)holder).avatar);

        if (holder instanceof ImageViewHolder){

            Glide.with(context)
                    .load(status.original_pic)
                    .centerCrop()
                    .dontAnimate()
                    .thumbnail(0.5f)
//                    .override(holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight())
                    .placeholder(R.drawable.default_image)
                    .into(((ImageViewHolder)holder).image);
        }

        if (holder instanceof RetweetViewHolder){

            ((RetweetViewHolder)holder).retweetContent.setText(AdditionText.retweet(status.retweeted_status.user.name) + status.retweeted_status.text);
        }
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (statusList.get(position).retweeted_status != null){
            return TYPE_RETWEET_ITEM;
        }
        else if (statusList.get(position).original_pic.equals("")){
            return TYPE_TEXT_ITEM;
        }else {
            return TYPE_IMAGE_ITEM;
        }
    }



    public class TextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{

        public Status status;

        ImageView avatar;
        TextView nickname;
        TextView time;
        EmojiTextView content;
        View rootView;

        public TextViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            time = (TextView) itemView.findViewById(R.id.time);
            content = (EmojiTextView) itemView.findViewById(R.id.content);

            itemView.setOnCreateContextMenuListener(this);

            itemView.setOnClickListener(this);
            avatar.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == avatar){
                Intent intent = new Intent(v.getContext(), UserActivity.class);
                intent.putExtra("nickname",status.user.screen_name);
                intent.putExtra("from","photo");
                v.getContext().startActivity(intent);
            } else {
                Intent intent = new Intent(v.getContext(), WeiboDetailActivity.class);
                intent.putExtra("id",status.id);
                v.getContext().startActivity(intent);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem transmit = menu.add("转发");
            transmit.setOnMenuItemClickListener(this);
            MenuItem comment = menu.add("评论");
            comment.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(rootView.getContext(), ReplyActivity.class);
            if (item.getTitle().equals("转发")){
                intent.putExtra("operation","repost");
            } else if (item.getTitle().equals("评论")){
                intent.putExtra("operation","comment");
            }
            intent.putExtra("id",Long.parseLong(status.id));
            rootView.getContext().startActivity(intent);
            return true;
        }
    }

    public class ImageViewHolder extends TextViewHolder implements View.OnClickListener{

        LinearLayout llImage;
        ImageView image;

        public ImageViewHolder(View itemView){
            super(itemView);

            llImage = (LinearLayout) itemView.findViewById(R.id.ll_image);
            image = (ImageView) itemView.findViewById(R.id.image);
            image.setOnClickListener(this);

            llImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == image){
                Intent intent = new Intent(rootView.getContext(), ImageDetailActivity.class);
                intent.putStringArrayListExtra("images",status.pic_urls);
                rootView.getContext().startActivity(intent);

            } else {
                super.onClick(v);
            }
        }
    }

    public class RetweetViewHolder extends TextViewHolder{

        LinearLayout llText;
        LinearLayout llRetweet;
        EmojiTextView retweetContent;

        public RetweetViewHolder(View itemView) {
            super(itemView);

            llText = (LinearLayout) itemView.findViewById(R.id.ll_text);
            llRetweet = (LinearLayout) itemView.findViewById(R.id.ll_retweet);
            retweetContent = (EmojiTextView) itemView.findViewById(R.id.retweet_content);

            llText.setOnClickListener(this);
            llRetweet.setOnClickListener(this);
        }
    }
}
