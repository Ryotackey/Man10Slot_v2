package red.man10.man10slot_ver2

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.sql.ResultSet
import java.util.concurrent.ExecutionException

class MySQLCreate(private val pl: Man10Slot_ver2): Thread() {

    override fun run() {

        val mysql = MySQLManager(pl, "man10slot")

        mysql.execute("CREATE TABLE IF NOT EXISTS `slot_record` (\n" +
                "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `user_name` VARCHAR(45) NULL,\n" +
                "  `user_uuid` TEXT NULL,\n" +
                "  `slot_name` VARCHAR(45) NULL,\n" +
                "  `win_name` VARCHAR(45) NULL,\n" +
                "  `win_level` VARCHAR(45) NULL,\n" +
                "  `inmoney` DOUBLE NULL,\n" +
                "  `outmoney` DOUBLE NULL,\n" +
                "  `table_num` INT NULL,\n" +
                "  `table_count` INT NULL,\n" +
                "  `date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`));\n")
        mysql.close()
    }
}

class MySQLSave(val pl: Man10Slot_ver2, val p: Player,val slot_name: String,val win_name: String,val win_level: Int,val inmoney: Double,val outmoney: Double,val table_num: Int,val table_count: Int): Thread() {

    override fun run() {

        val mysql = MySQLManager(pl, "man10slot")

        mysql.execute("INSERT INTO `slot_record` (`user_name`, `user_uuid`, `slot_name`, `win_name`, `win_level`, `inmoney`, `outmoney`, `table_num`, `table_count`) VALUES ('${p.name}', '${p.uniqueId}', '$slot_name', '$win_name', '$win_level', '$inmoney', '$outmoney', '$table_num', '$table_count');")


        mysql.close()
    }

}

class getData(val pl: Man10Slot_ver2, val key: String, val sender: CommandSender): Thread(){

    override fun run() {

        val slot = pl.slotmap[key]!!

        val mysql = MySQLManager(pl, "man10slot")

        var countrs: ResultSet? = null

        try {
            countrs = mysql.query("SELECT count(1) FROM slot_record WHERE slot_name='${key}';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (countrs==null){
            Bukkit.getServer().consoleSender.sendMessage("${pl.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        countrs.first()

        val count = countrs.getInt("count(1)")

        var sum: ResultSet? = null

        try {
            sum = mysql.query("SELECT SUM(inmoney), SUM(outmoney) FROM slot_record WHERE slot_name='${key}';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (sum==null){
            Bukkit.getServer().consoleSender.sendMessage("${pl.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        countrs.close()

        sum.first()

        val insum = sum.getDouble("SUM(inmoney)")
        val outsum = sum.getDouble("SUM(outmoney)")

        val wincount = HashMap<String, Int>()

        for (i in slot.wining_chance){

            var rs: ResultSet? = null

            try {
                rs = mysql.query("SELECT count(1) FROM slot_record WHERE slot_name='${key}' AND win_name='${i.key}';")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            if (rs==null){
                Bukkit.getServer().consoleSender.sendMessage("${pl.prefix}§cMysqlデータベースに接続できないため失敗しました")
                return
            }

            rs.first()

            wincount[i.key] = rs.getInt("count(1)")

        }

        sum.close()

        mysql.close()

        sender.sendMessage("§a§l--------§b§l${slot.slot_name}§a§l--------")
        sender.sendMessage("§e回された回数: §f${count}回")
        sender.sendMessage("")
        sender.sendMessage("§a<役の当たり数>")
        for (i in wincount){
            sender.sendMessage("§e${i.key}: §f${i.value}回")
        }
        sender.sendMessage("")
        sender.sendMessage("§e総出金額: §f ${outsum} §e/ 総入金額: §f${insum}")
        sender.sendMessage("§e還元率: §f${Math.round(outsum/insum*10000).toDouble()/100.0}%")

    }

}



