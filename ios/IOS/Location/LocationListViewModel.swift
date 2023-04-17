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

class LocationListViewModel: ObservableObject {

    @Published var items = [LocationViewModel]()

    private let api = BlockaApiService.shared
    private let sharedActions = SharedActionsService.shared

    func loadGateways(done: @escaping Ok<Void>) {
        self.items = items.map { i in
            LocationViewModel(gateway: i.gateway, selectedGateway: self.selectedGateway())
        }

        if !self.items.isEmpty {
            // Hides spinner immediatelly
            done(())
        }

        self.api.getGateways { error, gateways in
            onMain {
                guard error == nil else {
                    return done(())
                }

                self.items = gateways!.sorted { $0.location < $1.location }
                .map { gateway in
                    LocationViewModel(gateway: gateway, selectedGateway: self.selectedGateway())
                }
                return done(())
            }
        }
    }

    func changeLocation(_ item: LocationViewModel) {
        sharedActions.changeGateway(item.gateway)
    }

    private func selectedGateway() -> Gateway? {
        if Config.shared.hasLease() && Config.shared.hasGateway() {
            return Config.shared.gateway()
        } else {
            return nil
        }
    }
}
