//
//  CompareResult.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/18.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import Foundation

struct CompareResult: Codable {
//    let from: String
    var message: String
    let result: Float
    let left: FaceResult
    let right: FaceResult
}

struct FaceResult: Codable {
    let faceCount: Int
    let width: Int
    let height: Int
    let chosenBox: [Box]
    
    enum CodingKeys: String, CodingKey {
        case faceCount = "face_count"
        case width
        case height
        case chosenBox = "chosen_box"
    }
    
    
}

struct Box: Codable {
    let x: Int
    let y: Int
    let w: Int
    let h: Int
}
