package com.wyjqwy.app.ui.category.reorder

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun CategoryReorderGridScreen(
    vm: MainViewModel,
    onClickCategory: (Category) -> Unit = {}
) {
    val categories = vm.categories
    val reorderState = rememberReorderableLazyGridState(
        onMove = { from, to -> vm.move(from.index, to.index) },
        onDragEnd = { _, _ ->
            vm.persistOrder()
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = reorderState.gridState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(reorderState)
            .detectReorderAfterLongPress(reorderState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.size, key = { categories[it].id }) { index ->
            val item = categories[index]
            ReorderableItem(reorderState, key = item.id) { isDragging ->
                val scale by animateFloatAsState(if (isDragging) 1.08f else 1f, label = "itemScale")
                val alpha by animateFloatAsState(if (isDragging) 0.85f else 1f, label = "itemAlpha")
                val breath = if (isDragging) {
                    val t = rememberInfiniteTransition(label = "breath")
                    t.animateFloat(
                        initialValue = 0.985f,
                        targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "breathScale"
                    ).value
                } else 1f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.White)
                        .graphicsLayer {
                            scaleX = scale * breath
                            scaleY = scale * breath
                            this.alpha = alpha
                        }
                        .clickable { onClickCategory(item) }
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = item.iconResId),
                            contentDescription = item.name,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(text = item.name)
                }
            }
        }
    }
}
