package project.com.share_me.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import project.com.share_me.MainActivity;
import project.com.socio_fi.R;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        ImageView logo = (ImageView) findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        logo.startAnimation(animation); // starting the rotating animation for the logo
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) { // if animation is over then finish this activity and start a new activity
                finish();
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i); // starting MainActivity.java
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
