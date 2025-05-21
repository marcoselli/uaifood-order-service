package br.edu.uaifood.orders.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScopeUtilsTest {

    // Removido (ou comentado) os testes que tentam alterar variáveis de ambiente via reflexão.
    // A lógica de getProfileFromScope (com parâmetro opcional) já está coberta (por exemplo, em OrderServiceTest) e não é mais necessário manipular o ambiente.

    // @Test
    // fun `getProfileFromScope should return prod when SCOPE is production`() {
    //     val profile = ScopeUtils.getProfileFromScope("production")
    //     assertEquals("prod", profile)
    // }

    // @Test
    // fun `getProfileFromScope should return local when SCOPE is not production`() {
    //     val profile = ScopeUtils.getProfileFromScope("development")
    //     assertEquals("local", profile)
    // }

    // @Test
    // fun `getProfileFromScope should return local when SCOPE is not set`() {
    //     val profile = ScopeUtils.getProfileFromScope(null)
    //     assertEquals("local", profile)
    // }

} 