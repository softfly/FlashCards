package pl.softfly.flashcards.ui.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.softfly.flashcards.db.room.AppDatabase
import pl.softfly.flashcards.entity.app.AppConfig

/**
 * @author Grzegorz Ziemski
 */
class AppSettingsViewModel(
    private val appDb: AppDatabase,
    private val isSystemInDarkTheme: Boolean,
    application: Application
) : AndroidViewModel(application) {

    /**
     * This is to avoid access to the DB all the time the radio button is changed.
     */
    val darkModeOptionSelected: MutableLiveData<String> = MutableLiveData(AppConfig.DARK_MODE_DEFAULT)

    /**
     * Value saved after clicking OK in DB.
     */
    val darkModeDb: LiveData<AppConfig> = appDb.appConfigLiveData().findByKey(AppConfig.DARK_MODE)

    /**
     * Takes into consideration isSystemInDarkTheme()
     */
    val isDarkTheme = MutableLiveData(isSystemInDarkTheme)

    init {
        initDarkMode()
    }

    private fun initDarkMode() {
        appDb.appConfigAsync().findByKey(AppConfig.DARK_MODE)
            .subscribeOn(Schedulers.io())
            .doOnError(Throwable::printStackTrace)
            .subscribe {
                setDarkModeOption(it.value)
            }
    }

    fun setDarkModeOption(darkModeOption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            darkModeOptionSelected.postValue(darkModeOption)
            when (darkModeOption) {
                "System" -> isDarkTheme.postValue(isSystemInDarkTheme)
                "On" -> isDarkTheme.postValue(true)
                "Off" -> isDarkTheme.postValue(false)
            }
        }
    }

    fun updateDarkModeDb(darkMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (AppConfig.DARK_MODE_DEFAULT == darkMode) {
                appDb.appConfigAsync().deleteByKey(AppConfig.DARK_MODE)
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .subscribe()
            } else {
                appDb.appConfigAsync().findByKey(AppConfig.DARK_MODE)
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnEvent { value, error ->
                        if (value == null && error == null) {
                            appDb.appConfigAsync().insertAll(
                                AppConfig(
                                    AppConfig.DARK_MODE,
                                    darkMode
                                )
                            )
                                .subscribeOn(Schedulers.io())
                                .doOnError(Throwable::printStackTrace)
                                .subscribe()
                        }
                    }
                    .subscribe {
                        it.value = darkMode
                        appDb.appConfigAsync().updateAll(it)
                            .subscribeOn(Schedulers.io())
                            .doOnError(Throwable::printStackTrace)
                            .subscribe()
                    }
            }
        }
    }
}