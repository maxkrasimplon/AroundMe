package com.angopapo.aroundme.MyVisitores;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Angopapo, LDA on 14.09.16.
 */
public class MyVisitorsAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private List<User> mObjects;
    private HashMap<String, String> mLastSeeingDateHashMap;

    public MyVisitorsAdapter(Context context, List<User> objects, HashMap<String, String> lastSeeingDateHashMap) {
        super(context, R.layout.item_who_see_users, objects);
        mContext = context;
        mObjects = objects;
        mLastSeeingDateHashMap = lastSeeingDateHashMap;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_who_see_users, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userPhoto = (SimpleDraweeView)convertView.findViewById(R.id.user_photo);
            viewHolder.username = (TextView) convertView.findViewById(R.id.username);
            viewHolder.lastSeeingDateText = (TextView) convertView.findViewById(R.id.text_last_seeing_date);
            viewHolder.onlineStatusImage = (ImageView) convertView.findViewById(R.id.image_online_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }


        if(mObjects.get(position).getPhotoUrl().isEmpty()){


            viewHolder.userPhoto.setImageResource(R.drawable.profile_default_photo);

        } else {

            // Load profile photos
            Uri uriPhoto = Uri.parse(mObjects.get(position).getPhotoUrl());

            ImageRequest requestPhoto = ImageRequestBuilder.newBuilderWithSource(uriPhoto)
                    .setResizeOptions(new ResizeOptions(150, 150))
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerPhoto = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestPhoto)
                    .build();

            viewHolder.userPhoto.setController(newControllerPhoto);

        }
        viewHolder.username.setText(mObjects.get(position).getNickname());
        Log.d("myapp", String.format("hashMapSize: %d, lastDate: %s", mLastSeeingDateHashMap.size(), mLastSeeingDateHashMap.get(mObjects.get(position).getObjectId())));
        viewHolder.lastSeeingDateText.setText(mLastSeeingDateHashMap.get(mObjects.get(position).getObjectId()));

        // Online Sysytem in realtime

        if (mObjects.get(position).getLastActive() != null){

            if (mObjects.get(position).getOnlineTime().equals("Online")){

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else if (mObjects.get(position).getOnlineTime().equals("1 min ago")){

                viewHolder.onlineStatusImage.setImageResource(R.drawable.last_min);

            } else viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_offline_15_0_alizarin);


        } else {


            if (mObjects.get(position).getOnlineStatus().equals("online")) {

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else {

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_offline_15_0_alizarin);
            }

        }
        return convertView;
    }

    private class ViewHolder{
        SimpleDraweeView userPhoto;
        TextView username;
        TextView lastSeeingDateText;
        ImageView onlineStatusImage;
    }
}
