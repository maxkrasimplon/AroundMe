package com.angopapo.aroundme.Profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ActivityProfileEdit extends AppCompatActivity {

    public static String TAG = "pef";
    public static String ARG_AGE = "age";

    private User mCurrentUser;

    Intent mLoginIntent;


    private EditText mAbout;
    private ImageButton back, done;

    private TextView mContry, mBirth, mSexuality, mStatus, mOrientation, mGender;

    private MyProfile mMyProfile;

    LinearLayout mUpdateLayout, mAgeEdit ;

    private Dialog progressDialog;

    private int year;
    private int month;
    private int day;


    @Bind(R.id.name) EditText mNicknameEdit;
    @Bind(R.id.living) EditText mLiveing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);

        mLoginIntent = this.getIntent();

        back = (ImageButton) findViewById(R.id.back_image);
        done = (ImageButton) findViewById(R.id.done_button);
        mAgeEdit = (LinearLayout) findViewById(R.id.birth);
        mAbout = (EditText) findViewById(R.id.desc);
        mContry = (TextView) findViewById(R.id.country);
        mGender = (TextView) findViewById(R.id.gender);
        mSexuality = (TextView) findViewById(R.id.sexuality);
        mStatus = (TextView) findViewById(R.id.status);
        mOrientation = (TextView) findViewById(R.id.orientation);
        mBirth = (TextView) findViewById(R.id.birth2);

        mUpdateLayout = (LinearLayout) findViewById(R.id.update_layout);

        mCurrentUser = (User)User.getCurrentUser();

        mBirth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(ActivityProfileEdit.this, mDateSetListener, year, month, day);

                final Calendar calendar = Calendar.getInstance();
                final Calendar calendar2 = Calendar.getInstance();

                calendar2.add(Calendar.YEAR, -18);

                dpd.getDatePicker().setMaxDate(calendar2.getTimeInMillis());

                // Subtract 6 days from Calendar updated date
                //calendar.add(Calendar.DATE, -6);

                calendar.add(Calendar.YEAR, -65);

                // Set the Calendar new date as minimum date of date picker
                dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());


                //dpd.getDatePicker().setMaxDate(new Date().getTime());

                dpd.show();
            }
        });

        int mMonth1 = month + 1;

        if (mCurrentUser.getBirthDate() != null){


            LocalDate birthdate = new LocalDate(mCurrentUser.getBithdate());          //Birth date
            LocalDate now = new LocalDate();                                         //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());


            //Now access the values as below


            System.out.println(period.getDays());
            System.out.println(period.getMonths());
            System.out.println(period.getYears());

            //int ages = period.getYears();
            final Integer ageInt = period.getYears();
            final String ageS = ageInt.toString();

            if(mCurrentUser.getAge() > 0)

            {
                mBirth.setText( mCurrentUser.getBirthDate () + " " + "(" + ageS  + " " + "year old" + ")" );

            } else mBirth.setHint ("How old are you ?");

            mCurrentUser.setAge(Integer.parseInt((ageS)));

        } else mBirth.setHint("How old are you ?");


        //Age edit config
        if (mCurrentUser !=null)

        {


            // Sexuality preferences here is being queried

            if(mCurrentUser.getSexuality() == 1){

                mSexuality.setText(R.string.girls);

            } else if(mCurrentUser.getSexuality() == 2)

            {
                mSexuality.setText(R.string.both);

            }
            else if(mCurrentUser.getSexuality() == 0)

            {
                mSexuality.setText(R.string.mans);

            }
            else {

                mSexuality.setText(R.string.other);

            }

            // Status preferences here is being queried

            if(mCurrentUser.getStatus() == 0){

                mStatus.setText(R.string.married);

            } else if(mCurrentUser.getStatus() == 1)

            {
                mStatus.setText(R.string.dating);

            }
            else if(mCurrentUser.getStatus() == 2)

            {
                mStatus.setText(R.string.sigle);

            }
            else {

                mStatus.setText(R.string.other);

            }

            // Orientation preferences here is being queried

            if(mCurrentUser.getOrientation() == 0){

                mOrientation.setText(R.string.heterosexual);

            } else if(mCurrentUser.getOrientation() == 1)

            {
                mOrientation.setText(R.string.homosexual);

            }
            else if(mCurrentUser.getOrientation() == 2)

            {
                mOrientation.setText(R.string.bisexual);

            }
            else {

                mOrientation.setText(R.string.other);

            }


        }

        //Nickname edit config
        assert mCurrentUser != null;
        mAbout.setText(mCurrentUser.getDescription());
        mNicknameEdit.setText(mCurrentUser.getNickname());
        mLiveing.setText(mCurrentUser.getAtualCity());
        mContry.setText(mCurrentUser.getCountry());

        if(mCurrentUser != null){
            String gender = mCurrentUser.getGenderString();
            if(TextUtils.equals(gender, "male")) mGender.setText(R.string.male);
            else if(TextUtils.equals(gender, "female")) mGender.setText(R.string.female);
            else if(TextUtils.equals(gender, "not_defined")) mGender.setText(R.string.other);

        }



        back.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Intent loginIntent = new Intent(ActivityProfileEdit.this, MyProfile.class);
                ActivityProfileEdit.this.startActivity(loginIntent);
                finish();



            }
        });

        done.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (!validate()) {
                    onSignupFailed();
                    return;
                }

                if (isInternetAvailable()) {

                    done.setEnabled(true);

                    showProgressBar(getString(R.string.update_updating));
                    // Save all changes made

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    if (!TextUtils.isEmpty(mNicknameEdit.getText().toString()) && !TextUtils.equals(mNicknameEdit.getText().toString(), mCurrentUser.getNickname())) {
                        mCurrentUser.setNickname(mNicknameEdit.getText().toString());
                    }


                    String gender = mGender.getText().toString();
                    if(TextUtils.equals(gender, getString(R.string.male))) mCurrentUser.setGenderIsMale(true);
                    else if(TextUtils.equals(gender, getString(R.string.female))) mCurrentUser.setGenderIsMale(false);

                    // here we are going to save our user sexuality

                    String Sexuality = mSexuality.getText().toString();
                    if(TextUtils.equals(Sexuality, getString(R.string.mans))) mCurrentUser.setSexuality(0);
                    else if(TextUtils.equals(Sexuality, getString(R.string.girls))) mCurrentUser.setSexuality(1);
                    else if(TextUtils.equals(Sexuality, getString(R.string.both))) mCurrentUser.setSexuality(2);
                    else mCurrentUser.setSexuality(3);

                    // here we are going to save our user sexuality

                    String Statuss = mStatus.getText().toString();
                    if(TextUtils.equals(Statuss, getString(R.string.married))) mCurrentUser.setStatus(0);
                    else if(TextUtils.equals(Statuss, getString(R.string.dating))) mCurrentUser.setStatus(1);
                    else if(TextUtils.equals(Statuss, getString(R.string.sigle))) mCurrentUser.setStatus(2);
                    else mCurrentUser.setStatus(3);

                    // here we are going to save our user sexuality

                    String Orientations = mOrientation.getText().toString();
                    if(TextUtils.equals(Orientations, getString(R.string.heterosexual))) mCurrentUser.setOrientation(0);
                    else if(TextUtils.equals(Orientations, getString(R.string.homosexual))) mCurrentUser.setOrientation(1);
                    else if(TextUtils.equals(Orientations, getString(R.string.bisexual))) mCurrentUser.setOrientation(2);
                    else mCurrentUser.setOrientation(3);

                    // here we are going to save our user Eye color




                    mCurrentUser.setDescription(mAbout.getText().toString());
                    mCurrentUser.setAtualCity(mLiveing.getText().toString());
                    mCurrentUser.setCountry(mContry.getText().toString());





                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {

                                switch (e.getCode()) {

                                    case ParseException.TIMEOUT:
                                        Log.d("MyApp", "time out");

                                        Snackbar.make(mUpdateLayout, R.string.signup_request_t, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).setActionTextColor(Color.WHITE).show();

                                        dismissProgressBar();

                                        break;

                                    case ParseException.CONNECTION_FAILED:
                                        Log.d("MyApp", "connection failed");

                                        Snackbar.make(mUpdateLayout, R.string.login_cant, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).setActionTextColor(Color.WHITE).show();

                                        dismissProgressBar();

                                        break;
                                    case ParseException.INTERNAL_SERVER_ERROR:
                                        Log.d("MyApp", "internal server error");

                                        Snackbar.make(mUpdateLayout, R.string.login_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).setActionTextColor(Color.WHITE).show();

                                        dismissProgressBar();

                                        break;

                                    case ParseException.OTHER_CAUSE:
                                        Log.d("MyApp", "time out");

                                        Snackbar.make(mUpdateLayout, R.string.signup_request, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).setActionTextColor(Color.WHITE).show();

                                        dismissProgressBar();

                                        break;

                                }

                            } else {

                                dismissProgressBar();

                                Intent loginIntent = new Intent(ActivityProfileEdit.this, MyProfile.class);
                                startActivity(loginIntent);

                            }
                        }
                    });

                }
                else {

                    showInternetConnectionLostMessage();
                }
            }
        });



    }


    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int mYear, int monthOfYear, int dayOfMonth) {

            year = mYear;
            month = monthOfYear;
            day = dayOfMonth;

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            Date BirDate = calendar.getTime();

            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();


            dob.set(year, month, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }

            Integer ageInt = new Integer(age);
            String ageS = ageInt.toString();

            //return ageS;



            //////////////////////////////////

            mCurrentUser.setBirthDay(BirDate);


// Show selected date

            /*Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            Date BirDate = calendar.getTime();



            LocalDate birthdate = new LocalDate (mYear, monthOfYear, dayOfMonth);          //Birth date
            LocalDate now = new LocalDate();                    //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

            System.out.println(period.getDays());
            System.out.println(period.getMonths());
            System.out.println(period.getYears());

            //int ages = period.getYears();
            final Integer ageInt = period.getYears();
            final String ageS = ageInt.toString();*/

            mBirth.setText( new StringBuilder().append(month + 1).append("/").append(day).append("/").append(year).append(" ") + " " + "(" + ageS + " " + "year old" + ")" );

            mCurrentUser.setAge(ageInt);

        }

    };
    //AlertDialog with list view to select eye color
    public void CountryListView(View view){
        final CharSequence[] countries = {"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",

                "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria",

                "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",

                "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana",

                "Brazil", "British Indian Ocean Territory", "British Virgin Islands", "Brunei", "Bulgaria",

                "Burkina Faso", "Burma (Myanmar)", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde",

                "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island",

                "Cocos (Keeling) Islands", "Colombia", "Comoros", "Cook Islands", "Costa Rica",

                "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo",

                "Denmark", "Djibouti", "Dominica", "Dominican Republic",

                "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",

                "Ethiopia", "Falkland Islands", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia",

                "Gabon", "Gambia", "Gaza Strip", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece",

                "Greenland", "Grenada", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",

                "Haiti", "Holy See (Vatican City)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India",

                "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Ivory Coast", "Jamaica",

                "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kosovo", "Kuwait",

                "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",

                "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia",

                "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mayotte", "Mexico",

                "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco",

                "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia",

                "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea",

                "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama",

                "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland",

                "Portugal", "Puerto Rico", "Qatar", "Republic of the Congo", "Romania", "Russia", "Rwanda",

                "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin",

                "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino",

                "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",

                "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea",

                "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland",

                "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tokelau",

                "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands",

                "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "US Virgin Islands", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam",

                "Wallis and Futuna", "West Bank", "Yemen", "Zambia", "Zimbabwe"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileEdit.this);
        builder.setTitle(R.string.choose_country);
        builder.setItems(countries, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int country) {
                mContry.setText(countries[country]);
            }
        }).show();
    }


    //AlertDialog with list view to select hair color
    public void SexualityListView(View view){
        final CharSequence[] sexuality = {getString(R.string.mans), getString(R.string.girls), getString(R.string.both)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileEdit.this);
        builder.setTitle(R.string.looking);
        builder.setItems(sexuality, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int sexualities) {
                mSexuality.setText(sexuality[sexualities]);
            }
        }).show();
    }

    //AlertDialog with list view to select hair color
    public void OrientationListView(View view){
        final CharSequence[] orientation = {getString(R.string.homosexual), getString(R.string.heterosexual), getString(R.string.bisexual)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileEdit.this);
        builder.setTitle(R.string.sexual_ori);
        builder.setItems(orientation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int orien) {
                mOrientation.setText(orientation[orien]);
            }
        }).show();
    }

    //AlertDialog with list view to select hair color
    public void StatusListView(View view){
        final CharSequence[] status = {getString(R.string.sigle), getString(R.string.dating), getString(R.string.married)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileEdit.this);
        builder.setTitle(R.string.stauts_choose);
        builder.setItems(status, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int statu) {
                mStatus.setText(status[statu]);
            }
        }).show();
    }

    public void GenderChooser (View view){


        final CharSequence[] choice = {getString(R.string.male),getString(R.string.female)};

        final int[] from = new int[1];
        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityProfileEdit.this);
        alert.setTitle(R.string.gender_choose);
        alert.setCancelable(false);
        alert.setSingleChoiceItems(choice, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (choice[which] == getString(R.string.male)) {
                    from[0] = 1;
                } else if (choice[which] == getString(R.string.female)) {
                    from[0] = 2;
                }
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (from[0] == 0) {
                    Toast.makeText(ActivityProfileEdit.this, R.string.did_not,
                            Toast.LENGTH_LONG).show();
                } else if (from[0] == 1) {
                    // Your Code
                    mGender.setText(R.string.male);
                } else if (from[0] == 2) {
                    // Your Code
                    mGender.setText(R.string.female);
                }
            }
        });
        alert.show();
    }

    @Override
    public void onBackPressed()
    {

        Intent loginIntent = new Intent(ActivityProfileEdit.this, MyProfile.class);
        ActivityProfileEdit.this.startActivity(loginIntent);
        ActivityProfileEdit.this.finish();
    }

    public void showInternetConnectionLostMessage(){
        Snackbar.make(mUpdateLayout, R.string.login_no_int, Snackbar.LENGTH_SHORT).show();

    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(ActivityProfileEdit.this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.login_missi, Toast.LENGTH_LONG).show();

        done.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNicknameEdit.getText().toString();
        String living = mLiveing.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNicknameEdit.setError(getString(R.string.short_name));
            valid = false;
        } else {
            mNicknameEdit.setError(null);
        }

        if (living.isEmpty() ) {
            mLiveing.setError(getString(R.string.empty_living));
            valid = false;
        } else {
            mLiveing.setError(null);
        }


        return valid;
    }


}



