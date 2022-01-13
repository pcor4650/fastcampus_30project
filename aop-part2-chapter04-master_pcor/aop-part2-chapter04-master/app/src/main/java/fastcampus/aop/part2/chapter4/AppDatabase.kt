package fastcampus.aop.part2.chapter4

import androidx.room.Database
import androidx.room.RoomDatabase
import fastcampus.aop.part2.chapter4.dao.HistoryDao
import fastcampus.aop.part2.chapter4.model.History

//anotation으로 database인걸 알려주어야 한다. 버전을 알려주어야 한다. 왜? DB가 변경이 될 때 마이그레이션 시 데이터를 지키기 위해
@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}