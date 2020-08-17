package red.man10.man10slot_15

import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class SpinProcess(val plugin: Man10Slot_15, val p: Player, val win: String, val slot: Man10Slot_15.SlotInformation, val key: String, val freezeflag: String?): Thread() {

    val reel1 = slot.reel1
    val reel2 = slot.reel2
    val reel3 = slot.reel3

    val wait = slot.wait

    var win_item: MutableList<ItemStack>? = null
    var win_prize: Double? = null
    var win_name: String? = null
    var win_command: MutableList<String>? = null
    var win_sound: Man10Slot_15.Sound? = null
    var win_level: Int = -1

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"
    val winfrag = mutableListOf(0, 0, 0)

    val sleep = slot.sleep

    var step = slot.step

    val between = Random().nextInt(slot.between/2) + slot.between
    val between1 = Random().nextInt(slot.between/2) + slot.between

    @Synchronized
    override fun run() {

        if (win != "0") {

            when (freezeflag) {

                "default" -> {
                    val f = DefaultFreeze(slot, plugin, win, slot.frame, key, p)
                    f.start()
                    return
                }

                "slow" -> {
                    val s = SlowFreeze(slot, plugin, win, slot.frame, key, p)
                    s.start()
                    return
                }

            }

            val r = Random().nextInt(slot.wining_item[win]!!.size)

            win_level = slot.wining_level[win]!!
            win_item = slot.wining_item[win]!![r]
            win_prize = slot.wining_prize[win]
            win_name = slot.wining_name[win]
            win_command = slot.wining_command[win]
            win_sound = slot.wining_sound[win]

        }

        while (true) {

            step++

            if (step < slot.step+wait) {
                spin1()
                spin2()
                spin3()
            }else{
                if (win != "0"){

                    if (slot.frame[0].item == win_item!![0] && slot.frame[3].item == win_item!![3] && slot.frame[6].item == win_item!![6] && winfrag[0] == 0 && winfrag[1] == 0 && winfrag[2] == 0) winfrag[0] = step
                    if (slot.frame[1].item == win_item!![1] && slot.frame[4].item == win_item!![4] && slot.frame[7].item == win_item!![7] && winfrag[0] != 0 && winfrag[1] == 0 && winfrag[2] == 0 && winfrag[0]+8 < step) winfrag[1] = step
                    if (slot.frame[2].item == win_item!![2] && slot.frame[5].item == win_item!![5] && slot.frame[8].item == win_item!![8] && winfrag[0] != 0 && winfrag[1] != 0 && winfrag[2] == 0 && winfrag[1]+8 < step) winfrag[2] = step

                    if (winfrag[0] != 0 && winfrag[1] == 0 && winfrag[2] == 0){
                        spin2()
                        spin3()
                    }else if (winfrag[0] != 0 && winfrag[1] != 0 && winfrag[2] == 0){
                        spin3()
                    }else if (winfrag[0] != 0 && winfrag[1] != 0 && winfrag[2] != 0) {
                        if (slot.stopeffect != null) {
                            if (slot.stopeffect!!.command != null) {
                                plugin.runCommand(slot.stopeffect!!.command!!, "", p, slot.slot_name, 0.0)
                            }
                            if (slot.stopeffect!!.particle != null) {
                                val par = slot.stopeffect!!.particle!!
                                val r = Random().nextDouble()
                                if (r <= par.chance!!) {
                                    slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                                }
                            }
                            if (slot.stopeffect!!.sound != null) {
                                val sound = slot.stopeffect!!.sound!!
                                p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                            }
                        }

                        if (slot.countgame > 0) {
                            slot.countgame--
                        }

                        if (slot.countgame == 0) {
                            slot.chancenum = 0

                            if (slot.changeeffect != null) {
                                if (slot.changeeffect!!.command != null) {
                                    plugin.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                                }
                                if (slot.changeeffect!!.particle != null) {
                                    val par = slot.changeeffect!!.particle!!
                                    val r = Random().nextDouble()
                                    if (r <= par.chance!!) {
                                        slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                                    }
                                }
                                if (slot.changeeffect!!.sound != null) {
                                    val sound = slot.changeeffect!!.sound!!
                                    p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                                }
                            }
                        }

                        hit()

                        Thread(Runnable {

                            plugin.locc.getConfig()!!.set("chancenum.${key}", slot.chancenum)
                            plugin.locc.getConfig()!!.set("countgame.${key}", slot.countgame)
                            plugin.locc.getConfig()!!.set("spincount.${key}", slot.spincount)
                            plugin.locc.saveConfig()

                        }).start()

                        slot.flag = false
                        slot.step = step
                        return
                    }else {
                        spin1()
                        spin2()
                        spin3()
                    }
                }else{

                    if (step < slot.step+wait+between){
                        spin2()
                        spin3()
                    }else if (step < slot.step+wait+(between+between1) || loseCheck()){
                        spin3()
                    }else {
                        if (slot.stopeffect != null) {
                            if (slot.stopeffect!!.command != null) {
                                plugin.runCommand(slot.stopeffect!!.command!!, "", p, slot.slot_name, 0.0)
                            }
                            if (slot.stopeffect!!.particle != null) {
                                val par = slot.stopeffect!!.particle!!
                                val r = Random().nextDouble()
                                if (r <= par.chance!!) {
                                    slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                                }
                            }
                            if (slot.stopeffect!!.sound != null) {
                                val sound = slot.stopeffect!!.sound!!
                                p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                            }
                        }

                        if (slot.countgame > 0) {
                            slot.countgame--
                        }

                        if (slot.countgame == 0) {
                            slot.chancenum = 0

                            if (slot.changeeffect != null) {
                                if (slot.changeeffect!!.command != null) {
                                    plugin.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                                }
                                if (slot.changeeffect!!.particle != null) {
                                    val par = slot.changeeffect!!.particle!!
                                    val r = Random().nextDouble()
                                    if (r <= par.chance!!) {
                                        slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                                    }
                                }
                                if (slot.changeeffect!!.sound != null) {
                                    val sound = slot.changeeffect!!.sound!!
                                    p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                                }
                            }
                        }

                        slot.step = step
                        lose()

                        plugin.locc.getConfig()!!.set("chancenum.${key}", slot.chancenum)
                        plugin.locc.getConfig()!!.set("countgame.${key}", slot.countgame)
                        plugin.locc.getConfig()!!.set("spincount.${key}", slot.spincount)
                        slot.flag = false

                        return
                    }
                }
            }
            sleep(sleep)
        }

    }

    @Synchronized
    fun loseCheck(): Boolean{
        var check = false
        for (j in slot.wining_item){
            for (i in j.value){
                if (slot.frame[0].item == i[0] && slot.frame[3].item == i[3] && slot.frame[6].item == i[6] && slot.frame[1].item == i[1] &&
                        slot.frame[4].item == i[4] && slot.frame[7].item == i[7] && slot.frame[2].item == i[2] && slot.frame[5].item == i[5] && slot.frame[8].item == i[8]) check = true
            }
        }
        return check
    }

    @Synchronized
    fun hit() {

        val v = VaultManager(plugin)

        p.sendMessage(prefix + "§e§lおめでとうございます！${win_name!!}§e§lです！")
        slot.win = "0"

        val totalprize = win_prize!!

        val s = MySQLSave(plugin, p, key, win, win_level, slot.price, totalprize, slot.chancenum, slot.countgame)
        s.start()

        object : BukkitRunnable(){

            override fun run() {
                if (slot.countreset[win]!!){

                    slot.spincount = 0

                    val sign = plugin.signloc[key]!!.world!!.getBlockAt(plugin.signloc[key]!!)

                    if (sign.state is Sign){

                        val a = sign.state as Sign
                        a.setLine(2, "§lcount:§a§l${slot.spincount}")

                        a.update()

                    }

                }
            }

        }.runTask(plugin)



        if (slot.changetable[win] != null){

            val ct = slot.changetable[win]

            val r = Random().nextDouble()

            var sum = 0.0

            for (t in 0 until ct!!.chance.size){

                sum += ct.chance[t]

                if (r < sum) {
                    slot.chancenum = ct.table[t]
                    slot.countgame = ct.game[t]
                    if (slot.changeeffect != null) {
                        if (slot.changeeffect!!.command != null) {
                            plugin.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null) {
                            val par = slot.changeeffect!!.particle!!
                            val ran = Random().nextDouble()
                            if (ran <= par.chance!!) {
                                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null) {
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                    break
                }

            }
        }

        if (slot.coindrop[win] != null) {
            if (slot.coindrop[win]!!) {

                var count = 0

                p.location.world!!.playSound(p.location, "slot.coin", 1f, 1f)

                object : BukkitRunnable(){

                    override fun run() {

                        if (count >= 20){
                            cancel()
                        }

                        val item = ItemStackA (Material.GOLD_NUGGET, 0, 1, "§6MSlotCoin+${count}").build()

                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[8].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[7].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[6].location, item)

                        count++

                    }

                }.runTaskTimer(plugin, 0, 1)
            }
        }

        if (win_command != null) {
            plugin.runCommand(win_command!!, win_name!!, p, slot.slot_name, totalprize)
        }

        v.deposit(p.uniqueId, totalprize)

        val sound = win_sound!!
        p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)

        val list = slot.chancewin[win]
        if (list != null) {
            val r = Random().nextInt(list.size)
            slot.win = list[r]
        }

        if (slot.win_particle[win] != null) {
            val par = slot.win_particle[win]!!
            val r = Random().nextDouble()
            if (r <= par.chance!!) {
                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
            }
        }

        if (slot.wincount.containsKey(win)) {
            slot.wincount[win] = slot.wincount[win]!! + 1
        } else slot.wincount[win] = 1

    }

    fun lose() {

        p.sendMessage("$prefix§c外れました")
        val s = MySQLSave(plugin, p, key, "0", -1, slot.price, 0.0, slot.chancenum, slot.countgame)
        s.start()

    }

    @Synchronized
    fun spin1(){
        slot.frame[0].setItem(reel1[(step+1)%reel1.size])
        slot.frame[3].setItem(reel1[step%reel1.size])
        slot.frame[6].setItem(reel1[(step+reel1.size-1)%reel1.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin2(){
        slot.frame[1].setItem(reel2[(step+1)%reel2.size])
        slot.frame[4].setItem(reel2[step%reel2.size])
        slot.frame[7].setItem(reel2[(step+reel2.size-1)%reel2.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin3(){
        slot.frame[2].setItem(reel3[(step+1)%reel3.size])
        slot.frame[5].setItem(reel3[step%reel3.size])
        slot.frame[8].setItem(reel3[(step+reel3.size-1)%reel3.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }
}

class DefaultFreeze (val slot: Man10Slot_15.SlotInformation, val pl: Man10Slot_15, val win: String, val frame: MutableList<ItemFrame>, val key: String, val p: Player): Thread() {

    val reel1 = slot.reel1
    val reel2 = slot.reel2
    val reel3 = slot.reel3

    var step = 0

    val r = Random().nextInt(slot.wining_item[win]!!.size)

    var item = slot.freeze_item[win]!!
    var win_prize: Double = slot.wining_prize[win]!!
    var win_name: String = slot.wining_name[win]!!
    var win_command: MutableList<String>? = slot.wining_command[win]
    var win_sound: Man10Slot_15.Sound = slot.wining_sound[win]!!

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    val sleep = slot.sleep

    override fun run() {

        var sound1 = false
        var sound2 = false

        val fstep = slot.freeze_step[win]!!

        while(true){

            step++

            if (step == 10 && slot.freeze_command[win] != null){
                pl.runCommand(slot.freeze_command[win]!!, win_name, p, slot.slot_name, win_prize)
            }

            if (step < fstep) {
                if (step == 10) p.location.world!!.playSound(p.location, slot.freeze_pres[win]!!.sound, slot.freeze_pres[win]!!.volume, slot.freeze_pres[win]!!.pitch)
                spin1()
                spin2()
                spin3()
            }else if (step >= fstep && slot.frame[3].item != item[0]) {
                spin1()
                spin2()
                spin3()
            }else if (step >= fstep && slot.frame[3].item == item[0] && slot.frame[4].item != item[1]){
                if (!sound1){
                    sound1 = true
                    p.location.world!!.playSound(p.location, slot.freeze_stop1s[win]!!.sound, slot.freeze_stop1s[win]!!.volume, slot.freeze_stop1s[win]!!.pitch)
                }
                spin2()
                spin3()
            }else if (step >= fstep && slot.frame[3].item == item[0] && slot.frame[4].item == item[1] && slot.frame[5].item != item[2]){
                if (!sound2){
                    sound2 = true
                    p.location.world!!.playSound(p.location, slot.freeze_stop2s[win]!!.sound, slot.freeze_stop2s[win]!!.volume, slot.freeze_stop2s[win]!!.pitch)
                }
                spin3()
            }else if (step >= fstep && slot.frame[3].item == item[0] && slot.frame[4].item == item[1] && slot.frame[5].item == item[2]){

                p.location.world!!.playSound(p.location, slot.freeze_stop3s[win]!!.sound, slot.freeze_stop3s[win]!!.volume, slot.freeze_stop3s[win]!!.pitch)

                if (slot.countgame > 0) {
                    slot.countgame--
                }

                if (slot.countgame == 0) {
                    slot.chancenum = 0

                    if (slot.changeeffect != null) {
                        if (slot.changeeffect!!.command != null) {
                            pl.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null) {
                            val par = slot.changeeffect!!.particle!!
                            val r = Random().nextDouble()
                            if (r <= par.chance!!) {
                                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null) {
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                }

                hit()

                pl.locc.getConfig()!!.set("chancenum.${key}", slot.chancenum)
                pl.locc.getConfig()!!.set("countgame.${key}", slot.countgame)
                pl.locc.getConfig()!!.set("spincount.${key}", slot.spincount)
                pl.locc.saveConfig()

                slot.flag = false
                slot.freeze = false
                break
            }
            sleep(sleep)
        }

    }

    @Synchronized
    fun spin1(){
        slot.frame[0].setItem(reel1[(step+1)%reel1.size])
        slot.frame[3].setItem(reel1[step%reel1.size])
        slot.frame[6].setItem(reel1[(step+reel1.size-1)%reel1.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin2(){
        slot.frame[1].setItem(reel2[(step+1)%reel2.size])
        slot.frame[4].setItem(reel2[step%reel2.size])
        slot.frame[7].setItem(reel2[(step+reel2.size-1)%reel2.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin3(){
        slot.frame[2].setItem(reel3[(step+1)%reel3.size])
        slot.frame[5].setItem(reel3[step%reel3.size])
        slot.frame[8].setItem(reel3[(step+reel3.size-1)%reel3.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun hit() {

        val v = VaultManager(pl)

        p.sendMessage(prefix + "§e§lおめでとうございます！${win_name}§e§lです！")
        slot.win = "0"

        val totalprize = win_prize

        val s = MySQLSave(pl, p, key, win, slot.wining_level[win]!!, slot.price, totalprize, slot.chancenum, slot.countgame)
        s.start()

        object : BukkitRunnable(){

            override fun run() {
                if (slot.countreset[win]!!){

                    slot.spincount = 0

                    val sign = pl.signloc[key]!!.world!!.getBlockAt(pl.signloc[key]!!)

                    if (sign.state is Sign){

                        val a = sign.state as Sign

                        a.setLine(2, "§lcount:§a§l${slot.spincount}")

                        a.update()

                    }

                }
            }

        }.runTask(pl)



        if (slot.changetable[win] != null){

            val ct = slot.changetable[win]

            val r = Random().nextDouble()

            var sum = 0.0

            for (t in 0 until ct!!.chance.size){

                sum += ct.chance[t]

                if (r < sum) {
                    slot.chancenum = ct.table[t]
                    slot.countgame = ct.game[t]
                    if (slot.changeeffect != null) {
                        if (slot.changeeffect!!.command != null) {
                            pl.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null) {
                            val par = slot.changeeffect!!.particle!!
                            val ran = Random().nextDouble()
                            if (ran <= par.chance!!) {
                                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null) {
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                    break
                }

            }
        }

        if (slot.coindrop[win] != null) {
            if (slot.coindrop[win]!!) {

                var count = 0

                p.location.world!!.playSound(p.location, "slot.coin", 1f, 1f)

                object : BukkitRunnable(){

                    override fun run() {

                        if (count >= 20){
                            cancel()
                        }

                        val item = ItemStackA (Material.GOLD_NUGGET, 0, 1, "§6MSlotCoin+${count}").build()

                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[8].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[7].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[6].location, item)

                        count++

                    }

                }.runTaskTimer(pl, 0, 1)
            }
        }

        if (win_command != null) {
            pl.runCommand(win_command!!, win_name, p, slot.slot_name, totalprize)
        }

        v.deposit(p.uniqueId, totalprize)

        val sound = win_sound
        p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)

        val list = slot.chancewin[win]
        if (list != null) {
            val r = Random().nextInt(list.size)
            slot.win = list[r]
        }

        if (slot.win_particle[win] != null) {
            val par = slot.win_particle[win]!!
            val r = Random().nextDouble()
            if (r <= par.chance!!) {
                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
            }
        }

        if (slot.wincount.containsKey(win)) {
            slot.wincount[win] = slot.wincount[win]!! + 1
        } else slot.wincount[win] = 1

    }

}

class SlowFreeze(val slot: Man10Slot_15.SlotInformation, val pl: Man10Slot_15, val win: String, val frame: MutableList<ItemFrame>, val key: String, val p: Player): Thread() {

    val reel1 = slot.reel1
    val reel2 = slot.reel2
    val reel3 = slot.reel3

    var step = 0

    var win_prize: Double = slot.wining_prize[win]!!
    var win_name: String = slot.wining_name[win]!!
    var win_command: MutableList<String>? = slot.wining_command[win]
    var win_sound: Man10Slot_15.Sound = slot.wining_sound[win]!!

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    var sleep = slot.sleep

    override fun run() {

        var sound1 = false
        var sound2 = false

        val fstep = slot.freeze_step[win]!!

        val item = slot.freeze_item[win]!!

        while(true){

            step++

            if (step < 120) {
                spin1()
                spin2()
                spin3()
            }else if (step == 120) {
                p.location.world!!.playSound(p.location, slot.freeze_pres[win]!!.sound, slot.freeze_pres[win]!!.volume, slot.freeze_pres[win]!!.pitch)

                for (f in slot.frame){
                    f.setItem(ItemStack(Material.AIR))
                }

                if (slot.freeze_command[win] != null){
                    pl.runCommand(slot.freeze_command[win]!!, win_name, p, slot.slot_name, win_prize)
                }

            }else if (step == 170){
                sleep = slot.freeze_sleep[win]!!
            }else if (step in 171..fstep+170) {
                spin1()
                spin2()
                spin3()
            }else if (step >= fstep+170 && slot.frame[3].item != item[0]) {
                spin1()
                spin2()
                spin3()
            }else if (step >= fstep+170 && slot.frame[3].item == item[0] && slot.frame[4].item != item[1]){
                if (!sound1){
                    sound1 = true
                    p.location.world!!.playSound(p.location, slot.freeze_stop1s[win]!!.sound, slot.freeze_stop1s[win]!!.volume, slot.freeze_stop1s[win]!!.pitch)
                }
                spin2()
                spin3()
            }else if (step >= fstep+170 && slot.frame[3].item == item[0] && slot.frame[4].item == item[1] && slot.frame[5].item != item[2]){
                if (!sound2){
                    sound2 = true
                    p.location.world!!.playSound(p.location, slot.freeze_stop2s[win]!!.sound, slot.freeze_stop2s[win]!!.volume, slot.freeze_stop2s[win]!!.pitch)
                }
                spin3()
            }else if (step >= fstep+170 && slot.frame[3].item == item[0] && slot.frame[4].item == item[1] && slot.frame[5].item == item[2]){

                p.location.world!!.playSound(p.location, slot.freeze_stop3s[win]!!.sound, slot.freeze_stop3s[win]!!.volume, slot.freeze_stop3s[win]!!.pitch)

                if (slot.countgame > 0) {
                    slot.countgame--
                }

                if (slot.countgame == 0) {
                    slot.chancenum = 0

                    if (slot.changeeffect != null) {
                        if (slot.changeeffect!!.command != null) {
                            pl.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null) {
                            val par = slot.changeeffect!!.particle!!
                            val r = Random().nextDouble()
                            if (r <= par.chance!!) {
                                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null) {
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                }

                hit()

                pl.locc.getConfig()!!.set("chancenum.${key}", slot.chancenum)
                pl.locc.getConfig()!!.set("countgame.${key}", slot.countgame)
                pl.locc.getConfig()!!.set("spincount.${key}", slot.spincount)
                pl.locc.saveConfig()

                slot.flag = false
                slot.freeze = false
                break
            }
            sleep(sleep)
        }

    }

    @Synchronized
    fun spin1(){
        slot.frame[0].setItem(reel1[(step+1)%reel1.size])
        slot.frame[3].setItem(reel1[step%reel1.size])
        slot.frame[6].setItem(reel1[(step+reel1.size-1)%reel1.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin2(){
        slot.frame[1].setItem(reel2[(step+1)%reel2.size])
        slot.frame[4].setItem(reel2[step%reel2.size])
        slot.frame[7].setItem(reel2[(step+reel2.size-1)%reel2.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun spin3(){
        slot.frame[2].setItem(reel3[(step+1)%reel3.size])
        slot.frame[5].setItem(reel3[step%reel3.size])
        slot.frame[8].setItem(reel3[(step+reel3.size-1)%reel3.size])
        if (slot.spiningsound != null){
            p.location.world!!.playSound(p.location, slot.spiningsound!!.sound, slot.spiningsound!!.volume, slot.spiningsound!!.pitch)
        }
    }

    @Synchronized
    fun hit() {

        val v = VaultManager(pl)

        p.sendMessage(prefix + "§e§lおめでとうございます！${win_name}§e§lです！")
        slot.win = "0"

        val totalprize = win_prize

        val s = MySQLSave(pl, p, key, win, slot.wining_level[win]!!, slot.price, totalprize, slot.chancenum, slot.countgame)
        s.start()

        object : BukkitRunnable(){

            override fun run() {
                if (slot.countreset[win]!!){

                    slot.spincount = 0

                    val sign = pl.signloc[key]!!.world!!.getBlockAt(pl.signloc[key]!!)

                    if (sign.state is Sign){

                        val a = sign.state as Sign

                        a.setLine(2, "§lcount:§a§l${slot.spincount}")

                        a.update()

                    }

                }
            }

        }.runTask(pl)

        if (slot.changetable[win] != null){

            val ct = slot.changetable[win]

            val r = Random().nextDouble()

            var sum = 0.0

            for (t in 0 until ct!!.chance.size){

                sum += ct.chance[t]

                if (r < sum) {
                    slot.chancenum = ct.table[t]
                    slot.countgame = ct.game[t]
                    if (slot.changeeffect != null) {
                        if (slot.changeeffect!!.command != null) {
                            pl.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null) {
                            val par = slot.changeeffect!!.particle!!
                            val ran = Random().nextDouble()
                            if (ran <= par.chance!!) {
                                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null) {
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                    break
                }

            }
        }

        if (slot.coindrop[win] != null) {
            if (slot.coindrop[win]!!) {

                var count = 0

                p.location.world!!.playSound(p.location, "slot.coin", 1f, 1f)

                object : BukkitRunnable(){

                    override fun run() {

                        if (count >= 20){
                            cancel()
                        }

                        val item = ItemStackA (Material.GOLD_NUGGET, 0, 1, "§6MSlotCoin+${count}").build()

                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[8].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[7].location, item)
                        slot.frame[6].location.world!!.dropItemNaturally(slot.frame[6].location, item)

                        count++

                    }

                }.runTaskTimer(pl, 0, 1)
            }
        }

        if (win_command != null) {
            pl.runCommand(win_command!!, win_name, p, slot.slot_name, totalprize)
        }

        v.deposit(p.uniqueId, totalprize)

        val sound = win_sound
        p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)

        val list = slot.chancewin[win]
        if (list != null) {
            val r = Random().nextInt(list.size)
            slot.win = list[r]
        }

        if (slot.win_particle[win] != null) {
            val par = slot.win_particle[win]!!
            val r = Random().nextDouble()
            if (r <= par.chance!!) {
                slot.frame[4].location.world!!.spawnParticle(par.par!!, slot.frame[4].location, par.count!!)
            }
        }

        if (slot.wincount.containsKey(win)) {
            slot.wincount[win] = slot.wincount[win]!! + 1
        } else slot.wincount[win] = 1

    }

}
