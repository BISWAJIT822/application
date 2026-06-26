package com.goatinsurance.app.ui.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goatinsurance.app.ui.components.*
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    onComplete: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var currentStep by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Enrollment") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 7) currentStep++
                            else onComplete()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green40),
                    ) {
                        Text(
                            if (currentStep == 7) "Generate Policy" else "Next",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (currentStep == 7) Icons.Filled.Done else Icons.Filled.ArrowForward,
                            null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step Indicator
            StepIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "step_transition",
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    when (step) {
                        1 -> Step1FarmerDetails()
                        2 -> Step2GoatDetails()
                        3 -> Step3CapturePhotos()
                        4 -> Step4QRTagging()
                        5 -> Step5Vaccination()
                        6 -> Step6Premium()
                        7 -> Step7GeneratePolicy()
                    }
                }
            }
        }
    }
}

// ── Step 1: Farmer Details ──
@Composable
private fun Step1FarmerDetails() {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }

    Text("Farmer Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(20.dp))

    GoatTextField(value = name, onValueChange = { name = it }, label = "Full Name", leadingIcon = Icons.Filled.Person)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", leadingIcon = Icons.Filled.Phone, keyboardType = KeyboardType.Phone)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = village, onValueChange = { village = it }, label = "Village", leadingIcon = Icons.Filled.LocationCity)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = address, onValueChange = { address = it }, label = "Full Address", leadingIcon = Icons.Filled.Home, singleLine = false, maxLines = 3)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = "Aadhaar Number", leadingIcon = Icons.Filled.CreditCard, keyboardType = KeyboardType.Number)
    Spacer(modifier = Modifier.height(16.dp))

    // GPS Location button
    OutlinedButton(
        onClick = { /* Get GPS location */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Capture GPS Location")
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Photo capture
    OutlinedButton(
        onClick = { /* Capture photo */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Capture Farmer Photo")
    }
}

// ── Step 2: Goat Details ──
@Composable
private fun Step2GoatDetails() {
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var weight by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf("") }

    Text("Goat Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(20.dp))

    GoatTextField(value = breed, onValueChange = { breed = it }, label = "Breed", leadingIcon = Icons.Filled.Pets)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = age, onValueChange = { age = it }, label = "Age (months)", leadingIcon = Icons.Filled.CalendarToday, keyboardType = KeyboardType.Number)
    Spacer(modifier = Modifier.height(12.dp))

    // Gender selection
    Text("Gender", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("male" to "Male", "female" to "Female").forEach { (value, label) ->
            FilterChip(
                selected = gender == value,
                onClick = { gender = value },
                label = { Text(label) },
                leadingIcon = if (gender == value) {
                    { Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = weight, onValueChange = { weight = it }, label = "Weight (kg)", leadingIcon = Icons.Filled.Scale, keyboardType = KeyboardType.Decimal)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = color, onValueChange = { color = it }, label = "Color", leadingIcon = Icons.Filled.Palette)
    Spacer(modifier = Modifier.height(12.dp))
    GoatTextField(value = marks, onValueChange = { marks = it }, label = "Identification Marks", leadingIcon = Icons.Filled.Fingerprint, singleLine = false, maxLines = 3)
}

// ── Step 3: Capture Photos ──
@Composable
private fun Step3CapturePhotos() {
    val angles = listOf("Left", "Right", "Front", "Back", "Top", "Face")
    var capturedPhotos by remember { mutableStateOf(setOf<String>()) }

    Text("Capture Photos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Take photos of the goat from all 6 angles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(20.dp))

    angles.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { angle ->
                val captured = angle in capturedPhotos
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { capturedPhotos = capturedPhotos + angle },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (captured)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    border = if (!captured)
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    else null,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            if (captured) Icons.Filled.CheckCircle else Icons.Filled.CameraAlt,
                            contentDescription = angle,
                            modifier = Modifier.size(36.dp),
                            tint = if (captured) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            angle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
        "${capturedPhotos.size}/6 photos captured",
        style = MaterialTheme.typography.bodyMedium,
        color = if (capturedPhotos.size == 6) MaterialTheme.colorScheme.primary
               else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ── Step 4: QR Tagging ──
@Composable
private fun Step4QRTagging() {
    var qrCode by remember { mutableStateOf("") }
    var earTag by remember { mutableStateOf("") }
    var scanned by remember { mutableStateOf(false) }

    Text("QR Tag Assignment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Scan and assign QR/ear tag to the goat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(24.dp))

    // Scan QR Button
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { scanned = true; qrCode = "QR-${System.currentTimeMillis()}" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (scanned) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                if (scanned) Icons.Filled.CheckCircle else Icons.Filled.QrCodeScanner,
                null,
                modifier = Modifier.size(64.dp),
                tint = if (scanned) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (scanned) "QR Scanned ✓" else "Tap to Scan QR Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (scanned) {
                Text(
                    qrCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    GoatTextField(value = earTag, onValueChange = { earTag = it }, label = "Ear Tag Number", leadingIcon = Icons.Filled.Tag)
}

// ── Step 5: Vaccination ──
@Composable
private fun Step5Vaccination() {
    val vaccines = listOf("PPR", "FMD", "ET", "Goat Pox")
    var completedVaccines by remember { mutableStateOf(setOf<String>()) }

    Text("Vaccination Record", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Record vaccination status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(20.dp))

    vaccines.forEach { vaccine ->
        val completed = vaccine in completedVaccines
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        completedVaccines = if (completed) completedVaccines - vaccine
                                            else completedVaccines + vaccine
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = completed,
                    onCheckedChange = {
                        completedVaccines = if (completed) completedVaccines - vaccine
                                            else completedVaccines + vaccine
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(vaccine, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (completed) "Completed" else "Pending",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (completed) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(status = if (completed) "completed" else "pending")
            }
        }
    }
}

// ── Step 6: Premium Collection ──
@Composable
private fun Step6Premium() {
    var amount by remember { mutableStateOf("100") }
    var paymentMode by remember { mutableStateOf("cash") }

    Text("Premium Collection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(20.dp))

    GoatTextField(value = amount, onValueChange = { amount = it }, label = "Premium Amount (₹)", leadingIcon = Icons.Filled.CurrencyRupee, keyboardType = KeyboardType.Number)
    Spacer(modifier = Modifier.height(16.dp))

    Text("Payment Mode", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(8.dp))

    listOf("cash" to "Cash", "upi" to "UPI", "online" to "Online").forEach { (value, label) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { paymentMode = value }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = paymentMode == value, onClick = { paymentMode = value })
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Receipt, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Receipt will be generated", style = MaterialTheme.typography.labelLarge)
                Text("Premium: ₹$amount via ${paymentMode.uppercase()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── Step 7: Generate Policy ──
@Composable
private fun Step7GeneratePolicy() {
    Text("Generate Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Filled.VerifiedUser,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Enrollment Complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Policy will be generated and certificate will be available for download.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { /* Download */ }) {
                    Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download")
                }
                Button(onClick = { /* Share */ }) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }
    }
}
