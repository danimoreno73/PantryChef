package com.pantrychef.back.usecase

import com.pantrychef.back.model.User
import com.pantrychef.back.repository.AuthRepository

class RegisterUserUseCase (
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            authRepository.register(name, email, password)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}