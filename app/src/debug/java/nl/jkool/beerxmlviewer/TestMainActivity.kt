package nl.jkool.beerxmlviewer

import android.os.Build
import android.os.Bundle

class TestMainActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        super.onCreate(savedInstanceState)
    }

    override fun setInitialContent() = Unit
}
