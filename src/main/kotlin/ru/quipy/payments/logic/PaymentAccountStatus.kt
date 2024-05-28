package ru.quipy.payments.logic

import java.util.concurrent.atomic.AtomicInteger

class PaymentAccountStatus(val properties: ExternalServiceProperties) {

    private val timeOfLastPaymentRequest = AtomicInteger(System.currentTimeMillis().toInt())
    private val numberOfPaymentRequests = AtomicInteger(0)

    val speed: Double
        get() = with(properties) {
            val averageProcessingTime = request95thPercentileProcessingTime.toMillis().toDouble() / 1000
            val parallelRequestNumber = parallelRequests.toDouble()
            val rateLimitPerSecond = rateLimitPerSec.toDouble()

            parallelRequestNumber.coerceAtMost(rateLimitPerSecond) / averageProcessingTime;
        }

    val canExecuteRequest: Boolean
        get() {
            val currentTime = System.currentTimeMillis().toInt()

            synchronized(this) {
                if (currentTime - timeOfLastPaymentRequest.get() > INTERVAL_BETWEEN_REQUESTS) {
                    timeOfLastPaymentRequest.set(currentTime)
                    numberOfPaymentRequests.set(0)
                }

                if (numberOfPaymentRequests.get() < properties.rateLimitPerSec) {
                    numberOfPaymentRequests.incrementAndGet()
                    return true
                } else {
                    return false
                }
            }
        }

    fun addRequestForExecution() {
        numberOfPaymentRequests.incrementAndGet()
    }

    companion object {

        private const val INTERVAL_BETWEEN_REQUESTS = 1000
    }
}