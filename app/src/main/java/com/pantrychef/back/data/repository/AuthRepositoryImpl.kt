package com.pantrychef.back.data.repository

import com.pantrychef.back.data.mock.MockAuthDataSource
import com.pantrychef.back.model.User
import com.pantrychef.back.repository.AuthRepository

class AuthRepositoryImpl(
    private val mockAuthDataSource: MockAuthDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return mockAuthDataSource.login(email, password)
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return mockAuthDataSource.register(name, email, password)
    }

    override suspend fun logout(): Result<Unit> {
        return mockAuthDataSource.logout()
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return mockAuthDataSource.getCurrentUser()
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return mockAuthDataSource.resetPassword(email)
    }
}
