package com.angopapo.aroundme.Messaging;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.angopapo.aroundme.ClassHelper.AroundMeMessage;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Angopapo, LDA on 19.08.16.
 */
public class MessageAdapter extends ArrayAdapter<AroundMeMessage> {
    public static final int TYPE_MESSAGE_FROM = 0;
    public static final int TYPE_MESSAGE_TO = 1;

    private List<AroundMeMessage> objects;
    private LayoutInflater mInflater;
    private Context context;
    private String userToId;

    private String userToPhotoUrl, userFromPhotoUrl;

    private User mUserTo, mUserFrom;

    private static MessageImageListener messageImageListener = null;

    public MessageAdapter(Context context, List<AroundMeMessage> objects, String userToId) {
        super(context, R.layout.message_to_item, objects);
        this.objects = objects;
        this.context = context;
        this.userToId = userToId;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mUserFrom = (User)User.getCurrentUser();
        userFromPhotoUrl = mUserFrom.getPhotoUrl();
        ParseQuery<User> userParseQuery = User.getUserQuery();
        userParseQuery.fromLocalDatastore();
        userParseQuery.getInBackground(userToId, new GetCallback<User>() {
            @Override
            public void done(User user, ParseException e) {
                if (user != null) {
                    mUserTo = user;
                } else {
                    Log.d("myapp", e.toString());
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if(TextUtils.equals(objects.get(position).getUserFromId(), userToId)){
            return TYPE_MESSAGE_TO;
        } else {
            return TYPE_MESSAGE_FROM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public static void setMessageImageListener(MessageImageListener messageImageListener1){
        messageImageListener = messageImageListener1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if(convertView == null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_MESSAGE_FROM:
                    convertView = mInflater.inflate(R.layout.message_from_item, parent, false);
                    holder.messageText = (TextView) convertView.findViewById(R.id.message_txt);
                    holder.messageTime = (TextView) convertView.findViewById(R.id.message_time);
                    holder.sendProgress = (ImageView) convertView.findViewById(R.id.send_progress);
                    holder.profilePhoto = (SimpleDraweeView) convertView.findViewById(R.id.profile_photo);
                    holder.messageImage = (SimpleDraweeView) convertView.findViewById(R.id.message_image);
                    //holder.messageSeen = (ImageView) convertView.findViewById(R.id.message_image);
                    //holder.messageSend = (ImageView) convertView.findViewById(R.id.message_image);

                    if(mUserFrom.getPhotoUrl() != null){

                        // Load profile image
                        Uri uriProfle = Uri.parse((mUserFrom.getPhotoUrl()));


                        ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                                .setProgressiveRenderingEnabled(true)
                                .setLocalThumbnailPreviewsEnabled(true)
                                .build();

                        AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                                .setImageRequest(requestProfle)
                                .build();

                        holder.profilePhoto.setController(newControllerProfle);


                    } // Load profile image

                    else {



                        Uri uriProfle2 = Uri.parse(String.valueOf((R.drawable.profile_default_photo)));

                        ImageRequest requestProfle2 = ImageRequestBuilder.newBuilderWithSource(uriProfle2)
                                .setProgressiveRenderingEnabled(true)
                                .setLocalThumbnailPreviewsEnabled(true)
                                .build();

                        AbstractDraweeController newControllerProfle2 = Fresco.newDraweeControllerBuilder()
                                .setImageRequest(requestProfle2)
                                .build();

                        holder.profilePhoto.setController(newControllerProfle2);
                    }




                    break;
                case TYPE_MESSAGE_TO:
                    convertView = mInflater.inflate(R.layout.message_to_item, parent, false);
                    holder.messageText = (TextView) convertView.findViewById(R.id.message_txt);
                    holder.messageTime = (TextView) convertView.findViewById(R.id.message_time);
                    holder.sendProgress = (ImageView) convertView.findViewById(R.id.send_progress);
                    holder.profilePhoto = (SimpleDraweeView) convertView.findViewById(R.id.profile_photo);
                    holder.messageImage = (SimpleDraweeView) convertView.findViewById(R.id.message_image);

                    if(mUserTo.getPhotoUrl()!= null) {

                        // Load profile image
                        Uri uriProfle = Uri.parse((mUserTo.getPhotoUrl()));


                        ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                                .setProgressiveRenderingEnabled(true)
                                .setLocalThumbnailPreviewsEnabled(true)
                                .build();

                        AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                                .setImageRequest(requestProfle)
                                .build();

                        holder.profilePhoto.setController(newControllerProfle);


                    } // Load profile image

                    else {


                        Uri uriProfle2 = Uri.parse(String.valueOf((R.drawable.profile_default_photo)));

                        ImageRequest requestProfle2 = ImageRequestBuilder.newBuilderWithSource(uriProfle2)
                                .setProgressiveRenderingEnabled(true)
                                .setLocalThumbnailPreviewsEnabled(true)
                                .build();

                        AbstractDraweeController newControllerProfle2 = Fresco.newDraweeControllerBuilder()
                                .setImageRequest(requestProfle2)
                                .build();

                        holder.profilePhoto.setController(newControllerProfle2);

                    }
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.messageImage.setOnClickListener(new OnImageClickListener(position));


        // Seen

            if (objects.get(position).getUserFrom().equals(mUserFrom) &&  (objects.get(position).getRead().equals("yes"))){

                //if (objects.get(position).getRead().equals("yes")) {

                holder.sendProgress.setImageResource(R.drawable.message_seen);
           // }

        }

        // Not send yet
        if (objects.get(position).isDraft()) {

            holder.messageText.setText(objects.get(position).getText());
            holder.messageText.setTypeface(null, Typeface.ITALIC);
        } else {

            holder.messageText.setText(objects.get(position).getText());
            holder.messageText.setTypeface(null, Typeface.NORMAL);
        }

        // Send

        if(objects.get(position).getCreatedAt() == null){
            holder.sendProgress.setImageResource(R.drawable.message_sending);
            holder.messageTime.setVisibility(View.GONE);

        } else {

            if (objects.get(position).getUserFrom().equals(mUserFrom)){

                holder.sendProgress.setImageResource(R.drawable.message_send);
            }

            holder.messageTime.setVisibility(View.VISIBLE);
            holder.messageTime.setText(objects.get(position).getTime());
        }


        if(objects.get(position).getMessageImageUrl().isEmpty()){


            holder.messageImage.setVisibility(View.GONE);


        } else {

            holder.messageImage.setVisibility(View.VISIBLE);

            Uri uriProfle3 = Uri.parse(objects.get(position).getMessageImageUrl());

            ImageRequest requestProfle3 = ImageRequestBuilder.newBuilderWithSource(uriProfle3)
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerProfle3 = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestProfle3)
                    .build();

            holder.messageImage.setController(newControllerProfle3);


        }



        return convertView;
    }

    private class OnImageClickListener implements View.OnClickListener{

        int position;

        public OnImageClickListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            messageImageListener.onImageClickListener(position, v);
        }
    }

    public interface MessageImageListener{
        void onImageClickListener(int position, View view);
    }

    private class ViewHolder {
        ImageView sendProgress;
        SimpleDraweeView profilePhoto;
        SimpleDraweeView messageImage;
        TextView messageText;
        TextView messageTime;
    }
}
