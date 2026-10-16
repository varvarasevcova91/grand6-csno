package com.featherroad.quest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.featherroad.quest.core.di.ServiceLocator
import com.featherroad.quest.databinding.ActivityMainBinding
import com.featherroad.quest.presentation.splash.SplashFragment

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)

        val created = ActivityMainBinding.inflate(layoutInflater)
        binding = created
        setContentView(created.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SplashFragment())
                .commit()
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
