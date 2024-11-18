package com.example.dicodingevent.adapterviewmodel

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.dicodingevent.R
import com.example.dicodingevent.data.response.ListEventsItem
import com.example.dicodingevent.databinding.ListEventHorizontalBinding

class ReviewHorizontalAdapter(private val onItemClick: ((Int?) -> Unit)? = null) :
    ListAdapter<ListEventsItem, ReviewHorizontalAdapter.HorizontalAdapterViewHolder>(ListEventsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalAdapterViewHolder {
        val binding =
            ListEventHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HorizontalAdapterViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HorizontalAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HorizontalAdapterViewHolder(
        private val binding: ListEventHorizontalBinding,
        private val onItemClick: ((Int?) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: ListEventsItem) {
            binding.titleEvent.text = event.name
            binding.progressBarImageLoading.visibility = android.view.View.VISIBLE
            Glide.with(binding.imageEvent.context)
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
                        binding.progressBarImageLoading.visibility = android.view.View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBarImageLoading.visibility = android.view.View.GONE
                        return false
                    }
                })
                .into(binding.imageEvent)

            binding.root.setOnClickListener {
                onItemClick?.invoke(event.id)
            }
        }
    }

    class ListEventsDiffUtil : DiffUtil.ItemCallback<ListEventsItem>() {
        override fun areItemsTheSame(oldItem: ListEventsItem, newItem: ListEventsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListEventsItem, newItem: ListEventsItem): Boolean {
            return oldItem == newItem
        }
    }
}
