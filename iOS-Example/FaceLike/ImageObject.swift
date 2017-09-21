//
//  ImageObject.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/18.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import Foundation
import UIKit

struct ImageObject {
    let image: UIImage
    let mime: String
    let fileName: String
    
    init(image: UIImage, fileURL: URL) {
        self.image = image
        self.fileName = fileURL.lastPathComponent
        
        let exten = fileURL.pathExtension
        
        switch exten.lowercased() {
        case "jpeg", "jpg":
            mime = "image/jpeg"
        case "png":
            mime = "image/png"
        case "tiff":
            mime = "image/tiff"
        default:
            mime = "image/jpeg"
        }
    }
    
    init(image: UIImage, mime: String, fileName: String) {
        self.image = image
        self.mime = mime
        self.fileName = fileName
    }
    
    
}
