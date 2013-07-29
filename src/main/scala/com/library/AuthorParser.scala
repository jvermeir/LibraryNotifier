package com.library

import java.io.File
import org.apache.commons.io.FileUtils

object AuthorParser {
  def parseAuthorsFromListOfStrings(authors: List[String]):Map[String, Author] = {
    val authorList:List[Author] = authors map (Author(_))
    val lastNameList =  authorList map (_.lastName)
    (lastNameList zip authorList).toMap
  }

  def loadAuthorsFromFile(fileName:String): Map[String, Author] = {
    val authorsAsText = FileUtils.readFileToString(new File(fileName))
    parseAuthorsFromListOfStrings(authorsAsText.split("\n").toList.filter(_.trim.length>0))
  }
}
