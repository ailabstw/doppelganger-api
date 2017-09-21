//
//  AppDelegate.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/16.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import UIKit
import SVProgressHUD

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        SVProgressHUD.setDefaultMaskType(.clear)
        SVProgressHUD.setDefaultStyle(.dark)
        
        // Clear all cookies so that we can have a fresh start
        let url = URL(string: "https://api.ailabs.tw")!
        let cstorage = HTTPCookieStorage.shared
        if let cookies = cstorage.cookies(for: url) {
            for cookie in cookies {
                cstorage.deleteCookie(cookie)
            }
        }
        
        return true
    }

}

