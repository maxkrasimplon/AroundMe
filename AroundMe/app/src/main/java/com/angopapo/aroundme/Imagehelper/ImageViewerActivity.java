package com.angopapo.aroundme.Imagehelper;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.angopapo.aroundme.R;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class ImageViewerActivity extends AppCompatActivity {

    public static String EXTRA_IMAGE_URL = "image_url";

    SimpleDraweeView mImage;
    ProgressBar progressBar;
    Button mButtonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        mImage = (SimpleDraweeView)findViewById(R.id.image);
        mButtonClose = (Button) findViewById(R.id.image_close);

        String imageUrl = "";
        if(getIntent() != null){
            imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        }

        progressBar.setVisibility(View.VISIBLE);


        ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(


                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {

                progressBar.setVisibility(View.GONE);
                if (imageInfo == null) {

                    progressBar.setVisibility(View.GONE);
                    return;
                }
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received! " + "Size %d x %d", "Quality level %d, good enough: %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                //FLog.d("Intermediate image received");

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                progressBar.setVisibility(View.GONE);
                mImage.setImageURI(String.valueOf(R.drawable.profile_default_photo));
            }
        };

        // Load images
        Uri uriProfle = Uri.parse(imageUrl);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                .setProgressiveRenderingEnabled(true)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setControllerListener(controllerListener)
                .build();
        mImage.setController(controller);



        mButtonClose.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                ImageViewerActivity.super.onBackPressed();

            }
        });
    }

}
