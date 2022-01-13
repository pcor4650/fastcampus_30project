package fastcampus.aop.part2.chapter4.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fastcampus.aop.part2.chapter4.model.History

@Dao
interface HistoryDao {

    //history를 전부 가져오는 함수
    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history")
    fun deleteAll()

    //하나의 history만 삭제하고 싶을 때
//    @Delete
//    fun delete(history: History)

}