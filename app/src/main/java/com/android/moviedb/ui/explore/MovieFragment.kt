package com.android.moviedb.ui.explore

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.android.base.BaseFragment
import com.caner.common.ext.*
import com.android.data.Constants
import com.android.data.model.Movie
import com.android.presentation.adapter.paging.MoviesPagingAdapter
import com.android.presentation.utils.VerticalSpaceItemDecoration
import com.android.presentation.vm.MovieViewModel
import com.android.moviedb.R
import com.android.presentation.adapter.paging.MovieLoadStateAdapter
import com.android.presentation.worker.NotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movies.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MovieFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_movies

    private val viewModel: MovieViewModel by viewModels()

    private var movieAdapter = MoviesPagingAdapter { movie: Movie? ->
        recyclerItemClicked(movie)
    }

    companion object {
        const val MOVIE_TYPE = "MOVIE_TYPE"

        fun newInstance(movieType: Int): MovieFragment {
            return MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(MOVIE_TYPE, movieType)
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val movieType = arguments?.getInt(MOVIE_TYPE, 1) ?: 1
        viewModel.setMovieType(movieType)
        initPagingFlow()

        /**
         * Span count should be 1 when loader state is visible //TODO
         */
        moviesRv.apply {
            addItemDecoration(VerticalSpaceItemDecoration(8.dp2px()))
            adapter = movieAdapter.withLoadStateAll(
                refresh = MovieLoadStateAdapter(movieAdapter::refresh),
                header = MovieLoadStateAdapter(movieAdapter::retry),
                footer = MovieLoadStateAdapter(movieAdapter::retry)
            )
        }
    }

    private fun initPagingFlow() {
        lifecycleScope.launchWhenResumed {
            viewModel.moviePagingFlow.collectLatest { pagingData ->
                movieAdapter.submitData(lifecycle, pagingData)
            }
        }
    }

    private fun recyclerItemClicked(movie: Movie?) {
        val bundle = bundleOf(Constants.MOVIE_ID to (movie?.movieId ?: 0))
        findNavController().navigate(R.id.movieDetailFragment, bundle)
        startWorker(movie?.title, movie?.overview)
    }

    private fun startWorker(title: String?, overview: String?) {
        val inputs = Data.Builder().putString(Constants.MOVIE_TITLE, title)
            .putString(Constants.MOVIE_OVERVIEW, overview).build()
        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(inputs)
            .build()
        context?.let { WorkManager.getInstance(it).enqueue(request) }
    }
}