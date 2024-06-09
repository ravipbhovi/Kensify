package com.kensify.kpsc.activity;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

import static com.kensify.kpsc.utils.Ads.BANNER_1;
import static com.kensify.kpsc.utils.Ads.INTER_1;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kensify.kpsc.R;
import com.kensify.kpsc.adapter.CompetitiveExamsAdapter;
import com.kensify.kpsc.adapter.GeneralQuizAdapter;
import com.kensify.kpsc.adapter.RecentlyPlayedAdapter;
import com.kensify.kpsc.adapter.RecommendedAdapter;
import com.kensify.kpsc.adapter.ScoreAdapter;
import com.kensify.kpsc.model.Field;
import com.kensify.kpsc.model.Question;
import com.kensify.kpsc.model.Topic;
import com.kensify.kpsc.utils.Ads;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AllTopics extends AppCompatActivity{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    Toolbar toolbar;
    LinearLayout homePageToolbar;
    AppBarLayout appBarLayout;
    SpinKitView progressBar;
    ProgressBar  progressBar1;
    private ScoreAdapter.RecyclerViewClickListener listener;
    private String username;
    InterstitialAd mInterstitialAd;
    RecommendedAdapter.RecyclerViewClickListener recom_listener;
    //SwipeRefreshLayout swipeRefreshLayout;
    /*  String booster_date = null;*/
    private ImageView booster_image;
    CardView daily_booster_card;
    TextView daily_booster_current_date;
    RecentlyPlayedAdapter.PlayedClickListener recommended_listener;
    CompetitiveExamsAdapter.PlayedClickListener played_listener;
    LinearLayout most_played_layout;
    TextView user_points;
    RecommendedAdapter.RecyclerViewClickListener sub_listener;
    GeneralQuizAdapter.PlayedClickListener general_listener;
    ProgressBar db_progressBar;
    AlertDialog alert;
    Date d = new Date();
    CharSequence playedDate1  = DateFormat.format("MMMM dd, yyyy", d.getTime());
    String current_date = playedDate1.toString();
    private TextView question, timer;
    private LinearLayout option;
    private ArrayList<Question> list;
    private CardView optionA, optionB, optionC, optionD;
    private TextView optionAtext, optionBtext, optionCtext, optionDtext;
    private int questionNumber = 0, score = 0;
    private CountDownTimer countDown;
    private final Handler handler = new Handler();
    private String activityStatus = "Running";
    private long userPoints = 0;
    Vibrator vibrator;
    ImageView imageView;
    int selectedOption = 0;
    LinearLayout booster_question_layout;
    private int timePerQuestion = 0;
    private int pointsPerQuestion = 0;
    LinearLayout home_page_layout;
    LinearLayout success_quiz_plate, failure_quiz_plate;
    RecyclerView story_recyclerview, most_played_recyclerview;
    //LinearLayout profile_holder_layout;

    double average_points = 0.0;
    CardView quiz_statistics_Card;
    TextView announcement_Tag;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
    TextView user_quizzes, user_pr, user_attempted_percentage, user_accuracy;
    //ImageView user_photo;
    TextView user_fullname, user_name_text, main_toolbar_title, homePageTitle;
    LinearLayout search_card, leaderboard_card, notification_card, account_card, loadingLL, offlineLL;
    MediaPlayer mediaPlayer;
    /*TextView today_date;*/

    private Ads ads;

    public static ArrayList<Question> questions = new ArrayList<>();
    public static ArrayList<Topic> topics = new ArrayList<>();
    public ArrayList<Field> fields = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        findViewByIDs();
        setActivityWindow();

        if(!isConnectedToInternet(AllTopics.this)){
            offlineLL.setVisibility(View.VISIBLE);
            loadingLL.setVisibility(View.GONE);
        }else{
            offlineLL.setVisibility(View.GONE);
            loadingLL.setVisibility(View.VISIBLE);
        }


        getSupportActionBar().setTitle("Subjects");

        myRef.child("Active Users").removeValue();

        myRef.child("App Status Updates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String getToolbarTitle = snapshot.child("mainToolbarTitle").getValue(String.class);
                final String getAnnouncementLine = snapshot.child("announcementLine").getValue(String.class);

                announcement_Tag.setText(getAnnouncementLine);
                announcement_Tag.setSelected(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        LinearLayout s = (LinearLayout) findViewById(R.id.banner_AdView);
        ads = new Ads();
        ads.BannerAd(AllTopics.this,s, BANNER_1);
        ads.InterstitialLoad(AllTopics.this, INTER_1);
        refreshOverall();

    }



    private void setOnClickListener() {

        recom_listener = new RecommendedAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position, String topicName, String topicPicture, ArrayList<String> quizModels, String pin) {

                Intent intent = new Intent(AllTopics.this, TopicProfile.class);
                intent.putExtra("PIN", pin);
                startActivity(intent);

                ads.showInterstitialAd(AllTopics.this);

            }
        };

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.search_home:
                Intent intent = new Intent(AllTopics.this, Search.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
                return true;

            case R.id.notifications:
                Intent intent1 = new Intent(AllTopics.this, Notifications.class);
                intent1.putExtra("USERNAME", username);
                startActivity(intent1);
                return true;

            case R.id.leaderboard:
                Intent intent3 = new Intent(AllTopics.this,Leaderboard.class);
                intent3.putExtra("USERNAME", username);
                startActivity(intent3);
                return true;
            case R.id.profile:
                Intent intent4 = new Intent(AllTopics.this,UserProfile.class);
                intent4.putExtra("USERNAME", username);
                startActivity(intent4);
                return true;
            case R.id.briefs:
                Intent intent5 = new Intent(AllTopics.this,BrowseBriefs.class);
                intent5.putExtra("USERNAME", username);
                startActivity(intent5);
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:

                break;
        }

        return true;
    }


    private void refreshOverall(){

        leaderboard_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(AllTopics.this, Leaderboard.class);
                intent3.putExtra("USERNAME", username);
                startActivity(intent3);
            }
        });

        search_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllTopics.this, Search.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        notification_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(AllTopics.this, Notifications.class);
                intent1.putExtra("USERNAME", username);
                startActivity(intent1);
            }
        });


        quiz_statistics_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllTopics.this, Statistics.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });


        myRef.child("Fields").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot2: snapshot.getChildren()){
                    Field field = snapshot2.getValue(Field.class);
                    fields.add(field);
                }

                loadingLL.setVisibility(View.GONE);
                offlineLL.setVisibility(View.GONE);
                setOnClickListener();
                RecommendedAdapter recommendedAdapter = new RecommendedAdapter(fields, recom_listener);
                story_recyclerview.setLayoutManager((new GridLayoutManager(AllTopics.this,1, GridLayoutManager.VERTICAL, false)));
                story_recyclerview.setAdapter(recommendedAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void findViewByIDs(){
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar_layout);
        //swipeRefreshLayout = findViewById(R.id.home_page_Refresh);
        home_page_layout = findViewById(R.id.home_page_layout);
        progressBar = findViewById(R.id.home_page_progress_bar);
        daily_booster_card = findViewById(R.id.daily_booster_card);
        booster_image = findViewById(R.id.booster_image);
        daily_booster_current_date = findViewById(R.id.daily_booster_current_date);
        user_points = findViewById(R.id.current_user_points);
        user_quizzes = findViewById(R.id.current_user_quizzes_count);
        user_pr = findViewById(R.id.current_user_quiz_point_rate);
        user_attempted_percentage = findViewById(R.id.current_user_quiz_attempted_percentage);
        user_accuracy = findViewById(R.id.current_user_quiz_accuracy_percentage);
        quiz_statistics_Card = findViewById(R.id.user_points_statisticsCard);
        most_played_recyclerview = findViewById(R.id.user_most_played_recycler_view);
        story_recyclerview = findViewById(R.id.select_topics_Recyclerview);
        //user_photo = findViewById(R.id.current_user_profile_picture);
        user_fullname = findViewById(R.id.current_user_fullname);
        user_name_text = findViewById(R.id.current_user_name);
        // profile_holder_layout = findViewById(R.id.profile_holder_layout);
        most_played_layout = findViewById(R.id.most_played_layout);
        main_toolbar_title = findViewById(R.id.main_toolbar_title);
        imageView = findViewById(R.id.user_account_pic);
        search_card = findViewById(R.id.search_card);
        leaderboard_card = findViewById(R.id.leaderboard_card);
        notification_card = findViewById(R.id.notification_card);
        account_card = findViewById(R.id.user_account_card);
        homePageToolbar = findViewById(R.id.homePageToolbar);
        homePageTitle = findViewById(R.id.home_title);
        loadingLL = findViewById(R.id.loading_home_page_ll);
        //today_date = findViewById(R.id.today_date);
        announcement_Tag = findViewById(R.id.announcement_tag);
        offlineLL = findViewById(R.id.no_internet_connection_ll);
    }

    private void setActivityWindow(){
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(appBarLayout, "elevation", 0.1f));
            appBarLayout.setStateListAnimator(stateListAnimator);
        }

        getSupportActionBar().setTitle("Hello there!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
        }

        toolbar.setTitleTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
        toolbar.getOverflowIcon().setColorFilter(Color.BLACK , PorterDuff.Mode.SRC_ATOP);

        /*if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_black_24);
        }*/

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white2)));
    }

    private void createNotification(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("Kensify Thanks", "Kensify Thanks", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(AllTopics.this, "Kensify Thanks");
        builder.setContentTitle("Kensify Rankers");
        builder.setContentText("\nHurry up! Let's play and earn points. Everyone is getting ahead of you. Let's check out your competitors.");
        builder.setSmallIcon(R.drawable.new_logo_cti);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AllTopics.this);


        Intent resultIntent = new Intent(this, Leaderboard.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.putExtra("USERNAME", username);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManagerCompat.notify(1, builder.build());
    }


    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            return true;
        }else{
            return false;
        }
    }


}