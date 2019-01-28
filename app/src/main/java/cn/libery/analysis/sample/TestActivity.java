package cn.libery.analysis.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.libery.analysis.sample.test.Logger3;
import cn.libery.analysis.sample.test.Logger4;

/**
 * @author shizhiqiang on 2019/1/2.
 * @description
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("TestActivity", "onCreate");
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_main).setOnClickListener(this);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test("233");
            }
        });

        startSleepyThread();

        test("0988");

        test3("23");

        Greeter greeter = new Greeter("Jake");
        String hello = greeter.sayHello();
        Log.d("Greeting", hello);

        Charmer charmer = new Charmer("Jake");
        String ok = charmer.askHowAreYou();
        String name = modifyCharmer(charmer).name;
        Log.d("Charming", ok);
        Log.d("CharmingName", name);

        Logger.log("logger", "test");
        Logger2.log("logger2", "test");
        Logger3.log("logger3", "test");
        Logger4.log("logger4", "test");
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Charmer modifyCharmer(Charmer c) {
        c.name = "Mac";
        return c;
    }

    public void test2(int i, int t) {
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
                test2(2, 3);
                startActivity(new Intent(TestActivity.this, MainActivity.class));
                break;
            default:
        }
    }

    static class Greeter {
        private final String name;

        Greeter(String name) {
            this.name = name;
        }

        private String sayHello() {
            return "Hello, " + name;
        }
    }

    static class Charmer {
        private String name;

        private Charmer(String name) {
            this.name = name;
        }

        public String askHowAreYou() {
            return "How are you " + name + "?";
        }
    }

    private void startSleepyThread() {
        new Thread(new Runnable() {
            private static final long SOME_POINTLESS_AMOUNT_OF_TIME = 50;

            @Override
            public void run() {
                int length = Thread.currentThread().getStackTrace().length;
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[length - 1];
                Log.e("tag2", stackTraceElement.toString() + length);
                sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
            }

            private void sleepyMethod(long milliseconds) {
                SystemClock.sleep(milliseconds);
            }
        }, "I'm a lazy thr.. bah! whatever!").start();
    }
}
