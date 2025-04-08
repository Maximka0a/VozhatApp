package com.example.vozhatapp.presentation.children.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.repository.ChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class AddChildViewModel @Inject constructor(
    private val childRepository: ChildRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddChildState())
    val state: StateFlow<AddChildState> = _state.asStateFlow()

    fun loadChild(childId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val child = childRepository.getChildById(childId).first()

                _state.update {
                    it.copy(
                        id = child.id,
                        name = child.name,
                        lastName = child.lastName,
                        age = child.age,
                        squadName = child.squadName,
                        photoUrl = child.photoUrl,
                        parentPhone = child.parentPhone,
                        parentEmail = child.parentEmail,
                        address = child.address,
                        medicalNotes = child.medicalNotes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        saveError = "Не удалось загрузить данные ребенка: ${e.message}"
                    )
                }
            }
        }
    }

    fun onEvent(event: AddChildEvent) {
        when (event) {
            is AddChildEvent.NameChanged -> {
                _state.update { it.copy(
                    name = event.value,
                    nameError = null
                ) }
            }
            is AddChildEvent.LastNameChanged -> {
                _state.update { it.copy(
                    lastName = event.value,
                    lastNameError = null
                ) }
            }
            is AddChildEvent.AgeChanged -> {
                val age = event.value.toIntOrNull() ?: 0
                _state.update { it.copy(
                    age = age,
                    ageError = null
                ) }
            }
            is AddChildEvent.SquadNameChanged -> {
                _state.update { it.copy(
                    squadName = event.value,
                    squadNameError = null
                ) }
            }
            is AddChildEvent.ParentPhoneChanged -> {
                _state.update { it.copy(
                    parentPhone = event.value,
                    parentPhoneError = null
                ) }
            }
            is AddChildEvent.ParentEmailChanged -> {
                _state.update { it.copy(
                    parentEmail = event.value,
                    parentEmailError = null
                ) }
            }
            is AddChildEvent.AddressChanged -> {
                _state.update { it.copy(address = event.value) }
            }
            is AddChildEvent.MedicalNotesChanged -> {
                _state.update { it.copy(medicalNotes = event.value) }
            }
            is AddChildEvent.PhotoUrlChanged -> {
                _state.update { it.copy(photoUrl = event.value) }
            }
            AddChildEvent.SaveChild -> {
                saveChild()
            }
            AddChildEvent.DeleteChild -> {
                deleteChild()
            }
            AddChildEvent.ResetState -> {
                _state.update { AddChildState() }
            }
        }
    }

    private fun saveChild() {
        val validationResult = validateInputs()

        if (!validationResult.successful) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, saveError = null) }

            try {
                val child = Child(
                    id = state.value.id ?: 0, // Use existing ID if in edit mode
                    name = state.value.name,
                    lastName = state.value.lastName,
                    age = state.value.age,
                    squadName = state.value.squadName,
                    photoUrl = state.value.photoUrl,
                    parentPhone = state.value.parentPhone,
                    parentEmail = state.value.parentEmail,
                    address = state.value.address,
                    medicalNotes = state.value.medicalNotes,
                    createdAt = state.value.id?.let { System.currentTimeMillis() } ?: System.currentTimeMillis()
                )

                val result = if (state.value.id != null) {
                    // Update existing child
                    childRepository.updateChild(child)
                    state.value.id
                } else {
                    // Insert new child
                    childRepository.insertChild(child)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        savedChildId = result
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        saveError = e.message ?: "Не удалось сохранить данные ребенка"
                    )
                }
            }
        }
    }

    private fun deleteChild() {
        val childId = state.value.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, saveError = null) }

            try {
                val child = childRepository.getChildById(childId).first()
                childRepository.deleteChild(child)

                _state.update {
                    it.copy(
                        isLoading = false,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        saveError = e.message ?: "Не удалось удалить ребенка"
                    )
                }
            }
        }
    }

    private fun validateInputs(): ValidationResult {
        val nameResult = validateName(state.value.name)
        if (!nameResult.successful) {
            _state.update { it.copy(nameError = nameResult.errorMessage) }
            return ValidationResult(false, "Ошибка в имени")
        }

        val lastNameResult = validateLastName(state.value.lastName)
        if (!lastNameResult.successful) {
            _state.update { it.copy(lastNameError = lastNameResult.errorMessage) }
            return ValidationResult(false, "Ошибка в фамилии")
        }

        val ageResult = validateAge(state.value.age)
        if (!ageResult.successful) {
            _state.update { it.copy(ageError = ageResult.errorMessage) }
            return ValidationResult(false, "Ошибка в возрасте")
        }

        val squadResult = validateSquadName(state.value.squadName)
        if (!squadResult.successful) {
            _state.update { it.copy(squadNameError = squadResult.errorMessage) }
            return ValidationResult(false, "Ошибка в названии отряда")
        }

        // At least one contact method must be provided
        if (state.value.parentPhone.isNullOrBlank() && state.value.parentEmail.isNullOrBlank()) {
            _state.update {
                it.copy(parentPhoneError = "Укажите хотя бы один способ связи с родителями")
            }
            return ValidationResult(false, "Отсутствуют контактные данные")
        }

        // Validate phone if provided
        if (!state.value.parentPhone.isNullOrBlank()) {
            val phoneResult = validatePhone(state.value.parentPhone)
            if (!phoneResult.successful) {
                _state.update { it.copy(parentPhoneError = phoneResult.errorMessage) }
                return ValidationResult(false, "Неверный формат телефона")
            }
        }

        // Validate email if provided
        if (!state.value.parentEmail.isNullOrBlank()) {
            val emailResult = validateEmail(state.value.parentEmail)
            if (!emailResult.successful) {
                _state.update { it.copy(parentEmailError = emailResult.errorMessage) }
                return ValidationResult(false, "Неверный формат email")
            }
        }

        return ValidationResult(true)
    }

    // Validation functions

    private fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Имя не может быть пустым"
            )
        }

        if (name.length < 2) {
            return ValidationResult(
                successful = false,
                errorMessage = "Имя должно содержать хотя бы 2 символа"
            )
        }

        return ValidationResult(successful = true)
    }

    private fun validateLastName(lastName: String): ValidationResult {
        if (lastName.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Фамилия не может быть пустой"
            )
        }

        if (lastName.length < 2) {
            return ValidationResult(
                successful = false,
                errorMessage = "Фамилия должна содержать хотя бы 2 символа"
            )
        }

        return ValidationResult(successful = true)
    }

    private fun validateAge(age: Int): ValidationResult {
        if (age <= 0) {
            return ValidationResult(
                successful = false,
                errorMessage = "Возраст должен быть больше 0"
            )
        }

        if (age > 18) {
            return ValidationResult(
                successful = false,
                errorMessage = "Максимальный возраст — 18 лет"
            )
        }

        return ValidationResult(successful = true)
    }

    private fun validateSquadName(squadName: String): ValidationResult {
        if (squadName.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Название отряда не может быть пустым"
            )
        }

        return ValidationResult(successful = true)
    }

    private fun validatePhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult(successful = true)  // Phone is optional if email provided
        }

        // Validate phone number format
        val phonePattern = Pattern.compile("^\\+?[0-9]{10,15}$")
        if (!phonePattern.matcher(phone).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Неверный формат телефона"
            )
        }

        return ValidationResult(successful = true)
    }

    private fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult(successful = true)  // Email is optional if phone provided
        }

        // Validate email format
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        if (!emailPattern.matcher(email).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Неверный формат email"
            )
        }

        return ValidationResult(successful = true)
    }

    fun loadSquads(): Flow<List<String>> {
        return childRepository.getAllSquadNames()
    }
}

