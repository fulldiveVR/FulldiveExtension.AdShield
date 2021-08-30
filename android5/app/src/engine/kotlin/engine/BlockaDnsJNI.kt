package com.blocka.dns

class BlockaDnsJNI {
    companion object {
        external fun create_new_dns(
            listen_addr: String,
            dns_ips: String,
            dns_name: String,
            dns_path: String
        ): Long

        external fun dns_close(
            handle: Long
        )

        external fun engine_logger(level: String)
    }
}
