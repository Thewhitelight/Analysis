package cn.libery.analysis.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.libery.analysis.annotation.Track;

/**
 * @author shizhiqiang on 2019/1/2.
 * @description
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_main).setOnClickListener(this);

        Greeter greeter = new Greeter("Jake");
        Log.d("Greeting", greeter.sayHello());

        Charmer charmer = new Charmer("Jake");
        Log.d("Charming", charmer.askHowAreYou());

        startSleepyThread();
        test("0988");
        test3("23");
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void test2(int i) {
        System.out.println(i);
    }

    private void test(String s) {
        System.out.println(s);
    }

    protected void test3(String s) {
        System.out.println(s);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_main:
                test2(2);
                startActivity(new Intent(TestActivity.this, MainActivity.class));
                break;
            default:
        }
    }

    @Track(level = Log.ERROR)
    static class Greeter {
        private final String name;

        Greeter(String name) {
            this.name = name;
        }

        private String sayHello() {
            return "Hello, " + name;
        }
    }

    @Track(level = Log.WARN)
    static class Charmer {
        private final String name;

        private Charmer(String name) {
            this.name = name;
        }

        @Track(level = Log.ERROR)
        public String askHowAreYou() {
            return "How are you " + name + "?";
        }
    }

    @Track(level = Log.DEBUG)
    private void startSleepyThread() {
        new Thread(new Runnable() {
            private static final long SOME_POINTLESS_AMOUNT_OF_TIME = 50;

            @Override
            public void run() {
                sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
            }

            @Track(level = Log.INFO)
            private void sleepyMethod(long milliseconds) {
                SystemClock.sleep(milliseconds);
            }
        }, "I'm a lazy thr.. bah! whatever!").start();
    }
}
