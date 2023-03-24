//
//  This file is part of Blokada.
//
//  Blokada is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Blokada is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
//
//  Copyright © 2020 Blocka AB. All rights reserved.
//
//  @author Karol Gusak
//

import Foundation
import StoreKit

class PaymentService {

    static let shared = PaymentService()

    private let log = Logger("Payment")

    private let api = BlockaApiService.shared
    private let storeKit = StoreKitWrapper.shared

    private init() {
        storeKit.onOngoingTransaction = handleOngoingTransaction
    }

    func refreshProductsAfterStart() {
        self.refreshProducts(ok: { _ in }, fail: { error in
            self.log.e("syncPaymentsAfterStart: failed".cause(error))
        })
    }

    func handleOngoingTransaction() {
        self.finishOngoingTransaction(canRefreshReceipt: true, ok: {
            self.log.v("handleOngoingTransaction: Ongoing transaction succeeded")
        }, fail: { error in
            self.log.e("handleOngoingTransaction: Finishing ongoing transaction failed".cause(error))
        })
    }

    func refreshProducts(ok: @escaping Ok<[Product]> = { _ in }, fail: @escaping Fail = { _ in }) {
        onBackground {
            self.log.v("Refresh products")

            self.storeKit.fetchProducts(ok: { products in
                onMain {
                    let convertedProducts = products.map { p in
                        Product(id: p.productIdentifier, title: p.localTitle, description: p.localDescription,
                                price: p.localPrice, period: p.durationMonths)
                    }
                    ok(convertedProducts)
                }
            }, fail: { error in
                onMain {
                    return fail(error)
                }
            })
        }
    }

    func buy(_ product: Product, ok: @escaping Ok<Void>, fail: @escaping Fail) {
        onBackground {
            self.log.v("Buy: \(product.id)")
            self.storeKit.purchase(productId: product.id,
                ok: { restored in onMain {
                    self.finishOngoingTransaction(ok: ok, fail: { error in
                        if /*restored! &&*/ error is CommonError
                            && error as! CommonError == CommonError.paymentInactiveAfterRestore {
                            self.log.w("Pulling another restored transaction if any")
                            onBackground {
                                self.buy(product, ok: ok, fail: fail)
                            }
                            return
                        } else {
                            self.log.e("Finishing transaction failed".cause(error))
                            return fail(error)
                        }
                    })
                }},
                fail: { error in onMain {
                    self.log.e("Transaction failed".cause(error))

                    // Try finishing transaction anyway, StoreKit seems very finnicky about the states.
                    // This will also cause a request to our backend, which may have already activated.
                    self.finishOngoingTransaction(canRefreshReceipt: true, ok: ok, fail: { _ in
                        return fail(error)
                    })
                }}
            )
        }
    }

    private func finishOngoingTransaction(canRefreshReceipt: Bool = false, ok: @escaping Ok<Void>, fail: @escaping Fail) {
        guard let receipt = self.storeKit.getReceipt() else {
            if canRefreshReceipt {
                self.log.w("No receipt, attempting to refresh")
                return self.storeKit.refreshReceipt(ok: {
                    // Try again after the receipt is refreshed
                    return self.finishOngoingTransaction(canRefreshReceipt: false, ok: ok, fail: fail)
                }, fail: { error in
                    self.storeKit.finishPurchase()
                    return fail("Failed refreshing receipt".cause(error))
                })
            } else {
                self.storeKit.finishPurchase()
                return fail("Found no receipt")
            }
        }

        let request = AppleCheckoutRequest(
            account_id: Config.shared.accountId(),
            receipt: receipt
        )

        self.api.postAppleCheckout(request: request) { error, account in
            if let error = error {
                return fail(error)
            }

            self.storeKit.finishPurchase()

            if account != nil {
                SharedActionsService.shared.updateAccount(account!)
            }

            let active = account?.isActive() ?? Config.shared.accountActive()

            onMain {
                if active {
                    ok(())
                } else {
                    fail(CommonError.paymentInactiveAfterRestore)
                }
            }
        }
    }

    func restoreTransaction(ok: @escaping Ok<Void>, fail: @escaping Fail) {
        onBackground {
            self.log.v("Restore transaction")

            self.storeKit.restorePurchase(
                ok: {
                    onMain {
                        self.finishOngoingTransaction(canRefreshReceipt: true, ok: ok, fail: { error in
                            self.log.e("Finishing ongoing purchase failed".cause(error))
                            return fail(error)
                        })
                    }
                },
                fail: { error in
                    onMain {
                        // Try finishing transaction anyway, StoreKit seems very finnicky about the states.
                        // This will also cause a request to our backend, which may have already activated.
                        self.finishOngoingTransaction(canRefreshReceipt: true, ok: ok, fail: { _ in
                            return fail(error)
                        })
                    }
                }
            )
        }
    }

    func cancelTransaction() {
        onBackground {
            self.log.v("Cancel transaction")
            self.storeKit.finishPurchase()
        }
    }

    func startObservingPayments() {
        self.storeKit.startObservingPayments()
    }

    func stopObservingPayments() {
        self.storeKit.stopObservingPayments()
    }
}
