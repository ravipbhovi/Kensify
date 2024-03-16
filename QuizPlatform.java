package com.example.myapplication.fragment;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.view.View.GONE;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.activity.PointsBoard;
import com.example.myapplication.model.AttemptedQuestion;
import com.example.myapplication.model.AttemptedQuestionModel;
import com.example.myapplication.model.PlayedQuiz;
import com.example.myapplication.model.PlayedQuizModel;
import com.example.myapplication.model.Question;
import com.example.myapplication.model.Topic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuizPlatform#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuizPlatform extends Fragment implements View.OnClickListener, QuizPlatformBack {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private TextView question, timer, questionNum;
    private LinearLayout option;
    private List<Question> list;
    private TextView optionA, optionB, optionC, optionD;
    private CardView optionA_Card, optionB_Card, optionC_Card, optionD_Card;
    private int questionNumber = 0, score = 0, qnum = 0, counter = 0, count = 0;
    private CountDownTimer countDown;
    private final Handler handler = new Handler();
    private ProgressBar progressBar;
    private String username, activityStatus = "Running";
    private long userPoints = 0, userPlayedGames;
    Vibrator vibrator;
    int selectedOption = 0;
    private String pin = null;
    private int correctOptions = 0;
    private int wrongOptions = 0;
    private int unattemptedOptions = 0;
    private int attemptedQs = 0;
    private int unattemptedQs = 0;
    private int timeTaken = 0;
    private ArrayList<AttemptedQuestion> all_attemptedQuestionModels;
    private ArrayList<AttemptedQuestion> unAttemptedQuestionModels;
    private ArrayList<AttemptedQuestion> correct_attemptedQuestionModels;
    private ArrayList<AttemptedQuestion> wrong_attemptedQuestionModels;
    private ArrayList<AttemptedQuestion> only_attemptedQuestionModels;
    String score_string;
    long quizPlays = 0;
    List<String> subTopicList;
    private String getTopicPicture;
    private String topic_name;
    private int timePerQuestion = 0;
    private int pointsPerQuestion = 0;
    final String[] quiz_current_domain_index = new String[1];
    //TextView total_questions;

    MediaPlayer mediaPlayer;

    ArrayList<AttemptedQuestionModel> all_attemptedQuestionModels2 = new ArrayList<>();
    ArrayList<AttemptedQuestionModel> unAttemptedQuestionModels2 = new ArrayList<>();
    ArrayList<AttemptedQuestionModel> correct_attemptedQuestionModels2 = new ArrayList<>();
    ArrayList<AttemptedQuestionModel> wrong_attemptedQuestionModels2 = new ArrayList<>();
    ArrayList<AttemptedQuestionModel> only_attemptedQuestionModels2 = new ArrayList<>();

    LinearLayout  evaluation_layout, question_number_layout, quiz_platform;
    TextView evaluation_update_text;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public QuizPlatform() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuizPlatform.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizPlatform newInstance(String param1, String param2) {
        QuizPlatform fragment = new QuizPlatform();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.test_platform, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getActivity().getWindow().setStatusBarColor(Color.WHITE);


        Bundle bundle = this.getArguments();

        //username = bundle.getString("USERNAME");
        pin = bundle.getString("PIN");

        SharedPreferences preferences = getActivity().getSharedPreferences("Quizzy", MODE_PRIVATE);
        username = preferences.getString("username", "");
       // subTopicList = (List<String>) bundle.getSerializableExtra("QUIZMODELS");

        all_attemptedQuestionModels = new ArrayList<>();
        unAttemptedQuestionModels = new ArrayList<>();
        correct_attemptedQuestionModels = new ArrayList<>();
        wrong_attemptedQuestionModels = new ArrayList<>();
        only_attemptedQuestionModels = new ArrayList<>();


        myRef.child("Active Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userPoints = snapshot.child(username).child("status").child("coins").getValue(long.class);
                userPlayedGames = snapshot.child(username).child("status").child("plays").getValue(long.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.white2));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
        }

        //getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);


        FullScreencall();
        question = root.findViewById(R.id.question);
        optionA = root.findViewById(R.id.buttonA);
        optionB = root.findViewById(R.id.buttonB);
        optionC = root.findViewById(R.id.buttonC);
        optionD = root.findViewById(R.id.buttonD);
        timer = root.findViewById(R.id.countdown);
        option = root.findViewById(R.id.optionsBlock);
        questionNum = root.findViewById(R.id.currentQNumber);
        progressBar = root.findViewById(R.id.progressBar);
        question_number_layout = root.findViewById(R.id.question_number_layout);
        optionA_Card = root.findViewById(R.id.optionA_card);
        optionB_Card = root.findViewById(R.id.optionB_card);
        optionC_Card = root.findViewById(R.id.optionC_card);
        optionD_Card = root.findViewById(R.id.optionD_card);
        quiz_platform = root.findViewById(R.id.quiz_layout);
        evaluation_layout = root.findViewById(R.id.evaluation_layout);
        evaluation_update_text = root.findViewById(R.id.evaluation_update_tags);

        //total_questions = root.findViewById(R.id.total_questions);

        vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.simple_button_click);

        optionA_Card.setOnClickListener(this);
        optionB_Card.setOnClickListener(this);
        optionC_Card.setOnClickListener(this);
        optionD_Card.setOnClickListener(this);

        getQuestionsList();
        enableOption(false);
        return root;
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getActivity().getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void getQuestionsList() {


        question.setVisibility(View.INVISIBLE);
        optionA.setVisibility(View.INVISIBLE);
        optionB.setVisibility(View.INVISIBLE);
        optionC.setVisibility(View.INVISIBLE);
        optionD.setVisibility(View.INVISIBLE);
        timer.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);


        ArrayList<String> questionPINs = new ArrayList<>();

        list = new ArrayList<>();

        myRef.child("Topics").orderByChild("pin").equalTo(pin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snapshot1: snapshot.getChildren()){

                    Topic subject = snapshot1.getValue(Topic.class);

                        getTopicPicture = subject.getPicture();
                        topic_name = subject.getTitle();
                        timePerQuestion = subject.getTimePerQuestion();
                        pointsPerQuestion = subject.getPointsPerQuestion();
                        quizPlays = subject.getPlays();
                        quiz_current_domain_index[0] = snapshot1.getKey();

                        if(subject.getQuestionModels() != null){
                            for(String question: subject.getQuestionModels()){
                                questionPINs.add(question);
                            }
                        }

                        ArrayList<Question> all_questions = new ArrayList<>();

                        myRef.child("Questions").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot snapshot2: snapshot.getChildren()){
                                    Question question = snapshot2.getValue(Question.class);
                                    all_questions.add(question);
                                }

                                ArrayList<Question> topic_questions = new ArrayList<>();

                                for(String pin : questionPINs){

                                    for(Question question: all_questions){
                                        if(question.getPin().equals(pin)){
                                            topic_questions.add(question);
                                        }
                                    }

                                }


                                for(int i = 0; i < 5; i++){
                                    Collections.shuffle(topic_questions);
                                }

                                for(int i = 0; i < subject.getQuizQuestionCount(); i++){
                                    list.add(topic_questions.get(i));
                                }

                                questionNumber = 0;
                                qnum = questionNumber + 1;
                                questionNum.setText(Integer.toString(qnum));


                                handler.postDelayed(() -> {
                                    setQuestion();
                                    //question_number_layout.setVisibility(GONE);
                                    //evaluation_layout.setVisibility(GONE);
                                    quiz_platform.setVisibility(View.VISIBLE);
                                }, 2000);



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void setQuestion() {

        playAnim(question, 0, 0,100);

        handler.postDelayed(() -> playAnimO(optionA, 0, 1, 100), 0);
        handler.postDelayed(() -> playAnimO(optionB, 0, 2, 100), 0);
        handler.postDelayed(() -> playAnimO(optionC, 0, 3, 100),  0);
        handler.postDelayed(() -> playAnimO(optionD, 0, 4, 100),  0);

        handler.postDelayed(() -> {

            timer.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            startTimer();
            option.setVisibility(View.VISIBLE);
            enableOption(true);


        }, 2500);

    }



    @Override
    public void onClick(View view) {

        mediaPlayer.start();
        vibrator.vibrate(50);


        switch (view.getId()) {

            case R.id.optionA_card:
                selectedOption = 1;
                break;
            case R.id.optionB_card:
                selectedOption = 2;
                break;
            case R.id.optionC_card:
                selectedOption = 3;
                break;
            case R.id.optionD_card:
                selectedOption = 4;
                break;

            default:
                selectedOption = 0;
                break;
        }

        if(countDown != null) {
            countDown.cancel();
        }

        Log.e("TAG0", "Clicked Question num: " + questionNumber);

        Log.e("TAG120", "Before Clicked Q " + questionNumber + " PIN " +  list.get(questionNumber).getPin());




        Log.e("TAG120", "After Clicked Q " + questionNumber + " PIN " +  list.get(questionNumber).getPin());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            checkAnswer(selectedOption, view);


            if(questionNumber < list.size()-1){

                questionNumber++;
                qnum++;
                questionNum.setText(Integer.toString(qnum));

            }


            handler.postDelayed(() -> {

                changeQuestion();

                question.setVisibility(View.INVISIBLE);
                optionA.setVisibility(View.INVISIBLE);
                optionB.setVisibility(View.INVISIBLE);
                optionC.setVisibility(View.INVISIBLE);
                optionD.setVisibility(View.INVISIBLE);
                timer.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                if(count < list.size()){
                   // question_number_layout.setVisibility(GONE);
                    quiz_platform.setVisibility(View.VISIBLE);
                }else{
                    //question_number_layout.setVisibility(GONE);
                    quiz_platform.setVisibility(GONE);
                }


            }, 4000);

        }

    }

    private void startTimer() {

        long tpq = (long) ((timePerQuestion + 1) * 1000);
        int milliTime = (timePerQuestion * 100)/10;

        countDown = new CountDownTimer(tpq, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(String.valueOf(millisUntilFinished / 1000));
                progressBar.setProgress(Integer.parseInt(String.valueOf(millisUntilFinished / milliTime)));
                counter = Integer.parseInt(String.valueOf(millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {
                enableOption(false);
                option.setVisibility(View.INVISIBLE);
                question.setVisibility(View.INVISIBLE);
                quiz_platform.setVisibility(GONE);

                Log.e("TAG0", "Skipped Question num: " + questionNumber);

                Log.e("TAG120", "Skipped Q " + questionNumber + " PIN " +  list.get(questionNumber).getPin());

                if(questionNumber == list.size()-1)
                   question_number_layout.setVisibility(GONE);

                AttemptedQuestion attemptedQuestion = new AttemptedQuestion(qnum,
                            0,
                            0,
                            list.get(questionNumber).getPin());

                    all_attemptedQuestionModels.add(attemptedQuestion);
                    unAttemptedQuestionModels.add(attemptedQuestion);

                AttemptedQuestionModel attemptedQuestionModel = new AttemptedQuestionModel(qnum, list.get(questionNumber).getQuestion(),
                        list.get(questionNumber).getOptionA(),
                        list.get(questionNumber).getOptionB(),
                        list.get(questionNumber).getOptionC(),
                        list.get(questionNumber).getOptionD(),
                        list.get(questionNumber).getCorrectOption(),
                        0,
                        0,
                        list.get(questionNumber).getPin(),
                        list.get(questionNumber).getDescription());

                all_attemptedQuestionModels2.add(attemptedQuestionModel);
                unAttemptedQuestionModels2.add(attemptedQuestionModel);


                /*switch (list.get(questionNumber).getCorrectOption()){

                    case 1:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            optionA.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green2)));
                        }
                        optionA.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                        handler.postDelayed(() -> optionA.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);

                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            optionB.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green2)));
                        }
                        optionB.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                        handler.postDelayed(() -> optionB.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);
                        break;
                    case 3:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            optionC.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green2)));
                        }
                        optionC.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                        handler.postDelayed(() -> optionC.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);
                        break;
                    case 4:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            optionD.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green2)));
                        }
                        optionD.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                        handler.postDelayed(() -> optionD.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000"))), 2500);
                        break;
                }*/

                if(questionNumber < list.size()-1){

                    questionNumber++;
                    qnum++;
                    questionNum.setText(Integer.toString(qnum));

                }

                handler.postDelayed(() -> {
                    //question_number_layout.setVisibility(GONE);
                    question.setVisibility(View.INVISIBLE);
                    optionA.setVisibility(View.INVISIBLE);
                    optionB.setVisibility(View.INVISIBLE);
                    optionC.setVisibility(View.INVISIBLE);
                    optionD.setVisibility(View.INVISIBLE);
                    timer.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    quiz_platform.setVisibility(View.VISIBLE);
                    changeQuestion();

                }, 2000);

            }
        };

        countDown.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkAnswer(int selectedOption, View view) {

        enableOption(false);

        Log.e("TAG123", "QNUM: " + qnum + " QuestionNumber: " + questionNumber);



        if(selectedOption == list.get(questionNumber).getCorrectOption()) {

            correctOptions = correctOptions + 1;
            timeTaken = timeTaken + (timePerQuestion - counter);

            /*if(counter > 5){
                score = score + counter;
                AttemptedQuestionModel attemptedQuestionModel = new AttemptedQuestionModel(list.get(questionNumber).getQuestion(),
                        list.get(questionNumber).getOptionA(),
                        list.get(questionNumber).getOptionB(),
                        list.get(questionNumber).getOptionC(),
                        list.get(questionNumber).getOptionD(),
                        list.get(questionNumber).getCorrectOption(),
                        selectedOption,
                        counter,
                        list.get(questionNumber).getPin());

                all_attemptedQuestionModels.add(attemptedQuestionModel);
                correct_attemptedQuestionModels.add(attemptedQuestionModel);
                only_attemptedQuestionModels.add(attemptedQuestionModel);


            } else{*/

                score = score + pointsPerQuestion;

                AttemptedQuestion attemptedQuestion = new AttemptedQuestion(qnum, selectedOption, pointsPerQuestion, list.get(questionNumber).getPin());

                all_attemptedQuestionModels.add(attemptedQuestion);
                correct_attemptedQuestionModels.add(attemptedQuestion);
                only_attemptedQuestionModels.add(attemptedQuestion);


            AttemptedQuestionModel attemptedQuestionModel = new AttemptedQuestionModel(qnum, list.get(questionNumber).getQuestion(),
                    list.get(questionNumber).getOptionA(),
                    list.get(questionNumber).getOptionB(),
                    list.get(questionNumber).getOptionC(),
                    list.get(questionNumber).getOptionD(),
                    list.get(questionNumber).getCorrectOption(),
                    selectedOption,
                    pointsPerQuestion,
                    list.get(questionNumber).getPin(),
                    list.get(questionNumber).getDescription());

            all_attemptedQuestionModels2.add(attemptedQuestionModel);
            correct_attemptedQuestionModels2.add(attemptedQuestionModel);
            only_attemptedQuestionModels2.add(attemptedQuestionModel);

           //}

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (view).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange2)));
                ((TextView)view).setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));

                handler.postDelayed(() -> ((TextView)view).setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);
            }*/


        } else {

            wrongOptions = wrongOptions + 1;
            timeTaken = timeTaken + (timePerQuestion - counter);

            AttemptedQuestion attemptedQuestion = new AttemptedQuestion(qnum,
                    selectedOption,
                    0,
                    list.get(questionNumber).getPin());

            all_attemptedQuestionModels.add(attemptedQuestion);
            wrong_attemptedQuestionModels.add(attemptedQuestion);
            only_attemptedQuestionModels.add(attemptedQuestion);


            AttemptedQuestionModel attemptedQuestionModel = new AttemptedQuestionModel(qnum,
                    list.get(questionNumber).getQuestion(),
                    list.get(questionNumber).getOptionA(),
                    list.get(questionNumber).getOptionB(),
                    list.get(questionNumber).getOptionC(),
                    list.get(questionNumber).getOptionD(),
                    list.get(questionNumber).getCorrectOption(),
                    selectedOption,
                    0,
                    list.get(questionNumber).getPin(),
                    list.get(questionNumber).getDescription());

            all_attemptedQuestionModels2.add(attemptedQuestionModel);
            wrong_attemptedQuestionModels2.add(attemptedQuestionModel);
            only_attemptedQuestionModels2.add(attemptedQuestionModel);

            /*(view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
            ((TextView)view).setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


            handler.postDelayed(() -> ((TextView)view).setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);*/

           /* switch (list.get(questionNumber).getCorrectOption()){

                case 1:
                    optionA.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green2)));
                    optionA.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                    handler.postDelayed(() -> optionA.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);

                    break;
                case 2:
                    optionB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green2)));
                    optionB.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                    handler.postDelayed(() -> optionB.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);
                    break;
                case 3:
                    optionC.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green2)));
                    optionC.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                    handler.postDelayed(() -> optionC.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000") )), 2500);
                    break;
                case 4:
                    optionD.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green2)));
                    optionD.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF") ));


                    handler.postDelayed(() -> optionD.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000"))), 2500);
                    break;

            }*/


        }

        //Log.e("TAG2", "Q NUM: " + questionNumber);
        //Log.e("TAG2", "COUNT: " + count);

        switch (selectedOption){

            case 1:
                optionA_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.app_colour)));
                optionA.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));

                handler.postDelayed(() -> {
                    option.setVisibility(View.INVISIBLE);
                    question.setVisibility(View.INVISIBLE);
                    quiz_platform.setVisibility(GONE);

                    if(count == list.size() -1){
                        question_number_layout.setVisibility(GONE);
                    }

                    optionA.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
                    optionA_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));
                }, 2000);

                break;
            case 2:
                optionB_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.app_colour)));
                optionB.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));

                handler.postDelayed(() -> {
                    option.setVisibility(View.INVISIBLE);
                    question.setVisibility(View.INVISIBLE);
                    quiz_platform.setVisibility(GONE);
                    if(count == list.size() -1){
                        question_number_layout.setVisibility(GONE);
                    }
                    optionB.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
                    optionB_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));
                }, 2000);
                break;
            case 3:
                optionC_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.app_colour)));
                optionC.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));

                handler.postDelayed(() -> {
                    option.setVisibility(View.INVISIBLE);
                    question.setVisibility(View.INVISIBLE);
                    quiz_platform.setVisibility(GONE);
                    if(count == list.size() -1){
                        question_number_layout.setVisibility(GONE);
                    }
                    optionC.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
                    optionC_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));

                }, 2000);
                break;
            case 4:
                optionD_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.app_colour)));
                optionD.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));

                handler.postDelayed(() -> {
                    option.setVisibility(View.INVISIBLE);
                    question.setVisibility(View.INVISIBLE);
                    quiz_platform.setVisibility(GONE);
                    if(count == list.size() -1){
                        question_number_layout.setVisibility(GONE);
                    }
                    optionD.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
                    optionD_Card.setCardBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white2)));
                }, 2000);
                break;

        }

    }

    private void changeQuestion(){

        if(activityStatus.equals("Closed")){
            return;
        }

        count++;
        score_string = String.valueOf(score);

        if(count == list.size()){
            //question_number_layout.setVisibility(GONE);
            upLoadingResult();


        }else{

            //timer.setText(String.valueOf(10));

            if(questionNumber <= list.size()-1){

                playAnim(question, 0, 0, 100);

                handler.postDelayed(() -> playAnimO(optionA, 0, 1, 100),  0);
                handler.postDelayed(() -> playAnimO(optionB, 0, 2, 100),  0);
                handler.postDelayed(() -> playAnimO(optionC, 0, 3, 100),  0);
                handler.postDelayed(() -> playAnimO(optionD, 0, 4, 100),  0);

                handler.postDelayed(() -> {

                    counter = (timePerQuestion + 1);

                    timer.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    startTimer();
                    enableOption(true);
                    option.setVisibility(View.VISIBLE);
                    quiz_platform.setVisibility(View.VISIBLE);
                    //question_number_layout.setVisibility(GONE);

                }, 2500);
            }
        }
    }

    
    private void playAnim(View view, final int value, int viewNum, int sd){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(300).setStartDelay(100).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if( value == 0)
                {
                    switch (viewNum){

                        case 0:
                            ((TextView)view).setText(list.get(questionNumber).getQuestion());

                            break;
                        case 1:


                            ((TextView)view).setText(list.get(questionNumber).getOptionA());

                            break;
                        case 2:

                            ((TextView)view).setText(list.get(questionNumber).getOptionB());

                            break;
                        case 3:

                            ((TextView)view).setText(list.get(questionNumber).getOptionC());

                            break;
                        case 4:

                            ((TextView)view).setText(list.get(questionNumber).getOptionD());

                            break;
                    }

                    if(viewNum!=0){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            (view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }

                    playAnim(view, 1, viewNum, sd);
                    question.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void playAnimO(View view, final int value, int viewNum, int sd){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(200).setStartDelay(1000).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if( value == 0)
                {
                    switch (viewNum){

                        case 0:
                            ((TextView)view).setText(list.get(questionNumber).getQuestion());

                            break;
                        case 1:
                            ((TextView)view).setText(list.get(questionNumber).getOptionA());

                            break;
                        case 2:
                            ((TextView)view).setText(list.get(questionNumber).getOptionB());

                            break;
                        case 3:
                            ((TextView)view).setText(list.get(questionNumber).getOptionC());

                            break;
                        case 4:
                            ((TextView)view).setText(list.get(questionNumber).getOptionD());

                            break;
                    }

                    if(viewNum!=0){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            (view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }

                    playAnimO(view, 1, viewNum, sd);
                    optionA.setVisibility(View.VISIBLE);
                    optionB.setVisibility(View.VISIBLE);
                    optionC.setVisibility(View.VISIBLE);
                    optionD.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void enableOption(boolean enable){
        for (int i =0; i<4; i++){
            option.getChildAt(i).setEnabled(enable);
        }
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Quiz is running...");
        builder.setMessage("Do you want to submit it here itself?");
        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                upLoadingResult();
            }
        });
        builder.setNegativeButton("CONTINUE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();
    }

    private void upLoadingResult(){

        activityStatus = "Closed";
        quiz_platform.setVisibility(GONE);
        question_number_layout.setVisibility(GONE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.white2));
        }



        if(all_attemptedQuestionModels.size() != list.size()){

            int unAttemptedQuestions = list.size() - (questionNumber + 1);

            for(int i = 0; i <= unAttemptedQuestions; i++){

                AttemptedQuestion attemptedQuestion = new AttemptedQuestion(qnum+i,
                        0,
                        0,
                        list.get(questionNumber+i).getPin());

                all_attemptedQuestionModels.add(attemptedQuestion);
                unAttemptedQuestionModels.add(attemptedQuestion);

                AttemptedQuestionModel attemptedQuestionModel = new AttemptedQuestionModel(qnum, list.get(questionNumber+i).getQuestion(),
                        list.get(questionNumber+i).getOptionA(),
                        list.get(questionNumber+i).getOptionB(),
                        list.get(questionNumber+i).getOptionC(),
                        list.get(questionNumber+i).getOptionD(),
                        list.get(questionNumber+i).getCorrectOption(),
                        0,
                        0,
                        list.get(questionNumber+i).getPin(),
                        list.get(questionNumber+i).getDescription());

                all_attemptedQuestionModels2.add(attemptedQuestionModel);
                unAttemptedQuestionModels2.add(attemptedQuestionModel);

            }

        }


        userPlayedGames = userPlayedGames + 1;

        userPoints = userPoints + score;

        quizPlays = quizPlays + 1;

        myRef.child("Active Users").child(username).child("status").child("coins").setValue(userPoints);
        myRef.child("Active Users").child(username).child("status").child("plays").setValue(userPlayedGames);

        Date d = new Date();
        CharSequence playedDate1  = DateFormat.format("dd" + "/" + "MM" + "/" + "yyyy", d.getTime());
        CharSequence playedTime  = DateFormat.format("HH:mm:ss", d.getTime());

        unattemptedOptions = list.size() - (correctOptions + wrongOptions);

        timeTaken = timeTaken + unattemptedOptions * list.size();

        attemptedQs = correctOptions + wrongOptions;
        unattemptedQs = list.size() - attemptedQs;

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String playedDate = simpleDateFormat.format(c);

        String current_played_PIN = getPIN();

                PlayedQuiz playedQuiz = new PlayedQuiz(pin, score, correctOptions, wrongOptions, attemptedQs, unattemptedQs, playedDate, playedTime.toString(), all_attemptedQuestionModels, current_played_PIN);

                List<PlayedQuiz> playedQuizModelList = new ArrayList<>();

                myRef.child("Active Users").child(username).child("activity").child("quiz").child("played").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snapshot1:snapshot.getChildren()){
                            PlayedQuiz quizModel = snapshot1.getValue(PlayedQuiz.class);

                            if(quizModel.getPlayedPIN() == null){
                                quizModel.setPlayedPIN(getPIN());
                            }

                            playedQuizModelList.add(quizModel);
                        }

                        playedQuizModelList.add(playedQuiz);

                        ArrayList<PlayedQuizModel> attemptedExams = new ArrayList<>();

                        myRef.child("Active Users").child(username).child("activity").child("exams").child("attempted").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot snapshot1: snapshot.getChildren()){
                                    PlayedQuizModel quizModel = snapshot1.getValue(PlayedQuizModel.class);

                                    attemptedExams.add(quizModel);

                                }

                                for(PlayedQuizModel playedQuizModel : attemptedExams){

                                        ArrayList<AttemptedQuestion> attemptedExamQuestions = new ArrayList<>();

                                        for(AttemptedQuestionModel attemptedQuestionModel: playedQuizModel.getAttemptedQuestionModels()){

                                            AttemptedQuestion attemptedQuestion = new AttemptedQuestion(attemptedQuestionModel.getQuestion_number(),
                                                    attemptedQuestionModel.getClickedOption(),
                                                    attemptedQuestionModel.getPoints(),
                                                    attemptedQuestionModel.getPin());

                                            attemptedExamQuestions.add(attemptedQuestion);

                                        }

                                        String current_played_PIN = getPIN();

                                        PlayedQuiz playedQuiz = new PlayedQuiz(playedQuizModel.getPin(),
                                                playedQuizModel.getEarnedCoins(),
                                                playedQuizModel.getCorrectOptions(),
                                                playedQuizModel.getWrongOptions(),
                                                playedQuizModel.getAttemptedQs(),
                                                playedQuizModel.getUnattemptedQs(),
                                                playedQuizModel.getPlayedDate(),
                                                playedQuizModel.getPlayedTime(),
                                                attemptedExamQuestions,
                                                current_played_PIN);

                                        playedQuizModelList.add(playedQuiz);

                                    }

                                    myRef.child("Active Users").child(username).child("activity").child("quiz").child("played").setValue(playedQuizModelList);

                                myRef.child("Active Users").child(username).child("activity").child("exams").removeValue();

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                    }
                });



        myRef.child("Topics").child(quiz_current_domain_index[0]).child("plays").setValue(quizPlays);

        if(score_string == null){
            score_string = "0";
        }

        ArrayList<String> username_list = new ArrayList<>();

            myRef.child("Active Users").orderByChild("status/coins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String userName = ds.getKey();
                        username_list.add(userName);
                    }

                    for (DataSnapshot snapshot2 : snapshot.getChildren()){
                        String username2 = snapshot2.getKey();
                        myRef.child("Active Users").child(username2).child("status").child("rank").setValue(username_list.size() - username_list.indexOf(username2));
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



            evaluation_update_text.setText("Submitting...");


        handler.postDelayed(() -> {

            evaluation_update_text.setText("Uploading...");

        }, 300);

        handler.postDelayed(() -> {


            evaluation_update_text.setText("Evaluating...");

        }, 800);


        handler.postDelayed(() -> {


            evaluation_update_text.setText("Setting up your Quizort Rank...");

        }, 1300);


        handler.postDelayed(() -> {


            evaluation_update_text.setText("Your results are ready...");

        }, 1800);



            handler.postDelayed(() -> {

                Intent intent = new Intent(getActivity(), PointsBoard.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("PIN", current_played_PIN);
                startActivity(intent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().finish();


                }, 2300);





    }


    private String getPIN(){
        List<Character> ch14 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch13 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch12 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch11 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch10 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P','Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch9 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch8 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P','Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch7 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch6 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch5 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch4 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P','Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch3 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch2 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch1 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O','P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        List<Character> ch0 = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P','Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

        String pin =

                ch14.get(getRandomNumber())+""
                +ch13.get(getRandomNumber())+""
                +ch12.get(getRandomNumber())+""
                +ch11.get(getRandomNumber())+""
                        +ch10.get(getRandomNumber())+""
                        +ch9.get(getRandomNumber())+""
                        +ch8.get(getRandomNumber())+""
                        +ch7.get(getRandomNumber())+""
                        +ch6.get(getRandomNumber())+""
                        +ch5.get(getRandomNumber())+""
                        +ch4.get(getRandomNumber())+""
                        +ch3.get(getRandomNumber())+""
                        +ch2.get(getRandomNumber())+""
                        +ch1.get(getRandomNumber())+""
                        +ch0.get(getRandomNumber());


        ArrayList<String> pins = new ArrayList<>();

        myRef.child("Active Users").child(username).child("activity").child("quiz").child("played").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    PlayedQuiz data = snapshot1.getValue(PlayedQuiz.class);

                    if(data.getPlayedPIN() != null){
                        pins.add(data.getPlayedPIN());
                    }

                }

                if(pins.contains(pin)){
                    getPIN();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return pin;

    }

    private int getRandomNumber(){

        int min = 0;
        int max = 35;

        int n = (int) Math.floor(Math.random() * (max - min + 1) + min);
        return n;
    }
}