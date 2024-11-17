package ru.otus.coroutines.second

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import ru.otus.coroutines.second.data.CatsData
import ru.otus.coroutines.second.theme.ApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val model = CatsViewModel()
        setContent {
            ApplicationTheme {
                val data = model.result.collectAsStateWithLifecycle().value
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        data?.isFailure == true ->
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT)
                                .show()

                        data?.isSuccess == true -> {
                            ContentBody(
                                data = data.getOrNull(),
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                model.populateCats()
            }
        }
    }

    @Composable
    fun ContentBody(data: CatsData?, modifier: Modifier = Modifier) {
        Column(modifier = modifier.padding(horizontal = 10.dp)) {
            if (data != null) {
                Text(data.fact)
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        items(data.searchResult) { image ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(image.url)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .border(border = BorderStroke(1.dp, Color.Blue))
                                    .width(image.width.dp)
                                    .height(image.height.dp),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
