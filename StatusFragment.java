package com.ravipbhovi.statussaver.Fragments;


import static com.ravipbhovi.statussaver.Utils.AdUnits.banner1;
import static com.ravipbhovi.statussaver.Utils.AdUnits.banner2;
import static com.ravipbhovi.statussaver.Utils.Common.APP_DIR;
import static com.ravipbhovi.statussaver.Utils.Common.APP_NAME;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ravipbhovi.statussaver.Adapter.ImageAdapter;
import com.ravipbhovi.statussaver.Adapter.StatusAdapter;
import com.ravipbhovi.statussaver.Models.Status;
import com.ravipbhovi.statussaver.R;
import com.ravipbhovi.statussaver.ShowImageStatusActivity;
import com.ravipbhovi.statussaver.ShowVideoStatusActivity;
import com.ravipbhovi.statussaver.Utils.Ads;
import com.ravipbhovi.statussaver.Utils.Common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class StatusFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Status> statusList = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private StatusAdapter statusAdapter;
    private RelativeLayout container;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView messageTextView;
    private ImageAdapter.ImageClickOnListener imageClickOnListener;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor = null;
    private InterstitialAd mInterstitialAd;
    private StatusAdapter.StatusClickOnListener statusClickOnListener;
    private LinearLayout no_item_found;
    private Status status = null;
    private Context context = null;
    LinearLayout adContainer;
    Ads ads = new Ads();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();
        recyclerView = view.findViewById(R.id.recyclerViewImage);
        progressBar = view.findViewById(R.id.prgressBarImage);
        container = view.findViewById(R.id.image_container);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        messageTextView = view.findViewById(R.id.messageTextImage);
        no_item_found = view.findViewById(R.id.no_item_found);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        setWindowColors();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark)
                , ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark));

        swipeRefreshLayout.setOnRefreshListener(this::getStatus);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Common.GRID_COUNT));


        LinearLayout adContainer = view.findViewById(R.id.banner_AdView);
        ShimmerFrameLayout shimmerFrameLayout = view.findViewById(R.id.banner_shimmer);

        ads.BannerAdShimmer(getActivity(), adContainer, banner1, shimmerFrameLayout);


        getStatus();

        statusClickOnListener = new StatusAdapter.StatusClickOnListener() {
            @Override
            public void onClick(int position) {

                status = statusList.get(position);

                //InterstitialLoad(getActivity(), INTER);

                Intent intent;
                if(status.isVideo()) {

                   /* LayoutInflater inflater = LayoutInflater.from(context);
                    final View videoStatusView = inflater.inflate(R.layout.view_video_full_screen, null);

                    final Dialog dialog = new Dialog(context, R.style.DialogTheme);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(videoStatusView);

                    *//*if (videoStatusView.getParent() != null) {
                        ((ViewGroup) videoStatusView.getParent()).removeView(videoStatusView);
                    }*//*

                    FrameLayout mediaControls = dialog.findViewById(R.id.videoViewWrapper);
                    VideoView videoView = dialog.findViewById(R.id.video_full);
                    ImageView playButton = dialog.findViewById(R.id.play_button);
                    //LinearLayout adContainer = view1.findViewById(R.id.ll_main_screen_container);
                    //adContainer.addView(Common.loadBanner(BANNER_2, getActivity()));
                    LinearLayout save_button = dialog.findViewById(R.id.save_button);
                    LinearLayout share_button = dialog.findViewById(R.id.share_button);

                    final MediaController mediaController = new MediaController(context, false);

                    videoView.setOnPreparedListener(mp -> {
                        mp.start();
                        mediaController.show(0);
                        mp.setLooping(true);
                    });

                    videoView.setMediaController(mediaController);
                    mediaController.setMediaPlayer(videoView);

                    if (status.isApi30()) {
                        videoView.setVideoURI(status.getDocumentFile().getUri());
                    } else {
                        videoView.setVideoURI(Uri.fromFile(status.getFile()));
                    }
                    videoView.requestFocus();

                    ((ViewGroup) mediaController.getParent()).removeView(mediaController);

                    if (mediaControls.getParent() != null) {
                        mediaControls.removeView(mediaController);
                    }

                    mediaControls.addView(mediaController);


                   // Window window = dialog.getWindow();
                   // window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                   // dialog.show();

                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(videoView !=  null){
                                videoView.start();
                                playButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });



                    save_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Common.copyFile(status, context, container);

                            showDownloadDialog(getActivity(), mInterstitialAd);

                            if(videoView !=  null){
                                videoView.pause();
                                playButton.setVisibility(View.VISIBLE);
                            }


                        }
                    });



                    share_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);

                            shareIntent.setType("image/mp4");
                            if (status.isApi30()) {
                                shareIntent.putExtra(Intent.EXTRA_STREAM, status.getDocumentFile().getUri());
                            } else {
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + status.getFile().getAbsolutePath()));
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share image"));
                        }
                    });
*/

                    intent = new Intent(getActivity(), ShowVideoStatusActivity.class);

                } else {

                    /*final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.view_image_full_screen);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                   // dialog.getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
                    //LayoutInflater inflater = LayoutInflater.from(getActivity());
                    //View view = inflater.inflate(R.layout.view_image_full_screen, null);

                    //dialog.setContentView(view);

                    //LinearLayout adContainer = view.findViewById(R.id.ll_main_screen_container);
                    //adContainer.addView(Common.loadBanner(BANNER_1, getActivity()));

                    ImageView imageView = dialog.findViewById(R.id.img);
                    LinearLayout save_button = dialog.findViewById(R.id.save_button);
                    LinearLayout share_button = dialog.findViewById(R.id.share_button);

                    if (status.isApi30()) {
                        Glide.with(getActivity()).load(status.getDocumentFile().getUri()).into(imageView);
                    } else {
                        Glide.with(getActivity()).load(status.getFile()).into(imageView);
                    }


                   //Window window = dialog.getWindow();
                   //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                   // dialog.show();


                    save_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Common.copyFile(status, context, container);


                            Log.e("TAG2024", "Inter ad: " + mInterstitialAd);

                            showDownloadDialog(getActivity(), mInterstitialAd);

                                *//*if(mInterstitialAd != null){
                                    mInterstitialAd.show(getActivity());
                                }else {
                                    Log.e("TAG", "Ad is null");
                                }

                                Toast.makeText(context, "Video is saved to gallery", Toast.LENGTH_LONG).show();
    *//*
                        }
                    });


                    share_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);

                            shareIntent.setType("image/jpg");

                            if (status.isApi30()) {
                                shareIntent.putExtra(Intent.EXTRA_STREAM, status.getDocumentFile().getUri());
                            } else {
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + status.getFile().getAbsolutePath()));
                            }

                            getActivity().startActivity(Intent.createChooser(shareIntent, "Share image"));
                        }
                    });*/

                   // Log.e("TAG", "Status file: " + status.getDocumentFile().getUri());

                    intent = new Intent(getActivity(), ShowImageStatusActivity.class);

                }

                intent.putExtra("status", status);
                if (status.isApi30()) {
                    intent.putExtra("uri", status.getDocumentFile().getUri().toString());
                } else {
                    intent.putExtra("file-path", status.getFile().toString());
                }
                startActivity(intent);

            }
        };

    }



    private void getStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            executeNew();
        } else if (Common.STATUS_DIRECTORY.exists()) {
            executeOld();
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText(R.string.cant_find_whatsapp_dir);
            Toast.makeText(getActivity(), getString(R.string.cant_find_whatsapp_dir), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void executeOld() {

        Executors.newSingleThreadExecutor().execute(() -> {

            Handler mainHandler = new Handler(Looper.getMainLooper());

            File[] statusFiles;
            statusFiles = Common.STATUS_DIRECTORY.listFiles();
            Log.e("path", "path: " + Environment.getExternalStorageDirectory() + File.separator + "WhatsApp/Media/.Statuses");
            statusList.clear();

            if (statusFiles != null && statusFiles.length > 0) {

                Arrays.sort(statusFiles);
                for (File file : statusFiles) {

                    if (file.getName().contains(".nomedia"))
                        continue;

                    Status status = new Status(file, file.getName(), file.getAbsolutePath());

                        statusList.add(status);

                }

                mainHandler.post(() -> {

                    if (statusList.size() <= 0) {
                        messageTextView.setVisibility(View.VISIBLE);
                        messageTextView.setText(R.string.no_files_found);
                    } else {
                        messageTextView.setVisibility(View.GONE);
                        messageTextView.setText("");
                    }


                    statusAdapter = new StatusAdapter(statusList, statusClickOnListener);
                    recyclerView.setAdapter(statusAdapter);
                    statusAdapter.notifyItemRangeChanged(0, statusList.size());
                    progressBar.setVisibility(View.GONE);
                });

            } else {

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(R.string.no_files_found);
                    Toast.makeText(getActivity(), getString(R.string.no_files_found), Toast.LENGTH_SHORT).show();
                });

            }
            swipeRefreshLayout.setRefreshing(false);

        });
    }

    private void executeNew() {

        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            List<UriPermission> list = requireActivity().getContentResolver().getPersistedUriPermissions();

            Log.e("TAG", "permisssion list : " + list);

            DocumentFile file = DocumentFile.fromTreeUri(requireActivity(), list.get(0).getUri());

            Log.e("TAG", "documentfile: " + file);

            statusList.clear();

            if (file == null) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(R.string.no_files_found);
                    Toast.makeText(getActivity(), getString(R.string.no_files_found), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
                return;
            }

            DocumentFile[] statusFiles = file.listFiles();

            if (statusFiles.length == 0) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(R.string.no_files_found);
                    Toast.makeText(getActivity(), getString(R.string.no_files_found), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
                return;
            }

            for (DocumentFile documentFile : statusFiles) {

                if (Objects.requireNonNull(documentFile.getName()).contains(".nomedia"))
                    continue;

                Status status = new Status(documentFile);

                //if (!status.isVideo()) {
                    statusList.add(status);
                //}

            }

            mainHandler.post(() -> {

                if (statusList.size() <= 0) {
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(R.string.no_files_found);
                } else {
                    messageTextView.setVisibility(View.GONE);
                    messageTextView.setText("");
                }
                
                Log.e("TAG", "status: " + statusList.size());

                statusAdapter = new StatusAdapter(statusList, statusClickOnListener);
                recyclerView.setAdapter(statusAdapter);
                statusAdapter.notifyItemRangeChanged(0, statusList.size());
                progressBar.setVisibility(View.GONE);
            });

            swipeRefreshLayout.setRefreshing(false);

        });
    }

    private void setWindowColors(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_theme_1));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.dark_theme_1));
        }

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }



    public void showDownloadDialog(Activity activity, InterstitialAd interstitialAd){
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LinearLayout downloadingLL = (LinearLayout) dialog.findViewById(R.id.downloading);
        LinearLayout downloadedLL = (LinearLayout) dialog.findViewById(R.id.downloaded);
        TextView downloadedPath = (TextView) dialog.findViewById(R.id.downloaded_path);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            downloadedPath.setText("Saved to " + Environment.DIRECTORY_DCIM + "/" + APP_NAME);
        else
            downloadedPath.setText("Saved to " + APP_DIR);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                downloadedLL.setVisibility(View.VISIBLE);
                downloadingLL.setVisibility(View.INVISIBLE);

                 dialog.dismiss();

                // makeShortToast("File is saved to gallery");
                // showInterstitialAd(interstitialAd, activity);




               /* if(mInterstitialAd != null){
                    mInterstitialAd.show(activity);
                }*/




            }
        },2000);





        CardView earn_lives = (CardView) dialog.findViewById(R.id.open_button);
        earn_lives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();


                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Set the type of file you want to open
                intent.setType("*/*"); // You can set a specific file type here, e.g., "image/*" for images

                // Start the activity to open the file
                activity.startActivity(intent);
            }
        });

        ImageView cancelButton = (ImageView) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Window window = dialog.getWindow();
        //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        dialog.show();

    }


}
