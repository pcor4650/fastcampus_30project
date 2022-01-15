package fastcampus.aop.part3.aop_part3_chapter4.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fastcampus.aop.part3.aop_part3_chapter4.databinding.ItemHistoryBinding
import fastcampus.aop.part3.aop_part3_chapter4.model.History

class HistoryAdapter(val historyDeleteClickListener: (String) -> (Unit)) : ListAdapter<History, HistoryAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(historyModel: History) {
            binding.historyKeywordTextView.text = historyModel.keyword
            //delete버튼 클릭 이벤트 구, MainActivity에서 하지 않고 여기서 구현하려면 생성자 부분에다가 val historyDeleteClickListener... 추가해
            binding.historyKeywordDeleteButton.setOnClickListener {
                historyDeleteClickListener(historyModel.keyword.orEmpty())  //orEmpty() 어떻게 사용?
            }

            //검색어 눌렀을 때 동작 구현
//            binding.root.setOnClickListener {  }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<History>() {
            override fun areContentsTheSame(oldItem: History, newItem: History) =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: History, newItem: History) =
                oldItem.uid == newItem.uid
        }
    }

}