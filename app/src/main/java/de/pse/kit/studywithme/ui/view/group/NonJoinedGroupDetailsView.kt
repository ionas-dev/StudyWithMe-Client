package de.pse.kit.studywithme.ui.view.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import de.pse.kit.myapplication.ui.theme.MyApplicationTheme3
import de.pse.kit.studywithme.model.repository.FakeGroupRepository
import de.pse.kit.studywithme.model.repository.FakeSessionRepository
import de.pse.kit.studywithme.ui.component.Button
import de.pse.kit.studywithme.ui.component.NavigationBar
import de.pse.kit.studywithme.ui.component.TopBar
import de.pse.kit.studywithme.ui.layout.GroupDetailsLayout
import de.pse.kit.studywithme.viewModel.group.NonJoinedGroupDetailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Collections.list


@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun NonJoinedGroupDetailsView(viewModel: NonJoinedGroupDetailsViewModel) {
    MyApplicationTheme3 {
        val group by viewModel.group
        val groupAdmins by viewModel.admins

        Scaffold(
            topBar = {
                TopBar(
                    title = group?.name ?: "",
                    subtitle = group?.lecture?.lectureName ?: "",
                    navClick = { viewModel.navBack() })
            },
            bottomBar = {
                NavigationBar(
                    selectedItem = remember { mutableStateOf(1) },
                    clickLeft = { viewModel.navToJoinedGroups() },
                    clickMiddle = { viewModel.navBack() },
                    clickRight = { viewModel.navToProfile() })
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 0.dp)) {
                GroupDetailsLayout(
                    groupAdmin = groupAdmins.map {
                        it.name
                    },
                    groupMember = listOf(),
                    description = group?.description ?: "",
                    selectedChips = listOf(
                        group?.sessionFrequency?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() }
                            ?: "",
                        group?.sessionType?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                            ?: "").filter { it != "" },
                    chapterNumber = group?.lectureChapter,
                    exerciseSheetNumber = group?.exercise
                )
                Button(text = "Beitreten", onClick = { viewModel.joinRequest() })
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Preview
@Composable
fun NonJoinedGroupDetailsViewPreview() {
    NonJoinedGroupDetailsView(
        NonJoinedGroupDetailsViewModel(
            navController = rememberNavController(),
            groupID = 0,
            groupRepo = FakeGroupRepository()
        )
    )
}