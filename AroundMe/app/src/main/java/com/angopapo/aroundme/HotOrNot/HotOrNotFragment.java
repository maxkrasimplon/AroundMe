package com.angopapo.aroundme.HotOrNot;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.angopapo.aroundme.Imagehelper.ImageViewerActivity;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

;

/**
 * Created by MSinga Pro on 31.08.15.
 */
public class HotOrNotFragment extends Fragment {

    public static String ARG_USERNAME = "username";
    public static String ARG_DESCRIPTION = "description";
    public static String ARG_AGE = "age";
    public static String ARG_GENDER = "gender";
    public static String ARG_PROFILE_PHOTO_URL = "profile_photo_url";
    public static String ARG_DISTANCE = "distance";
    public static String ARG_CITY = "city";


    TextView mUsernameText, mDescriptionText, mAgeText, mGenderText, mDistanceText, mCity, mSatus, mUniversity;
    SimpleDraweeView mProfilePhotoImage;

    Bundle mArguments;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArguments = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hot_user, container, false);
        mUsernameText = (TextView) v.findViewById(R.id.text_username);
        mDescriptionText = (TextView) v.findViewById(R.id.text_description);
        mAgeText = (TextView) v.findViewById(R.id.text_age);
        mGenderText = (TextView) v.findViewById(R.id.text_gender);
        mProfilePhotoImage = (SimpleDraweeView) v.findViewById(R.id.image_profile_photo);
        mDistanceText = (TextView) v.findViewById(R.id.text_distance);

        if(mArguments != null){
            mUsernameText.setText(mArguments.getString(ARG_USERNAME));
            mDescriptionText.setText(mArguments.getString(ARG_DESCRIPTION));
            mAgeText.setText(mArguments.getString(ARG_AGE) + getString(R.string.Match_years));

            mGenderText.setText(mArguments.getString(ARG_GENDER));

            if("female".equals(mArguments.getString(ARG_GENDER))){
                mGenderText.setText(R.string.profile_femele);
            }
            if("male".equals(mArguments.getString(ARG_GENDER))){
                mGenderText.setText(R.string.profile_male);
            }
            if("no".equals(mArguments.getString(ARG_GENDER))){
                mGenderText.setText(R.string.profile_not_defined );
            }


            // Load profile image
            Uri uriProfle = Uri.parse(mArguments.getString(ARG_PROFILE_PHOTO_URL));

            ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestProfle)
                    .build();

            mProfilePhotoImage.setController(newControllerProfle);

            mDistanceText.setText(mArguments.getString(ARG_DISTANCE));

            mProfilePhotoImage.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {

                    Intent imageViewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
                    imageViewerIntent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mArguments.getString(ARG_PROFILE_PHOTO_URL));
                    getActivity().startActivity(imageViewerIntent);

                }
            });
        }
        return v;
    }
}
