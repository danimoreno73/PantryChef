package com.pantrychef.back.data.mock

import com.pantrychef.back.model.User
import kotlinx.coroutines.delay
import java.util.UUID


class MockAuthDataSource {

    private var currentUser: User? = null

    suspend fun login(email: String, password: String): Result<User>{
        delay(500)
        val user = User(
            id = "mock-user-${UUID.randomUUID()}",
            name = "User Mock",
            email = email,
            createdAt = System.currentTimeMillis()
        )
        currentUser = user
        return Result.success(user)
    }

    suspend fun register(name: String, email: String, password: String): Result<User>{
        delay(500)
        val user = User(
            id = "mock-user-${UUID.randomUUID()}",
            name = "User Mock",
            email = email,
            createdAt = System.currentTimeMillis()
        )
        currentUser = user
        return Result.success(user)
    }
    suspend fun logout(): Result<Unit> {
        currentUser = null
        return Result.success(Unit)
    }
    suspend fun getCurrentUser(): Result<User?> {
        return Result.success(currentUser)
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }
}