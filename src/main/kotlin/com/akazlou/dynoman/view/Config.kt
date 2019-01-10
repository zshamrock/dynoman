package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.ConnectionProperties
import com.amazonaws.regions.Regions
import tornadofx.*

/**
 * Configuration singleton keeping all the internals of working with application configurations.
 */
// XXX: Should use instead the instance of the class, and inject it accordingly to each of the view
// (or research on how enable it the global wide similar to the app.config)
object Config {
    const val REGION = "region"
    const val ACCESS_KEY = "access_key"
    const val SECRET_KEY = "secret_key"
    const val PROFILE = "profile"
    const val CREDENTIALS_FILE = "credentials_file"
    @JvmField
    val DEFAULT_REGION = Regions.US_WEST_2
    const val LOCAL = "local"

    private const val SYSTEM_PROPERTY_PROFILE_NAME = "aws.profile"
    private const val ENV_PROPERTY_PROFILE_NAME = "AWS_PROFILE"

    fun getRegion(config: ConfigProperties): String {
        return config.string(REGION, DEFAULT_REGION.name)
    }

    fun isLocal(config: ConfigProperties): Boolean {
        return config.boolean(LOCAL, false)!!
    }

    fun getProfile(config: ConfigProperties): String {
        val profile = config.string(PROFILE)
        return if (profile.isNullOrBlank()) {
            System.getProperty(SYSTEM_PROPERTY_PROFILE_NAME)
                    .orEmpty()
                    .ifBlank { System.getenv(ENV_PROPERTY_PROFILE_NAME) }
                    .orEmpty()
        } else {
            profile
        }
    }

    fun getAccessKey(config: ConfigProperties): String {
        return config.string(ACCESS_KEY).orEmpty()
    }

    fun getSecretKey(config: ConfigProperties): String {
        return config.string(SECRET_KEY).orEmpty()
    }

    fun getCredentialsFile(config: ConfigProperties): String {
        return config.string(CREDENTIALS_FILE).orEmpty()
    }

    fun getConnectionProperties(config: ConfigProperties): ConnectionProperties {
        return ConnectionProperties(
                Regions.fromName(getRegion(config)),
                getAccessKey(config),
                getSecretKey(config),
                getProfile(config),
                getCredentialsFile(config),
                isLocal(config))
    }
}