package com.android.domain.usecase

import com.android.base.BaseUseCase
import com.android.domain.qualifier.IoDispatcher
import com.android.domain.repository.MovieGalleryRepository
import com.caner.common.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class MovieGalleryUseCase @Inject constructor(
    private val galleryRepository: MovieGalleryRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : BaseUseCase<Any, Int>() {

    override fun buildRequest(params: Int?): Flow<Resource<Any>> {
        return galleryRepository.getMovieGallery(params)
            .onStart { emit(Resource.Loading(true)) }
            .onCompletion { emit(Resource.Loading(false)) }
            .flowOn(dispatcher)
    }
}