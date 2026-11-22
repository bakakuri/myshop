package ge.myshop.app.ui.screens.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ge.myshop.app.domain.model.Category
import ge.myshop.app.ui.components.AppHeader
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.components.ShimmerBox
import ge.myshop.app.ui.theme.CategoryCardShape
import ge.myshop.app.ui.theme.PrimaryPurple

@Composable
fun CategoriesScreen(
    onCategoryClick: (categoryId: String, name: String) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                onSearchClick = onSearchClick,
                onCartClick = onCartClick,
                onNotificationsClick = onNotificationsClick,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> CategoriesShimmer(padding)
            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    CategoryGridCard(category = category, onClick = { onCategoryClick(category.id, category.nameKa) })
                }
            }
        }
    }
}

@Composable
private fun CategoryGridCard(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(CategoryCardShape)
            .clickable(onClick = onClick),
        shape = CategoryCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            if (!category.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = category.imageUrl,
                    contentDescription = category.nameKa,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                        // Semi-transparent overlay
                        .clip(CategoryCardShape),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Text(
                    text = category.nameKa,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (category.imageUrl != null) Color.White else PrimaryPurple,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun CategoriesShimmer(padding: PaddingValues) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 12.dp, end = 12.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = 8.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(8) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(130.dp).clip(CategoryCardShape))
        }
    }
}
