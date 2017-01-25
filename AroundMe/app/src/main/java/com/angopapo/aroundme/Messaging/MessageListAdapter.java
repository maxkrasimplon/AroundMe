package com.angopapo.aroundme.Messaging;

import android.content.Context;
import android.net.Uri;
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
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

;
;

/**
 * Created by Angopapo, LDA on 25.08.16.
 */
public class MessageListAdapter extends ArrayAdapter<User> {
    Context mContext;
    List<User> mObjects;
    String mUserFromId;
    private User userTo, userFrom;

    public MessageListAdapter(Context context, List<User> objects, String userFromId){
        super(context, R.layout.message_list_item, objects);
        mObjects = objects;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_list_item, parent, false);
            holder = new ViewHolder();
            holder.messageText = (TextView) convertView.findViewById(R.id.message_txt);
            holder.messageTime = (TextView) convertView.findViewById(R.id.time);
            holder.usernameText = (TextView) convertView.findViewById(R.id.username);
            //holder.nofifcount = (Button) convertView.findViewById(R.id.notif_count);
            holder.profilePhoto = (SimpleDraweeView) convertView.findViewById(R.id.profile_photo);
            holder.onlineSatus = (ImageView) convertView.findViewById(R.id.image_online_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }



        User currentUser = mObjects.get(position);


        if(!currentUser.getPhotoUrl().isEmpty()) {


            // Load profile image
            Uri uriProfle = Uri.parse(currentUser.getPhotoUrl());


            ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                    .setResizeOptions(new ResizeOptions(55, 55))
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestProfle)
                    .build();

            holder.profilePhoto.setController(newControllerProfle);



        } else holder.profilePhoto.setImageURI(Uri.parse(String.valueOf(R.drawable.profile_default_photo)));


        userFrom = (User)User.getCurrentUser();

        holder.usernameText.setText(currentUser.getNickname());

        ParseQuery<AroundMeMessage> getLastOutMessageQuery = AroundMeMessage.getParseMessageQuery();
        getLastOutMessageQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, userFrom);
        getLastOutMessageQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, currentUser);


        ParseQuery<AroundMeMessage> getLastInMessageQuery = AroundMeMessage.getParseMessageQuery();
        getLastInMessageQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, userFrom );
        getLastInMessageQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, currentUser );

        List<ParseQuery<AroundMeMessage>> getLastMessageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
        getLastMessageQueries.add(getLastOutMessageQuery);
        getLastMessageQueries.add(getLastInMessageQuery);

        ParseQuery<AroundMeMessage> getLastMessageQuery = ParseQuery.or(getLastMessageQueries);
        final ViewHolder finalHolder = holder;
        getLastMessageQuery.fromLocalDatastore();
        getLastMessageQuery.orderByDescending(AroundMeMessage.COL_CREATED_AT);

        getLastMessageQuery.getFirstInBackground(new GetCallback<AroundMeMessage>() {
            @Override
            final public void done(AroundMeMessage aroundMeMessage, ParseException e) {

                if (!aroundMeMessage.getText().isEmpty()){
                    finalHolder.messageText.setText(aroundMeMessage.getText());

                } else {

                    finalHolder.messageText.setText(R.string.imame_mesage);
                }


                finalHolder.messageTime.setText(aroundMeMessage.getTime());


            }
        });

        // Online Sysytem in realtime

        if (currentUser.getLastActive() != null){

            if (currentUser.getOnlineTime().equals("Online")){

                finalHolder.onlineSatus.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else if (currentUser.getOnlineTime().equals("1 min ago")){

                finalHolder.onlineSatus.setImageResource(R.drawable.last_min);

            } else finalHolder.onlineSatus.setImageResource(R.drawable.ic_offline_15_0_alizarin);


        } else {


            if (currentUser.getOnlineStatus().equals("online")) {

                finalHolder.onlineSatus.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else {

                finalHolder.onlineSatus.setImageResource(R.drawable.ic_offline_15_0_alizarin);
            }

        }



        return convertView;
    }

    private class ViewHolder {
        SimpleDraweeView profilePhoto;
        TextView usernameText;
        TextView messageTime;
        TextView messageText;
        ImageView onlineSatus;
        //Button nofifcount;
    }
}
