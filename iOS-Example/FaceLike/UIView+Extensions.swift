//
//  UIView+Extensions.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/19.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import Foundation
import UIKit

extension UIView {
    var screenShotWithLabel: UIImage {
        UIGraphicsBeginImageContextWithOptions(self.frame.size, true, 1)
        if self.responds(to: #selector(drawHierarchy(in:afterScreenUpdates:))) {
            self.drawHierarchy(in: self.bounds, afterScreenUpdates: true)
        } else {
            self.layer.render(in: UIGraphicsGetCurrentContext()!)
        }
        let font = UIFont(name: "HelveticaNeue-Light", size: 10.0)!
        let textAttributes = [
            NSAttributedStringKey.font: font as Any,
            NSAttributedStringKey.foregroundColor: UIColor.lightGray as Any,
        ]
        let textRect = CGRect(x: self.frame.size.width - 91, y: self.frame.size.height - 12, width: 91, height: 12)
        
        let text = "Powered by AILabs" as NSString
        text.draw(in: textRect, withAttributes: textAttributes)
        
        let rasterizedView = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()
        
        return rasterizedView
    }
}
