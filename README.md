## scraper

Web Crawler + GraphQL FP demo.    
Some code is based on official Cats Effect demo, Sangria demos.   

First, start GraphQLServer for GraphQL (it'll init the DB and the table).
Then, start WebCrawler to scrape the page (set up in config).  

Example GraphQL query:
```
{
  news(limit: 100, offset: 0) {
    title
    link
  }
}
```

Libs used: Sangria, Http4s, Sttp, Cats/Cats Effect, Scala-scraper, Quill.
Tagless Final approach.  

TODO:
1. Auto-stop WebCrawler IOApp.
2. Logging, error handling, auth, more tests, docker.