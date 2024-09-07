package com.mifos.feature.loan.loan_repayment_schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mifos.core.common.utils.Constants
import com.mifos.core.data.repository.LoanRepaymentScheduleRepository
import com.mifos.core.objects.accounts.loan.LoanWithAssociations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Aditya Gupta on 12/08/23.
 */
@HiltViewModel
class LoanRepaymentScheduleViewModel @Inject constructor(
    private val repository: LoanRepaymentScheduleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val loanId = savedStateHandle.getStateFlow(key = Constants.LOAN_ACCOUNT_NUMBER, initialValue = 0)

    private val _loanRepaymentScheduleUiState =
        MutableStateFlow<LoanRepaymentScheduleUiState>(LoanRepaymentScheduleUiState.ShowProgressbar)
    val loanRepaymentScheduleUiState: StateFlow<LoanRepaymentScheduleUiState> get() = _loanRepaymentScheduleUiState

    fun loadLoanRepaySchedule(loanId : Int) {
        _loanRepaymentScheduleUiState.value = LoanRepaymentScheduleUiState.ShowProgressbar
        repository.getLoanRepaySchedule(loanId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<LoanWithAssociations>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable) {
                    _loanRepaymentScheduleUiState.value =
                        LoanRepaymentScheduleUiState.ShowFetchingError(e.message.toString())
                }

                override fun onNext(loanWithAssociations: LoanWithAssociations) {
                    _loanRepaymentScheduleUiState.value =
                        LoanRepaymentScheduleUiState.ShowLoanRepaySchedule(loanWithAssociations)
                }
            })
    }

}