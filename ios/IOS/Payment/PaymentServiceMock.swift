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

class PaymentService {

    static let shared = PaymentService()

    private let log = Logger("Mock")
    private let api = BlockaApiService.shared

    var onChangeAccount = { (account: Account) in }

    private init() {
        // singleton
    }

    func refreshProductsAfterStart() {

    }

    func refreshProducts(ok: @escaping Ok<[Product]>, fail: @escaping Fail) {
        onBackground {
            self.log.v("Refresh products")
            sleep(3)
            onMain {
                let products = [
                    Product(id: "1", title: "Product 1", description: "Description of Product 1", price: "9.99", period: 1),
                    Product(id: "2", title: "Product 2", description: "Description of Product 2", price: "19.99", period: 6),
                    Product(id: "3", title: "Product 3", description: "Description of Product 3", price: "39.99", period: 12)
                ]
                ok((products))
            }
        }
    }

    func buy(_ product: Product, ok: @escaping Ok<Void>, fail: @escaping Fail) {
        onBackground {
            self.log.v("Buy: \(product.id)")
            sleep(3)
            onMain {
                if Int.random(in: 1..<3) > 1 {
                    let request = AppleCheckoutRequest(
                        account_id: Config.shared.accountId(),
                        receipt: ""
                    )

                    self.api.postAppleCheckout(request: request) { error, account in
                        SharedActionsService.shared.updateAccount(account!)
                        onMain {
                            ok(())
                        }
                    }
                } else {
                    fail("Mocked error")
                }
            }
        }
    }

    func restoreTransaction(ok: @escaping Ok<Void>, fail: @escaping Fail) {
        onBackground {
            self.log.v("Restore transaction")
            sleep(1)
            self.log.v("Slept")
            onMain {
                ok(())
            }
        }
    }

    func cancelTransaction() {
        self.log.v("Cancel transaction")
    }

    func startObservingPayments() {
    }

    func stopObservingPayments() {
    }
}
