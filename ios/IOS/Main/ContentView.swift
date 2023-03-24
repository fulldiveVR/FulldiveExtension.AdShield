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

import SwiftUI

enum ActiveSheet: Identifiable {
    case help, plus, location, activated, askvpn, encryptionExplain,
         log, sharelog, debug, rate, counter, sharecounter

    var id: Int {
        hashValue
    }
}

struct ContentView: View {

    let accountVM: AccountViewModel
    let tabVM: TabViewModel
    let paymentVM: PaymentGatewayViewModel
    let locationVM: LocationListViewModel
    let logVM: LogViewModel
    let packsVM: PacksViewModel
    let activityVM: ActivityViewModel
    let inboxVM: InboxViewModel
    let leaseVM: LeaseListViewModel

    @ObservedObject var vm: HomeViewModel

    @State var showPreviewAllScreens = false
    @State var activeSheet: ActiveSheet?

    var body: some View {
        // Set accent color on all switches
        UISwitch.appearance().onTintColor = UIColor(named: "Orange")

        return GeometryReader { geometry in
            ZStack {
                MainView(accountVM: self.accountVM, packsVM: self.packsVM, activityVM: self.activityVM, vm: self.vm, inboxVM: self.inboxVM, leaseVM: self.leaseVM, tabVM: self.tabVM, activeSheet: self.$activeSheet)
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                    .padding(.top, geometry.safeAreaInsets.top)
                    .sheet(item: self.$activeSheet) { item in
                        switch item {
                        case .plus:
                            PaymentGatewayView(vm: self.paymentVM, activeSheet: self.$activeSheet)
                        case .location:
                            LocationListView(vm: self.locationVM, activeSheet: self.$activeSheet)
                        case .activated:
                            AfterActivatedView(activeSheet: self.$activeSheet)
                        case .askvpn:
                            AskVpnProfileView(homeVM: self.vm, activeSheet: self.$activeSheet)
                        case .encryptionExplain:
                            EncryptionExplanationView(activeSheet: self.$activeSheet, vm: self.vm, level: self.vm.encryptionLevel)
                        case .log:
                            LogView(vm: self.logVM, activeSheet: self.$activeSheet)
                        case .sharelog:
                            ShareSheet(activityItems: [LoggerSaver.logFile])
                        case .debug:
                            DebugView(vm: DebugViewModel(homeVM: self.vm), activeSheet: self.$activeSheet, showPreviewAllScreens: self.$showPreviewAllScreens)
                        case .rate:
                            RateAppView(activeSheet: self.$activeSheet)
                        case .counter:
                            AdsCounterShareView(homeVM: self.vm, activeSheet: self.$activeSheet)
                        case .sharecounter:
                            ShareSheet(activityItems: [L10n.mainShareMessage(self.vm.blockedCounter.compact)])
                        case .help:
                            SupportView(activeSheet: self.$activeSheet)
                        default:
                            VStack {
                                Spacer()
                                HStack {
                                    Spacer()
                                    SpinnerView()
                                    Spacer()
                                }
                                Spacer()
                            }
                            .background(Color.cBackground)
                        }
                    }
            }
            .background(Color.cBackground)

//            if self.showPreviewAllScreens {
//                PreviewAllScreensView(showPreviewAllScreens: self.$showPreviewAllScreens)
//                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
//                    .padding(.top, geometry.safeAreaInsets.top)
//                    .background(Color.cBackground)
//                    .opacity(self.showPreviewAllScreens ? 1 : 0)
//                    .transition(.opacity)
//                    .animation(.easeInOut)
//            }

            // We need animated and non-animated cover screen when changing orientation
            Rectangle()
                .fill(Color.cBackground)
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                .opacity(self.vm.showSplash ? 1 : 0)
            SplashView()
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                .opacity(self.vm.showSplash ? 1 : 0)
                .transition(.opacity)
                .animation(.easeInOut(duration: 0.5))
        }
        .background(Color.cSecondaryBackground.edgesIgnoringSafeArea(.all))
        .edgesIgnoringSafeArea(.top)
        .alert(isPresented: self.$vm.showError) {
            Alert(title: Text(self.vm.errorHeader ?? L10n.alertErrorHeader), message: Text(self.vm.showErrorMessage()),
                  dismissButton: Alert.Button.default(
                    Text(L10n.universalActionClose), action: { self.vm.error = nil }
                )
            )
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let tabVM = TabViewModel()
        let homeVM = HomeViewModel()
        homeVM.showSplash = false

        return Group {
            ContentView(
                accountVM: AccountViewModel(),
                tabVM: tabVM,
                paymentVM: PaymentGatewayViewModel(),
                locationVM: LocationListViewModel(),
                logVM: LogViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                vm: HomeViewModel()
            )
            .previewDevice(PreviewDevice(rawValue: "iPhone X"))
            .environment(\.colorScheme, .dark)

            ContentView(
                accountVM: AccountViewModel(),
                tabVM: tabVM,
                paymentVM: PaymentGatewayViewModel(),
                locationVM: LocationListViewModel(),
                logVM: LogViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                vm: HomeViewModel()
            )

            ContentView(
                accountVM: AccountViewModel(),
                tabVM: tabVM,
                paymentVM: PaymentGatewayViewModel(),
                locationVM: LocationListViewModel(),
                logVM: LogViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                vm: HomeViewModel()
            )
            .environment(\.sizeCategory, .extraExtraExtraLarge)
            .environment(\.colorScheme, .dark)

            ContentView(
                accountVM: AccountViewModel(),
                tabVM: tabVM,
                paymentVM: PaymentGatewayViewModel(),
                locationVM: LocationListViewModel(),
                logVM: LogViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                vm: homeVM
            )
            .previewDevice(PreviewDevice(rawValue: "iPad Pro (12.9-inch) (3rd generation)"))
        }
    }
}
