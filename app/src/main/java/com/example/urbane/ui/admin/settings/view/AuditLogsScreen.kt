
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.urbane.data.model.AuditLog
import com.example.urbane.ui.admin.settings.viewmodel.AuditLogsViewModel
import com.example.urbane.utils.getAuditMessage

@Composable
fun AuditLogsScreen(
    viewModel: AuditLogsViewModel, goBack : () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadLogs()
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.errorMessage!!)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.logs) { log ->
                    LogItem(log)
                }
            }
        }
    }
}


@Composable
fun LogItem(log: AuditLog) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = getAuditMessage(context, log),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = log.createdAt ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Divider()
    }
}
