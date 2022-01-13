package fastcampus.aop.part3.chapter3

data class AlarmDisplayModel(
    val hour: Int,
    val minute: Int,
    var onOff: Boolean
) {

    val timeText: String  //="1230"이런식으로 저장해주면 값이 변경되지 않기에 getter를 사용한다
    //
        get() {
            val h = "%02d".format(if (hour < 12) hour else hour - 12)  // 두자리수 중 공백이 있으면 0을 채워주겠다 %02d
            val m = "%02d".format(minute)

            return "$h:$m"
        }

    val ampmText: String
        get() {
            return if (hour < 12) "AM" else "PM"
        }

    val onOffText: String
        get() {
            return if (onOff) "알람 끄기" else "알람 켜기"
        }

    fun makeDataForDB(): String {
        return "$hour:$minute"
    }

}