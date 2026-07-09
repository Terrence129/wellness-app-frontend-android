// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.ui.history.HistoryActivity
import com.example.wellnessapp.ui.summary.WeeklySummaryActivity

class TrendsHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trends_history)

        findViewById<View>(R.id.btnTrendsHistoryBack).setOnClickListener { finish() }
        findViewById<View>(R.id.cardTrends).setOnClickListener {
            startActivity(Intent(this, WeeklySummaryActivity::class.java))
        }
        findViewById<View>(R.id.cardHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        BottomNavigationController.attach(this, BottomNavigationController.ActiveItem.TRENDS_HISTORY)
    }
}
