package ru.quipy.payments.logic

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class PaymentAccountManagementService(accounts: List<ExternalServiceProperties>) {

    private val paymentAccountsStatuses: List<PaymentAccountStatus> =
        accounts.map { account -> PaymentAccountStatus(account) }

    @Async
    fun chooseAccountToExecutePayment(): PaymentAccountStatus? =
        paymentAccountsStatuses
            .filter { paymentAccountStatus -> paymentAccountStatus.canExecuteRequest }
            .sortedBy { paymentAccountStatus ->  paymentAccountStatus.properties.cost }
            .maxByOrNull { paymentAccountsStatus -> paymentAccountsStatus.speed }
            ?.apply { addRequestForExecution() }
}
