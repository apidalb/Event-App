package com.example.dicodingevent.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.dicodingevent.R
import com.example.dicodingevent.adapterviewmodel.MainViewModel
import com.example.dicodingevent.adapterviewmodel.MainViewModelFactory
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent
import com.example.dicodingevent.data.response.EventDetailResponse
import com.example.dicodingevent.databinding.ActivityDetailEventBinding

class DetailEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailEventBinding

    private val detailViewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(this) //for ActivityBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Detail Event"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eventId = intent.getIntExtra("EXTRA_EVENT_ID", -1)
        eventId.takeIf { it != -1 }?.let { detailViewModel.getEventDetail(it) }
        observeViewModel()

        detailViewModel.getFavoriteEvents()
    }



    private fun observeViewModel() {
        detailViewModel.eventDetail.observe(this) { eventDetail ->
            eventDetail?.let {
                setupUI(it)
                binding.btnOpenLink.visibility = View.VISIBLE
            }
        }

        detailViewModel.errorMessage.observe(this) { errorMessage ->
            when {
                !errorMessage.isNullOrEmpty() -> {
                    binding.tvErrorMessage.text = errorMessage
                    binding.tvErrorMessage.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvErrorMessage.visibility = View.GONE
                }
            }
        }

        detailViewModel.isLoadingDetail.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnOpenLink.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(detail: EventDetailResponse) {
        binding.apply {
            detail.event.let { event ->
                progressBarImageLoading.visibility = View.VISIBLE
                Glide.with(this@DetailEventActivity)
                    .load(event.imageLogo)
                    .placeholder(R.mipmap.image_placeholder_background)
                    .error(R.mipmap.broken_image_background)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBarImageLoading.visibility = View.GONE
                            imgEventLogo.setImageResource(R.mipmap.broken_image_background)
                            imgEventLogo.setOnClickListener {
                                Glide.with(this@DetailEventActivity)
                                    .load(event.imageLogo)
                                    .placeholder(R.mipmap.image_placeholder_background)
                                    .error(R.mipmap.broken_image_background)
                                    .listener(this)
                                    .into(imgEventLogo)
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBarImageLoading.visibility = View.GONE
                            imgEventLogo.setOnClickListener(null)
                            return false
                        }
                    })
                    .into(imgEventLogo)

                tvEventName.text = event.name
                tvOwnerName.text = "Penyelenggara: ${event.ownerName}"
                tvBeginTime.text = "Waktu Acara: ${event.beginTime}"
                tvQuota.text = "Sisa Kuota: ${event.quota?.minus(event.registrants ?: 0)}"

                val htmlDescription = HtmlCompat.fromHtml(
                    event.description ?: "",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                tvDescription.text = htmlDescription
                tvDescription.movementMethod = LinkMovementMethod.getInstance()

                btnOpenLink.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                    startActivity(intent)
                }

                // Set initial favorite status
                val isFavorited = event.id?.let { detailViewModel.isEventFavorited(it) }
                if (isFavorited != null) {
                    event.isFavorited = isFavorited
                }
                if (isFavorited != null) {
                    updateFavoriteIcon(isFavorited)
                }

                fabLove.visibility = View.VISIBLE
                fabLove.setOnClickListener {
                    if (event.isFavorited) {
                        fabLove.setImageResource(R.drawable.baseline_favorite_border_24)
                        event.isFavorited = false
                        event.id?.let { it1 -> detailViewModel.removeItemFavorite(it1) }
                        Toast.makeText(this@DetailEventActivity, "Item ini telah dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    } else {
                        fabLove.setImageResource(R.drawable.baseline_favorite_24)
                        event.isFavorited = true
                        val favoriteEvent = RoomDBFavoriteEvent(
                            eventID = event.id,
                            name = event.name,
                            description = event.summary,
                            image = event.imageLogo
                        )
                        detailViewModel.addItemFavorite(favoriteEvent)
                        Toast.makeText(this@DetailEventActivity, "Item ini telah ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.imgEventLogo.post {
                    binding.imgEventLogo.outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            val radius = 24f
                            outline.setRoundRect(0, 0, view.width, view.height, radius)
                            view.clipToOutline = true
                        }
                    }
                }
            }
        }
    }

    private fun updateFavoriteIcon(isFavorited: Boolean) {
        if (isFavorited) {
            binding.fabLove.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            binding.fabLove.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
