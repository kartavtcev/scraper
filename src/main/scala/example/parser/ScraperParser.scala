package example.parser

import example.domain.News.NewsItem
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._

object ScraperParser {
  def parseNews(scrapeClass: String, content: Either[String, String]): Either[String, List[NewsItem]] =
    content.map(text => {

      val browser = JsoupBrowser()
      val doc = browser.parseString(text)
      val items: List[Element] = doc >> elementList(scrapeClass)
      val news = items.map(item => NewsItem(item.text, item >> attr("href")))

      news
    })
}
