package com.kensify.kpsc.activity;

import static com.kensify.kpsc.utils.Ads.BANNER_3;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kensify.kpsc.R;
import com.kensify.kpsc.fragment.QuizFragment;
import com.kensify.kpsc.interfaces.QuizPlatformBack;
import com.kensify.kpsc.model.Topic;
import com.kensify.kpsc.utils.Ads;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    private String username, activityStatus = "Running", pin;
    CardView start_quiz_button;
    TextView counter, start_text;
    TextView basic_topic, basic_total_questions, basic_time_per_question, basic_points_per_question;
    ImageView basic_topic_coverPicture;
    Vibrator vibrator;
    private CountDownTimer countDown;
    private final Handler handler = new Handler();
    LinearLayout basic_quiz_intro_layout;
    SpinKitView quiz_intro_progressBar;
    boolean isRunning = false;
    LinearLayout out_of_questions_layout;
    private String from = null;
    private Ads ads;
    private Topic quizModel = null;
    private ArrayList<Topic> topics = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.question);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        appBarLayout = findViewById(R.id.appbar_quiz_activity);
        toolbar = findViewById(R.id.quiz_activity_toolbar);

        start_quiz_button = findViewById(R.id.start_quiz_button);
        counter = findViewById(R.id.start_counter);
        start_text = findViewById(R.id.start_text);

        out_of_questions_layout = findViewById(R.id.out_of_questions_layout);

        basic_topic_coverPicture = findViewById(R.id.basic_topic_coverpic);
        basic_topic = findViewById(R.id.basic_topic_name);
        basic_total_questions = findViewById(R.id.basic_quiz_total_questions);
        basic_time_per_question = findViewById(R.id.basic_time_per_question);
        basic_points_per_question = findViewById(R.id.basic_points_per_question);
        basic_quiz_intro_layout = findViewById(R.id.basic_quiz_intro_layout);

        quiz_intro_progressBar = findViewById(R.id.quiz_intro_progressBar);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        pin = getIntent().getStringExtra("PIN");
        from = getIntent().getStringExtra("FROM");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(appBarLayout, "elevation", 0.1f));
            appBarLayout.setStateListAnimator(stateListAnimator);
        }

        LinearLayout s = (LinearLayout) findViewById(R.id.banner_AdView);
        ads = new Ads();
        ads.BannerAd(QuizActivity.this, s, BANNER_3);

        toolbar.setTitleTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
        toolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_black_24);
        }

        myRef.child("Topics").orderByChild("pin").equalTo(pin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot2: snapshot.getChildren()){
                    Topic topic = snapshot2.getValue(Topic.class);
                    topics.add(topic);
                }

                for (Topic topic : topics) {
                    if (topic.getPin().equals(pin)) {
                        quizModel = topic;
                    }
                }

                if (quizModel.getQuizQuestionCount() == 0) {
                    out_of_questions_layout.setVisibility(View.VISIBLE);
                    quiz_intro_progressBar.setVisibility(View.INVISIBLE);
                    start_quiz_button.setVisibility(View.INVISIBLE);
                } else {
                    Picasso.get().load(quizModel.getPicture()).into(basic_topic_coverPicture);
                    basic_topic.setText(quizModel.getTitle());
                    basic_total_questions.setText(String.valueOf(quizModel.getQuizQuestionCount()));
                    basic_time_per_question.setText(quizModel.getTimePerQuestion() + "s");
                    int total_points = quizModel.getQuizQuestionCount() * quizModel.getPointsPerQuestion();
                    basic_points_per_question.setText(String.valueOf(total_points));

                    DecimalFormat df = new DecimalFormat("0.0");
                    String numberString = "";
                    df.setRoundingMode(RoundingMode.DOWN);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

                    basic_quiz_intro_layout.setVisibility(View.VISIBLE);
                    quiz_intro_progressBar.setVisibility(View.INVISIBLE);
                    start_quiz_button.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        start_quiz_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(50);
                start_text.setVisibility(View.INVISIBLE);
                counter.setVisibility(View.VISIBLE);

                handler.postDelayed(() -> {
                    startTimer();
                }, 1000);

            }
        });

        if(from != null){
            if(from.equals("ScoreBoard")){

                FragmentManager fragmentManager = getSupportFragmentManager();

                Bundle bundle = new Bundle();
                bundle.putString("PIN", pin);

                QuizFragment QuizFragment = new QuizFragment();
                QuizFragment.setArguments(bundle);
                appBarLayout.setVisibility(View.INVISIBLE);
                fragmentManager.beginTransaction()
                        .replace(R.id.questionConstraint, QuizFragment, "handlingBackPressed")
                        .addToBackStack(null)
                        .commit();
            }
        }


    }



    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("handlingBackPressed");

        if (fragment instanceof QuizPlatformBack) {
            ((QuizPlatformBack) fragment).onBackPressed();
        }

        if(count == 0){
            super.onBackPressed();
            finish();
        }

        if(isRunning){
            countDown.cancel();
            finish();
        }

    }

    private void startTimer() {

        countDown = new CountDownTimer(3000, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                isRunning = true;
                counter.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isRunning= false;
                counter.setVisibility(View.VISIBLE);

                FragmentManager fragmentManager = getSupportFragmentManager();

                Bundle bundle = new Bundle();
                bundle.putString("PIN", pin);


               QuizFragment quizFragment = new QuizFragment();

                quizFragment.setArguments(bundle);

                fragmentManager.beginTransaction()
                        .replace(R.id.questionConstraint, quizFragment, "handlingBackPressed")
                        .addToBackStack(null)
                        .commit();

                appBarLayout.setVisibility(View.INVISIBLE);
            }
        }.start();

    }

    @Override
    public boolean onSupportNavigateUp() {
        if(isRunning){
            countDown.cancel();

        }
        onBackPressed();
        finish();
        return true;
    }


}