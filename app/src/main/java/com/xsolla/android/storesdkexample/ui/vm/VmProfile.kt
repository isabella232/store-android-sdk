package com.xsolla.android.storesdkexample.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xsolla.android.login.XLogin
import com.xsolla.android.login.callback.GetCurrentUserDetailsCallback
import com.xsolla.android.login.callback.ResetPasswordCallback
import com.xsolla.android.login.callback.UpdateCurrentUserDetailsCallback
import com.xsolla.android.login.callback.UpdateCurrentUserPhoneCallback
import com.xsolla.android.login.entity.response.GenderResponse
import com.xsolla.android.login.entity.response.UserDetailsResponse
import com.xsolla.android.storesdkexample.R
import com.xsolla.android.storesdkexample.data.local.IResourceProvider
import com.xsolla.android.storesdkexample.util.SingleLiveEvent
import com.xsolla.android.storesdkexample.util.extensions.BirthdayFormat
import com.xsolla.android.storesdkexample.util.extensions.formatBirthday
import java.util.Locale
import java.util.regex.Pattern

class VmProfile(private val resourceProvider: IResourceProvider) : ViewModel() {
    private companion object {
        private val PHONE_PATTERN = Pattern.compile("^\\+(\\d){5,25}\$")
    }

    private val _state = MutableLiveData<UserDetailsUi>()
    val state: LiveData<UserDetailsUi> = _state

    val stateForChanging = MutableLiveData<UserDetailsUi>()

    val message = SingleLiveEvent<String>()

    init {
        load()
    }

    fun load() {
        XLogin.getCurrentUserDetails(object : GetCurrentUserDetailsCallback {
            override fun onSuccess(data: UserDetailsResponse) {
                val uiEntity = UserDetailsUi(
                    id = data.id,
                    email = data.email ?: "",
                    username = data.username ?: "",
                    nickname = data.nickname ?: "",
                    firstName = data.firstName ?: "",
                    lastName = data.lastName ?: "",
                    birthday = data.birthday ?: "",
                    phone = data.phone ?: "",
                    gender = Gender.getBy(data.gender),
                    avatar = data.picture ?: ""
                )
                _state.value = uiEntity
                stateForChanging.value = uiEntity
            }

            override fun onError(throwable: Throwable?, errorMessage: String?) {
                message.value = throwable?.message ?: errorMessage ?: resourceProvider.getString(R.string.failure)
            }
        })
    }

    fun updateFields(newState: UserDetailsUi) {
        val gender = newState.gender?.name?.toLowerCase(Locale.getDefault())?.first()?.toString()
        val birthday = newState.birthday.formatBirthday(BirthdayFormat.FROM_UI_TO_BACKEND)
        XLogin.updateCurrentUserDetails(birthday, newState.firstName, gender, newState.lastName, newState.nickname, object : UpdateCurrentUserDetailsCallback {
            override fun onSuccess() {
                message.value = resourceProvider.getString(R.string.profile_fields_were_changed)
                _state.value = newState.copy(phone = state.value!!.phone)

                if (newState.phone != state.value!!.phone) {
                    updatePhone(newState.phone)
                }
            }

            override fun onError(throwable: Throwable?, errorMessage: String?) {
                message.value = throwable?.message ?: errorMessage ?: resourceProvider.getString(R.string.failure)
            }
        })
    }

    fun updatePhone(newPhone: String) {
        XLogin.updateCurrentUserPhone(newPhone, object : UpdateCurrentUserPhoneCallback {
            override fun onSuccess() {
                _state.value = state.value!!.copy(phone = newPhone)
            }

            override fun onError(throwable: Throwable?, errorMessage: String?) {
                message.value = throwable?.message ?: errorMessage ?: resourceProvider.getString(R.string.failure)
            }
        })
    }

    fun validateField(field: FieldsForChanging, text: String): ValidateFieldResult {
        if (field in FieldsForChanging.textFields) {
            return if (text.length < 255) {
                ValidateFieldResult(true)
            } else {
                ValidateFieldResult(false, resourceProvider.getString(R.string.profile_text_field_validation, field.name))
            }
        } else if (field == FieldsForChanging.PHONE) {
            return if (PHONE_PATTERN.matcher(text).matches()) {
                ValidateFieldResult(true, null)
            } else {
                ValidateFieldResult(false, resourceProvider.getString(R.string.profile_phone_validation))
            }
        }

        return ValidateFieldResult(true, null)
    }

    fun updateAvatar(avatar: String?) {
        _state.value = _state.value?.copy(avatar = avatar)
        stateForChanging.value = stateForChanging.value!!.copy(avatar = avatar)
    }

    fun resetPassword() {
        val username = state.value!!.username
        val email = state.value!!.email
        if (username.isBlank() || email.isBlank()) return
        XLogin.resetPassword(username, object : ResetPasswordCallback {
            override fun onSuccess() {
                message.value = "A letter has been sent to the $email. Follow the link in the letter — and you can create a new password"
            }

            override fun onError(throwable: Throwable?, errorMessage: String?) {
                message.value = throwable?.message ?: errorMessage ?: resourceProvider.getString(R.string.failure)
            }
        })
    }
}

data class UserDetailsUi(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val nickname: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: String = "",
    val phone: String = "",
    val gender: Gender? = null,
    val avatar: String? = null
)

enum class FieldsForChanging {
    NICKNAME {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(nickname = value)
        }
    },
    PHONE {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(phone = value)
        }
    },
    FIRST_NAME {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(firstName = value)
        }
    },
    LAST_NAME {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(lastName = value)
        }
    },
    BIRTHDAY {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(birthday = value)
        }
    },
    GENDER {
        override fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>) {
            val nonNullState = state.value ?: return
            state.value = nonNullState.copy(gender = Gender.valueOf(value))
        }
    };

    abstract fun updateStateForChanging(value: String, state: MutableLiveData<UserDetailsUi>)

    companion object {
        val textFields = arrayOf(NICKNAME, FIRST_NAME, LAST_NAME)
    }
}

enum class Gender(val response: GenderResponse) {
    Female(GenderResponse.F),
    Male(GenderResponse.M);

    companion object {
        fun getBy(response: GenderResponse?): Gender? {
            return when (response) {
                GenderResponse.F -> Female
                GenderResponse.M -> Male
                null -> null
            }
        }
    }
}

data class ValidateFieldResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)