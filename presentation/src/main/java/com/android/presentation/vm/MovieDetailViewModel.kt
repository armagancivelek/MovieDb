package com.android.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.data.model.MovieDetailModel
import com.android.domain.usecase.MovieDetailUseCase
import com.caner.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val movieDetailUseCase: MovieDetailUseCase
) : ViewModel() {

    private val _movieDetailState = MutableStateFlow<Resource<MovieDetailModel>>(Resource.Empty)
    val movieDetailState: StateFlow<Resource<MovieDetailModel>> get() = _movieDetailState

    fun getMovieDetail(movieId: Int?) {
        viewModelScope.launch {
            movieDetailUseCase.execute(movieId)
                .collect {
                    _movieDetailState.value = it
                }
        }
    }
}
