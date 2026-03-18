package ru.gr05307.kareta05307

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import kareta05307.composeapp.generated.resources.Res
import kareta05307.composeapp.generated.resources.compose_multiplatform

@Composable
fun App(viewModel: GraphicsUI) {
    MaterialTheme {
        Column(Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                reverseLayout = true,
                verticalArrangement = spacedBy(4.dp),
            ) {
                items(viewModel.messages){

                }
            }
            Row(
                Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    viewModel.userText,
                    {
                        viewModel.userText = it
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 5,
                )
                IconButton(
                    onClick = {},
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null
                    )
                }
            }
        }
    }
}