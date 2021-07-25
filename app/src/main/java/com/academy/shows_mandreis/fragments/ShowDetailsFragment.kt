package com.academy.shows_mandreis.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.academy.shows_mandreis.R
import com.academy.shows_mandreis.databinding.DialogAddReviewBinding
import com.academy.shows_mandreis.databinding.FragmentShowDetailsBinding
import com.academy.shows_mandreis.model.Review
import com.academy.shows_mandreis.ui.ReviewsAdapter
import com.academy.shows_mandreis.ui.ShowDetailsActivity
import com.academy.shows_mandreis.utility.MockDatabase
import com.academy.shows_mandreis.view_models.ReviewsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.roundToInt

class ShowDetailsFragment : Fragment() {

    private var _binding: FragmentShowDetailsBinding? = null
    private var adapter: ReviewsAdapter? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val args: ShowDetailsFragmentArgs by navArgs()

    private val viewModel: ReviewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.title = args.name
        binding.descriptionText.text = args.desc
        binding.showImage.setImageResource(args.pic)

        binding.writeReviewButton.setOnClickListener {
            showBottomSheet()
        }

        initRecyclerView()
        refreshScreen()

        binding.topAppBar.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        viewModel.initReviews(args.id)
        viewModel.getReviewsLiveData().observe(viewLifecycleOwner, { reviews ->
            loadItems(reviews)
        })
    }

    private fun loadItems(reviews: List<Review>) {
        adapter?.setItems(reviews)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshScreen() {
        val reviews = adapter?.getReviews()!!
        if (reviews.isEmpty()) {
            binding.reviewsRecycler.visibility = View.GONE
            binding.noReviewsText.visibility = View.VISIBLE
        } else {
            binding.reviewsRecycler.visibility = View.VISIBLE
            binding.noReviewsText.visibility = View.GONE
            val params = binding.writeReviewButton.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = binding.reviewsRecycler.id
            binding.reviewRatingBar.visibility = View.VISIBLE
            binding.reviewsStatsText.visibility = View.VISIBLE

            var total = 0.0
            for(review in reviews) {
                total += review.rating
            }

            val average = ((total / reviews.size) * 100).roundToInt() / 100.0

            binding.reviewsStatsText.text = reviews.size.toString().plus(" REVIEWS, ").plus(average.toString()).plus(" AVERAGE")
            binding.reviewRatingBar.rating = average.toFloat()
        }
    }

    private fun initRecyclerView() {
        binding.reviewsRecycler.layoutManager = LinearLayoutManager(view?.context, LinearLayoutManager.VERTICAL, false)

        val reviews = emptyList<Review>()
        adapter = ReviewsAdapter(reviews)
        binding.reviewsRecycler.adapter = adapter
    }

    private fun showBottomSheet() {
        val dialog = view?.let { BottomSheetDialog(it.context) }

        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        dialog?.setContentView(dialogBinding.root)

        dialogBinding.confirmButton.setOnClickListener {
            val review = Review("imenko.prezimenovic", dialogBinding.commentInput.editText?.text.toString(), dialogBinding.reviewRatingBar.rating.toInt(), R.drawable.ic_profile_placeholder)
            // adapter?.addReview(review)
            viewModel.addReview(review)
            dialog?.dismiss()
            refreshScreen()
        }

        dialog?.show()
    }
}