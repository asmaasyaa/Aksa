package com.aksa.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateSessionStarsUseCaseTest {

    private val useCase = CalculateSessionStarsUseCase()

    @Test
    fun `akurasi 85 persen ke atas mendapat 3 bintang`() {
        assertEquals(3, useCase(85f))
        assertEquals(3, useCase(100f))
    }

    @Test
    fun `akurasi 60 sampai 84 persen mendapat 2 bintang`() {
        assertEquals(2, useCase(60f))
        assertEquals(2, useCase(84.9f))
    }

    @Test
    fun `akurasi di bawah 60 persen tetap mendapat 1 bintang apresiasi`() {
        assertEquals(1, useCase(59f))
        assertEquals(1, useCase(10f))
        assertEquals(1, useCase(0f))
    }
}