package com.angopapo.aroundme.Aplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.Authetication.LoginActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.HotOrNot.HotOrNotActivity;
import com.angopapo.aroundme.Messaging.MessageListActivity;
import com.angopapo.aroundme.MyVisitores.MyVisitorsActivity;
import com.angopapo.aroundme.MyVisitores.VisitorActivity;
import com.angopapo.aroundme.Passport.MapsActivity;
import com.angopapo.aroundme.Passport.TravelActivity;
import com.angopapo.aroundme.PrivateProfile.PrivateProfileActivity;
import com.angopapo.aroundme.Profile.MyProfile;
import com.angopapo.aroundme.R;
import com.angopapo.aroundme.Settings.SettingsActivity;
import com.angopapo.aroundme.VipAccount.VipActivationActivity;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.view.BezelImageView;
import com.squareup.picasso.Picasso;


/**
 * Created by Angopapo, LDA on 07.09.15.
 */
public class NavigationDrawer extends BaseActivity{


    public static Drawer createDrawer(final ActivityWithToolbar activity){

        User currentUser = (User) User.getCurrentUser();

        if (currentUser == null){

            Intent intent = new Intent();
            intent.setClass((Context) activity, LoginActivity.class);
            activity.startActivity(intent);

        }

        // Here is where we are showing the user credits in header of Drawer

        ProfileDrawerItem profileDrawerItem = null;
        if (currentUser != null) {
            if (currentUser.getCredits() != null) {
                profileDrawerItem = new ProfileDrawerItem().withEmail(currentUser.getCredits().toString() + " " + "Credits" ).withName(currentUser.getNickname());
            } else profileDrawerItem = new ProfileDrawerItem().withEmail("0,00" + " " + "Credits" ).withName(currentUser.getNickname());
        }

        // Drawer features settings

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(activity.getActivity())
                .withHeaderBackground(R.color.alizarin)
                .addProfiles(profileDrawerItem)
                .withDividerBelowHeader(true)
                .withProfileImagesClickable(true)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        Intent intent = new Intent();
                        intent.setClass((Context) activity, MyProfile.class);
                        activity.startActivity(intent);
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withAlternativeProfileHeaderSwitching(false)
                .build();

        // Here is where we are showing the cover picture in header of Drawer

        if (currentUser != null) {
            if (currentUser.getCoverUrl().isEmpty()) {

                ImageView ConverPhotoThumb = (ImageView) accountHeader.getView().findViewById(R.id.material_drawer_account_header_background);


                Picasso.with(activity.getActivity()).load(R.drawable.profile_default_cover).into(ConverPhotoThumb);

            } else

            {

                ImageView ConverPhotoThumb = (ImageView) accountHeader.getView().findViewById(R.id.material_drawer_account_header_background);


                Picasso.with(activity.getActivity()).load(currentUser.getCoverUrl()).centerCrop().resize(250, 148).placeholder(R.drawable.profile_default_cover).error(R.drawable.profile_default_cover).into(ConverPhotoThumb);

            }
        }

        // Here is where we are showing the profile picture in header of Drawer

        if (currentUser.getPhotoUrl().isEmpty()){

            String photo = String.valueOf(R.drawable.profile_default_photo);

        } else
        {
            currentUser.getPhotoUrl();
        }

        BezelImageView profilePhotoThumb = (BezelImageView) accountHeader.getView().findViewById(R.id.material_drawer_account_header_current);
        if (!User.getUser().getPhotoUrl().isEmpty()) {
            Picasso.with(activity.getActivity())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.profile_default_photo)
                    .error(R.drawable.profile_default_photo)
                    .into(profilePhotoThumb);
        } else if (User.getUser().getPhotoUrl().isEmpty()) {
            Picasso.with(activity.getActivity())
                    .load(R.drawable.profile_default_photo)
                    .placeholder(R.drawable.profile_default_photo)
                    .error(R.drawable.profile_default_photo)
                    .into(profilePhotoThumb);
        }

