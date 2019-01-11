package com.akazlou.dynoman.view

import com.amazonaws.regions.Regions
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class ConnectionPropertiesFragment : Fragment("Connection") {
    private val region = SimpleStringProperty(Config.getRegion(app.config))
    private val key = SimpleStringProperty(Config.getAccessKey(app.config))
    private val secret = SimpleStringProperty(Config.getSecretKey(app.config))
    private val profile = SimpleStringProperty(Config.getProfile(app.config))
    private val credentialsFile = SimpleStringProperty(Config.getCredentialsFile(app.config))
    private val local = SimpleBooleanProperty(Config.isLocal(app.config))
    override val root = form {
        fieldset("AWS Properties") {
            field("Access Key:") {
                textfield(key)
            }
            field("Secret Key:") {
                textfield(secret)
            }
            field("Credentials Profile:") {
                textfield(profile)
            }
            field("Credentials File:") {
                textfield(credentialsFile)
                button("...") {
                    action {
                        val files = chooseFile("Open AWS Credentials File", emptyArray())
                        credentialsFile.value = files.map { it.absolutePath }.firstOrNull().orEmpty()
                    }
                }
            }
            field("Region:") {
                combobox(region, Regions.values().map { it.getName() })
                checkbox("local", local)
            }
        }
        // TODO: Investigate on layout and default/cancel buttons
        // TODO: Try without buttonbar
        buttonbar {
            button("Connect") {
                //setPrefSize(100.0, 40.0)
                prefWidth = 100.0
                action {
                    with(app.config) {
                        set(Config.REGION to region.value)
                        set(Config.LOCAL to local.value)
                        set(Config.ACCESS_KEY to key.value)
                        set(Config.SECRET_KEY to secret.value)
                        set(Config.PROFILE to profile.value)
                        set(Config.CREDENTIALS_FILE to credentialsFile.value)
                        save()
                    }
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }
}
