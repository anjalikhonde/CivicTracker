package com.civictracker.app.data.local

import androidx.room.*
import com.civictracker.app.data.model.Issue
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY timestamp DESC")
    fun getAllIssues(): Flow<List<Issue>>

    @Query("SELECT * FROM issues WHERE id = :id")
    suspend fun getIssueById(id: String): Issue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<Issue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: Issue)

    @Query("DELETE FROM issues")
    suspend fun clearAll()
}
