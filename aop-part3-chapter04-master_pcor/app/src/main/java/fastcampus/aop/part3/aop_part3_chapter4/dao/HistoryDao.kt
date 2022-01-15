package fastcampus.aop.part3.aop_part3_chapter4.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fastcampus.aop.part3.aop_part3_chapter4.model.History

@Dao
interface HistoryDao {

    //해석 : select all from history
    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    //검색 작업할 때 마다 추가해주는 것 필
    @Insert
    fun insertHistory(history: History)

    //검색어 하나만 삭제해주는 명령어
    @Query("DELETE FROM history WHERE keyword = :keyword")
    fun delete(keyword: String)

}