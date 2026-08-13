//
//  AppDelegate.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/9/23.
//

import Foundation
import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseAnalytics
import TeeTimeCaddieKit
import NSExceptionKtCrashlytics

// Firebase requires us to use an App Delegate, and to disable method swizzing.
// More Info: https://firebase.google.com/docs/ios/learn-more#swiftui
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

            // Xcode Previews launch the app, which runs this delegate. Skip Firebase + KMP init:
            // FirebaseApp.configure() throws without a GoogleService-Info context, and calling into
            // TeeTimeCaddieKit spins up the Kotlin/Native runtime in the preview host. Views render
            // from preview* model data and don't need the live SDK.
            if IS_PREVIEW {
                return true
            }

            FirebaseApp.configure() // do this first
            NSExceptionKt.addReporter(.crashlytics(causedByStrategy: .append))
            // The app provides the platform Firebase-backed loggers; the shared SDK depends only
            // on the logger interfaces, not on Firebase directly.
            TeeTimeCaddieSdk.companion.initialize(
                firestoreClient: FirebaseFirestoreClient(useEmulator: IS_DEBUG_BUILD),
                authService: FirebaseAuthService(useEmulator: IS_DEBUG_BUILD),
                errorLogger: FirebaseErrorLogger(),
                transactionLogger: FirebaseTransactionLogger()
            )
            Analytics.setAnalyticsCollectionEnabled(!IS_DEBUG_BUILD && !IS_PREVIEW)
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        //This is required to setup Firebase Authentication without swizzling.
        //More Info: https://firebase.google.com/docs/auth/ios/phone-auth#appendix:-using-phone-sign-in-without-swizzling
        //      and: https://firebase.google.com/docs/ios/learn-more#swiftui
        Auth.auth().setAPNSToken(deviceToken, type: .prod)

        // Further handling of the device token if needed by the app
        // ...
    }
    
    func application(_ application: UIApplication,
        didReceiveRemoteNotification notification: [AnyHashable : Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {

        // Handle Firebase notifications
        if Auth.auth().canHandleNotification(notification) {
            completionHandler(.noData)
            return
        }
        
        // Add other handling here
    }
    
    func application(_ application: UIApplication, open url: URL,
                              options: [UIApplication.OpenURLOptionsKey : Any]) -> Bool {

        // Handle firebase URL's
        if Auth.auth().canHandle(url) {
            return true
        }
        
        // URL not auth related;.
        return false
    }
    
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
      for urlContext in URLContexts {
          let url = urlContext.url
          Auth.auth().canHandle(url)
      }
      // URL not auth related; it should be handled separately.
    }
    
}
