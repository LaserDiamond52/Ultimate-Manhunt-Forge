<img width="480" height="480" alt="ultimate_manhunt" src="https://github.com/user-attachments/assets/c906ce9a-594b-4806-b0e2-23df2d4c7c6a" />

## About

Ultimate Manhunt is a Minecraft Forge mod for version 1.20.1/1.21.1 containing all the features you'd need for starting your own manhunt game. 

In a manhunt game, there are hunters and speed runners. The hunters must elimate the speedrunner before the speed runner kills the Ender Dragon.

**Note: If no manhunt is active, the Ender Dragon is immune to all damage. The Ender Dragon can only be damaged while a manhunt game is active.**

## Running a Manhunt Game:

If you are here to find out how to quickly set up a manhunt game, or need a refresher, here are the steps to running a simple Manhunt game with this mod:

1. Set some players to be hunters using ```/ultimate_manhunt roles current <player> hunter``` (add a true/false argument if you want to specify whether this player is a buffed hunter)
2. Set any necessary gamerules, options, etc. (refer to the 'Commands' section at the bottom of this README.md)
3. Run ```/ultimate_manhunt gameState start```

For more options on commands and how you can customize your manhunt game, refer to the 'Commands' section at the bottom of this README.md.

## Features

**Ultimate Manhunt contains the following features:**

**Speed Runners:**

- Head-start timers for speed runners
- Grace-period timers/protection for speed runners (to prevent spawn-killing)
- Adjustable life counter for speed runners (from 1 - 99)
- Post-elimination speed runner spawn  options (if there are multiple speed runners). Speed runners can respawn as hunters or spectators.
- **Wind Torch**: Exclusive knockback item for speed runners with 50 uses.

<img width="132" height="191" alt="image" src="https://github.com/user-attachments/assets/bb8d7395-65b4-400c-be20-ac12c0dcf79d" />


- Red Vignetting and heart-beating for speed runners when hunters get within 50 blocks of a speed runner. The Prowler theme will also play. While a speed runner has this on their screen, any death will take a life.

<img width="1920" height="1017" alt="hunter_detection_tint-png" src="https://github.com/user-attachments/assets/39ec7392-057e-46f8-9b19-125b8a17db69" />


**Hunters:**

- Built-in player tracker for hunters. There is a 3D player tracker that hunters can use to cycle between available speed runners to track.
<img width="1887" height="926" alt="image" src="https://github.com/user-attachments/assets/ab0c2dbf-ad52-44cf-b8da-e4c5a67e3564" />

- Hunters can be declared as buffed hunters, and can be given buffs for:
  - Armor points
  - Health
  - Attack Damage
  - Movement Speed
  - Movement Efficiency (1.21.1)
  - Water Movement Efficiency (1.21.1)
  - Mining Efficiency (1.21.1)
  - Submerged Mining Efficiency (1.21.1)
  - Option for permanent saturation
  - Passive health regeneration

**Misc:**

- Adjustable timers for speed runner grace period/headstart
- Adjustable hunter buffs
- Friendly fire options
- Player role options for player joining mid-manhunt
- **Hardcore Option:** By default, only deaths from a hunter will count as a life (excluding the red vignetting effect, which ignores this). Setting the manhunt game to hardcore will count any speed runner death as a life, and no grace period/spawn protection is present.
- Pausing/Resuming features. During a pause, speed runners cannot lose lives, the directional player tracker for hunters disappears, and the Ender Dragon is invulnerable. Resuming simply continues the game.

## Game Profiles/Hunter Buff Profiles:

**Game Profiles:**

Since there are a lot of settings for this mod, you will also be able to save set game rules to a ```.json``` file in your game directory under ```/ultimate_manhunt/game_profiles```. The following settings are saved in a Game Profile ```.json```:
- Hunter grace period (speed runner headstart)
- Speed runner grace period
- Friendly fire
- Wind Torch usage
- Buffed hunters on speed runner elimination (only active if speed runners turn into hunters after losing their last life)
- Max speed runner lives
- Player role options for joining mid-game and after speed runner elimination

**Hunter Buff Profiles**

Hunter buffs also have a decent amount of options, and are saved in a ```.json``` file in your game directory under ```/ultimate_manahunt/hunter_buffs```. All values and modifiers for buffs are saved in a Hunter Buffs ```.json```.

