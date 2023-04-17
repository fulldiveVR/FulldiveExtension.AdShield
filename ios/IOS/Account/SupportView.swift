//
//  This file is part of Blokada.
//
//  This Source Code Form is subject to the terms of the Mozilla Public
//  License, v. 2.0. If a copy of the MPL was not distributed with this
//  file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
//  Copyright © 2020 Blocka AB. All rights reserved.
//
//  @author Karol Gusak
//

import SwiftUI

struct SupportView: View {

    @Binding var activeSheet: ActiveSheet?

    var body: some View {
        NavigationView {
            VStack {
                BlokadaView(animate: true)
                    .frame(width: 100, height: 100)

                Text(L10n.accountSupportActionHowHelp)
                    .font(.largeTitle)
                    .bold()
                    .padding()

                VStack {
                    Button(action: {
                        self.activeSheet = nil
                        Links.openInBrowser(Links.knowledgeBase())
                    }) {
                        ZStack {
                            ButtonView(enabled: .constant(true), plus: .constant(true))
                                .frame(height: 44)
                            Text(L10n.accountSupportActionKb)
                                .foregroundColor(.white)
                                .bold()
                        }
                    }

                    Button(action: {
                        self.activeSheet = nil
                        Links.openInBrowser(Links.support())
                    }) {
                        ZStack {
                            ButtonView(enabled: .constant(true), plus: .constant(true))
                                .frame(height: 44)
                            Text(L10n.universalActionContactUs)
                                .foregroundColor(.white)
                                .bold()
                        }
                    }
                }
                .padding(40)
            }
            .frame(maxWidth: 500)

            .navigationBarItems(trailing:
                Button(action: {
                    self.activeSheet = nil
                }) {
                    Text(L10n.universalActionDone)
                }
                .contentShape(Rectangle())
            )
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .accentColor(Color.cAccent)
    }
}

struct SupportView_Previews: PreviewProvider {
    static var previews: some View {
        SupportView(activeSheet: .constant(nil))
    }
}
