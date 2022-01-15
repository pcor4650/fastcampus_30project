package fastcampus.aop.part3.aop_part3_chapter4.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fastcampus.aop.part3.aop_part3_chapter4.databinding.ItemBookBinding
import fastcampus.aop.part3.aop_part3_chapter4.model.Book

//adapter은 ListAdapter을 상속해야한
class BookAdapter(val clickListener: (Book) -> Unit) : ListAdapter<Book, BookAdapter.ViewHolder>(diffUtil) {
    //item_book과 ItemBookBinding
    inner class ViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {

        //book라는 데이터를 가져와서
        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description

            Glide
                .with(binding.coverImageView.context)
                .load(bookModel.coverSmallUrl)
                .into(binding.coverImageView)

            binding.root.setOnClickListener {
                clickListener(bookModel)
            }
        }

    }

    //ListAdapter에서 구현해줘야하는 오버라이딩 함수 아래 2개
    //미리 안들어지 뷰홀더가 없을 경우 뷰홀더 새로 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))//parent관련 이해 안
    }

    //뷰홀더가 뷰에 그려지게 됐을 때 데이터를 그려주는(바인드해주는) 함
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        //리싸이클러뷰가 뷰의 포지션이 변경되었을 때 새로운 값을 할 지 말지 결정
        //같은 아이템이면 굳이 새 값을 할당할 필요가 없
        val diffUtil = object : DiffUtil.ItemCallback<Book>() {
            override fun areContentsTheSame(oldItem: Book, newItem: Book) =  //코틀린 적 표현? =말고 :Boolean {return oldItem == newItem} 으로 변경 가능
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Book, newItem: Book) =
                oldItem.id == newItem.id
        }
    }

}