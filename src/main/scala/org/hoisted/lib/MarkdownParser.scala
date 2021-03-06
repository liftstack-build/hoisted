package org.hoisted.lib

import net.liftweb._
import common._
import util.Html5
import org.pegdown.PegDownProcessor
import xml.{Elem, NodeSeq}

/**
 * Created with IntelliJ IDEA.
 * User: dpp
 * Date: 6/8/12
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */


object MarkdownParser {
  lazy val matchMetadata = """(?m)\A(:?[ \t]*\n)?(?:-{3,}+\n)?(^([^:\n]+)[=:]([^\n]*)\n+(:?[ \t]*\n)?)+(:?-{3,}+\n)?""".r

  lazy val topMetadata = """(?m)^([^:]+):[ \t]*(.*)$""".r

  lazy val lineSplit = """(?m)^(.*)$""".r

  lazy val linkDefs = """(?m)^\p{Space}{0,3}\[([^:]+)[=:](?:[ ]*)(.+)\]:""".r


  def readTopMetadata(in: String): (String, List[(String, String)]) = {
    val (_in, pairs): (String, List[(String, String)]) = matchMetadata.findFirstIn(in) match {
      case Some(data) => (matchMetadata.replaceAllIn(in, ""),
        lineSplit.findAllIn(data).toList.flatMap(s =>
          topMetadata.findAllIn(s).matchData.toList.map(md => (md.group(1).trim, md.group(2).trim))))
      case None => (in, Nil)
    }

    val pairs2: List[(String, String)] =
      linkDefs.findAllIn(_in).matchData.toList.map(md => (md.group(1).trim, md.group(2).trim))

    (in, pairs ::: pairs2)
  }

  def parse(in: String): Box[(NodeSeq, List[(String, String)])] = {
    val (_in, retPairs) = readTopMetadata(in)

    val pd = new PegDownProcessor()
    val raw = pd.markdownToHtml(_in)
    Html5.parse("<div>"+raw+"</div>").map(ns => (ns, retPairs))
  }
}
