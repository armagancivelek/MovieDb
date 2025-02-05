package com.android.moviedb.ui.movie.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.base.BaseFragment
import com.android.moviedb.databinding.FragmentMovieDetailBinding
import com.android.presentation.adapter.recyclerview.MovieGenresAdapter
import com.android.presentation.vm.MovieDetailViewModel
import com.caner.common.Constants
import com.caner.common.Resource
import com.caner.common.ext.dp2px
import com.caner.common.ext.init
import com.caner.common.ext.toast
import com.caner.common.utils.HorizontalSpaceItemDecoration
import com.caner.common.utils.VerticalSpaceItemDecoration
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment<FragmentMovieDetailBinding>() {

    private val viewModel: MovieDetailViewModel by viewModels()

    private val movieGenresAdapter = MovieGenresAdapter()

    override fun initView(savedInstanceState: Bundle?) {
        initObservers()
        setMovieGenres()

        val movieId = arguments?.getInt(Constants.MOVIE_ID)
        viewModel.getMovieDetail(movieId)

        binding.backIv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.movieDetailState.onEach { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading(resource.status)
                    is Resource.Success -> {
                        resource.data.apply {
                            binding.item = this
                            movieGenresAdapter.submitList(genres)
                        }
                    }
                    is Resource.Error -> toast("error happened ${resource.apiError.code} ${resource.apiError.message}")
                    else -> Timber.v("Initial Empty state")
                }
            }.launchIn(this)
        }
    }

    private fun setMovieGenres() {
        val flexBoxLayoutManager = FlexboxLayoutManager(context).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }

        binding.movieGenresRv.init(
            movieGenresAdapter,
            listOf(
                HorizontalSpaceItemDecoration(4.dp2px()),
                VerticalSpaceItemDecoration(4.dp2px())
            ),
            flexBoxLayoutManager
        )
    }

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMovieDetailBinding
        get() = FragmentMovieDetailBinding::inflate
}
