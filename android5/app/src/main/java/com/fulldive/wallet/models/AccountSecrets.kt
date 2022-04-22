package com.fulldive.wallet.models

class AccountSecrets(
    val entropy: String,
    val mnemonic: List<String>,
    val address: String,
    val path: Int
)
