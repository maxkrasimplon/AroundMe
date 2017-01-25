package com.angopapo.aroundme.ClassHelper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;

/**
 * Created by Angopapo, LDA on 02.09.16.
 */
public class AroundMeUploader extends Activity {
    private static final int UPLOAD_CODE = 1335;
    private static final int REQUEST_EXTERNAL_STORAGE = 19;
    private static final int REQUEST_CAMERA = 20;

    private Activity mActivity;
    private ProgressDialog mProgressDialog;

    public AroundMeUploader(Activity activity){
        mActivity = activity;
    }

    public void SendPhotoDialog (){

        final CharSequence[] items = {"Camera", "Gallery"};

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);

        builder.setTitle("Send picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Camera")) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestCamera();



                    } else {


                        Camera();
                    }



                } else if (items[item].equals("Gallery")) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestStorage();


                    } else {

                        Gallery();
                    }


                }

            }
        });
        builder.show();

    }

    public void openFileSelectDialog(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        mActivity.startActivityForResult(galleryIntent, UPLOAD_CODE);
    }

    /*public Bitmap getBitmap (int requestCode, int resultCode, Intent data){
        if(requestCode == UPLOAD_CODE && resultCode == Activity.RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            // Get the cursor
            Cursor cursor = mActivity.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();


            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
            return bitmap;
        } else {
            return null;
        }
    }*/

    public Uri getImageUri (int requestCode, int resultCode, Intent data){
        if(requestCode == 2 && resultCode == Activity.RESULT_OK){

            Uri selectedImage = data.getData();

            return selectedImage;

        } else if(requestCode == 1 && resultCode == Activity.RESULT_OK){

            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
            try {
                cropCapturedImage(Uri.fromFile(file));
            }
            catch(ActivityNotFoundException aNFE){
                String errorMessage = "Sorry - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }

        }if(requestCode == 3 && resultCode == Activity.RESULT_OK){


            Bundle extras = data.getExtras();

            if (extras != null){

                //Uri selectedImage = extras.getParcelable("data");

                Uri selectedImage = data.getData();
                //profilePic = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);

                // Uri selectedImage = Uri.parse(String.valueOf(extras.getParcelable("data")));

                return selectedImage;

            } return null;

        } else {

            return null;
        }
    }

    public void uploadParseFile(ParseFile file, SaveCallback saveCallback, ProgressCallback progressCallback){
        file.saveInBackground(saveCallback, progressCallback);
    }

    public void Camera(){

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        mActivity.startActivityForResult(intent, 1);

    }

    public void cropCapturedImage(Uri picUri){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        cropIntent.putExtra("return-data", true);
        mActivity.startActivityForResult(cropIntent, 3);
    }

    public void Gallery(){

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        mActivity.startActivityForResult(photoPickerIntent, 2);

    }

    public void PermissionRequestStorage(){

        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(mActivity)
                        .setTitle("Permission Needed")
                        .setMessage("Storage permission is needed to access your gallery to update your profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        } else {

            Gallery();
        }
    }

    public void PermissionRequestCamera(){
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(mActivity)
                        .setTitle("Permission Needed")
                        .setMessage("Camera permission is needed to take picture to update profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);

            }
        } else{

            Camera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Gallery();

                } else {

                    Context context = mActivity;
                    CharSequence text = "You denied the storage permission, We Disabled the function. Grant the permission to use this function !";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

            case REQUEST_CAMERA:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Camera();

                } else {

                    Context context = mActivity;
                    CharSequence text = "You denied the camera permission, We Disabled the function. Grant the permission to use this function !";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
                return;

            }

        }
    }


}
