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
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent
import com.example.dicodingevent.databinding.ListEventFavoriteBinding

class FavoriteEventAdapter(private val onItemClick: ((Int) -> Unit)? = null) :
    ListAdapter<RoomDBFavoriteEvent, FavoriteEventAdapter.FavoriteViewHolder>(FavoriteEventDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ListEventFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(
        private val binding: ListEventFavoriteBinding,
        private val onItemClick: ((Int) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: RoomDBFavoriteEvent) {
            binding.tvEventName.text = event.name
            binding.tvEventDescription.text = event.description
            binding.imageLoadingIndicator.visibility = View.VISIBLE

            Glide.with(binding.ivEventImage.context)
                .load(event.image)
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
                .into(binding.ivEventImage)

            binding.root.setOnClickListener {
                event.eventID?.let { it1 -> onItemClick?.invoke(it1) }
            }
        }
    }

    class FavoriteEventDiffUtil : DiffUtil.ItemCallback<RoomDBFavoriteEvent>() {
        override fun areItemsTheSame(oldItem: RoomDBFavoriteEvent, newItem: RoomDBFavoriteEvent): Boolean {
            return oldItem.eventID == newItem.eventID
        }

        override fun areContentsTheSame(oldItem: RoomDBFavoriteEvent, newItem: RoomDBFavoriteEvent): Boolean {
            return oldItem == newItem
        }
    }
}

