# API to create tiny/short url 


## Tech stack

- [Scala](https://www.scala-lang.org/)
- [HTTP4s](https://github.com/http4s/http4s)
- [Circe](https://github.com/circe/circe)
- [Munits cats effect](https://github.com/typelevel/munit-cats-effect)
- [SBT](https://github.com/sbt/sbt)


## Algorithm for short url creation

Algorithm can be simply represented as

`substr(base62(md5(url)), 6)`

- MD5 of the url
- Take base62 of the MD5 result
- Choose substring of the first six characters

Algorithm taken from  - https://medium.com/@sandeep4.verma/system-design-scalable-url-shortener-service-like-tinyurl-106f30f23a82

## Persistence service

- Each successful conversion of url to short url stores short url in database
- Before conversion of a url, database is checked for existing short url. If present then return from DB.
- Current persistence service is an inmemory implementation, which can be easily changed to a db like Redis or Cassandra.

## Further Improvements

- Move inmemory persistence to redis/cassandra
- Perform validation on incoming data , ex- valid url

## Commands

### Run

Run `sbt run`, it will start the api on localhost at port 8080

Alternatively , we can also run via the  `main` class

```sh
sbt run
```

## HTTP REQUEST AND RESPONSES

```sh
# Shorten
curl --request POST 'http://localhost:8080/app/shorten' --header 'Content-Type: application/json' --data-raw '{"uri": "https://andtvc.com"}'
# returns: {"short":"AxMHYw"}

# Call shortened url
curl --request GET 'http://localhost:8080/app/AxMHYw'

# Get Stats for one url
curl --request GET 'http://localhost:8080/app/stats/AxMHYw'
# returns: {"accessCount":2}

# Get Stats for all url's
curl --request GET 'http://localhost:8080/app/stats/all'
# returns: {"result":[{"short":"AxMHYw","url":"https://andtvc.com"}]}
```

### Create executable

```sh
sbt packageBin
```

### Test

```sh
sbt test
```