package com.litura.app.data.repository

import android.content.Context
import com.litura.app.data.local.database.LituraDatabase
import com.litura.app.data.local.entity.BiteEntity
import com.litura.app.data.local.entity.BookEntity
import com.litura.app.data.local.entity.QuestionEntity
import com.litura.app.domain.model.BitesFile
import com.litura.app.domain.model.BookPackage
import com.litura.app.domain.model.ChoiceData
import com.litura.app.domain.model.QuestionsFile
import com.litura.app.util.AssetReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: LituraDatabase,
    private val json: Json
) {
    suspend fun importAllBooks() {
        val bookDirs = AssetReader.listBookDirectories(context)
        for (dir in bookDirs) {
            importBook(dir)
        }
    }

    private suspend fun importBook(bookDir: String) {
        val bookJson = AssetReader.readJsonFromAssets(context, "books/$bookDir/book.json")
        val bitesJson = AssetReader.readJsonFromAssets(context, "books/$bookDir/bites.json")
        val questionsJson = AssetReader.readJsonFromAssets(context, "books/$bookDir/questions.json")

        val bookPackage = json.decodeFromString<BookPackage>(bookJson)
        val bitesFile = json.decodeFromString<BitesFile>(bitesJson)
        val questionsFile = json.decodeFromString<QuestionsFile>(questionsJson)

        val bookEntity = mapToBookEntity(bookPackage)
        val biteEntities = mapToBiteEntities(bitesFile)
        val questionEntities = mapToQuestionEntities(questionsFile)

        database.bookDao().insertBooks(listOf(bookEntity))
        database.biteDao().insertBites(biteEntities)
        database.questionDao().insertQuestions(questionEntities)
    }

    private fun mapToBookEntity(pkg: BookPackage): BookEntity {
        val purchaseState = if (pkg.economics.price.amount == 0.0 || pkg.economics.purchaseType == "OWNED") {
            "OWNED_DOWNLOADED"
        } else {
            "NOT_OWNED"
        }

        return BookEntity(
            bookId = pkg.bookId,
            title = pkg.identity.title,
            author = pkg.identity.author,
            series = pkg.identity.series,
            publicationYear = pkg.identity.publicationYear,
            language = pkg.identity.language,
            genresJson = json.encodeToString(pkg.classification.genres),
            lexile = pkg.classification.readerLevel.lexile,
            difficultyTier = pkg.classification.difficultyTier,
            totalBites = pkg.structure.totalBites,
            chaptersJson = json.encodeToString(pkg.structure.chapters),
            primarySkillsJson = json.encodeToString(pkg.learningFocus.primarySkills),
            secondarySkillsJson = json.encodeToString(pkg.learningFocus.secondarySkills),
            completionBadge = pkg.badges.completionBadge,
            specialtyBadgesJson = json.encodeToString(pkg.badges.specialtyBadges),
            hiddenBadgesJson = json.encodeToString(pkg.badges.hiddenBadges),
            priceAmount = pkg.economics.price.amount,
            priceCurrency = pkg.economics.price.currency,
            purchaseType = pkg.economics.purchaseType,
            includedInPlansJson = json.encodeToString(pkg.economics.includedInPlans),
            coverImagePath = "books/${pkg.bookId}/${pkg.assets.coverImage}",
            estimatedReadTimeMinutes = pkg.analytics.estimatedReadTimeMinutes,
            engagementWeight = pkg.analytics.engagementWeight,
            purchaseState = purchaseState,
            importedAt = System.currentTimeMillis()
        )
    }

    private fun mapToBiteEntities(bitesFile: BitesFile): List<BiteEntity> {
        return bitesFile.bites.map { bite ->
            BiteEntity(
                biteId = bite.biteId,
                bookId = bitesFile.bookId,
                chapterId = bite.chapterId,
                orderIndex = bite.orderIndex,
                text = bite.text,
                estimatedSeconds = bite.estimatedSeconds
            )
        }
    }

    private fun mapToQuestionEntities(questionsFile: QuestionsFile): List<QuestionEntity> {
        return questionsFile.questions.map { q ->
            QuestionEntity(
                questionId = q.questionId,
                biteId = q.biteId,
                bookId = questionsFile.bookId,
                type = q.type,
                difficulty = q.difficulty,
                prompt = q.prompt,
                choicesJson = json.encodeToString(q.choices),
                correctChoiceId = q.correctChoiceId,
                explanation = q.explanation
            )
        }
    }
}
