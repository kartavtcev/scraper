## scraper

Web Crawler + GraphQL FP demo.    
Some code is based on official Cats Effect demo, Sangria demos.   


How to run:  
Install & start H2 database in `Generic H2 (Server)` mode.  
Start GraphQLServer for GraphQL.  
Start WebCrawler to scrape the page (set up in config).  

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

TODO: logging, error handling, auth, more tests, docker.