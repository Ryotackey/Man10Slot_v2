package red.man10.man10slot_15

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class Man10Slot_15 : JavaPlugin() {

    var enable = true

    val locc = CustomConfig(this, "location.yml")
    val config = CustomConfig(this)
    val slotfile = File(this.dataFolder.absolutePath + File.separator + "slots")
    val frameloc = ConcurrentHashMap<String, MutableList<Location>>()
    var leverloc = ConcurrentHashMap<Location, String>()
    var signloc = ConcurrentHashMap<String, Location>()

    val frameselectmap = HashMap<Player, MutableList<String>>()
    val leverselect = HashMap<Player, String>()
    val signselect = HashMap<Player, String>()

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    val slotmap = ConcurrentHashMap<String, SlotInformation>()

    override fun onEnable() {
        // Plugin startup logic

        locc.saveDefaultConfig()
        config.saveDefaultConfig()

        slotfile.mkdir()

        getCommand("mslot")!!.setExecutor(Command(this))

        server.pluginManager.registerEvents(Events(this), this)

        loadLocation()
        loadSlot()

        object : BukkitRunnable(){
            override fun run() {
                for (j in slotmap) {
                    val key = j.key
                    val slot = j.value
                    if (frameloc.containsKey(key)) {
                        for (loca in frameloc[key]!!) {
                            loca.world!!.loadChunk(loca.blockX, loca.blockY)
                            for (i in loca.chunk.entities) {
                                if (i.location.block.location == loca && i.type == EntityType.ITEM_FRAME) {
                                    slot.frame.add(i as ItemFrame)
                                }
                            }
                        }

                    }
                }
            }
        }.runTaskLater(this, 100)

        val m = MySQLCreate(this)
        m.start()

        saveProcess()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun locationSave(){

        val config = locc.getConfig() ?: return

        for (i in config.getKeys(false)){
            config.set(i, null)
        }

        for (i in leverloc){

            config.set("location.lever.${i.value}.x", i.key.blockX)
            config.set("location.lever.${i.value}.y", i.key.blockY)
            config.set("location.lever.${i.value}.z", i.key.blockZ)
            config.set("location.lever.${i.value}.world", i.key.world!!.uid.toString())

        }

        for (i in signloc){

            config.set("location.sign.${i.key}.x", i.value.blockX)
            config.set("location.sign.${i.key}.y", i.value.blockY)
            config.set("location.sign.${i.key}.z", i.value.blockZ)
            config.set("location.sign.${i.key}.world", i.value.world!!.uid.toString())

        }

        for (i in frameloc){

            for (j in 0 until i.value.size){

                config.set("location.frame.${i.key}.${j}.x", i.value[j].blockX)
                config.set("location.frame.${i.key}.${j}.y", i.value[j].blockY)
                config.set("location.frame.${i.key}.${j}.z", i.value[j].blockZ)
                config.set("location.frame.${i.key}.${j}.world", i.value[j].world!!.uid.toString())

            }
        }

        for (i in slotmap){

            val key = i.key
            val slot = i.value

            config.set("spincount.$key", slot.spincount)
            config.set("chancenum.$key", slot.chancenum)
            config.set("countgame.$key", slot.countgame)

        }

        config.set("enable", enable)

        locc.saveConfig()

    }

    fun loadLocation(){

        locc.reloadConfig()

        val config = locc.getConfig() ?: return

        leverloc.clear()
        signloc.clear()
        frameloc.clear()

        enable = config.getBoolean("enable")

        if (config.contains("location.lever")) {
            for (i in config.getConfigurationSection("location.lever")!!.getKeys(false)) {

                val loc = Location(Bukkit.getWorld(UUID.fromString(config.getString("location.lever.$i.world"))), config.getDouble("location.lever.$i.x"), config.getDouble("location.lever.$i.y"), config.getDouble("location.lever.$i.z"))
                leverloc[loc] = i

            }
        }

        if (config.contains("location.sign")) {
            for (i in config.getConfigurationSection("location.sign")!!.getKeys(false)) {

                val loc = Location(Bukkit.getWorld(UUID.fromString(config.getString("location.sign.$i.world"))), config.getDouble("location.sign.$i.x"), config.getDouble("location.sign.$i.y"), config.getDouble("location.sign.$i.z"))
                signloc[i] = loc

            }
        }
        if (config.contains("location.frame")) {
            for (i in config.getConfigurationSection("location.frame")!!.getKeys(false)) {
                val list = mutableListOf<Location>()
                for (j in config.getConfigurationSection("location.frame.$i")!!.getKeys(false)) {

                    val loc = Location(Bukkit.getWorld(UUID.fromString(config.getString("location.frame.$i.$j.world"))), config.getDouble("location.frame.$i.$j.x"), config.getDouble("location.frame.$i.$j.y"), config.getDouble("location.frame.$i.$j.z"))
                    list.add(loc)

                }
                frameloc[i] = list
            }
        }

    }

    fun removeLocation(key: String){

        signloc.remove(key)

        frameloc.remove(key)

        for (i in leverloc){
            if (i.value == key){
                leverloc.remove(i.key)
                break
            }
        }

        for (i in slotmap){
            if (i.key == key){
                i.value.spincount = 0
            }
        }

        locationSave()

    }

    fun saveProcess(){
        object : BukkitRunnable(){
            override fun run() {
                locationSave()
                server.consoleSender.sendMessage("[Man10Slot] §asave complete")
            }
        }.runTaskTimerAsynchronously(this, 600, 12000)
    }

    fun mapSort(map: HashMap<Double, String>): Map<Double, String>{

        val result = map.toList().sortedBy { (_, value) -> value}.toMap()

        return result
    }

    fun loadSlot(){

        locc.reloadConfig()

        config.reloadConfig()

        val filelist = slotfile.listFiles()

        val slotlist = mutableListOf<FileConfiguration>()

        for (i in filelist){

            if (!i.isFile)continue
            if (!i.path.endsWith(".yml"))continue

            val config = YamlConfiguration.loadConfiguration(i)
            slotlist.add(config)
        }

        server.consoleSender.sendMessage("§aスロットのロードを開始します")

        if (slotlist.size == 0) return

        for (c in slotlist) {

            for (key in c.getKeys(false)) {

                val slot = SlotInformation()

                slot.slot_name = c.getString("$key.general_setting.slot_name")!!
                slot.price = c.getDouble("$key.general_setting.price")

                if (c.contains("$key.general_setting.permission")){

                    slot.permission = c.getString("$key.general_setting.permission")

                }

                val reel1 = stringToItemList(c.getString("$key.general_setting.items_reel1")!!)
                val reel2 = stringToItemList(c.getString("$key.general_setting.items_reel2")!!)
                val reel3 = stringToItemList(c.getString("$key.general_setting.items_reel3")!!)

                slot.reel1 = reel1
                slot.reel2 = reel2
                slot.reel3 = reel3

                slot.wait = c.getLong("$key.general_setting.wait")

                slot.sleep = c.getLong("$key.general_setting.sleep")

                if (c.contains("$key.general_setting.between")){

                    slot.between = c.getInt("$key.general_setting.between")

                }

                if (c.contains("$key.general_setting.spining_sound")) {
                    val s = Sound()
                    s.sound = c.getString("$key.general_setting.spining_sound.sound")!!
                    s.volume = c.getDouble("$key.general_setting.spining_sound.volume").toFloat()
                    s.pitch = c.getDouble("$key.general_setting.spining_sound.pitch").toFloat()
                    slot.spiningsound = s
                }

                for (win in c.getConfigurationSection("$key.wining_setting")!!.getKeys(false)) {

                    slot.wining_chance[win] = c.getList("$key.wining_setting.$win.chance") as MutableList<Double>

                    slot.wining_name[win] = c.getString("$key.wining_setting.$win.name")!!
                    slot.wining_item[win] = wining_itemCreate(c.getString("$key.wining_setting.$win.item")!!, reel1, reel2, reel3)
                    slot.wining_prize[win] = c.getDouble("$key.wining_setting.$win.prize")
                    slot.coindrop[win] = c.getBoolean("$key.wining_setting.$win.coindrop")
                    slot.wining_level[win] = c.getInt("$key.wining_setting.$win.level")

                    if (c.contains("$key.wining_setting.$win.changetable")) {
                        val list = c.getStringList("$key.wining_setting.$win.changetable")

                        val ct = ChangeTable()

                        for (i in list){
                            val str = i.split("-")
                            ct.table.add(str[0].toInt())
                            ct.chance.add(str[1].toDouble())
                            ct.game.add(str[2].toInt())
                        }

                        slot.changetable[win] = ct

                    }

                    if (c.contains("$key.wining_setting.$win.command")) {
                        slot.wining_command[win] = c.getList("$key.wining_setting.$win.command") as MutableList<String>
                    }

                    if (c.contains("$key.wining_setting.$win.chancewin")) {
                        slot.chancewin[win] = c.getList("$key.wining_setting.$win.chancewin") as MutableList<String>
                    }

                    if (c.contains("$key.wining_setting.$win.countreset")){
                        slot.countreset[win] = c.getBoolean("$key.wining_setting.$win.countreset")
                    }else slot.countreset[win] = false

                    if (c.contains("$key.wining_setting.$win.win_particle")) {
                        val par = Particle()
                        par.par = org.bukkit.Particle.valueOf(c.getString("$key.wining_setting.$win.win_particle.particle")!!)
                        par.count = c.getInt("$key.wining_setting.$win.win_particle.count")
                        par.chance = c.getDouble("$key.wining_setting.$win.win_particle.chance")
                        slot.win_particle[win] = par
                    }

                    if (c.contains("$key.wining_setting.$win.freeze")) {
                        slot.wining_freeze[win] = c.getDouble("$key.wining_setting.$win.freeze.chance")
                        slot.freeze_kind[win] = c.getString("$key.wining_setting.$win.freeze.kind")!!

                        val itemstr = c.getString("$key.wining_setting.$win.item")!!.split(",")

                        slot.freeze_item[win] = mutableListOf(reel1[itemstr[0].toInt()-1], reel2[itemstr[1].toInt()-1], reel3[itemstr[2].toInt()-1])

                        val pres = Sound()
                        pres.sound = c.getString("$key.wining_setting.$win.freeze.predict_sound.sound")!!
                        pres.volume = c.getDouble("$key.wining_setting.$win.freeze.predict_sound.volume").toFloat()
                        pres.pitch = c.getDouble("$key.wining_setting.$win.freeze.predict_sound.pitch").toFloat()

                        slot.freeze_pres[win] = pres

                        val stop1s = Sound()
                        stop1s.sound = c.getString("$key.wining_setting.$win.freeze.stop1_sound.sound")!!
                        stop1s.volume = c.getDouble("$key.wining_setting.$win.freeze.stop1_sound.volume").toFloat()
                        stop1s.pitch = c.getDouble("$key.wining_setting.$win.freeze.stop1_sound.pitch").toFloat()

                        slot.freeze_stop1s[win] = stop1s

                        val stop2s = Sound()
                        stop2s.sound = c.getString("$key.wining_setting.$win.freeze.stop2_sound.sound")!!
                        stop2s.volume = c.getDouble("$key.wining_setting.$win.freeze.stop2_sound.volume").toFloat()
                        stop2s.pitch = c.getDouble("$key.wining_setting.$win.freeze.stop2_sound.pitch").toFloat()

                        slot.freeze_stop2s[win] = stop2s

                        val stop3s = Sound()
                        stop3s.sound = c.getString("$key.wining_setting.$win.freeze.stop3_sound.sound")!!
                        stop3s.volume = c.getDouble("$key.wining_setting.$win.freeze.stop3_sound.volume").toFloat()
                        stop3s.pitch = c.getDouble("$key.wining_setting.$win.freeze.stop3_sound.pitch").toFloat()

                        slot.freeze_stop3s[win] = stop3s

                        slot.freeze_sleep[win] = c.getLong("$key.wining_setting.$win.freeze.sleep")

                        slot.freeze_step[win] = c.getInt("$key.wining_setting.$win.freeze.step")

                        if (c.contains("$key.wining_setting.$win.freeze.command")) {
                            slot.freeze_command[win] = c.getList("$key.wining_setting.$win.freeze.command") as MutableList<String>
                        }

                    }

                    val sound = Sound()
                    sound.sound = c.getString("$key.wining_setting.$win.winsound.sound")!!
                    sound.volume = c.getDouble("$key.wining_setting.$win.winsound.volume").toFloat()
                    sound.pitch = c.getDouble("$key.wining_setting.$win.winsound.pitch").toFloat()

                    slot.wining_sound[win] = sound

                }

                if (c.contains("$key.spin_setting")) {
                    val effect = Effect()
                    if (c.contains("$key.spin_setting.sound")) {
                        val sound = Sound()
                        sound.sound = c.getString("$key.spin_setting.sound.sound")!!
                        sound.volume = c.getDouble("$key.spin_setting.sound.volume").toFloat()
                        sound.pitch = c.getDouble("$key.spin_setting.sound.pitch").toFloat()
                        effect.sound = sound
                    }
                    if (c.contains("$key.spin_setting.particle")) {
                        val par = Particle()
                        par.par = org.bukkit.Particle.valueOf(c.getString("$key.spin_setting.particle.particle")!!)
                        par.count = c.getInt("$key.spin_setting.particle.count")
                        par.chance = c.getDouble("$key.spin_setting.particle.chance")
                        effect.particle = par
                    }
                    if (c.contains("$key.spin_setting.command")) {
                        effect.command = c.getList("$key.spin_setting.command") as MutableList<String>
                    }
                    slot.spineffect = effect
                }

                if (c.contains("$key.stop_setting")) {
                    val effect = Effect()
                    if (c.contains("$key.stop_setting.sound")) {
                        val sound = Sound()
                        sound.sound = c.getString("$key.stop_setting.sound.sound")!!
                        sound.volume = c.getDouble("$key.stop_setting.sound.volume").toFloat()
                        sound.pitch = c.getDouble("$key.stop_setting.sound.pitch").toFloat()
                        effect.sound = sound
                    }
                    if (c.contains("$key.stop_setting.particle")) {
                        val par = Particle()
                        par.par = org.bukkit.Particle.valueOf(c.getString("$key.stop_setting.particle.particle")!!)
                        par.count = c.getInt("$key.stop_setting.particle.count")
                        par.chance = c.getDouble("$key.stop_setting.particle.chance")
                        effect.particle = par
                    }
                    if (c.contains("$key.stop_setting.command")) {
                        effect.command = c.getList("$key.stop_setting.command") as MutableList<String>
                    }
                    slot.stopeffect = effect
                }

                if (c.contains("$key.change_setting")) {
                    val effect = Effect()
                    if (c.contains("$key.change_setting.sound")) {
                        val sound = Sound()
                        sound.sound = c.getString("$key.change_setting.sound.sound")!!
                        sound.volume = c.getDouble("$key.change_setting.sound.volume").toFloat()
                        sound.pitch = c.getDouble("$key.change_setting.sound.pitch").toFloat()
                        effect.sound = sound
                    }
                    if (c.contains("$key.change_setting.particle")) {
                        val par = Particle()
                        par.count = c.getInt("$key.change_setting.particle.count")
                        par.par = org.bukkit.Particle.valueOf(c.getString("$key.change_setting.particle.particle")!!)
                        par.chance = c.getDouble("$key.change_setting.particle.chance")
                        effect.particle = par
                    }
                    if (c.contains("$key.change_setting.command")) {
                        effect.command = c.getList("$key.change_setting.command") as MutableList<String>
                    }
                    slot.changeeffect = effect
                }

                val l = locc.getConfig()!!

                if (l.contains("spincount.$key")) {
                    slot.spincount = l.getInt("spincount.$key")
                }

                if (l.contains("chancenum.$key")) {
                    slot.chancenum = l.getInt("chancenum.$key")
                }

                if (l.contains("countgame.$key")) {
                    slot.countgame = l.getInt("countgame.$key")
                }

                slotmap[key] = slot
                server.consoleSender.sendMessage("§a$key:ロード完了")
            }
        }
    }

    fun wining_itemCreate(itemstr: String, reel1: MutableList<ItemStack>, reel2: MutableList<ItemStack>, reel3: MutableList<ItemStack>): MutableList<MutableList<ItemStack>>{

        val itemlist = mutableListOf<ItemStack>()

        val list = mutableListOf<MutableList<ItemStack>>()

        if (!itemstr.contains(Regex(",")))return list

        val item = itemstr.split(Regex(","))

        if (item.size == 3){

            for (i in item.indices) {

                if (item[i] == "*") {
                    itemlist.add(ItemStack(Material.AIR))
                } else {

                    var num = 0
                    try {
                        num = item[i].toInt()
                    } catch (e: NumberFormatException) {
                        return list
                    }

                    when (i % 3) {
                        0 -> itemlist.add(reel1[num - 1])
                        1 -> itemlist.add(reel2[num - 1])
                        2 -> itemlist.add(reel3[num - 1])
                    }
                }
            }

            for (i1 in 0 until reel1.size){
                for (i2 in 0 until reel2.size){
                    for (i3 in 0 until reel3.size){

                        val i = mutableListOf(reel1[(i1+1)%reel1.size], reel2[(i2+1)%reel2.size], reel3[(i3+1)%reel3.size], reel1[i1%reel1.size], reel2[i2%reel2.size],
                                reel3[i3%reel3.size], reel1[(i1+reel1.size-1)%reel1.size], reel2[(i2+reel2.size-1)%reel2.size], reel3[(i3+reel3.size-1)%reel3.size])

                        if (itemlist.contains(ItemStack(Material.AIR))){

                            val nonwild = mutableListOf<Int>()

                            for (j in 0 until itemlist.size){
                                if (itemlist[j] != ItemStack(Material.AIR)) nonwild.add(j)
                            }

                            when (nonwild.size){

                                1-> if (i[nonwild[0]] == itemlist[nonwild[0]] || i[nonwild[0]+3] == itemlist[nonwild[0]] || i[nonwild[0]+6] == itemlist[nonwild[0]]) list.add(i)


                                2->{

                                    when (nonwild[0] + nonwild[1]){

                                        1-> if ((i[0] == itemlist[0] && i[1] == itemlist[1]) || (i[0] == itemlist[0] && i[4] == itemlist[1])
                                                || (i[3] == itemlist[0] && i[4] == itemlist[1]) || (i[6] == itemlist[0] && i[4] == itemlist[1]) || (i[6] == itemlist[0] && i[7] == itemlist[1])) list.add(i)

                                        2-> if ((i[0] == itemlist[0] && i[2] == itemlist[2]) || (i[0] == itemlist[0] && i[8] == itemlist[2])
                                                || (i[3] == itemlist[0] && i[5] == itemlist[2]) || (i[6] == itemlist[0] && i[8] == itemlist[2]) || (i[6] == itemlist[0] && i[2] == itemlist[2])) list.add(i)

                                        3-> if ((i[2] == itemlist[2] && i[1] == itemlist[1]) || (i[2] == itemlist[2] && i[4] == itemlist[1])
                                                || (i[5] == itemlist[2] && i[4] == itemlist[1]) || (i[8] == itemlist[2] && i[4] == itemlist[1]) || (i[8] == itemlist[2] && i[7] == itemlist[1])) list.add(i)

                                    }

                                }

                            }

                        }else {

                            if ((i[0] == itemlist[0] && i[1] == itemlist[1] && i[2] == itemlist[2]) || (i[0] == itemlist[0] && i[4] == itemlist[1] && i[8] == itemlist[2]) || (i[3] == itemlist[0] && i[4] == itemlist[1] && i[5] == itemlist[2]) || (i[6] == itemlist[0] && i[7] == itemlist[1] && i[8] == itemlist[2]) ||
                                    (i[6] == itemlist[0] && i[4] == itemlist[1] && i[2] == itemlist[2])) list.add(i)

                        }
                    }
                }
            }

        }
        return list
    }

    fun stringToItemList(itemstr: String): MutableList<ItemStack>{

        val reel = mutableListOf<ItemStack>()

        if (!itemstr.contains(Regex(",")))return reel

        val item = itemstr.split(Regex(",")) as MutableList<String>

        for (i in item){

            val reelitem =if (i.contains("-")) ItemStackA(Material.matchMaterial(i.split("-")[0])!!, i.split("-")[1].toInt())
            else ItemStackA(Material.matchMaterial(i)!!)

            reel.add(reelitem.build())
        }
        return reel
    }

    fun runCommand(commands: MutableList<String>, winname: String, p: Player, slotname: String, money: Double){

        object : BukkitRunnable(){
            override fun run() {
                for (i in commands) {
                    val i2: String = if (i.contains(Regex("<player>"))){
                        i.replace("<player>".toRegex(), p.name)
                    }else i

                    val i3 = if (i2.contains(Regex("<slot>"))){
                        i2.replace("<slot>".toRegex(), slotname)
                    }else i2

                    val i4= if (i3.contains(Regex("<prize>"))){
                        i3.replace("<prize>".toRegex(), money.toString())
                    }else i3

                    val i5 = if (i4.contains(Regex("<win>"))){
                        i4.replace("<win>".toRegex(), winname)
                    }else i4

                    server.dispatchCommand(server.consoleSender, i5)
                }
            }
        }.runTask(this)

    }

    class SlotInformation{

        var slot_name = ""
        var price: Double = 0.0

        var reel1 = mutableListOf<ItemStack>()
        var reel2 = mutableListOf<ItemStack>()
        var reel3 = mutableListOf<ItemStack>()

        var sleep: Long = 80

        var permission: String? = null

        var wait: Long = 100

        var step = 0

        var win = "0"

        var freeze = false

        var chancenum = 0

        var between = 10

        var freeze_item = ConcurrentHashMap<String, MutableList<ItemStack>>()

        val wining_name = ConcurrentHashMap<String, String>()
        val wining_level = ConcurrentHashMap<String, Int>()
        val wining_item = ConcurrentHashMap<String, MutableList<MutableList<ItemStack>>>()
        val wining_prize = ConcurrentHashMap<String, Double>()
        val wining_chance = ConcurrentHashMap<String, MutableList<Double>>()
        val wining_command = ConcurrentHashMap<String, MutableList<String>?>()
        val wining_sound = ConcurrentHashMap<String, Sound>()
        val win_particle = ConcurrentHashMap<String, Particle?>()
        val chancewin = ConcurrentHashMap<String, MutableList<String>?>()
        var countgame = 0
        val countreset = ConcurrentHashMap<String, Boolean>()

        val coindrop = ConcurrentHashMap<String, Boolean>()

        val wining_freeze = ConcurrentHashMap<String, Double>()
        val freeze_kind = ConcurrentHashMap<String, String>()
        val freeze_sleep = ConcurrentHashMap<String, Long>()

        val freeze_pres = ConcurrentHashMap<String, Sound>()
        val freeze_stop1s = ConcurrentHashMap<String, Sound>()
        val freeze_stop2s = ConcurrentHashMap<String, Sound>()
        val freeze_stop3s = ConcurrentHashMap<String, Sound>()
        val freeze_step = ConcurrentHashMap<String, Int>()
        val freeze_command = ConcurrentHashMap<String, MutableList<String>>()

        var spiningsound: Sound? = null

        var changetable = ConcurrentHashMap<String, ChangeTable>()

        var flag = false

        var p: Player? = null

        var spincount = 0
        val wincount = ConcurrentHashMap<String, Int>()

        var stopeffect: Effect? = null
        var changeeffect: Effect? = null
        var spineffect: Effect? = null

        val frame = mutableListOf<ItemFrame>()

    }

    class ChangeTable{
        var table = mutableListOf<Int>()
        var chance = mutableListOf<Double>()
        var game = mutableListOf<Int>()
    }

    class Sound{
        var sound: String = ""
        var volume = 0f
        var pitch = 0f
    }
    class Particle{
        var par: org.bukkit.Particle? = null
        var count: Int? = null
        var chance: Double? = null
    }

    class Effect {
        var particle: Particle? = null
        var sound: Sound? = null
        var command: MutableList<String>? = null
    }
}
