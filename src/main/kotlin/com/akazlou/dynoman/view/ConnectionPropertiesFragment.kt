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
    private val credentialsFile = SimpleStringProperty("")
    private val local = SimpleBooleanProperty(Config.isLocal(app.config))
    override val root = form {
        fieldset("Properties") {
            field("AWS Access Key:") {
                textfield(key)
            }
            field("AWS Secret Key:") {
                textfield(secret)
            }
            field("AWS Credentials Profile:") {
                textfield(profile)
            }
            field("AWS Credentials File:") {
                textfield(credentialsFile)
                button("...") {
                    action {
                        val files = chooseFile("Open AWS Credentials File", emptyArray())
                        credentialsFile.value = files.map { it.absolutePath }.firstOrNull().orEmpty()
                    }
                }
            }
            field("AWS Region:") {
                combobox(region, Regions.values().map { it.getName() })
                checkbox("local", local)
            }
        }
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
                    save()
                }
                close()
            }
        }
    }
}
