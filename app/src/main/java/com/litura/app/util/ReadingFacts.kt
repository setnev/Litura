package com.litura.app.util

object ReadingFacts {
    private val facts = listOf(
        "Reading for 20 minutes a day exposes you to 1.8 million words per year.",
        "Studies show reading reduces stress by up to 68%.",
        "The average person reads 200-250 words per minute.",
        "Reading fiction improves empathy and social understanding.",
        "Your brain creates new neural pathways every time you read.",
        "People who read regularly sleep better than non-readers.",
        "Reading just 6 minutes can reduce stress levels significantly.",
        "The smell of old books comes from vanilla-scented compounds in paper.",
        "Bibliotherapy uses reading as a therapeutic tool for mental health.",
        "Reading aloud improves comprehension and memory retention.",
        "The word 'bookworm' dates back to 1580.",
        "Speed readers can process over 1,000 words per minute.",
        "Reading before bed helps signal your body it's time to sleep.",
        "Children who read for fun perform better in all school subjects.",
        "The human brain processes written words in about 100 milliseconds.",
        "Reading regularly can slow cognitive decline by up to 32%.",
        "The Library of Congress holds over 170 million items.",
        "Your vocabulary grows by about 1,000 words per year through reading.",
        "Reading activates the same brain regions as experiencing events firsthand.",
        "People who read books live an average of 2 years longer."
    )

    fun random(): String = facts.random()
}
