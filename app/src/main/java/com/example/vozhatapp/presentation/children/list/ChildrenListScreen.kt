package com.example.vozhatapp.presentation.children

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.R
import com.example.vozhatapp.presentation.children.components.*
import com.example.vozhatapp.presentation.children.list.ChildrenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenListScreen(
    onAddChildClick: () -> Unit,
    onChildClick: (Long) -> Unit,
    childrenViewModel: ChildrenViewModel = hiltViewModel()
) {
    val childrenState = childrenViewModel.childrenState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var selectedSquad by remember { mutableStateOf<String?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    val squads = childrenState.value.children
        .map { it.squadName }
        .distinct()
        .sorted()

    val filteredChildren = childrenState.value.children.filter { child ->
        (searchQuery.isEmpty() || child.fullName.contains(searchQuery, ignoreCase = true)) &&
                (selectedSquad == null || child.squadName == selectedSquad)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChildrenTopAppBar(
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedAddChildFab(
                expanded = fabExpanded,
                onClick = {
                    coroutineScope.launch {
                        fabExpanded = true
                        delay(200)
                        onAddChildClick()
                        delay(300)
                        fabExpanded = false
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Search field
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                focused = searchFocused,
                onFocusChange = { searchFocused = it }
            )

            // Squad filter section
            SquadFilterSection(
                squads = squads,
                selectedSquad = selectedSquad,
                onSquadSelected = { selectedSquad = it }
            )

            // Children list
            if (childrenState.value.isLoading) {
                LoadingChildrenListPlaceholder()
            } else if (filteredChildren.isEmpty()) {
                EmptyChildrenList(searchQuery.isNotEmpty())
            } else {
                // Results count and scroll to top button
                ResultsHeader(
                    filteredCount = filteredChildren.size,
                    totalCount = childrenState.value.children.size,
                    onScrollToTop = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                )

                // List of children
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredChildren) { index, child ->
                        AnimatedChildListItem(
                            child = child,
                            onClick = { onChildClick(child.id) },
                            index = index
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    filteredCount: Int,
    totalCount: Int,
    onScrollToTop: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                R.string.children_count_template,
                filteredCount,
                totalCount
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        SmallFloatingActionButton(
            onClick = onScrollToTop,
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Вернуться в начало списка"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildrenTopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.children_list_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        scrollBehavior = scrollBehavior
    )
}