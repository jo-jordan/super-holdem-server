package jojo.game.dao

import jojo.game.entity.Room
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RedisToolTest {
    @Test
    fun cacheRoom_should_work() {
        val room = Room()
        RedisTool.cacheRoom(room)

        val r = RedisTool.getRoom(room.id)

        assertEquals(r?.id, room.id)
    }

    @Test
    fun delRoom_should_work() {
        RedisTool.delRoom("")
    }

}

