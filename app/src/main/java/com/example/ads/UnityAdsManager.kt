package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object UnityAdsManager {
  private const val TAG = "UnityAdsManager"

  // Game ID provided for Unity Ads
  const val DEFAULT_GAME_ID = "c306a5a7-2cb1-4875-bd40-6313443bf2c7"

  // Standard Unity placement IDs
  const val INTERSTITIAL_PLACEMENT = "Interstitial_Android"
  const val REWARDED_PLACEMENT = "Rewarded_Android"
  const val BANNER_PLACEMENT = "Banner_Android"

  var isSdkInitialized = false
    private set

  var isInterstitialLoaded = false
    private set

  var isRewardedLoaded = false
    private set

  /**
   * Initializes the Unity Ads SDK safely.
   * Safe to call multiple times (idempotent) and protected against all runtime crashes.
   */
  fun initialize(context: Context, gameId: String = DEFAULT_GAME_ID, testMode: Boolean = true) {
    try {
      if (UnityAds.isInitialized) {
        isSdkInitialized = true
        return
      }

      Log.d(TAG, "Initializing Unity Ads with Game ID: $gameId (testMode: $testMode)")
      UnityAds.initialize(
        context.applicationContext,
        gameId,
        testMode,
        object : IUnityAdsInitializationListener {
          override fun onInitializationComplete() {
            isSdkInitialized = true
            Log.d(TAG, "Unity Ads initialized successfully")
            loadInterstitial()
            loadRewarded()
          }

          override fun onInitializationFailed(
            error: UnityAds.UnityAdsInitializationError?,
            message: String?
          ) {
            isSdkInitialized = false
            Log.w(TAG, "Unity Ads initialization failed: $error - $message")
          }
        }
      )
    } catch (e: Throwable) {
      Log.e(TAG, "Exception while initializing Unity Ads (handled safely): ${e.message}", e)
      isSdkInitialized = false
    }
  }

  fun loadInterstitial(placementId: String = INTERSTITIAL_PLACEMENT) {
    try {
      if (!UnityAds.isInitialized) return

      UnityAds.load(
        placementId,
        object : IUnityAdsLoadListener {
          override fun onUnityAdsAdLoaded(placementId: String?) {
            Log.d(TAG, "Interstitial ad loaded: $placementId")
            isInterstitialLoaded = true
          }

          override fun onUnityAdsFailedToLoad(
            placementId: String?,
            error: UnityAds.UnityAdsLoadError?,
            message: String?
          ) {
            Log.w(TAG, "Failed to load interstitial ad: $placementId - $error ($message)")
            isInterstitialLoaded = false
          }
        }
      )
    } catch (e: Throwable) {
      Log.w(TAG, "Exception loading interstitial: ${e.message}")
      isInterstitialLoaded = false
    }
  }

  fun loadRewarded(placementId: String = REWARDED_PLACEMENT) {
    try {
      if (!UnityAds.isInitialized) return

      UnityAds.load(
        placementId,
        object : IUnityAdsLoadListener {
          override fun onUnityAdsAdLoaded(placementId: String?) {
            Log.d(TAG, "Rewarded ad loaded: $placementId")
            isRewardedLoaded = true
          }

          override fun onUnityAdsFailedToLoad(
            placementId: String?,
            error: UnityAds.UnityAdsLoadError?,
            message: String?
          ) {
            Log.w(TAG, "Failed to load rewarded ad: $placementId - $error ($message)")
            isRewardedLoaded = false
          }
        }
      )
    } catch (e: Throwable) {
      Log.w(TAG, "Exception loading rewarded ad: ${e.message}")
      isRewardedLoaded = false
    }
  }

  /**
   * Shows an interstitial ad safely.
   * If not loaded, uninitialized, or fails, smoothly executes [onDismiss] to ensure the game continues.
   */
  fun showInterstitial(
    activity: Activity,
    placementId: String = INTERSTITIAL_PLACEMENT,
    onDismiss: () -> Unit = {}
  ) {
    try {
      if (activity.isFinishing || activity.isDestroyed) {
        onDismiss()
        return
      }

      if (!UnityAds.isInitialized) {
        Log.d(TAG, "Unity Ads not initialized; skipping interstitial safely")
        onDismiss()
        return
      }

      UnityAds.show(
        activity,
        placementId,
        UnityAdsShowOptions(),
        object : IUnityAdsShowListener {
          override fun onUnityAdsShowStart(placementId: String?) {
            Log.d(TAG, "Interstitial ad show started: $placementId")
          }

          override fun onUnityAdsShowClick(placementId: String?) {
            Log.d(TAG, "Interstitial ad clicked: $placementId")
          }

          override fun onUnityAdsShowComplete(
            placementId: String?,
            state: UnityAds.UnityAdsShowCompletionState?
          ) {
            Log.d(TAG, "Interstitial ad show completed: $placementId ($state)")
            isInterstitialLoaded = false
            loadInterstitial(placementId ?: INTERSTITIAL_PLACEMENT)
            activity.runOnUiThread { onDismiss() }
          }

          override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
          ) {
            Log.w(TAG, "Interstitial ad show failed: $placementId - $error ($message)")
            isInterstitialLoaded = false
            loadInterstitial(placementId ?: INTERSTITIAL_PLACEMENT)
            activity.runOnUiThread { onDismiss() }
          }
        }
      )
    } catch (e: Throwable) {
      Log.e(TAG, "Error in showInterstitial (executing fallback): ${e.message}", e)
      onDismiss()
    }
  }

  /**
   * Shows a rewarded ad safely.
   * If watched completely, [onRewardEarned] is invoked.
   * In all cases when the ad finishes, fails, or cannot load, [onDismiss] is invoked.
   */
  fun showRewarded(
    activity: Activity,
    placementId: String = REWARDED_PLACEMENT,
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit = {}
  ) {
    try {
      if (activity.isFinishing || activity.isDestroyed) {
        onDismiss()
        return
      }

      if (!UnityAds.isInitialized) {
        Log.d(TAG, "Unity Ads not initialized; executing fallback safely")
        onDismiss()
        return
      }

      UnityAds.show(
        activity,
        placementId,
        UnityAdsShowOptions(),
        object : IUnityAdsShowListener {
          override fun onUnityAdsShowStart(placementId: String?) {
            Log.d(TAG, "Rewarded ad show started: $placementId")
          }

          override fun onUnityAdsShowClick(placementId: String?) {
            Log.d(TAG, "Rewarded ad clicked: $placementId")
          }

          override fun onUnityAdsShowComplete(
            placementId: String?,
            state: UnityAds.UnityAdsShowCompletionState?
          ) {
            Log.d(TAG, "Rewarded ad show completed: $placementId ($state)")
            isRewardedLoaded = false
            loadRewarded(placementId ?: REWARDED_PLACEMENT)
            activity.runOnUiThread {
              if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                onRewardEarned()
              }
              onDismiss()
            }
          }

          override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
          ) {
            Log.w(TAG, "Rewarded ad show failed: $placementId - $error ($message)")
            isRewardedLoaded = false
            loadRewarded(placementId ?: REWARDED_PLACEMENT)
            activity.runOnUiThread { onDismiss() }
          }
        }
      )
    } catch (e: Throwable) {
      Log.e(TAG, "Error in showRewarded (executing fallback): ${e.message}", e)
      onDismiss()
    }
  }
}
