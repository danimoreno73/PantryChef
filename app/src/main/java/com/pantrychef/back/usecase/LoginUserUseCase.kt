package com.pantrychef.back.usecase

import com.pantrychef.back.model.User
import com.pantrychef.back.repository.AuthRepository

class LoginUserUseCase (
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User>{
        return try {
            authRepository.login(email,password)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

}