## Commands

All arguments for settings/options for running a Manhunt game can be accseed from the command ```/ultaimte_manhunt``` in-game. Here are all the possible commands

**Game Profiles:**
- ```/ultimate_manhunt gameProfiles <game_profile_name> (load|save|delete)``` - Main command for saving, loading, and deleting game profiles.

**Game Rules:**
- ```/ultimate_manhunt gamerule allowWindTorches <false|true>``` - Enables/disables Wind Torches.
- ```/ultimate_manhunt gamerule buffedHuntersOnFinalDeath <false|true>``` - If speed runners turn into hunters after losing their final life, this determines if they become normal or buffed hunters.
- ```/ultimate_manhunt gamerule setFriendlyFire <false|true>``` - Determines if friendly fire is enabled.
- ```/ultimate_manhunt gamerule setHardcore <false|true>``` - Sets the manhunt game to hardcore. This **DOES NOT** change the world to hardcore!

**Game State (This is used to start/pause/resume/end the game!)**
- ```/ultimate_manhunt gameState pause``` - Pauses the manhunt game
- ```/ultimate_manhunt gameState resume``` - Resumes the manhunt game
- ```/ultimate_manhunt gameState start``` - Starts the manhunt game
- ```/ultimate_manhunt gameState end``` - Ends the manhunt game

**Grace Period:**
- ```/ultimate_manhunt gracePeriod hunter <durationTicks>``` - Sets the hunter 'grade period' (speed runner headstart)
- ```/ultimate_manhunt gracePeriod speed_runner <durationTicks>``` - Sets the grace period/protection duration after a speed runner loses a life.

**Hunter Buffs:**
- ```/ultimate_manhunt getBuffs <hunter_buffs_profile_name> (load|save|delete)``` - Main command for saving, loading, and deleting hunter buff profiles.

- ```/ultimate_manhunt setBuffs armor <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the armor buff value and modifier
- ```/ultimate_manhunt setBuffs attackDamage <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the attack damage buff value and modifier
- ```/ultimate_manhunt setBuffs maxHealth <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the max health buff value and modifier
- ```/ultimate_manhunt setBuffs miningEfficiency <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the mining efficiency value and modifier (1.21.1 exclusive)
- ```/ultimate_manhunt setBuffs movementEfficiency <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the movement efficiency value and modifier (1.21.1 exclusive)
- ```/ultimate_manhunt setBuffs movementSpeed <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the movement speed value and modifier
- ```/ultimate_manhunt setBuffs passiveRegen <amount>``` - Sets the passive regeneration amount
- ```/ultimate_manhunt setBuffs saturation <false|true>``` - Enables/disables permanent satuartion
- ```/ultimate_manhunt setBuffs submergedMiningEfficiency <amount> modifier (add|multiplyBase|multiplyTotal)``` - Sets the submerged mining efficiency value and modifier (1.21.1 exclusive)
- ```/ultimate_manhunt setBuffs waterMovementEfficiency <amount> modifier (add|multiplyBase|multiplyTotal)``` - SEts the water movement efficiency value and modifier (1.21.1 exclusive)

**Speed Runner Lives:**
- ```/ultimate_manhunt lives setCurrent <player> (add|remove|set) <newLives>``` - Manages a speed runner's current life count
- ```/ultimate_manhunt lives setMax <amount>``` - Sets the maximum lives a speed runner will have (can be 1-99)

**Player Roles:**
- ```/ultimate_manhunt roles (current|dead|newPlayerRole) <player> (hunter|spectator|speed_runner)``` - Main command for managing player roles for a manhunt game.
  - **Notes:**
    - ```speed_runner``` is not an available role for dead speed runners
    - Additional optional true/false argument for setting a player as a buffed hunter setting a player's current role as a hunter

**Spawn:**

- ```ultimate_manhunt setSpawn <x> <z> <false|true>``` - Sets the manhunt game spawn location (and world spawn if specified)
  - **Notes:**
    - ```<x>``` and ```<z>``` set the x and z game start coordinates. The y value is the highest available space
    - ```<false|true>``` argument determines if world spawn should be moved to the specified location

