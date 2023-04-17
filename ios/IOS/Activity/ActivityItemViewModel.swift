//
//  This file is part of Blokada.
//
<<<<<<< HEAD
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
=======
//  This Source Code Form is subject to the terms of the Mozilla Public
//  License, v. 2.0. If a copy of the MPL was not distributed with this
//  file, You can obtain one at https://mozilla.org/MPL/2.0/.
>>>>>>> 63a7ee16293d39745148c05cf2e03c80b3dc239c
//
//  Copyright © 2020 Blocka AB. All rights reserved.
//
//  @author Karol Gusak
//

import Foundation

class ActivityItemViewModel: ObservableObject {

    @Published var entry: HistoryEntry
    @Published var whitelisted: Bool
    @Published var blacklisted: Bool

    private var timer: Timer? = nil

    init(entry: HistoryEntry, whitelisted: Bool, blacklisted: Bool) {
        self.entry = entry
        self.whitelisted = whitelisted
        self.blacklisted = blacklisted
        startTimer()
    }

    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { timer in
            self.objectWillChange.send()
        }
    }

    deinit {
        timer?.invalidate()
    }
}
