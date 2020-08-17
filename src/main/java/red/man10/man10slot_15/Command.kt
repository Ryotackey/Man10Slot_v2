package red.man10.man10slot_15

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import java.util.*
import kotlin.collections.HashMap

class Command(val plugin: Man10Slot_15): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player) {

            if (args[0].equals("simulate", ignoreCase = true)){

                if (!plugin.slotmap.containsKey(args[1])) {
                    sender.sendMessage(plugin.prefix + "§cこのスロットは存在しません")
                    return true
                }

                var amount = 0

                try {
                    amount = args[2].toInt()
                }catch (e: NumberFormatException){
                    sender.sendMessage(plugin.prefix + "§c数字を指定してください")
                    return true
                }

                val slot = plugin.slotmap[args[1]]!!

                val s = Simulate(plugin, slot, amount, sender)
                s.start()

                return true

            }

            return false
        }

        val p = sender as Player

        when (args.size){

            0->{
                sendHelp(sender)
                return true
            }

            1->{

                if (args[0].equals("help", ignoreCase = true)){
                    sendHelp(sender)
                    return true
                }

                if (args[0].equals("list", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    p.sendMessage("§e§l-------§b§lスロット一覧§e§l-------")
                    for (i in plugin.slotmap){
                        p.sendMessage("§6${i.key}")
                    }
                    sender.sendMessage("§e§l---------------------------")
                    return true
                }

                if (args[0].equals("save", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    plugin.locationSave()
                    p.sendMessage(plugin.prefix + "§asave complete")
                    return true
                }

                if (args[0].equals("reload", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    plugin.slotmap.clear()
                    plugin.loadLocation()
                    plugin.loadSlot()
                    for (j in plugin.slotmap) {
                        val key = j.key
                        val slot = j.value
                        if (plugin.frameloc.containsKey(key)) {
                            for (loca in plugin.frameloc[key]!!) {
                                loca.world!!.loadChunk(loca.blockX, loca.blockY)
                                for (i in loca.chunk.entities) {
                                    if (i.location.block.location == loca && i.type == EntityType.ITEM_FRAME) {
                                        slot.frame.add(i as ItemFrame)
                                    }
                                }
                            }

                        }
                    }
                    p.sendMessage(plugin.prefix + "§areload complete")
                    return true
                }

                if (args[0].equals("on", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (plugin.enable){
                        p.sendMessage("${plugin.prefix}§cすでにオンです")
                        return true
                    }

                    plugin.enable = true

                    p.sendMessage("${plugin.prefix}§aオンにしました")

                    return true

                }

                if (args[0].equals("off", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.enable){
                        p.sendMessage("${plugin.prefix}§cすでにオフです")
                        return true
                    }

                    plugin.enable = false

                    p.sendMessage("${plugin.prefix}§aオフにしました")

                    return true

                }

            }

            2->{

                if (args[0].equals("create", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.slotmap.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは見つかりませんでした")
                        return true
                    }

                    if (plugin.frameloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されています")
                        return true
                    }

                    plugin.frameselectmap[p] = mutableListOf(args[1])
                    p.sendMessage(plugin.prefix + "§a額縁を")
                    p.sendMessage("§l123\n§l456\n§l789")
                    p.sendMessage("§aとなるように設置してください")

                    return true
                }

                if (args[0].equals("setsign", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (plugin.signloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットの看板はすでに設置されています")
                        return true
                    }

                    if (!plugin.frameloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されていません")
                        return true
                    }

                    plugin.signselect[p] = args[1]
                    p.sendMessage("${plugin.prefix}§a看板を設置してください")

                    return true

                }

                if (args[0].equals("remove", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.frameloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されていません")
                        return true
                    }

                    plugin.removeLocation(args[1])

                    p.sendMessage(plugin.prefix + "§aremove complete")

                    return true

                }

                if (args[0].equals("view", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    if (!plugin.slotmap.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは見つかりませんでした")
                        return true
                    }

                    val get = getData(plugin, args[1], sender)
                    get.start()

                    return true

                }

                if (args[0].equals("table", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.frameloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されていません")
                        return true
                    }

                    val slot = plugin.slotmap[args[1]]!!

                    p.sendMessage("${plugin.prefix}§aこのスロットのテーブル番号は§e${slot.chancenum}§aで残り§e${slot.countgame}§aゲームで戻ります")

                    return true

                }

            }

            3->{

                if (args[0].equals("simulate", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.slotmap.containsKey(args[1])) {
                        sender.sendMessage(plugin.prefix + "§cこのスロットは存在しません")
                        return true
                    }

                    var amount = 0

                    try {
                        amount = args[2].toInt()
                    }catch (e: NumberFormatException){
                        sender.sendMessage(plugin.prefix + "§c数字を指定してください")
                        return true
                    }

                    val slot = plugin.slotmap[args[1]]!!

                    val s = Simulate(plugin, slot, amount, sender)
                    s.start()

                    return true

                }

                if (args[0].equals("flag", ignoreCase = true)){

                    if (!plugin.slotmap.containsKey(args[1])) {
                        p.sendMessage(plugin.prefix + "§cこのスロットは存在しません")
                        return true
                    }

                    val slot = plugin.slotmap[args[1]]!!

                    val flag = args[2]

                    if (!slot.wining_chance.containsKey(flag)){
                        p.sendMessage(plugin.prefix + "§cそのフラグは存在しません")
                        return true
                    }

                    slot.win = flag
                    p.sendMessage(plugin.prefix + "§aフラグを設定しました")

                    return true
                }

            }

            5->{

                if (args[0].equals("setchance", ignoreCase = true)){

                    if (!plugin.slotmap.containsKey(args[1])) {
                        p.sendMessage(plugin.prefix + "§cこのスロットは存在しません")
                        return true
                    }

                    val slot = plugin.slotmap[args[1]]!!

                    val win = args[2]

                    if (!slot.wining_chance.containsKey(win)){
                        p.sendMessage(plugin.prefix + "§cその役は存在しません")
                        return true
                    }

                    var index = 0

                    try {
                        index = args[3].toInt()
                    }catch (e: NumberFormatException){
                        p.sendMessage(plugin.prefix + "§c数字を指定してください")
                        return true
                    }

                    var chance = 0.0

                    try {
                        chance = args[4].toDouble()
                    }catch (e: NumberFormatException){
                        p.sendMessage(plugin.prefix + "§c数字を指定してください")
                        return true
                    }

                    if (slot.wining_chance[win]!!.size <= index){
                        p.sendMessage(plugin.prefix + "§cそのテーブル番号は存在しません")
                        return true
                    }

                    slot.wining_chance[win]!![index] = chance

                    val filelist = plugin.slotfile.listFiles()

                    p.sendMessage("${plugin.prefix}§a確率を設定しました")

                    if (filelist == null)return true
                    if (filelist.isEmpty())return true

                    for (j in filelist){

                        val config = YamlConfiguration.loadConfiguration(j)

                        for (key in config.getConfigurationSection("")!!.getKeys(false)) {

                            if (key != args[1]) continue

                            for (i in config.getConfigurationSection("${key}.wining_setting")!!.getKeys(false)){

                                if (i != win)continue

                                val list = config.getList("${key}.wining_setting.${i}.chance") as MutableList<Double>

                                list[index] = chance

                                config.set("${key}.wining_setting.${i}.chance", list)

                            }

                        }

                        config.save(j)

                    }

                    return true

                }

            }

        }

        return false
    }

    fun sendHelp(sender: CommandSender){

        sender.sendMessage("§e§l-------${plugin.prefix}§e§l-------")
        sender.sendMessage("§6§l/mslot create [名前] §f§r:[名前]のスロットを設置する")
        sender.sendMessage("§6§l/mslot list §f§r:ロードされているスロットのリストを見る")
        sender.sendMessage("§6§l/mslot save §f§r:スロットの設置してある場所を保存する")
        sender.sendMessage("§6§l/mslot reload §f§r:スロットファイルをリロードする")
        sender.sendMessage("§6§l/mslot view [名前] §f§r:[名前]のスロットの回された数などの情報を見れる")
        sender.sendMessage("§6§l/mslot simulate [名前] [回数] §f§r:[名前]のスロットを[回数]分回したシミュレーション結果が見れる")
        sender.sendMessage("§6§l/mslot on/off §f§r:スロットを回すのをon/offできる")
        sender.sendMessage("§6§l/mslot flag [名前] [flag名] §f§r:[名前]のスロットのフラグを強制的に[flag名]にする")
        sender.sendMessage("§6§l/mslot setchance [名前] [flag名] [table番号] [確率] §f§r:[名前]のスロットの[flag名]の[table番号]の確立を[確率]にする")
        sender.sendMessage("§6§l/mslot setsign [名前] §f§r:[名前]のスロットのカウント看板を設置する")
        sender.sendMessage("§6§l/mslot table [名前] §f§r:[名前]のスロットのテーブル番号と残りゲーム数を表示する")
        sender.sendMessage("§bcreated by Ryotackey")
        sender.sendMessage("§6Ver 2.1")
        sender.sendMessage("§e§l------------------------------")

    }

}

class Simulate(val plugin: Man10Slot_15, val slot: Man10Slot_15.SlotInformation, val amount: Int, val p: CommandSender): Thread(){

    override fun run() {
        val count = HashMap<String, Int>()

        val chancenum = slot.chancenum

        var prize = 0.0

        for (i in 0 until amount) {

            val map = HashMap<Double, String>()

            for (j in slot.wining_chance){

                val num = slot.chancenum % j.value.size
                map[j.value[num]] = j.key

            }

            val winlist = plugin.mapSort(map)

            var win = "0"

            for (j in winlist) {

                val ran = Random().nextDouble()

                if (j.key > ran) win = j.value
            }

            if (slot.countgame == 0) {
                slot.chancenum = 0
            }

            if (slot.countgame > 0) {
                slot.countgame--
            }

            if (win != "0"){
                if (count.containsKey(win)){
                    count[win] = count[win]!! + 1
                }else{
                    count[win] = 1
                }

                if (slot.changetable[win] != null){

                    val ct = slot.changetable[win]

                    val r = Random().nextDouble()

                    var sum = 0.0

                    for (t in 0 until ct!!.chance.size){

                        sum += ct.chance[t]

                        if (r < sum) {
                            slot.chancenum = ct.table[t]
                            slot.countgame = ct.game[t]
                            break
                        }

                    }
                }

                prize += slot.wining_prize[win]!!

            }

        }

        p.sendMessage("§6---------------------------------")
        p.sendMessage("§a§lシュミレーション結果")
        p.sendMessage("§l${amount}回中")

        var c = 0

        for (i in count){
            p.sendMessage("§a§l${i.key} §f§l: §e§l${i.value}回")
            c += i.value
        }

        val price = slot.price*amount

        p.sendMessage("§l総当たり回数§e§l${c}回")

        p.sendMessage("§l合計獲得額§e§l${prize}§f§l円 / 合計投入額§e§l${price}円")
        p.sendMessage("§e§l還元率§6§l${Math.round(prize/price*1000).toDouble()/10.0}%")

        p.sendMessage("§6---------------------------------")

        slot.chancenum = chancenum

    }


}