package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
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
    private val tableListView: TableListView by inject()
    private val controller: MainController by inject()
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
                    addClass("button-select")
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
        buttonbar {
            button("Connect") {
                action {
                    runAsyncWithProgress {
                        with(app.config) {
                            set(Config.REGION to region.value)
                            set(Config.LOCAL to local.value)
                            set(Config.ACCESS_KEY to key.value)
                            set(Config.SECRET_KEY to secret.value)
                            set(Config.PROFILE to profile.value)
                            set(Config.CREDENTIALS_FILE to credentialsFile.value)
                            save()
                        }
                        val properties = Config.getConnectionProperties(app.config)
                        val operation = controller.getClient(properties)
                        val tables = controller.listTables(properties)
                        Triple(operation, properties, tables)
                    } ui { result ->
                        tableListView.refresh(result.first, result.second, result.third)
                        close()
                    }
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
