package com.piooda.recycrew.feature.mypage.detail.faq

import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piooda.data.model.FAQItem
import com.piooda.recycrew.databinding.ItemFaqBinding

class FAQListAdapter : ListAdapter<FAQItem, FAQListAdapter.FAQViewHolder>(diffCallback) {

    inner class FAQViewHolder(private val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FAQItem) {
            with(binding) {
                faqCategory.text = item.category
                faqQuestion.text = item.question
                answerTextView.text = item.answer // 확인된 답변

                // 펼침 상태에 따라 답변 컨테이너 표시/숨김
                answerContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            }

            // 클릭 이벤트로 펼침 상태 토글 + 애니메이션 추가
            itemView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
                notifyItemChanged(adapterPosition) // 최신 포지션 사용
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FAQViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<FAQItem>() {
            override fun areItemsTheSame(oldItem: FAQItem, newItem: FAQItem): Boolean {
                return oldItem.question == newItem.question // ID가 있다면 oldItem.id == newItem.id 로 변경
            }

            override fun areContentsTheSame(oldItem: FAQItem, newItem: FAQItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
