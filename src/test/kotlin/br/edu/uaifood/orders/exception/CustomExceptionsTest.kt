package br.edu.uaifood.orders.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class CustomExceptionsTest {

    @Test
    fun `OrderPaymentException should contain correct message`() {
        // Given
        val message = "Payment failed"
        val exception = OrderPaymentException(message)

        // When/Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `OrderNotFoundException should contain correct message`() {
        // Given
        val message = "Order not found"
        val exception = OrderNotFoundException(message)

        // When/Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `ProductValidationException should contain default values`() {
        // Given
        val exception = ProductValidationException()

        // When/Then
        assertEquals("Invalid product data", exception.reason)
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun `ProductValidationException should contain custom values`() {
        // Given
        val message = "Custom validation error"
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val exception = ProductValidationException(message, status)

        // When/Then
        assertEquals(message, exception.reason)
        assertEquals(status, exception.statusCode)
    }

    @Test
    fun `OrderAlreadyFinishedException should contain correct message`() {
        // Given
        val message = "Order is already finished"
        val exception = OrderAlreadyFinishedException(message)

        // When/Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `ProductNotFoundException should contain default values`() {
        // Given
        val exception = ProductNotFoundException()

        // When/Then
        assertEquals("Product not found", exception.reason)
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `ProductNotFoundException should contain custom values`() {
        // Given
        val message = "Custom not found message"
        val status = HttpStatus.GONE
        val exception = ProductNotFoundException(message, status)

        // When/Then
        assertEquals(message, exception.reason)
        assertEquals(status, exception.statusCode)
    }

    @Test
    fun `InvalidUpdateRequestException should contain default message`() {
        // Given
        val exception = InvalidUpdateRequestException()

        // When/Then
        assertEquals("Path parameter and request body names must be equal", exception.reason)
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun `InvalidUpdateRequestException should contain custom message`() {
        // Given
        val message = "Custom invalid update message"
        val exception = InvalidUpdateRequestException(message)

        // When/Then
        assertEquals(message, exception.reason)
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }
} 