package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.FrostwaveTheme
import com.example.viewmodel.FrostwaveViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: FrostwaveViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      FrostwaveTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Frostwave Siege: Freeze and roll arcade action! Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  FrostwaveTheme { Greeting("Player") }
}


