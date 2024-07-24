package com.mifos.mifosxdroid.offline.offlinedashbarod

import android.annotation.SuppressLint
import androidx.compose.compiler.plugins.kotlin.ComposeCallableIds.remember
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.objects.noncore.Document
import com.mifos.feature.document.R
import com.mifos.feature.document.document_list.DocumentListScreen
import com.mifos.feature.document.document_list.DocumentListUiState
import kotlinx.coroutines.launch

@Composable
fun OfflineDashboardScreen(
    viewModel: OfflineDashboardViewModel = hiltViewModel(),
) {




}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun OfflineDashboardScreen(
    uiState: OfflineDashboardUiState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    MifosScaffold(snackbarHostState = snackBarHostState ) {

        when (uiState) {

            is OfflineDashboardUiState.ShowError -> {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        message = uiState.message,
                        actionLabel = "Ok",
                        duration = SnackbarDuration.Short
                    )
                }
            }

            is OfflineDashboardUiState.ShowProgressbar -> {
                MifosCircularProgress()
            }

            is OfflineDashboardUiState.ShowGroups -> {

            }

            is OfflineDashboardUiState.ShowCenters -> {

            }

            is OfflineDashboardUiState.ShowClients -> {

            }

            is OfflineDashboardUiState.ShowLoanRepaymentTransactions -> {

            }

            is OfflineDashboardUiState.ShowSavingsAccountTransaction -> {

            }
        }

    }
}



@Composable
fun PayloadCard(
    imageRes: Int,
    name: String,
    count: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = count,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .padding(top = 28.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

class OfflineDashboardUiStateProvider : PreviewParameterProvider<OfflineDashboardUiState> {

    override val values: Sequence<OfflineDashboardUiState>
        get() = sequenceOf(
            OfflineDashboardUiState.ShowProgressbar,
            OfflineDashboardUiState.ShowError("Error"),
            OfflineDashboardUiState.ShowClients(listOf()),
            OfflineDashboardUiState.ShowGroups(listOf()),
            OfflineDashboardUiState.ShowCenters(listOf()),
            OfflineDashboardUiState.ShowLoanRepaymentTransactions(listOf()),
            OfflineDashboardUiState.ShowSavingsAccountTransaction(listOf())
        )
}

@Preview(showBackground = true)
@Composable
private fun OfflineDashboardPreview(
    @PreviewParameter(OfflineDashboardUiStateProvider::class) state: OfflineDashboardUiState
) {
    OfflineDashboardScreen(
        uiState = state
    )
}

val sampleDocumentList = List(10) {
    Document(name = "Document $it", description = "desc $it")
}