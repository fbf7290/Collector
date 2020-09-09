**현재 개발 진행 중 (2020.08 ~)**

# **Stock Collector : 미국/한국 주식시장 주식  수집기**

사용 기술 : Scala, Cats, Akka, Lagom, Cassandra

Cassandra 실행 : docker run --name asset-cassandra -v /Users/wonryool/Desktop/Source/asset\ portfolio/Cassandra:/var/lib/cassandra -p 9042:9042 -d cassandra

설정할 환경 변수 : FCM_PROJECT_KEY, ADMIN_FCM_ID
메일 : rladnjsfbf@gmail.com




## ****코스피 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/kospi/stockList

[{"market":"kospi","name":"DSR","code":"155660"},
    {"market":"kospi","name":"GS","code":"078930"},
    {"market":"kospi","name":"GS글로벌","code":"001250"},
    {"market":"kospi","name":"HDC현대산업개발", "code":"294870"},
    {"market":"kospi","name":"LG이노텍","code":"011070"},
    {"market":"kospi","name":"LG전자","code":"066570"} ... ] ```
```


## ****코스닥 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/kosdaq/stockList

[{"market":"kosdaq","name":"CJ프레시웨이","code":"051500"},
    {"market":"kosdaq","name":"EDGC","code":"245620"},
    {"market":"kosdaq","name":"GST","code":"083450"}... ] 
```



## ****한국 전체 ETF 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/etf/stockList

[{"market":"kospi","name":"KODEX 200","code":"069500"},
    {"market":"kospi","name":"TIGER 200","code":"102110"}, 
    {"market":"kospi","name":"KODEX 200선물인버스2X","code":"252670"},
    {"market":"kospi","name":"KODEX 레버리지","code":"122630"},
    {"market":"kospi", "name":"KODEX 단기채권","code":"153130"}, ...] 
```


## ****한국 주식 일봉 가격 정보****
```
curl  http://localhost:9000/stock/korea/prices/:종목코드
    Ex. curl http://localhost:9000/stock/korea/prices/005930

[
    {
        "code": "005930",
        "date": 19900103,
        "close": 44800,
        "open": 44000,
        "high": 45000,
        "low": 43200,
        "volume": 26240
    }
    ... , 
    {
      "code": "005930",
      "date": 20200831,
      "close": 54000,
      "open": 56000,
      "high": 56100,
      "low": 54000,
      "volume": 31686567
    }
]
```






## ****미국 나스닥 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/usa/nasdaq/stockList

[{"market":"nasdaq","name":"Altaba Inc.","code":"AABA"},
    {"market":"nasdaq","name":"American Airlines Group, Inc.","code":"AAL"}, ...] 
```


## ****미국 뉴욕 증권 거래소 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/usa/nyse/stockList

[{"market":"nyse","name":"Agilent Technologies, Inc.","code":"A"},
    {"market":"nyse","name":"Alcoa Corporation","code":"AA"},
    {"market":"nyse","name":"AAC Holdings, Inc.","code":"AAC"},
    {"market":"nyse","name":"Aaron&#39;s,  Inc.","code":"AAN"},
    {"market":"nyse","name":"Advance Auto Parts Inc","code":"AAP"} ...]
```

## ****미국 AMEX 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/usa/amex/stockList

[{"market":"amex","name":"Altisource Asset Management Corp","code":"AAMC"},
    {"market":"amex","name":"Almaden Minerals, Ltd.","code":"AAU"},
    {"market":"amex","name":"Acme United Corporation.","code":"ACU"},
    {"market":"amex","name":"AeroCentury Corp.","code":"ACY"} ...]
```



## ****미국 주식 일봉 가격 정보****
```
curl  http://localhost:9000/stock/usa/prices/:ticker
    Ex. curl http://localhost:9000/stock/usa/prices/tsla

[
    {
        "code": "tsla",
        "date": "20100629",
        "close": "4.778000",
        "open": "3.800000",
        "high": "5.000000",
        "low": "3.508000",
        "volume": "93831500"
    },
    {
        "code": "tsla",
        "date": "20100630",
        "close": "4.766000",
        "open": "5.158000",
        "high": "6.084000",
        "low": "4.660000",
        "volume": "85935500"
    },
    ...,
   {
       "code": "tsla",
       "date": "20200908",
       "close": "330.209991",
       "open": "356.000000",
       "high": "368.739990",
       "low": "329.880005",
       "volume": "113168200"
   }
]
```
