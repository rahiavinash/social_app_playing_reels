package com.wedo8020.app.mainui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.wedo8020.app.R
import com.wedo8020.app.mainui.base.BaseActivity
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import io.branch.referral.Branch.*

class SplashActivity : BaseActivity() {
    val handler = Handler(Looper.myLooper()!!)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setStatusBarTransparent()
//        setUpSplash()

        Log.e("sdgkjhsf",Prefs.init().isLogIn.toString())
    }
    private fun setUpSplash(){
        handler.postDelayed({
               val intent = if (Prefs.init().isLogIn){
                Intent(this, MainActivity::class.java)
            }else{
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        },3000)
    }

    override fun onStart() {
        super.onStart()
        initBranch()
    }
    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        reInitBranchIO(intent)
    }
    private fun reInitBranchIO(intent: Intent?){
        this.intent = intent
        if (intent != null && intent.hasExtra("branch_force_new_session") && intent.getBooleanExtra("branch_force_new_session",false)) {
            sessionBuilder(this).withCallback { referringParams, error ->
                if (error != null) {
                    setUpSplash()
                    Log.e("BranchSDK_Tester", error.message)
                } else if (referringParams != null) {
                    Log.i("BranchSDK_Tester", referringParams.toString())
                }else{
                    setUpSplash()
                    Log.i("BranchSDK_Tester", "not by deep link")
                }
            }.reInit()
        }
    }
    private fun initBranch(){
        sessionBuilder(this).withCallback { branchUniversalObject, linkProperties, error ->
            if (error != null) {
                setUpSplash()
                Log.e("BranchSDK_Tester", "branch init failed. Caused by -" + error.message)
            } else {
                Log.i("BranchSDK_Tester", "branch init complete!")
                if (branchUniversalObject != null) {
                    Utils.runWithDelay(3000){
                        val intent = if (Prefs.init().isLogIn){
                            Intent(this, MainActivity::class.java)
                                .putExtra(AppConstant.CANONICAL_IDENTIFIER,branchUniversalObject.canonicalIdentifier)
                        }else{
                            Intent(this, LoginActivity::class.java)
                                .putExtra(AppConstant.CANONICAL_IDENTIFIER,branchUniversalObject.canonicalIdentifier)
                        }
                        startActivity(intent)
                        finish()
                    }
                    Log.i("BranchSDK_Tester", "title " + branchUniversalObject.title)
                    Log.i("BranchSDK_Tester", "CanonicalIdentifier " + branchUniversalObject.canonicalIdentifier)
                    Log.i("BranchSDK_Tester", "metadata " + branchUniversalObject.contentMetadata.convertToJson())
                }else{
                    setUpSplash()
                    Log.i("BranchSDK_Tester", "not by deep link")
                }
            }
        }.withData(this.intent.data).init()
    }

}