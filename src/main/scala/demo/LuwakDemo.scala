package demo

import scala.collection.JavaConversions._
import scala.io.Source

import java.io.File
import java.util.ArrayList
import java.util.List
import java.util.Locale

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.flax.luwak._
import uk.co.flax.luwak.matchers.HighlightingMatcher
import uk.co.flax.luwak.matchers.HighlightsMatch
import uk.co.flax.luwak.presearcher.TermFilteredPresearcher
import uk.co.flax.luwak.queryparsers.LuceneQueryParser

class LuwakDemo(var queriesFile: String = "", var inputDirectory: String = "") {
  val analyzer = new StandardAnalyzer()
  val logger = LoggerFactory.getLogger(classOf[LuwakDemo])

  logger.info("queriesFile: " + queriesFile)
  logger.info("inputDirectory: " + inputDirectory)

  def addQueries(monitor: Monitor, queriesFile: String) = {
    val queries = new ArrayList[MonitorQuery]()
    var count = 0
    logger.info(s"Loading queries from $queriesFile")
    for (query <- Source.fromFile(queriesFile).getLines) {
      logger.info(s"Parsing[$query]")
      count += 1
      var queryId = f"$count%d-$query%s"
      queries.add(new MonitorQuery(queryId, query))
    }
    monitor.update(queries)
    logger.info(s"Added $count queries to monitor")
  }

  def buildDocs(inputDirectory: String): List[InputDocument] = {
    logger.info(s"Iterating over the files in $inputDirectory")
    val docs = new ArrayList[InputDocument]()

    for {
      file <- new File(inputDirectory).listFiles.toIterator if file.isFile
      } {
        var filename = file.toString()
        logger.info(s"reading the lines from $filename")
        val source = Source.fromFile(filename)
        val content = try source.getLines mkString "\n" finally source.close()
        var doc = InputDocument
          .builder(filename)
          .addField("text", content, new StandardAnalyzer())
          .build()
          docs.add(doc)
      }
      docs
  }

  def outputMatches(matches: Matches[HighlightsMatch]) = {
    val batchSize = matches.getBatchSize()
    val searchTime = matches.getSearchTime()
    val queriesRun = matches.getQueriesRun()
    logger.info(s"Matched batch of $batchSize documents in $searchTime milliseconds with $queriesRun queries run")
    for (docMatches <- matches) {
      var id = docMatches.getDocId()
      logger.info(s"Matches from $id")
      for (m <- docMatches) {
        logger.info("\tQuery: {} ({} hits)", m.getQueryId(), m.getHitCount());
      }
    }
  }

  def run() = {
    try {
      val monitor = new Monitor(new LuceneQueryParser("text", analyzer), new TermFilteredPresearcher())
      addQueries(monitor, queriesFile)
      val batch = DocumentBatch.of(buildDocs(inputDirectory))
      val matches = monitor.`match`(batch, HighlightingMatcher.FACTORY)
      outputMatches(matches)
      monitor.close()
    } catch {
      case e: Exception => {
        logger.error(e.toString())
      }
    }
  }
}

object Main extends App {
  val demo = new LuwakDemo("src/test/resources/demoqueries", "src/test/resources/gutenberg")
  demo.run()
}
