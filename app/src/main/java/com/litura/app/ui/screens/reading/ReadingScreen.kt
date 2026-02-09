package com.litura.app.ui.screens.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litura.app.ui.components.HealthIndicator
import com.litura.app.ui.theme.CorrectGreen
import com.litura.app.ui.theme.GrayedOut
import com.litura.app.ui.theme.IncorrectRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    bookId: String,
    initialBiteId: String?,
    onQuit: () -> Unit,
    viewModel: ReadingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(bookId) {
        viewModel.startReading(bookId, initialBiteId)
    }

    Scaffold(
        topBar = {
            if (state.phase != ReadingPhase.READING_FACT_SPLASH &&
                state.phase != ReadingPhase.EXIT_FACT_SPLASH &&
                state.phase != ReadingPhase.LOADING
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = state.chapterTitle,
                                style = MaterialTheme.typography.labelSmall
                            )
                            LinearProgressIndicator(
                                progress = { state.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onQuit) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quit")
                        }
                    },
                    actions = {
                        HealthIndicator(currentHealth = state.health)
                    }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = state.phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "reading_phase",
            modifier = Modifier.padding(padding)
        ) { phase ->
            when (phase) {
                ReadingPhase.LOADING -> LoadingContent()
                ReadingPhase.READING_FACT_SPLASH -> SplashContent(
                    fact = state.readingFact,
                    onDismiss = { viewModel.dismissSplash() }
                )
                ReadingPhase.BITE_TEXT -> BiteTextContent(
                    text = state.currentBite?.text ?: "",
                    reviewTextVisible = state.reviewTextVisible,
                    reviewTextRemaining = state.reviewTextRemaining,
                    onToggleReview = { viewModel.toggleReviewText() },
                    onContinue = { viewModel.finishReadingBite() }
                )
                ReadingPhase.QUESTION -> QuestionContent(
                    question = state.currentQuestions.getOrNull(state.currentQuestionIndex),
                    choices = viewModel.getChoicesForCurrentQuestion(),
                    selectedChoiceId = state.selectedChoiceId,
                    eliminatedChoices = state.eliminatedChoices,
                    correctChoiceRevealed = state.correctChoiceRevealed,
                    currentAttempt = state.currentAttempt,
                    questionNumber = state.currentQuestionIndex + 1,
                    totalQuestions = state.currentQuestions.size,
                    onSelectChoice = { viewModel.selectChoice(it) },
                    onSubmit = { viewModel.submitAnswer() },
                    onNext = { viewModel.nextQuestion() }
                )
                ReadingPhase.RECAP -> RecapContent(
                    recap = state.biteRecap,
                    onNextBite = { viewModel.nextBite() },
                    onQuit = onQuit
                )
                ReadingPhase.EXIT_FACT_SPLASH -> SplashContent(
                    fact = state.readingFact,
                    onDismiss = onQuit
                )
                ReadingPhase.NO_HEALTH -> NoHealthContent(onQuit = onQuit)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SplashContent(fact: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Did you know?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = fact,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Tap to continue",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BiteTextContent(
    text: String,
    reviewTextVisible: Boolean,
    reviewTextRemaining: Int,
    onToggleReview: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (reviewTextRemaining > 0) {
                TextButton(onClick = onToggleReview) {
                    Icon(Icons.Filled.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review ($reviewTextRemaining)")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Button(onClick = onContinue) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: com.litura.app.data.local.entity.QuestionEntity?,
    choices: List<com.litura.app.domain.model.ChoiceData>,
    selectedChoiceId: String?,
    eliminatedChoices: Set<String>,
    correctChoiceRevealed: Boolean,
    currentAttempt: Int,
    questionNumber: Int,
    totalQuestions: Int,
    onSelectChoice: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    if (question == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Question $questionNumber of $totalQuestions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (currentAttempt > 1 && !correctChoiceRevealed) {
            Text(
                text = "Try again!",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = question.prompt,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        choices.forEach { choice ->
            val isEliminated = choice.id in eliminatedChoices
            val isSelected = choice.id == selectedChoiceId
            val isCorrect = choice.id == question.correctChoiceId

            val backgroundColor = when {
                correctChoiceRevealed && isCorrect -> CorrectGreen.copy(alpha = 0.2f)
                correctChoiceRevealed && isSelected && !isCorrect -> IncorrectRed.copy(alpha = 0.2f)
                isEliminated -> GrayedOut.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                correctChoiceRevealed && isCorrect -> CorrectGreen
                correctChoiceRevealed && isSelected && !isCorrect -> IncorrectRed
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isEliminated && !correctChoiceRevealed) {
                        onSelectChoice(choice.id)
                    },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = if (isSelected || (correctChoiceRevealed && isCorrect)) 2.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${choice.id}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isEliminated) GrayedOut else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = choice.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEliminated) GrayedOut else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (correctChoiceRevealed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        } else {
            Button(
                onClick = onSubmit,
                enabled = selectedChoiceId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun RecapContent(
    recap: BiteRecap?,
    onNextBite: () -> Unit,
    onQuit: () -> Unit
) {
    if (recap == null) return
    val minutes = (recap.timeSpentMs / 60000).toInt()
    val seconds = ((recap.timeSpentMs % 60000) / 1000).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bite Complete!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RecapRow("Time", "${minutes}m ${seconds}s")
                Spacer(modifier = Modifier.height(12.dp))
                RecapRow("XP Earned", "+${recap.xpEarned}")
                Spacer(modifier = Modifier.height(12.dp))
                RecapRow("Competency", "${recap.competencyPercent.toInt()}%")
            }
        }

        if (recap.newBadges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "New Badge${if (recap.newBadges.size > 1) "s" else ""} Earned!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNextBite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next Bite")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onQuit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Quit Reading")
        }
    }
}

@Composable
private fun RecapRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun NoHealthContent(onQuit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Health Remaining",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your health segments have been depleted. They will recharge over time (1 segment every 5 minutes).",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onQuit) {
            Text("Go Back")
        }
    }
}
