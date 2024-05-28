package ru.quipy.payments.logic

import java.util.concurrent.atomic.AtomicInteger

class PaymentAccountStatus(val properties: ExternalServiceProperties) {

    private val timeOfLastPaymentRequest = AtomicInteger(System.currentTimeMillis().toInt())
    private val numberOfPaymentRequests = AtomicInteger(0)

    val canExecuteRequest: Boolean
        get() {
            val currentTime = System.currentTimeMillis().toInt()

            if (currentTime - timeOfLastPaymentRequest.get() > INTERVAL_BETWEEN_REQUESTS) {
                timeOfLastPaymentRequest.set(currentTime)
                numberOfPaymentRequests.set(0)
            }

            return numberOfPaymentRequests.get() < properties.rateLimitPerSec
        }

    fun addRequestForExecution() {
        numberOfPaymentRequests.incrementAndGet()
    }

    companion object {

        private const val INTERVAL_BETWEEN_REQUESTS = 1000
    }
}