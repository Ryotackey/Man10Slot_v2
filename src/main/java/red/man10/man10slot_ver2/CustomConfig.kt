package red.man10.man10slot_ver2

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin

import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level

class CustomConfig @JvmOverloads constructor(private val plugin: Plugin, private val file: String = "config.yml") {

    private var config: FileConfiguration? = null
    private val configFile: File

    init {
        configFile = File(plugin.dataFolder, file)
    }

    fun saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(file, false)
        }
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)

        val defConfigStream = plugin.getResource(file) ?: return

        config!!.defaults = YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream, StandardCharsets.UTF_8))
    }

    fun getConfig(): FileConfiguration? {
        if (config == null) {
            reloadConfig()
        }
        return config
    }

    fun saveConfig() {
        if (config == null) {
            return
        }
        try {
            getConfig()!!.save(configFile)
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save config to $configFile", ex)
        }

    }
}
