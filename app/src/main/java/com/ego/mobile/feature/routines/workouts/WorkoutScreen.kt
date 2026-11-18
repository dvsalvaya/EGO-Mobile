package com.ego.mobile.feature.routines.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.feature.routines.workouts.components.ActiveWorkoutView
import com.ego.mobile.feature.routines.workouts.components.AddExerciseDialog
import com.ego.mobile.feature.routines.workouts.components.WorkoutHistoryView
import com.ego.mobile.feature.routines.workouts.components.formatDuration

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = rememberWorkoutViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStartDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var viewingActiveWorkout by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.hasActiveSession) {
        if (uiState.hasActiveSession) {
            viewingActiveWorkout = true
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkoutEvent.Error -> {
                    snackbarHostState.showSnackbar(event.error)
                }
                is WorkoutEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                WorkoutEvent.SessionStarted -> {
                    viewingActiveWorkout = true
                    snackbarHostState.showSnackbar("Treino iniciado!")
                }
                WorkoutEvent.SessionCompleted -> {
                    viewingActiveWorkout = false
                    snackbarHostState.showSnackbar("Treino finalizado e salvo no histórico!")
                }
                WorkoutEvent.SessionDiscarded -> {
                    viewingActiveWorkout = false
                    snackbarHostState.showSnackbar("Treino descartado.")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val activeSession = uiState.activeSession

                if (viewingActiveWorkout && activeSession != null) {
                    // Tela de execução da sessão ativa
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { viewingActiveWorkout = false }) {
                                Text("← Ver Visão Geral")
                            }
                        }

                        ActiveWorkoutView(
                            session = activeSession,
                            elapsedTimeMillis = uiState.elapsedTimeMillis,
                            onPause = { viewModel.pauseSession() },
                            onResume = { viewModel.resumeSession() },
                            onComplete = { viewModel.completeSession() },
                            onDiscard = { viewModel.discardSession() },
                            onAddExerciseClick = { showAddExerciseDialog = true },
                            onRemoveExercise = { exId -> viewModel.removeExercise(exId) },
                        )
                    }
                } else {
                    // Painel de entrada / Visão Geral
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Banner de Sessão Ativa (se houver)
                        if (activeSession != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    ) {
                                        Text(
                                            text = "⚡ Treino em andamento",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${activeSession.title} • Tempo: ${formatDuration(uiState.elapsedTimeMillis)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewingActiveWorkout = true },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Continuar Treino")
                                        }
                                    }
                                }
                            }
                        }

                        // Ação de iniciar novo treino
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        text = "Iniciar Novo Treino",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Registre seus exercícios, séries, repetições e cargas em tempo real.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (activeSession != null) {
                                                viewingActiveWorkout = true
                                            } else {
                                                showStartDialog = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            if (activeSession != null) "Ver Treino Ativo" else "Iniciar Treino",
                                        )
                                    }
                                }
                            }
                        }

                        // Histórico de Treinos
                        item {
                            WorkoutHistoryView(
                                completedSessions = uiState.completedSessions,
                                onStartWorkout = {
                                    if (activeSession != null) viewingActiveWorkout = true else showStartDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStartDialog) {
        var titleInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text("Novo Treino") },
            text = {
                Column {
                    Text(
                        text = "Informe um título para o seu treino (opcional):",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Ex: Treino A - Peito e Tríceps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStartDialog = false
                        viewModel.startSession(titleInput)
                    },
                ) {
                    Text("Iniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, sets, reps, weight, notes ->
                showAddExerciseDialog = false
                viewModel.addExercise(name, sets, reps, weight, notes)
            },
        )
    }
}
