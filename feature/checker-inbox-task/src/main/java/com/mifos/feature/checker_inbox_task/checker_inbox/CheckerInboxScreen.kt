@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)

package com.mifos.feature.checker_inbox_task.checker_inbox

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.component.MifosDialogBox
import com.mifos.core.designsystem.component.MifosSweetError
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.designsystem.theme.Black
import com.mifos.core.designsystem.theme.LightGray
import com.mifos.core.designsystem.theme.White
import com.mifos.core.objects.checkerinboxandtasks.CheckerTask
import com.mifos.core.ui.components.SelectionModeTopAppBar
import com.mifos.feature.checker_inbox_task.R
import com.mifos.feature.checker_inbox_task.checker_inbox_dialog.CheckerInboxTasksFilterDialog
import java.sql.Timestamp

@Composable
fun CheckerInboxScreen(
    onBackPressed: () -> Unit
) {

    val context = LocalContext.current
    val viewModel: CheckerInboxViewModel = hiltViewModel()
    val state by viewModel.checkerInboxUiState.collectAsStateWithLifecycle()
    var isDialogBoxActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var checkerList by rememberSaveable { mutableStateOf(listOf<CheckerTask>()) }
    var filterList by rememberSaveable { mutableStateOf(listOf<CheckerTask>()) }
    var isFiltered by rememberSaveable { mutableStateOf(false) }
    var isSearching by rememberSaveable { mutableStateOf(false) }

    var fromDate by rememberSaveable { mutableStateOf<Timestamp?>(null) }
    var toDate by rememberSaveable { mutableStateOf<Timestamp?>(null) }
    var action: String? by rememberSaveable { mutableStateOf(null) }
    var entity: String? by rememberSaveable { mutableStateOf(null) }
    var resourceId: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(key1 = true) {
        viewModel.loadCheckerTasks()
    }

    if (isDialogBoxActive) {
        CheckerInboxTasksFilterDialog(
            closeDialog = { isDialogBoxActive = false },
            fromDate = fromDate,
            toDate = toDate,
            action = action,
            entity = entity,
            resourceId = resourceId,
            filter = { newAction, newEntity, newResourceId, newFromDate, newToDate ->
                try {
                    action = newAction
                    entity = newEntity
                    resourceId = newResourceId
                    fromDate = newFromDate
                    toDate = newToDate
                    isFiltered = true
                    filterList = getFilteredList(
                        searchQuery,
                        fromDate,
                        toDate,
                        action,
                        entity,
                        resourceId,
                        checkerList
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                isDialogBoxActive = false
            },
            clearFilter = {
                isFiltered = false
                isDialogBoxActive = false
                action = null
                entity = null
                resourceId = null
                fromDate = null
                toDate = null
            }
        )
    }

    CheckerInboxScreen(
        state = state,
        onBackPressed = onBackPressed,
        onApprove = { viewModel.approveCheckerEntry(it) },
        onReject = { viewModel.rejectCheckerEntry(it) },
        onDelete = { viewModel.deleteCheckerEntry(it) },
        onRetry = { viewModel.loadCheckerTasks() },
        onApproveList = { list ->
            list.forEach {
                viewModel.approveCheckerEntry(it)
            }
        },
        onRejectList = { list ->
            list.forEach {
                viewModel.rejectCheckerEntry(it)
            }
        },
        onDeleteList = { list ->
            list.forEach {
                viewModel.deleteCheckerEntry(it)
            }
        },
        search = { query ->
            isSearching = query.isNotEmpty()
            searchQuery = query
            filterList = getFilteredList(
                searchQuery,
                fromDate,
                toDate,
                action,
                entity,
                resourceId,
                checkerList
            )
        },
        filter = {
            isDialogBoxActive = true
        },
        isFiltered = isFiltered,
        isSearching = isSearching,
        filteredList = filterList,
        setList = { checkerList = it }
    )
}


@Composable
fun CheckerInboxScreen(
    state: CheckerInboxUiState,
    onBackPressed: () -> Unit,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onRetry: () -> Unit = {},
    onApproveList: (List<Int>) -> Unit,
    onRejectList: (List<Int>) -> Unit,
    onDeleteList: (List<Int>) -> Unit,
    search: (String) -> Unit,
    filter: () -> Unit,
    isFiltered: Boolean,
    isSearching: Boolean,
    filteredList: List<CheckerTask>,
    setList: (List<CheckerTask>) -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    var searchInbox by rememberSaveable { mutableStateOf("") }
    var approveId by rememberSaveable { mutableIntStateOf(0) }
    var showApproveDialog by rememberSaveable { mutableStateOf(false) }
    var rejectId by rememberSaveable { mutableIntStateOf(0) }
    var showRejectDialog by rememberSaveable { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableIntStateOf(0) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isInSelectionMode by rememberSaveable { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Int>() }
    var fetchedList: List<CheckerTask> = listOf()
    val resetSelectionMode = {
        isInSelectionMode = false
        selectedItems.clear()
    }
    BackHandler(enabled = isInSelectionMode) {
        resetSelectionMode()
    }
    LaunchedEffect(
        key1 = isInSelectionMode,
        key2 = selectedItems.size,
    ) {
        if (isInSelectionMode && selectedItems.isEmpty()) {
            isInSelectionMode = false
        }
    }

    MifosDialogBox(
        showDialogState = showApproveDialog,
        onDismiss = { showApproveDialog = false },
        title = R.string.feature_checker_inbox_task_are_you_sure_you_want_to_approve_this_task,
        confirmButtonText = R.string.feature_checker_inbox_task_yes,
        onConfirm = {
            onApprove(approveId)
            showApproveDialog = false
        },
        dismissButtonText = R.string.feature_checker_inbox_task_no
    )

    MifosDialogBox(
        showDialogState = showRejectDialog,
        onDismiss = { showRejectDialog = false },
        title = R.string.feature_checker_inbox_task_are_you_sure_you_want_to_reject_this_task,
        confirmButtonText = R.string.feature_checker_inbox_task_yes,
        onConfirm = {
            onReject(rejectId)
            showRejectDialog = false
        },
        dismissButtonText = R.string.feature_checker_inbox_task_no
    )


    MifosDialogBox(
        showDialogState = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        title = R.string.feature_checker_inbox_task_are_you_sure_you_want_to_delete_this_task,
        confirmButtonText = R.string.feature_checker_inbox_task_yes,
        onConfirm = {
            onDelete(deleteId)
            showDeleteDialog = false
        },
        dismissButtonText = R.string.feature_checker_inbox_task_no
    )

    Scaffold(
        topBar = {
            if (isInSelectionMode) {
                SelectionModeTopAppBar(
                    itemCount = selectedItems.size,
                    resetSelectionMode = resetSelectionMode,
                    actions = {
                        IconButton(onClick = {
                            onApproveList(selectedItems)
                            resetSelectionMode()
                        }) {
                            Icon(
                                imageVector = MifosIcons.check,
                                tint = Color.Green,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            onRejectList(selectedItems)
                            resetSelectionMode()
                        }) {
                            Icon(
                                imageVector = MifosIcons.close,
                                tint = Color.Yellow,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            onDeleteList(selectedItems)
                            resetSelectionMode()
                        }) {
                            Icon(
                                imageVector = MifosIcons.delete,
                                tint = Color.Red,
                                contentDescription = null
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = White),
                    navigationIcon = {
                        IconButton(
                            onClick = { onBackPressed() },
                        ) {
                            Icon(
                                imageVector = MifosIcons.arrowBack,
                                contentDescription = null,
                                tint = Black,
                            )
                        }

                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.feature_checker_inbox_task_checker_inbox),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Normal
                            ),
                            color = Black,
                            textAlign = TextAlign.Start
                        )
                    },
                    actions = { }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ElevatedCard(
                modifier = Modifier.padding(8.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp),
                colors = CardDefaults.elevatedCardColors(White)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.weight(1f),
                        imageVector = MifosIcons.search,
                        contentDescription = null
                    )
                    TextField(
                        modifier = Modifier
                            .height(52.dp)
                            .weight(4f),
                        value = searchInbox,
                        onValueChange = {
                            searchInbox = it
                            search.invoke(it)
                        },
                        placeholder = { Text(stringResource(id = R.string.feature_checker_inbox_task_search_by_user)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White
                        )
                    )
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = { filter.invoke() }
                    ) {
                        Icon(
                            imageVector = MifosIcons.filter,
                            contentDescription = null
                        )
                    }
                }
            }

            when (state) {
                is CheckerInboxUiState.CheckerTasksList -> {
                    fetchedList = state.checkerTasks
                    setList.invoke(fetchedList)
                    CheckerInboxContent(
                        checkerTaskList = if (isFiltered || isSearching) filteredList else fetchedList,
                        onApprove = {
                            approveId = it
                            showApproveDialog = true
                        },
                        onReject = {
                            rejectId = it
                            showRejectDialog = true
                        },
                        onDelete = {
                            deleteId = it
                            showDeleteDialog = true
                        },
                        isInSelectionMode = isInSelectionMode,
                        selectedItems = selectedItems,
                        selectedMode = {
                            isInSelectionMode = true
                        }
                    )
                }

                is CheckerInboxUiState.Error -> {
                    MifosSweetError(
                        message = stringResource(id = R.string.feature_checker_inbox_task_failed_to_Load_Check_Inbox)
                    ) {
                        onRetry()
                    }
                }

                is CheckerInboxUiState.Loading -> {
                    MifosCircularProgress()
                }

                is CheckerInboxUiState.SuccessResponse -> {
                    val message = stringResource(state.message)
                    LaunchedEffect(key1 = message) {
                        snackbarHostState.showSnackbar(message = message)
                    }
                }
            }

        }
    }

}

@Composable
fun CheckerInboxContent(
    checkerTaskList: List<CheckerTask>,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    isInSelectionMode: Boolean,
    selectedItems: SnapshotStateList<Int>,
    selectedMode: () -> Unit
) {
    LazyColumn {
        items(checkerTaskList.size) { index ->
            CheckerInboxItem(
                checkerTask = checkerTaskList[index],
                onApprove = onApprove,
                onReject = onReject,
                onDelete = onDelete,
                isInSelectionMode = isInSelectionMode,
                selectedItems = selectedItems,
                selectedMode = selectedMode
            )
        }
    }
}


@Composable
fun CheckerInboxItem(
    checkerTask: CheckerTask,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    isInSelectionMode: Boolean,
    selectedItems: SnapshotStateList<Int>,
    selectedMode: () -> Unit
) {
    val isSelected = selectedItems.contains(checkerTask.id)
    var cardColor by remember { mutableStateOf(White) }

    var expendCheckerTask by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isInSelectionMode) {
                        cardColor = if (isSelected) {
                            selectedItems.remove(checkerTask.id)
                            White
                        } else {
                            selectedItems.add(checkerTask.id)
                            LightGray
                        }
                    } else {
                        expendCheckerTask = expendCheckerTask.not()
                    }
                },
                onLongClick = {
                    if (isInSelectionMode) {
                        cardColor = if (isSelected) {
                            selectedItems.remove(checkerTask.id)
                            White
                        } else {
                            selectedItems.add(checkerTask.id)
                            LightGray
                        }
                    } else {
                        selectedMode()
                        selectedItems.add(checkerTask.id)
                        cardColor = LightGray
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedItems.isEmpty()) {
                cardColor = White
                White
            } else cardColor,
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(8.dp)
                    .height(60.dp), colors = CardDefaults.cardColors(Color.Yellow)
            ) {
            }
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "# ${checkerTask.id} ${checkerTask.actionName} ${checkerTask.entityName}",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = checkerTask.processingResult,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text(
                            text = stringResource(id = R.string.feature_checker_inbox_task_create_by),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = checkerTask.maker,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        )
                    }
                    Text(
                        text = checkerTask.getDate(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    )
                }
            }
        }
        HorizontalDivider()
        if (expendCheckerTask) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = checkerTask.entityName,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onApprove(checkerTask.id) }) {
                        Icon(
                            imageVector = MifosIcons.check,
                            tint = Color.Green,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { onReject(checkerTask.id) }) {
                        Icon(
                            imageVector = MifosIcons.close,
                            tint = Color.Yellow,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { onDelete(checkerTask.id) }) {
                        Icon(
                            imageVector = MifosIcons.delete,
                            tint = Color.Red,
                            contentDescription = null
                        )
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = checkerTask.getDate(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider()
        }
    }
}


private fun getFilteredList(
    searchQuery: String,
    fromDate: Timestamp?,
    toDate: Timestamp?,
    action: String?,
    entity: String?,
    resourceId: String?,
    list: List<CheckerTask>
): List<CheckerTask> {

    var checkerList = list
    if (searchQuery.isNotEmpty()) {
        checkerList = checkerList.filter {
            it.maker.contains(searchQuery, true)
        }
    }

    val filteredList = mutableListOf<CheckerTask>()
    val ALL = "ALL"

    if (!resourceId.isNullOrEmpty()) {
        // If resource id is available there is no need to check for other filter options
        for (checkerTask in checkerList) {

            if (resourceId == checkerTask.resourceId) {
                filteredList.add(checkerTask)
            }
        }
        return filteredList
    } else {
        // Resource Id is not available.
        // If Clear Filter clicked
        if (fromDate == null && toDate == null) {
            return checkerList
        } else if (fromDate == null) {
            // From Date is not available
            if (action == ALL && entity == ALL) {
                // No need to check for Action and Entity
                for (checkerTask in checkerList) {
                    if (!checkerTask.getTimeStamp().after(toDate)) {
                        filteredList.add(checkerTask)
                    }
                }
                return filteredList
            } else if (action == ALL) {
                // Entity has a specific value
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().before(toDate)) {
                        if (entity.equals(checkerTask.entityName, true)) {
                            filteredList.add(checkerTask)
                        }

                    }
                }
                return filteredList
            } else if (entity == ALL) {
                // Action has a specific value
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().before(toDate)) {
                        if (action.equals(checkerTask.actionName, true)) {
                            filteredList.add(checkerTask)
                        }
                    }
                }
                return filteredList
            } else {
                // Both Action and Entity have specific values
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().before(toDate)) {
                        if (action.equals(checkerTask.actionName, true) &&
                            entity.equals(checkerTask.entityName, true)
                        ) {
                            filteredList.add(checkerTask)
                        }
                    }
                }
                return filteredList
            }
        } else {
            // Both dates are available
            if (action == ALL && entity == ALL) {
                // No need to check for Action and Entity
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().after(fromDate)
                        && checkerTask.getTimeStamp().before(toDate)
                    ) {
                        filteredList.add(checkerTask)
                    }
                }
                return filteredList
            } else if (action == ALL) {
                // Entity has a specific value
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().after(fromDate)
                        && checkerTask.getTimeStamp().before(toDate)
                    ) {
                        if (entity.equals(checkerTask.entityName, true)) {
                            filteredList.add(checkerTask)
                        }

                    }
                }
                return filteredList
            } else if (entity == ALL) {
                // Action has a specific value
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().after(fromDate)
                        && checkerTask.getTimeStamp().before(toDate)
                    ) {
                        if (action.equals(checkerTask.actionName, true)) {
                            filteredList.add(checkerTask)
                        }
                    }
                }
                return filteredList
            } else {
                // Both Action and Entity have specific values
                for (checkerTask in checkerList) {
                    if (checkerTask.getTimeStamp().after(fromDate)
                        && checkerTask.getTimeStamp().before(toDate)
                    ) {
                        if (action.equals(checkerTask.actionName, true) &&
                            entity.equals(checkerTask.entityName, true)
                        ) {
                            filteredList.add(checkerTask)
                        }
                    }
                }
                return filteredList
            }
        }
    }
}

