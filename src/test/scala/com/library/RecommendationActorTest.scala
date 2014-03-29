package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class RecommendationActorTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  feature("The Recommendation Actor selects a short list of random books from the list of unread books to be presented to the user") {
    info("As a family member")
    info("I want some suggestions what to read")
    info("So that I don't have to choose")

  }
}
