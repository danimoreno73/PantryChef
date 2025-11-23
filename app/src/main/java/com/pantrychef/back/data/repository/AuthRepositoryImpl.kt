package com.pantrychef.back.data.repository

import com.pantrychef.back.data.mock.MockAuthDataSource
import com.pantrychef.back.model.User
import com.pantrychef.back.repository.AuthRepository

class AuthRepositoryImpl(mockAuthDataSource: MockAuthDataSource) : AuthRepository{
    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(): Result<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        TODO("Not yet implemented")
    }

}
