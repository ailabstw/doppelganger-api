//
//  ViewController.swift
//  FaceLike
//
//  Created by Pofat Tseng on 2017/9/16.
//  Copyright © 2017年 AILabs. All rights reserved.
//

import UIKit
import Alamofire
import SVProgressHUD

class ViewController: UIViewController {
    
    @IBOutlet weak var leftContainer: UIImageView!
    @IBOutlet weak var rightContainer: UIImageView!
    
    @IBOutlet weak var resultLabel: UILabel!
    @IBOutlet weak var textView: UITextView!
    
    @IBOutlet weak var privactyButton: UIButton!
    @IBOutlet weak var containerView: UIView!
    @IBOutlet weak var shareButton: UIButton!
    
    fileprivate var imagePicker = UIImagePickerController()
    fileprivate var selectedPosition: ImagePosition! = nil
    fileprivate var leftImage: ImageObject? = nil
    fileprivate var rightImage: ImageObject? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // TapGesture for both imageViews
        let leftTapGesture = UITapGestureRecognizer(target: self, action: #selector(clickImag(recognizer:)))
        let rightTapGesture = UITapGestureRecognizer(target: self, action: #selector(clickImag(recognizer:)))
        
        leftContainer.addGestureRecognizer(leftTapGesture)
        rightContainer.addGestureRecognizer(rightTapGesture)
        
        // UI tweaks
        imagePicker.allowsEditing = false
        imagePicker.delegate = self
        
        textView.isEditable = false
        textView.isScrollEnabled = false
        textView.delegate = self
        
        shareButton.layer.cornerRadius = 7
        shareButton.clipsToBounds = true
        shareButton.isHidden = true
        
        let attributes = [
            NSAttributedStringKey.font : UIFont(name: "PingFangTC-Light", size: 12.0)! as Any,
            NSAttributedStringKey.foregroundColor : UIColor.darkGray as Any,
            NSAttributedStringKey.underlineStyle : NSNumber(value: NSUnderlineStyle.styleSingle.rawValue) as Any
        ]
        let attributedTitle = NSAttributedString(string: "AILabs 軟體使用條款", attributes: attributes)
        privactyButton.setAttributedTitle(attributedTitle, for: .normal)
        
    }
    
    // Make textView resign firstresponder while touch other part of the screen
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        textView.endEditing(true)
    }
    
    @objc func clickImag(recognizer: UITapGestureRecognizer) {
        if let sourceView = recognizer.view, sourceView == leftContainer {
            selectedPosition = .left
        } else if let sourceView = recognizer.view, sourceView == rightContainer {
            selectedPosition = .right
        } else {
            print("come from nowhere")
            return
        }
        
        // Action sheet
        let alert = UIAlertController(title: "請選擇含有人臉的照片", message: "", preferredStyle: .actionSheet)
        
        let cameraAction = UIAlertAction(title: "拍新照片", style: .default) { [unowned self] action in
            self.imagePicker.sourceType = .camera
            self.present(self.imagePicker, animated: true)
        }
        
        let photoAction = UIAlertAction(title: "從相本裡選", style: .default) { action  in
            self.imagePicker.sourceType = .photoLibrary
            self.present(self.imagePicker, animated: true)
        }
        
        let cancel = UIAlertAction(title: "取消", style: .cancel)
        
        alert.addAction(cameraAction)
        alert.addAction(photoAction)
        alert.addAction(cancel)
        present(alert, animated: true)
    }
    
    // Share the result
    @IBAction func share() {
        let screenshot = containerView.screenShotWithLabel
        let activityViewController = UIActivityViewController(activityItems: [screenshot], applicationActivities: nil)
        activityViewController.completionWithItemsHandler = { type, success, items, error in
            if success {
                SVProgressHUD.showInfo(withStatus: "分享成功，再換個臉試試吧")
            }
        }
        present(activityViewController, animated: true)
    }
    
    // Go to privacy policy
    @IBAction func privacy() {
        let privacyVC = WebViewController()
        present(privacyVC, animated: true)
    }
    
