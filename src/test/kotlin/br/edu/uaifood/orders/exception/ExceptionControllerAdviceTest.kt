package br.edu.uaifood.orders.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExceptionControllerAdviceTest {

    private val advice = ExceptionControllerAdvice()

    @Test
    fun `handleProductValidationException should return error response with BAD_REQUEST status`() {
        // Given
        val exception = ProductValidationException("Invalid product data")

        // When
        val response = advice.handleProductValidationException(exception)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.body?.statusCode)
        assertEquals("Invalid product data", response.body?.message)
    }

    @Test
    fun `handleProductNotFoundException should return error response with NOT_FOUND status`() {
        // Given
        val exception = ProductNotFoundException("Product not found")

        // When
        val response = advice.handleProductNotFoundException(exception)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(HttpStatus.NOT_FOUND.value(), response.body?.statusCode)
        assertEquals("Product not found", response.body?.message)
    }
} 