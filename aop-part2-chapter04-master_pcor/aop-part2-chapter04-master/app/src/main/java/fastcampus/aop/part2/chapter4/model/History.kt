package fastcampus.aop.part2.chapter4.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


//데이터 클래스는 생성자에 변수를 입력하는 방식으로 손쉽게 작성할수 있다
//@Entity 추가 History data 클래스를 DB의 테이블로 사용하기 위해
@Entity
data class History(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "expression") val expression: String?,
    @ColumnInfo(name = "result") val result: String?
)