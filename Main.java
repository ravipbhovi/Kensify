package com.kensify.kpsc.activity;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
import static com.kensify.kpsc.utils.Ads.NATIVE_2;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.kensify.kpsc.R;
import com.kensify.kpsc.admin.activity.AdminDashboard;
import com.kensify.kpsc.utils.Ads;
import com.kensify.kpsc.utils.AppUtils;

public class Main extends AppCompatActivity {

    private Ads ads;
    private Dialog closeAppDialog;
    private InstallStateUpdatedListener installStateUpdatedListener = null;
    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 11;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        com.kensify.kpsc.databinding.MainBinding mainBinding = DataBindingUtil.setContentView(this, R.layout.main);
        mainBinding.setAppName(AppUtils.appName);

        ads = new Ads();

      //  initCloseAppDialog();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             getWindow().setNavigationBarColor(getResources().getColor(R.color.white2));
             getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
         }


        TextView startButton = findViewById(R.id.start_button_ll);
         startButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Main.this, HomePage.class);
                 startActivity(intent);
             }
         });

         startButton.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 Toast.makeText(Main.this, "Welcome to Admin Console", Toast.LENGTH_SHORT).show();
                 Intent adminIntent = new Intent(Main.this, AdminDashboard.class);
                 startActivity(adminIntent);
                 return true;
             }
         });


        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState state) {
                if (state.installStatus() == InstallStatus.DOWNLOADED){
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackBarForCompleteUpdate();
                } else if (state.installStatus() == InstallStatus.INSTALLED){
                    if (mAppUpdateManager != null){
                        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                    }

                } else {
                    Log.e("TAG", "InstallStateUpdatedListener: state: " + state.installStatus());
                }
            }
        };


        //FrameLayout native_ad_container = findViewById(R.id.id_native_ad);
        //ads.loadNativeAd(Main.this,native_ad_container, NATIVE_1);


        TextView textViewTerms = findViewById(R.id.textViewTerms);

        String text = getString(R.string.terms_text);

        SpannableString spannableString = new SpannableString(text);

        // Terms of Service span
        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://therpbcompany.blogspot.com/2024/07/terms-conditions.html"));
                startActivity(browserIntent);
            }
        };
        ForegroundColorSpan termsColorSpan = new ForegroundColorSpan(Color.BLUE);

        // Privacy Policy span
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://therpbcompany.blogspot.com/2024/07/the-rpb-company-privacy-policy.html"));
                startActivity(browserIntent);
            }
        };
        ForegroundColorSpan privacyColorSpan = new ForegroundColorSpan(Color.BLUE);

        int termsStart = text.indexOf("Terms of Service");
        int termsEnd = termsStart + "Terms of Service".length();

        int privacyStart = text.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();

        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(termsColorSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyColorSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewTerms.setText(spannableString);
        textViewTerms.setMovementMethod(LinkMovementMethod.getInstance());

     }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("TAG", "onActivityResult: app download failed");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }



    private void popupSnackBarForCompleteUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "New app is ready!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Restart", view -> {
            if (mAppUpdateManager != null){
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_blue_bright));
        snackbar.show();
    }

    public void initCloseAppDialog() {
        closeAppDialog = new Dialog(this);
        closeAppDialog.requestWindowFeature(1);
        closeAppDialog.setContentView(R.layout.dialog_go_back);

        Window window = closeAppDialog.getWindow();
        if(window != null){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }


        FrameLayout nativeAdContainer = closeAppDialog.findViewById(R.id.native_ad_container);
        ads.loadNativeAd(Main.this,nativeAdContainer, NATIVE_2);

        ((TextView) closeAppDialog.findViewById(R.id.tv_dialog_text)).setText(getString(R.string.sure_close_app));
        Button button = (Button) closeAppDialog.findViewById(R.id.bt_cancel);
        button.setText(getString(R.string.no));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeAppDialog.dismiss();
            }
        });
        Button button2 = (Button) closeAppDialog.findViewById(R.id.bt_yes);
        button2.setText(getString(R.string.yes));
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeAppDialog.dismiss();
                finish();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)){

                try {
                    mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, Main.this, RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackBarForCompleteUpdate();
            } else {
                Log.e("TAG", "checkForAppUpdateAvailability: something else");
            }
        });
    }

}