    // Call API
    func sendRequest() {
        
        Alamofire.upload(multipartFormData: { [unowned self] multipartFormData in
            // You can send both or one of the images
            if let leftImage = self.leftImage?.image {
                multipartFormData.append(UIImagePNGRepresentation(leftImage)!, withName: "left", fileName: self.leftImage!.fileName, mimeType: self.leftImage!.mime)
            }
            
            if let rightImage = self.rightImage?.image {
                multipartFormData.append(UIImagePNGRepresentation(rightImage)!, withName: "right", fileName: self.rightImage!.fileName, mimeType: self.rightImage!.mime)
            }
            
            
        }, to: "https://api.ailabs.tw/doppelganger/upload") { result in
            
            switch result {
            case .success(let upload, _, _):
                upload.responseJSON { [unowned self] response in
                    // Sitll need one more image
                    if response.response?.statusCode == 202 {
                        // Do nothing
                        print("still one side of image not uploaded")
                    } else {
                        let decoder = JSONDecoder()
                        
                        if let data = response.data, let faceResult = try? decoder.decode(CompareResult.self, from: data) {
                            
                            let resultInt = Int(ceil(faceResult.result))
                            self.resultLabel.attributedText = self.generateAttributedStringOfResult(from: "\(resultInt)分像")
                            self.textView.text = self.messageBasedOn(result: resultInt)
                            self.textView.isEditable = true
                            self.textView.becomeFirstResponder()
                            self.shareButton.isHidden = false
                            
                            SVProgressHUD.dismiss()
                        } else {
                            print("parse failed")
                            SVProgressHUD.showError(withStatus: "Server 好像出了點小錯")
                        }
                    }
                    
                    
                }
            case .failure(let decodingError):
                print("error : \(decodingError.localizedDescription)")
                SVProgressHUD.showError(withStatus: "網路請求出了點問題")
            }
        }
    }
    
    // Generate attributed string for result
    func generateAttributedStringOfResult(from string: String) -> NSAttributedString {
        let restFont = UIFont(name: "PingFangTC-Semibold", size: 24.0)!
        let attributedString =  NSMutableAttributedString.init(string: string)
        
        if let index = string.index(of: "分") {
            let intIndex = string.distance(from: string.startIndex, to: index)
            attributedString.setAttributes([NSAttributedStringKey.font : restFont], range: NSMakeRange(intIndex,2))
        }
        
        return attributedString
    }

    // Generate message based on result
    func messageBasedOn(result: Int) -> String {
        switch result {
        case 90 ... 100:
            return "你們是同一個人吧"
        case 70 ..< 90:
            return "有點神似"
        case 40 ..< 70:
            return "說像也不像，說不像也像"
        default:
            return "不像"
        }
    }
}

// MARK: - UIImagePickerController Delegate
extension ViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        if let pickedImage = info[UIImagePickerControllerOriginalImage] as? UIImage {
            var imageObject: ImageObject! = nil
            if let imageURL = info[UIImagePickerControllerImageURL] as? URL {
                imageObject = ImageObject(image: pickedImage, fileURL: imageURL)
            } else {
                imageObject = ImageObject(image: pickedImage, mime: "image/jpeg", fileName: "camera.jpeg")
            }
            
            switch selectedPosition {
            case .left:
                leftImage = imageObject
                leftContainer.image = pickedImage
            case .right:
                rightImage = imageObject
                rightContainer.image = pickedImage
            default:
                print("some error")
            }
        }
        
        // Send request when there's any image
        if leftImage != nil || rightImage != nil {
            
            // Show loading dialog only when both images are ready
            if leftImage != nil && rightImage != nil {
                SVProgressHUD.show()
            }
            
            sendRequest()
        }
        
        dismiss(animated: true)
    }
}

// MARK: - UITextView Delegate
extension ViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if text == "\n" {
            textView.resignFirstResponder()
            return false
        } else {
            return true
        }
    }
}


// Enum for mark image position
enum ImagePosition {
    case left, right
}


