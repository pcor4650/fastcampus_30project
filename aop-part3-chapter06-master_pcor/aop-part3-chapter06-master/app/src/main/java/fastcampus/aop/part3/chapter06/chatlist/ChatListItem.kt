package fastcampus.aop.part3.chapter06.chatlist

data class ChatListItem(
    val buyerId: String,
    val sellerId :String,
    val itemTitle: String,
    val key: Long
) {
    //파라미터 값이 빈 생성자가 있어야 함
    constructor(): this("", "", "", 0)
}