        Drawer drawer = new DrawerBuilder()
                .withActivity(activity.getActivity())
                .withToolbar(activity.getToolbar())
                .withAccountHeader(accountHeader)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)

                // Menu drawer items scrollable
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_around_me).withIcon(R.drawable.menu_around).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_hot_or_not).withIcon(R.drawable.menu_hot).withIdentifier(2),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_messaging).withIcon(R.drawable.menu_messaging).withIdentifier(3),


                        new SectionDrawerItem().withName(R.string.vip_features_titlte),

                        new PrimaryDrawerItem().withName(R.string.drawer_item_visitors).withIcon(R.drawable.menu_visitor).withIdentifier(4),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_passport).withIcon(R.drawable.menu_travel).withIdentifier(5),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_private).withIcon(R.drawable.private_prifile).withIdentifier(6)

                )


                // Menu drawer items Fixed
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.drawer_item_profile).withIcon(R.drawable.menu_profile).withIdentifier(7),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(R.drawable.menu_settingss).withIdentifier(8)
                )
                .build();
        drawer.setSelection(activity.getDriwerId());
        drawer.setOnDrawerItemClickListener(new NavigationDrawerItemClickListener(activity.getActivity()));
        return drawer;


    }

    private static class NavigationDrawerItemClickListener implements Drawer.OnDrawerItemClickListener{
        private Activity mActivity;

        User currentUser = (User)User.getCurrentUser();



        NavigationDrawerItemClickListener(Activity activity){
            mActivity = activity;
        }


        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

            if (drawerItem != null) {
                //Intent intent = null;
                if (drawerItem.getIdentifier() == 1) {
                    Intent userNearMeIntent = new Intent(mActivity, AroundMeActivity.class);
                    mActivity.startActivity(userNearMeIntent);
                } else if (drawerItem.getIdentifier() == 2) {
                    Intent startMatchIntent = new Intent(mActivity, HotOrNotActivity.class);
                    mActivity.startActivity(startMatchIntent);
                    mActivity.finish();
                } else if (drawerItem.getIdentifier() == 3) {
                    Intent messageListIntent = new Intent(mActivity, MessageListActivity.class);
                    mActivity.startActivity(messageListIntent);
                }  else if (drawerItem.getIdentifier() == 4) {
                    if(currentUser != null) {
                        if (currentUser.isVip()) {

                            Intent mapslIntent = new Intent(mActivity, MyVisitorsActivity.class);
                            mActivity.startActivity(mapslIntent);
                        }

                        else if (currentUser.isVisitor()) {

                            Intent mapslIntent = new Intent(mActivity, MyVisitorsActivity.class);
                            mActivity.startActivity(mapslIntent);

                        } else {

                            Intent travelIntent = new Intent(mActivity, VisitorActivity.class);
                            mActivity.startActivity(travelIntent);
                        }

                    }

                } else if (drawerItem.getIdentifier() == 5) {
                    if(currentUser != null) {
                        if (currentUser.isVip()) {

                            Intent WhoseeIntent = new Intent(mActivity, MapsActivity.class);
                            mActivity.startActivity(WhoseeIntent);
                        }

                        else if (currentUser.isTravel()) {

                            Intent WhoseeIntent = new Intent(mActivity, MapsActivity.class);
                            mActivity.startActivity(WhoseeIntent);

                        } else {

                            Intent visitorIntent = new Intent(mActivity, TravelActivity.class);
                            mActivity.startActivity(visitorIntent);
                        }

                    }
                }

                 else if (drawerItem.getIdentifier() == 6) {
                    Intent PrivateIntent = new Intent(mActivity, PrivateProfileActivity.class);
                    mActivity.startActivity(PrivateIntent);
                }else if (drawerItem.getIdentifier() == 7) {
                    Intent profileIntent = new Intent(mActivity, MyProfile.class);
                    mActivity.startActivity(profileIntent);
                } else if (drawerItem.getIdentifier() == 8) {
                    Intent settingsIntent = new Intent(mActivity, SettingsActivity.class);
                    mActivity.startActivity(settingsIntent);
                }
            }

            return false;
        }
    }

}
