package com.example.dicodingevent.adapterviewmodel

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
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
import com.example.dicodingevent.databinding.ListEventVerticalBinding

class ReviewVerticalAdapter(private val onItemClick: ((Int?) -> Unit)? = null) :
    ListAdapter<ListEventsItem, ReviewVerticalAdapter.VerticalAdapterViewHolder>(ListEventsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalAdapterViewHolder {
        val binding = ListEventVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerticalAdapterViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: VerticalAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VerticalAdapterViewHolder(
        private val binding: ListEventVerticalBinding,
        private val onItemClick: ((Int?) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: ListEventsItem) {
            binding.titleEvent.text = event.name
            binding.descriptionEvent.text = event.summary
            binding.imageLoadingIndicator.visibility = View.VISIBLE

            // Use Glide with placeholder, error image, and RequestListener
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
                        binding.imageLoadingIndicator.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageLoadingIndicator.visibility = View.GONE
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
            return oldItem.id == newItem.id // Assuming ID is unique for each event
        }

        override fun areContentsTheSame(oldItem: ListEventsItem, newItem: ListEventsItem): Boolean {
            return oldItem == newItem
        }
    }
}
