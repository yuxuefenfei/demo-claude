package cn.wangwenzhu.tankwar.service;

import cn.wangwenzhu.tankwar.config.GameConfig;
import cn.wangwenzhu.tankwar.model.*;
import cn.wangwenzhu.tankwar.model.Bullet;
import cn.wangwenzhu.tankwar.model.Direction;
import cn.wangwenzhu.tankwar.model.PowerUp;
import cn.wangwenzhu.tankwar.model.Tank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    @Autowired
    private GameConfig gameConfig;

    // Package-private setter for testing
    void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    private final Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameRoom createGameRoom(String roomId) {
        GameRoom room = new GameRoom(roomId, gameConfig);
        gameRooms.put(roomId, room);
        return room;
    }

    public GameRoom joinGameRoom(String roomId, String playerId, WebSocketSession session) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null && room.getPlayers().size() < gameConfig.getMaxPlayers()) {
            Player player = new Player(playerId, session);
            players.put(playerId, player);
            room.addPlayer(player);

            // Create tank for player
            Tank tank = new Tank(100, 100, playerId, true);
            room.addTank(tank);

            return room;
        }
        return null;
    }

    public void leaveGameRoom(String roomId, String playerId) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            room.removePlayer(playerId);
            room.removeTank(playerId);
            players.remove(playerId);

            if (room.getPlayers().isEmpty()) {
                gameRooms.remove(roomId);
            }
        }
    }

    public void handlePlayerInput(String roomId, String playerId, GameInput input) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            Tank tank = room.getTank(playerId);
            if (tank != null && tank.isAlive()) {
                switch (input.getAction()) {
                    case MOVE:
                        tank.move(input.getDirection(), 0.016, room.getGameMap().getWidth(), room.getGameMap().getHeight());
                        if (room.getGameMap().checkCollision(tank)) {
                            // Revert movement if collision
                            tank.move(input.getDirection().opposite(), 0.016, room.getGameMap().getWidth(), room.getGameMap().getHeight());
                        }
                        break;
                    case SHOOT:
                        Bullet bullet = tank.shoot();
                        if (bullet != null) {
                            room.addBullet(bullet);
                        }
                        break;
                    case STOP:
                        // Handle stop action
                        break;
                }
            }
        }
    }

    @Scheduled(fixedRate = 16) // 60 FPS
    public void updateGameState() {
        for (GameRoom room : gameRooms.values()) {
            updateRoom(room);
        }
    }

    private void updateRoom(GameRoom room) {
        // Update bullets and collect bullets to remove
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : room.getBullets()) {
            bullet.update(0.016, room.getGameMap().getWidth(), room.getGameMap().getHeight());

            if (!bullet.isAlive()) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // Check bullet collision with walls
            if (room.getGameMap().checkCollision(bullet)) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // Check bullet collision with tanks
            for (Tank tank : room.getTanks().values()) {
                if (tank.isAlive() && !bullet.getOwnerId().equals(tank.getPlayerId()) &&
                    bullet.collidesWith(tank)) {
                    tank.takeDamage(bullet.getDamage());
                    bulletsToRemove.add(bullet);
                    break;
                }
            }
        }

        // Remove bullets outside of iteration
        room.getBullets().removeAll(bulletsToRemove);

        // Update AI tanks
        updateAITanks(room);

        // Check power-up collisions
        for (Tank tank : room.getTanks().values()) {
            if (tank.isAlive()) {
                PowerUp powerUp = room.getGameMap().checkPowerUpCollision(tank);
                if (powerUp != null) {
                    powerUp.applyEffect(tank);
                }
            }
        }

        // Remove dead tanks
        room.getTanks().values().removeIf(tank -> !tank.isAlive());

        // Remove expired power-ups
        room.getGameMap().getPowerUps().removeIf(PowerUp::isExpired);
    }

    private void updateAITanks(GameRoom room) {
        for (Tank tank : room.getTanks().values()) {
            if (!tank.isPlayer() && tank.isAlive()) {
                // Simple AI logic
                if (Math.random() < 0.02) { // 2% chance to change direction
                    Direction randomDirection = Direction.values()[(int)(Math.random() * 4)];
                    tank.move(randomDirection, 0.016, room.getGameMap().getWidth(), room.getGameMap().getHeight());
                }

                if (Math.random() < 0.01) { // 1% chance to shoot
                    Bullet bullet = tank.shoot();
                    if (bullet != null) {
                        room.addBullet(bullet);
                    }
                }
            }
        }
    }

    public GameState getGameState(String roomId) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            return new GameState(room);
        }
        return null;
    }

    public Collection<GameRoom> getAllRooms() {
        return gameRooms.values();
    }
}