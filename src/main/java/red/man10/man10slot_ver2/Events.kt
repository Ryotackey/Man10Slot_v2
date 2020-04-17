package red.man10.man10slot_ver2

import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitRunnable
import red.man10.man10vaultapiplus.Man10VaultAPI
import red.man10.man10vaultapiplus.enums.TransactionCategory
import red.man10.man10vaultapiplus.enums.TransactionType
import java.util.*

class Events(val plugin: Man10Slot_ver2): Listener {

    @EventHandler
    fun onClick(e: PlayerInteractEvent) {

        val p = e.player

        if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.LEFT_CLICK_AIR) return
        val b = e.clickedBlock

        if (plugin.leverloc.containsKey(b.location)){

            if (!p.hasPermission("mslot.use")){
                p.sendMessage(plugin.prefix + "§c権限がありません")
                return
            }

            if (!plugin.enable){
                p.sendMessage("${plugin.prefix}§c現在使用できません")
                e.isCancelled = true
                return
            }

            val v = Man10VaultAPI("Man10Slot")

            val key = plugin.leverloc[b.location]!!
            val slot = plugin.slotmap[key]!!

            if (slot.permission != null){
                if (!p.hasPermission(slot.permission)){
                    p.sendMessage(plugin.prefix + "§c権限がありません")
                    return
                }
            }

            if (slot.spin){
                e.isCancelled = true
                p.sendMessage(plugin.prefix + "§c今回っています")
                return
            }

            if (v.getBalance(p.uniqueId) < slot.price){
                p.sendMessage(plugin.prefix + "§cお金が足りません")
                return
            }

            v.transferMoneyPlayerToCountry(p.uniqueId, slot.price, TransactionCategory.GAMBLE, TransactionType.BET, "slot bet")

            val map = HashMap<Double, String>()

            for (i in slot.wining_chance){

                map[i.value[slot.chancenum]] = i.key

            }

            val winlist = plugin.mapSort(map)

            var win = "0"

            for (i in winlist){

                val ran = Random().nextDouble()

                if (i.key >= ran) win = i.value
            }

            if (slot.win != "0"){
                win = slot.win
            }

            var freezeflag: String? = null

            if (win != "0"){
                if (slot.wining_freeze.containsKey(win)) {
                    val r = Random().nextDouble()
                    if (r < slot.wining_freeze[win]!!) {
                        freezeflag = slot.freeze_kind[win]
                        slot.freeze = true
                    }
                }
            }

            slot.spin = true

            slot.p = p

            slot.spincount++

            if (slot.spineffect != null){
                if (slot.spineffect!!.command != null){
                    plugin.runCommand(slot.spineffect!!.command!!, "", p, slot.slot_name, 0.0)
                }
                if (slot.spineffect!!.particle != null){
                    val par = slot.spineffect!!.particle!!
                    val r = Random().nextDouble()
                    if (r <= par.chance!!) {
                        plugin.frameloc[key]!![4].world!!.spawnParticle(par.par!!, plugin.frameloc[key]!![4], par.count!!)
                    }
                }
                if (slot.spineffect!!.sound != null){
                    val sound = slot.spineffect!!.sound!!
                    p.location.world!!.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                }
            }

            val spin = SpinProcess(plugin, p, win, slot, key, freezeflag)
            spin.start()

        }
    }

    @EventHandler
    fun hanging(e: HangingPlaceEvent){

        if (e.entity.type != EntityType.ITEM_FRAME){
            return
        }

        val p = e.player!!
        val b = e.entity

        if (plugin.frameselectmap.containsKey(p)){

            val key = plugin.frameselectmap[p]!![0]

            val loc = b.location.block.location

            p.sendMessage(plugin.prefix + "§a登録しました")

            when(plugin.frameselectmap[p]!!.size){

                1->{
                    plugin.frameloc[key] = mutableListOf(loc)
                    plugin.frameselectmap[p]!!.add(key)
                }

                2,3,4,5,6,7,8->{
                    for (i in plugin.frameloc.entries){
                        if (i.key != key) continue
                        i.value.add(loc)
                    }
                    plugin.frameselectmap[p]!!.add(key)
                }

                9->{
                    for (i in plugin.frameloc.entries){
                        if (i.key != key) continue
                        i.value.add(loc)
                    }
                    plugin.frameselectmap.remove(p)
                    p.sendMessage(plugin.prefix + "§a次にレバーを設置してください")
                    plugin.leverselect[p] = key
                }
            }
        }
    }

    @EventHandler
    fun blockPlace(e: BlockPlaceEvent){

        val p = e.player
        val b = e.block

        if (plugin.leverselect.containsKey(p)){

            if (b.type != Material.LEVER)return

            val key = plugin.leverselect[p]!!

            plugin.leverloc[b.location] = key
            plugin.leverselect.remove(p)

            if (plugin.frameloc.containsKey(key)) {
                for (loca in plugin.frameloc[key]!!) {
                    for (i in loca.chunk.entities) {
                        if (i.location.block.location == loca && i.type == EntityType.ITEM_FRAME) {
                            plugin.slotmap[key]!!.frame.add(i as ItemFrame)
                        }

                    }

                }
            }

            p.sendMessage(plugin.prefix + "§a登録が完了しました")

        }

    }

    @EventHandler
    fun onDrop(e: ItemSpawnEvent){
        val i = e.entity

        if (!i.itemStack.hasItemMeta())return
        if (!i.itemStack.itemMeta.displayName.contains("§6MSlotCoin".toRegex()))return

        i.pickupDelay = 5000

        object : BukkitRunnable(){

            override fun run() {
                i.remove()
            }

        }.runTaskLaterAsynchronously(plugin, 50)

    }

}