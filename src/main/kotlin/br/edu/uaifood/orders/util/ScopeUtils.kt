package br.edu.uaifood.orders.util

class ScopeUtils {
    companion object {
        private const val ENV_SCOPE = "SCOPE"
        private const val PROD_SCOPE = "production"

        fun getProfileFromScope(scope: String? = System.getenv(ENV_SCOPE)): String {
            return when(scope) {
                PROD_SCOPE -> "prod"
                else -> "local"
            }
        }
    }
}