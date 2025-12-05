package com.pantrychef.back.usecase

import com.pantrychef.back.model.User
import com.pantrychef.back.repository.AuthRepository

class LogOutUserUseCase (
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logout()
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}