class CheckerInboxUiStateProvider : PreviewParameterProvider<CheckerInboxUiState> {

    override val values: Sequence<CheckerInboxUiState>
        get() = sequenceOf(
            CheckerInboxUiState.Loading,
            CheckerInboxUiState.Error(R.string.feature_checker_inbox_task_failed_to_Load_Check_Inbox),
            CheckerInboxUiState.CheckerTasksList(sampleCheckerTaskList),
            CheckerInboxUiState.SuccessResponse(R.string.feature_checker_inbox_task_client_Approval)
        )
}

@Preview
@Composable
private fun CheckerInboxItemPreview() {
    CheckerInboxItem(
        checkerTask = sampleCheckerTaskList[0],
        onApprove = {},
        onReject = {},
        onDelete = {},
        isInSelectionMode = false,
        selectedItems = remember { mutableStateListOf() },
        selectedMode = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckerInboxContentPreview() {
    CheckerInboxContent(
        checkerTaskList = sampleCheckerTaskList,
        onApprove = {},
        onReject = {},
        onDelete = {},
        isInSelectionMode = false,
        selectedItems = remember { mutableStateListOf() },
        selectedMode = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckerInboxScreenPreview(
    @PreviewParameter(CheckerInboxUiStateProvider::class) state: CheckerInboxUiState
) {
    CheckerInboxScreen(
        state = state,
        onBackPressed = {},
        onApprove = {},
        onReject = {},
        onDelete = {},
        onApproveList = {},
        onRejectList = {},
        onDeleteList = {},
        filter = {},
        setList = {},
        filteredList = sampleCheckerTaskList,
        isFiltered = false,
        isSearching = false,
        search = {}
    )
}

val sampleCheckerTaskList = List(10) {
    CheckerTask(
        id = it,
        madeOnDate = it.toLong(),
        processingResult = (it % 2 == 0).toString(),
        maker = "maker $it",
        actionName = "action $it",
        entityName = "entity $it",
        resourceId = "resourceId $it",
    )
}