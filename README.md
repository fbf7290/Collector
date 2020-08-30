**현재 개발 진행 중 (2020.08 ~)**

### Stock Collector : 미국/한국 주식시장 주식  수집기

사용 기술 : Scala, Cats, Akka, Lagom, Cassandra

Cassandra 실행 : docker run --name asset-cassandra -v /Users/wonryool/Desktop/Source/asset\ portfolio/Cassandra:/var/lib/cassandra -p 9042:9042 -d cassandra

메일 : rladnjsfbf@gmail.com


#### **### **한국 전체 ETF 이름 및 종목 코드****
------
`curl  http://localhost:9000/stock/korea/etf/stockList
[{"market":"kospi","name":"KODEX 200","code":"069500"},{"market":"kospi","name":"TIGER 200","code":"102110"},{"market":"kospi","name":"KODEX 200선물인버스2X","code":"252670"},{"market":"kospi","name":"KODEX 레버리지","code":"122630"},{"market":"kospi","name":"KODEX 단기채권","code":"153130"}, ...]`

