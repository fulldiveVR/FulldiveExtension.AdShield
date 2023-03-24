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

struct MainView: View {

    let accountVM: AccountViewModel
    let packsVM: PacksViewModel
    let activityVM: ActivityViewModel
    let vm: HomeViewModel
    let inboxVM: InboxViewModel
    let leaseVM: LeaseListViewModel

    @ObservedObject var tabVM: TabViewModel

    @Binding var activeSheet: ActiveSheet?

    @State var showHelpActions = false

    @State var startPoint = UnitPoint(x: 0, y: 0)
    @State var endPoint = UnitPoint(x: 0, y: 2)

    var body: some View {
        VStack {
            ZStack {
                HomeView(vm: self.vm, activeSheet: self.$activeSheet)
                    .opacity(self.tabVM.activeTab == "home" ? 1 : 0)
                ActivityView(vm: self.activityVM, tabVM: self.tabVM)
                    .opacity(self.tabVM.activeTab == "activity" ? 1 : 0)
                PacksView(vm: self.packsVM, tabVM: self.tabVM)
                    .opacity(self.tabVM.activeTab == "packs" ? 1 : 0)
                SettingsTabView(homeVM: self.vm, vm: self.accountVM, tabVM: self.tabVM, inboxVM: self.inboxVM, leaseVM: self.leaseVM, activeSheet: self.$activeSheet)
                    .opacity(self.tabVM.activeTab == "more" ? 1 : 0)

                VStack {
                    HStack {
                        Spacer()

                        Button(action: {
                            withAnimation {
                                self.activeSheet = .help
                            }
                        }) {
                            Image(systemName: Image.fHelp)
                                .imageScale(.large)
                                .foregroundColor(.primary)
                                .frame(width: 32, height: 32, alignment: .center)
                                .padding(8)
                                .padding(.top, 28)
                                .onTapGesture {
                                    self.showHelpActions = true
                                }
                                .actionSheet(isPresented: $showHelpActions) {
                                    ActionSheet(title: Text(L10n.accountSupportActionHowHelp), buttons: [
                                        .default(Text(L10n.accountSupportActionKb)) {
                                            Links.openInBrowser(Links.knowledgeBase())
                                        },
                                        .default(Text(L10n.universalActionContactUs)) {
                                            Links.openInBrowser(Links.support())
                                        },
                                        .default(Text(L10n.universalActionShowLog)) {
                                            self.activeSheet = .log
                                        },
                                        .default(Text(L10n.universalActionShareLog)) {
                                            self.activeSheet = .sharelog
                                        },
                                        .cancel()
                                    ])
                                }
                                .contextMenu {
                                    Button(action: {
                                        self.activeSheet = .log
                                    }) {
                                        Text(L10n.universalActionShowLog)
                                        Image(systemName: "list.dash")
                                    }

                                    Button(action: {
                                        self.activeSheet = .sharelog
                                    }) {
                                        Text(L10n.universalActionShareLog)
                                        Image(systemName: "square.and.arrow.up")
                                    }

                                    if !Env.isProduction {
                                        Button(action: {
                                            self.activeSheet = .debug
                                        }) {
                                            Text("Debug tools")
                                            Image(systemName: "ant.circle")
                                        }
                                    }
                                }
                        }
                    }
                    Spacer()
                }
            }
            TabView(vm: self.tabVM)
        }
        .onAppear {
            self.tabVM.load()
            self.packsVM.fetch()
            self.activityVM.fetch()
        }
    }
}

private func afterDelay(callback: @escaping () -> Void) {
    onBackground {
        sleep(3)
        onMain {
            callback()
        }
    }
}

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        let tabVM = TabViewModel()

        return Group {
            MainView(
                accountVM: AccountViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                vm: HomeViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                tabVM: tabVM,
                activeSheet: .constant(nil)
            )
            .previewDevice(PreviewDevice(rawValue: "iPhone X"))
            .environment(\.colorScheme, .dark)

            MainView(
                accountVM: AccountViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                vm: HomeViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                tabVM: tabVM,
                activeSheet: .constant(nil)
            )
            .environment(\.sizeCategory, .extraExtraExtraLarge)
            .environment(\.colorScheme, .dark)

            MainView(
                accountVM: AccountViewModel(),
                packsVM: PacksViewModel(tabVM: tabVM),
                activityVM: ActivityViewModel(),
                vm: HomeViewModel(),
                inboxVM: InboxViewModel(),
                leaseVM: LeaseListViewModel(),
                tabVM: tabVM,
                activeSheet: .constant(nil)

            )
        }
    }
}
