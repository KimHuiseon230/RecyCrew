package com.piooda.recycrew.feature.mypage.detail.notice

import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.NoticeItem
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ItemNoticeBinding

class NoticeListAdapter :
    ListAdapter<NoticeItem, NoticeListAdapter.NoticeViewHolder>(diffCallback) {

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeItem) {
            with(binding) {
                noticeCategory.text = item.category
                noticeTitle.text = item.title
                noticeDescription.text = item.description

                // Glide를 사용하여 이미지 로드
                if (item.imageUrl.isNullOrEmpty()) {
                    noticeImageView.visibility = View.GONE // 이미지 URL이 없을 경우 숨김
                } else {
                    noticeImageView.visibility = View.VISIBLE
                    Glide.with(noticeImageView.context)
                        .load(item.imageUrl) // Firestore에서 가져온 이미지 URL
                        .placeholder(R.drawable.placeholder_image) // 로딩 중 보여줄 이미지
                        .error(R.drawable.error_image) // 로드 실패 시 보여줄 이미지
                        .into(noticeImageView)
                }

                noticeContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            }

            itemView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }


    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<NoticeItem>() {
            override fun areItemsTheSame(oldItem: NoticeItem, newItem: NoticeItem): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: NoticeItem, newItem: NoticeItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}


