
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.SurveyRepository
import com.example.urbane.ui.admin.settings.model.SurveysState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SurveysViewModel(sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(SurveysState())
    val state: StateFlow<SurveysState> = _state.asStateFlow()

    val repository = SurveyRepository(sessionManager)

    fun loadSurveys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val surveys = repository.getAllSurveys()
                _state.update {
                    it.copy(
                        surveys = surveys,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error desconocido al cargar encuestas"
                    )
                }
                Log.e("SurveysViewModel", "Error loading surveys: ${e.message}", e)
            }
        }
    }
}

