//
//  WebViewController.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/20.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import UIKit
import WebKit

class WebViewController: UIViewController {

    var webView: WKWebView!
    
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.allowsAirPlayForMediaPlayback = false
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        view = webView
        
        let closeButton = UIButton()
        closeButton.setImage(#imageLiteral(resourceName: "Close Icon"), for: .normal)
        closeButton.frame = CGRect(x: UIScreen.main.bounds.size.width - 20 - 28, y: 28, width: 28, height: 28)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        view.addSubview(closeButton)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let myURL = URL(string: "http://ailabs.tw/privacy/index.html")
        let myRequest = URLRequest(url: myURL!)
        webView.load(myRequest)
    }

    @objc func close() {
        dismiss(animated: true)
    }

}
