package br.edu.uaifood.orders.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ProductNotFoundException(
    reason: String = "Product not found",
    statusCode: HttpStatus = HttpStatus.NOT_FOUND
) : ResponseStatusException(statusCode, reason) 