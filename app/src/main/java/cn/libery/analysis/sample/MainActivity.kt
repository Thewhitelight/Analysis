package cn.libery.analysis.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.libery.analysis.annotation.Track

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test()
        val greeter = Greeter("libery")
        greeter.sayHello()
    }

    @Track
    private fun test() {
        val test = "123"
        Log.e("Main", test)
    }


    @Track
    internal class Greeter(private val name: String) {

        fun sayHello(): String {
            return "Hello, $name"
        }
    }
}
