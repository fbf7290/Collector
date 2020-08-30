**현재 개발 진행 중 (2020.08 ~)**

# **Stock Collector : 미국/한국 주식시장 주식  수집기**

사용 기술 : Scala, Cats, Akka, Lagom, Cassandra

Cassandra 실행 : docker run --name asset-cassandra -v /Users/wonryool/Desktop/Source/asset\ portfolio/Cassandra:/var/lib/cassandra -p 9042:9042 -d cassandra

메일 : rladnjsfbf@gmail.com




## ****코스피 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/kospi/stockList

[{"market":"kospi","name":"DSR","code":"155660"},{"market":"kospi","name":"GS","code":"078930"},
    {"market":"kospi","name":"GS글로벌","code":"001250"},{"market":"kospi","name":"HDC현대산업개발",
    "code":"294870"},{"market":"kospi","name":"LG이노텍","code":"011070"},
    {"market":"kospi","name":"LG전자","code":"066570"} ... ] ```
```


## ****코스닥 전체 주식 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/kosdaq/stockList

[[{"market":"kosdaq","name":"CJ프레시웨이","code":"051500"},
    {"market":"kosdaq","name":"EDGC","code":"245620"},
    {"market":"kosdaq","name":"GST","code":"083450"}... ] ```
```



## ****한국 전체 ETF 이름 및 종목 코드****
```
curl  http://localhost:9000/stock/korea/etf/stockList

[{"market":"kospi","name":"KODEX 200","code":"069500"},{"market":"kospi","name":"TIGER 200",
    "code":"102110"}, {"market":"kospi","name":"KODEX 200선물인버스2X","code":"252670"},
    {"market":"kospi","name":"KODEX 레버리지","code":"122630"},{"market":"kospi",
    "name":"KODEX 단기채권","code":"153130"}, ...]```

