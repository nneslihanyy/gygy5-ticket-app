package com.turkcell.ticketapp.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.TicketType
import com.turkcell.core.util.formatIsoDateTr
import com.turkcell.core.util.formatPriceTl
import com.turkcell.ticketapp.viewmodel.EventDetailUiState
import com.turkcell.ticketapp.viewmodel.EventDetailViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    onNavigateToMyTickets: () -> Unit,
    viewModel: EventDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Satın alma başarılı → Biletlerim'e navigate
    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            snackbarHostState.showSnackbar("Satın alma başarılı! Biletleriniz oluşturuldu.")
            viewModel.consumePurchaseSuccess()
            onNavigateToMyTickets()
        }
    }

    LaunchedEffect(state.purchaseError) {
        state.purchaseError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumePurchaseError()
        }
    }

    // Ödeme Onayı Dialogu
    if (state.showPaymentDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPaymentDialog,
            title = { Text("Ödeme Onayı") },
            text = {
                Text("Toplam ${formatPriceTl(state.totalCents)} tutarındaki satın almayı onaylıyor musunuz?")
            },
            confirmButton = {
                Button(onClick = viewModel::confirmPayment) {
                    Text("Onayla ve Öde")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPaymentDialog) {
                    Text("İptal")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etkinlik Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.event != null) {
                PurchaseBottomBar(state = state, onPurchase = viewModel::startPurchase)
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                }
            }
            state.event != null -> {
                EventDetailContent(
                    event = state.event!!,
                    quantities = state.quantities,
                    onIncrement = viewModel::incrementQuantity,
                    onDecrement = viewModel::decrementQuantity,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun PurchaseBottomBar(state: EventDetailUiState, onPurchase: () -> Unit) {
    AnimatedVisibility(visible = state.hasSelection, enter = fadeIn(), exit = fadeOut()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Toplam", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatPriceTl(state.totalCents), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = onPurchase,
                    enabled = state.hasSelection && !state.isPurchasing,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (state.isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Satın Al", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event, quantities: Map<String, Int>,
    onIncrement: (String) -> Unit, onDecrement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
                .background(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(event.name.take(1).uppercase(), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(event.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            if (event.description.isNotBlank()) {
                Text(event.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(20.dp))
            }
            InfoRow(Icons.Default.LocationOn, "Mekan", event.venue.ifBlank { "Belirtilmemiş" })
            Spacer(Modifier.height(12.dp))
            InfoRow(Icons.Default.CalendarMonth, "Başlangıç", formatIsoDateTr(event.startsAt))
            if (event.endsAt.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                InfoRow(Icons.Default.AccessTime, "Bitiş", formatIsoDateTr(event.endsAt))
            }
            Spacer(Modifier.height(32.dp))
            Text("Bilet Türleri", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(16.dp))
            if (event.ticketTypes.isEmpty()) {
                Text("Bu etkinlik için bilet türü tanımlanmamış.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                event.ticketTypes.forEach { tt ->
                    TicketTypeCard(tt, quantities[tt.id] ?: 0, { onIncrement(tt.id) }, { onDecrement(tt.id) })
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun TicketTypeCard(ticketType: TicketType, quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val maxQty = min(20, ticketType.remaining.toInt())
    val isSoldOut = ticketType.remaining <= 0

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ConfirmationNumber, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(ticketType.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
                Text(formatPriceTl(ticketType.priceCents), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Text("${ticketType.remaining} / ${ticketType.capacity} kalan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            if (isSoldOut) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEF4444).copy(alpha = 0.1f)).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text("Tükendi", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFFEF4444))
                }
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDecrement, enabled = quantity > 0, modifier = Modifier.size(38.dp).clip(CircleShape),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Icon(Icons.Default.Remove, "Azalt", Modifier.size(18.dp))
                        }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        IconButton(onClick = onIncrement, enabled = quantity < maxQty, modifier = Modifier.size(38.dp).clip(CircleShape),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Icon(Icons.Default.Add, "Artır", Modifier.size(18.dp))
                        }
                    }
                    if (quantity > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Color(0xFF22C55E))
                            Spacer(Modifier.width(4.dp))
                            Text(formatPriceTl(ticketType.priceCents * quantity), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF22C55E))
                        }
                    } else {
                        Text("Maks: $maxQty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}