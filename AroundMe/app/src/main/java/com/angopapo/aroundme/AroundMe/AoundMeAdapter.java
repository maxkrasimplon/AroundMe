package com.angopapo.aroundme.AroundMe;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by Angopapo, LDA on 24.11.16.
 */
  public class AoundMeAdapter extends ParseQueryAdapter<User> {
    private User mCurrentUser;
    public static final int TYPE_MALE = 0;
    public static final int TYPE_FEMALE = 1;
    public static final int TYPE_BOTH = 2;

    public AoundMeAdapter(Context context, final int type) {
        super(context, new QueryFactory<User>() {
            @Override
            public ParseQuery<User> create() {
                User currentUser = (User)User.getCurrentUser();
                ParseQuery<User> query = User.getUserQuery();
                query.whereNotEqualTo(User.COL_PRIVATE_ACTIVE, "true");
                query.whereWithinKilometers(User.COL_GEO_POINT, currentUser.getGeoPoint(), 100);
                query.whereExists(User.COL_GEO_POINT);
                query.fromLocalDatastore();
                switch (type){
                    case TYPE_MALE:
                        query.whereEqualTo(User.COL_IS_MALE, "true");
                        break;
                    case TYPE_FEMALE:
                        query.whereEqualTo(User.COL_IS_MALE, "false");
                        break;
                    case TYPE_BOTH:
                        break;
                }
                
                query.whereNotEqualTo(User.COL_ID, currentUser.getObjectId());
                return query;
            }
        });


        mCurrentUser = (User)User.getCurrentUser();
    }

    @Override
    public View getNextPageView(View v, ViewGroup parent) {
        if(v == null){
            v = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_load_more, null);
            v.measure(v.getMeasuredWidth(), v.getMeasuredWidth());
        }
        return v;
    }

    @Override
    public View getItemView(User object, View v, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(v == null){
            v = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.users_new_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userPhoto = (SimpleDraweeView)v.findViewById(R.id.user_photo);
            viewHolder.username = (TextView) v.findViewById(R.id.username);
            viewHolder.age = (TextView) v.findViewById(R.id.user_age);
            viewHolder.onlineStatusImage = (ImageView) v.findViewById(R.id.image_online_status);
            viewHolder.distance = (TextView)v.findViewById(R.id.text_distance);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)v.getTag();
        }



        // Load profile photos
        Uri uriPhoto = Uri.parse(object.getPhotoUrl());

        ImageRequest requestPhoto = ImageRequestBuilder.newBuilderWithSource(uriPhoto)
                .setResizeOptions(new ResizeOptions(150, 150))
                .setProgressiveRenderingEnabled(true)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();

        AbstractDraweeController newControllerPhoto = Fresco.newDraweeControllerBuilder()
                .setImageRequest(requestPhoto)
                .build();

        viewHolder.userPhoto.setController(newControllerPhoto);


        viewHolder.username.setText(object.getNickname());

        if (object.getAge() > 0) {
            viewHolder.age.setText(object.getAge().toString());

        } else viewHolder.age.setText("18+");

        viewHolder.distance.setText(String.format("%.2f km",object.getGeoPoint().distanceInKilometersTo(mCurrentUser.getGeoPoint())));


        // Online Sysytem in realtime

        if (object.getLastActive() != null){

            if (object.getOnlineTime().equals("Online")){

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else if (object.getOnlineTime().equals("1 min ago")){

                viewHolder.onlineStatusImage.setImageResource(R.drawable.last_min);

            } else viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_offline_15_0_alizarin);


        } else {


            if (object.getOnlineStatus().equals("online")) {

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_online_15_0_alizarin);

            } else {

                viewHolder.onlineStatusImage.setImageResource(R.drawable.ic_offline_15_0_alizarin);
            }

        }

        return v;
    }

    private class ViewHolder{
        SimpleDraweeView userPhoto;
        ImageView onlineStatusImage;
        TextView username;
        TextView age;
        TextView distance;
    }
}
