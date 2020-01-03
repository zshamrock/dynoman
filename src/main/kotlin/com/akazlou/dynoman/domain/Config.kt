package com.akazlou.dynoman.domain

import com.amazonaws.regions.Regions
import tornadofx.*
import java.nio.file.Path

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
    private const val DEFAULT_AWS_PROFILE = "default"

    private const val SESSIONS_STORE_PATH = "sessions"
    private const val QUERIES_STORE_PATH = "queries"

    fun getRegion(config: ConfigProperties): String {
        return config.string(REGION, DEFAULT_REGION.getName())
    }

    fun isLocal(config: ConfigProperties): Boolean {
        return config.boolean(LOCAL, false)
    }

    fun getProfile(config: ConfigProperties): String {
        val profile = config.string(PROFILE)
        return if (profile.isNullOrBlank()) {
            System.getProperty(SYSTEM_PROPERTY_PROFILE_NAME,
                    System.getenv(ENV_PROPERTY_PROFILE_NAME).orEmpty().ifEmpty { DEFAULT_AWS_PROFILE })
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

    fun getSavedSessionsPath(profile: String, base: Path): Path {
        return getPath(base, profile, SESSIONS_STORE_PATH)
    }

    fun getSavedQueriesPath(profile: String, base: Path): Path {
        return getPath(base, profile, QUERIES_STORE_PATH)
    }

    private fun getPath(base: Path, profile: String, store: String): Path {
        return base.resolve(profile).resolve(store)
    }
}