data class AddChildState(
    val id: Long? = null,  // Added to track if we're editing an existing child
    val name: String = "",
    val nameError: String? = null,
    val lastName: String = "",
    val lastNameError: String? = null,
    val age: Int = 0,
    val ageError: String? = null,
    val squadName: String = "",
    val squadNameError: String? = null,
    val parentPhone: String? = null,
    val parentPhoneError: String? = null,
    val parentEmail: String? = null,
    val parentEmailError: String? = null,
    val address: String? = null,
    val medicalNotes: String? = null,
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,  // Added to track successful deletion
    val saveError: String? = null,
    val savedChildId: Long? = null
)

sealed class AddChildEvent {
    data class NameChanged(val value: String) : AddChildEvent()
    data class LastNameChanged(val value: String) : AddChildEvent()
    data class AgeChanged(val value: String) : AddChildEvent()
    data class SquadNameChanged(val value: String) : AddChildEvent()
    data class ParentPhoneChanged(val value: String) : AddChildEvent()
    data class ParentEmailChanged(val value: String) : AddChildEvent()
    data class AddressChanged(val value: String) : AddChildEvent()
    data class MedicalNotesChanged(val value: String) : AddChildEvent()
    data class PhotoUrlChanged(val value: String?) : AddChildEvent()
    object SaveChild : AddChildEvent()
    object DeleteChild : AddChildEvent()  // Added to handle deletion
    object ResetState : AddChildEvent()
}

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)