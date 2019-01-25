package cn.libery.analysis.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test()
        val greeter = Greeter("libery")
        greeter.sayHello()
    }

    private fun test(): String {
        val test = "123"
        Log.e("Main", test)
        return test
    }

    internal class Greeter(private val name: String) {

        fun sayHello(): String {
            return "Hello, $name"
        }
    }

}
