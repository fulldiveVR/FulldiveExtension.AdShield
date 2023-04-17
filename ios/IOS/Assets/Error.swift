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

extension String: Error {} // Enables you to throw a string

extension String: LocalizedError { // Adds error.localizedDescription to Error instances
    public var errorDescription: String? { return self }
}

extension String {
    func cause(_ cause: Error?) -> String {
        if let c = cause {
            return self + ": " + mapCauseForDebug(c)
        } else {
            return self
        }
    }
}

extension Error {
    func isCommon(_ error: CommonError) -> Bool {
        if let e = self as? CommonError {
            return e == error
        } else if let c = self as? SKError {
            return c.code == .paymentCancelled && error == CommonError.paymentCancelled
        } else {
            return false
        }
    }
}

enum NetworkError: Error {
    case http(_ code: Int)
}

extension NetworkError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .http(code: let code):
            return NSLocalizedString("HTTP \(code)", comment: "NetworkError")
        }
    }
}

enum CommonError : Error {
    case deviceOffline
    case accountInactive
    case accountInactiveAfterRestore
    case failedCreatingAccount
    case failedFetchingData
    case failedTunnel
    case failedVpn
    case vpnNoPermissions
    case paymentInactiveAfterRestore
    case paymentFailed
    case paymentNotAvailable
    case paymentCancelled
    case noCurrentLease
    case tooManyLeases
    case unknownError
}

let errorDescriptions = [
    CommonError.deviceOffline: L10n.errorDeviceOffline,
    CommonError.accountInactive: L10n.errorAccountInactive,
    CommonError.accountInactiveAfterRestore: L10n.errorAccountInactiveAfterRestore,
    CommonError.failedCreatingAccount: L10n.errorCreatingAccount,
    CommonError.failedFetchingData: L10n.errorFetchingData,
    CommonError.failedTunnel: L10n.errorTunnel,
    CommonError.failedVpn: L10n.errorVpn,
    CommonError.vpnNoPermissions: L10n.errorVpnPerms,
    CommonError.paymentInactiveAfterRestore: L10n.errorPaymentInactiveAfterRestore,
    CommonError.paymentFailed: L10n.errorPaymentFailed,
    CommonError.paymentCancelled: L10n.errorPaymentCanceled,
    CommonError.paymentNotAvailable: L10n.errorPaymentNotAvailable,
    CommonError.noCurrentLease: L10n.errorVpnNoCurrentLease,
    CommonError.tooManyLeases: L10n.errorVpnTooManyLeases,
    CommonError.unknownError: L10n.errorUnknown
]

func mapErrorForUser(_ error: Error, cause: Error? = nil) -> String {
    var userFriendlyError = errorDescriptions[CommonError.unknownError]!
    if let e = error as? CommonError {
        userFriendlyError = errorDescriptions[e]!
    }

    Logger.e("Error", "Detailed error: " + error.localizedDescription.cause(cause))

    if let c = cause {
        Logger.e("Error", "Detailed cause: " + mapCauseForDebug(c))

        if let userCause = mapCauseForUser(c) {
            return userCause
        }
    }

    Logger.e("Error", userFriendlyError)
    return userFriendlyError
}

private func mapCauseForUser(_ cause: Error) -> String? {
    if let c = cause as? CommonError {
        return errorDescriptions[c]
    } else if let c = cause as? SKError {
        switch c.code {
        case .paymentCancelled:
            return errorDescriptions[CommonError.paymentCancelled]!
        default:
            return nil
        }
    }

    return nil
}

private func mapCauseForDebug(_ cause: Error) -> String {
    switch cause {
    case let c where c is CommonError:
        return errorDescriptions[c as! CommonError] ?? errorDescriptions[CommonError.unknownError]!
    case let c where c is SKError:
        return c.localizedDescription.cause(mapSKErrorForDebug(c as! SKError))
    default:
        return cause.localizedDescription
    }
}

private func mapSKErrorForDebug(_ error: SKError) -> String {
    switch error.code {
    case .unknown:
        return "Unknown payment error"
    case .clientInvalid:
        return "Client invalid"
    case .paymentCancelled:
        return "Payment was canceled"
    case .paymentInvalid:
        return "Payment is invalid"
    case .paymentNotAllowed:
        return "Payment not allowed"
    case .storeProductNotAvailable:
        return "Store product not available"
    case .cloudServicePermissionDenied:
        return "Cloud service permission denied"
    case .cloudServiceNetworkConnectionFailed:
        return "Cloud service network connection failed"
    case .cloudServiceRevoked:
        return "Cloud service revoked"
    case .privacyAcknowledgementRequired:
        return "Privacy acknowledgement required"
    case .unauthorizedRequestData:
        return "Unauthorized request data"
    case .invalidOfferIdentifier:
        return "Invalid offer identifier"
    case .invalidOfferPrice:
        return "Invalid offer price"
    case .invalidSignature:
        return "Invalid signature"
    case .missingOfferParams:
        return "Missing offer params"
    default:
        return "Unknown payment error"
    }
}
