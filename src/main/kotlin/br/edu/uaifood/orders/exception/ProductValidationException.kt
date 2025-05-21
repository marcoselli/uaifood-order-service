package br.edu.uaifood.orders.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ProductValidationException(
    reason: String = "Invalid product data",
    statusCode: HttpStatus = HttpStatus.BAD_REQUEST
) : ResponseStatusException(statusCode, reason) 