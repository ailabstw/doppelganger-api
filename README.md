#  AILabs 相似度 API

你可以透過 RESTFul API 輕易地使用 AILabs 的演算法來取得不同相片中人臉的相似程度以及相關資訊。

## Demo App 下載

<a href='https://play.google.com/store/apps/details?id=tw.ailabs.doppelganger&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="256"/></a>

## API 說明

透過 `form-data` 的 POST 方式上傳兩張照片以取得結果，你也可以一次只上傳一張，等到兩張都上傳完成時便可收到比較的結果，一次上人傳一張的功能需要 enable `session-cookie`，server 會以 session 來紀錄你的身分與狀態。

## API Endpoint 

```
https://api.ailabs.tw/doppelganger/upload
```

## 參數

|      參數名稱 | 格式              |
| --------: | :-------------- |
|  **left** | binary of image |
| **right** | binary of image |

**注意:** 目前只接受 `jpg` 與 `jpeg` 格式的圖片

## Request Example 

```shell
curl -X POST -F "left=@宋芸樺.jpg" -F "right=@夏于喬.jpg" https://api.ailabs.tw/doppelganger/upload
```

## HTTP Status 列表

|     Status Code | 描述                   |
| --------------: | :------------------- |
|    **200 - OK** | 一切正常，應收到辨識結果         |
|  **202 - 單張OK** | 已經收到一張圖片，需要再上傳另一邊的圖片 |
| **415 -  格式錯誤** | 上傳了非 jpg 格式的檔案       |

 ## Example Response

```javascript
{
  "from":"doppelganger",
  "message":"",
  "result":67.0695381087316,
  "left":{
    "chosen_box":[{
      "h":110,
      "w":105,
      "x":110,
      "y":122
    }],
    "face_count":1,
    "height":400,
    "width":264
  },
  "right":{
    "chosen_box":[{
      "h":115,
      "w":111,
      "x":48,
      "y":109
    }],
    "face_count":1,
    "height":400,
    "width":265
  }
}
```

|                 Attributes | 描述                                       |
| -------------------------: | ---------------------------------------- |
|         **from**  [String] | 演算法名稱，固定為 doppelganger                   |
|      **message**  [String] | 結果訊息                                     |
|         **result** [Float] | 相似度分數，介於 0 - 100 之間                      |
|       **left**,  **right** | 左邊圖或右邊圖的臉部分析結果                           |
|      **height**, **width** | 結果圖片的高和寬，可能因為效能而改變圖片尺寸，用來對比 chosen_box 的座標與尺寸 |
|             **face_count** | 圖中的人臉數目                                  |
| **h**, **w**, **x**, **y** | 偵測到臉的方形範圍，分別為 高、寬、左上角的  x 、 y 座標         |

## 聯絡我們

* 若有發現任何問題，歡迎發送 [issue](https://github.com/ailabstw/doppelganger-api/issues)

## License

MIT license
