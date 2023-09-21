package jojo.game.dao

import jojo.game.entity.Player
import jojo.game.entity.Room
import jojo.game.global.GameGlobal
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Pipeline


const val ROOM_KEY = "holdem:room:%s"
const val PLAYER_KEY = "holdem:player:%s"

object RedisTool {

    val jedis = JedisPooled("localhost", 6379)

    fun cacheRoom(room: Room) {
        jedis.hset(ROOM_KEY.format(room.id), room.snapshot())
        GameGlobal.roomMap[room.id] = room;
    }

    fun delRoom(id: String) {
        jedis.del(ROOM_KEY.format(id))
        GameGlobal.roomMap.remove(id);
    }

    private fun list(keyWildcard: String): Map<String, Map<String, String>> {
        val keys = jedis.keys(keyWildcard)

        // Use pipelining to fetch hashmaps for all keys
        val pipeline: Pipeline = jedis.pipelined()
        val allResponses = keys.map { key ->
            key to pipeline.hgetAll(key)
        }
        pipeline.sync()

        val allHashmaps = allResponses.map { (key, response) ->
            key to response.get()
        }.toMap()

        return allHashmaps;
    }

    fun searchRoom(keyword: String): List<Room> {
        val result = list(ROOM_KEY.format("*"))
        return result.values.map { Room.fromCache(it) }.filter { it.name.contains(keyword) }
    }

    fun getRoom(id: String): Room? {
        val map = jedis.hgetAll(ROOM_KEY.format(id))
        if (map.isEmpty()) return null

        return Room.fromCache(map)
    }

    fun initRooms() {
        val result = list(ROOM_KEY.format("*"))
        GameGlobal.roomMap = result.values.map { Room.fromCache(it) }.associateBy { it.id }.toMutableMap()
    }

    fun initPlayers() {
        val result = list(PLAYER_KEY.format("*"))
        GameGlobal.playerMap = result.values.map { Player.fromCache(it) }.associateBy { it.id }.toMutableMap()
    }

    fun cachePlayer(player: Player) {
        jedis.hset(ROOM_KEY.format(player.id), player.snapshot())
        GameGlobal.playerMap[player.id] = player
    }

    fun getPlayer(id: String): Player {
        val map = jedis.hgetAll(PLAYER_KEY.format(id))
        return Player.fromCache(map)
    }

    fun getPlayers(room: Room, playerIds: List<String>): List<Player> {
        val keys = playerIds.map { PLAYER_KEY.format(it) }

        // Use pipelining to fetch hashmaps for all keys
        val pipeline: Pipeline = jedis.pipelined()
        val allResponses = keys.map { key ->
            key to pipeline.hgetAll(key)
        }
        pipeline.sync()

        val players = allResponses.associate { (key, response) ->
            key to response.get()
        }.filter { (_, v) -> v.isNotEmpty() }.map { (_, v) ->
            // todo
            var player = GameGlobal.playerMap[v.getValue("id")]
            if (player == null) {
                player = Player()
            }
            player.name = v.getValue("name")
            player.id = v.getValue("id")
            player.room = room
            player.nextPlayer = GameGlobal.playerMap[v.getValue("nextPlayer")]
            player.isFold = v.getValue("isFold").toBoolean()
            player
        }

        return players;
    }
}