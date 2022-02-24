# PatreonHalo

### A simple paper plugin that allows you to give halo's to patreon's *(or really anyone)*

## Permissions
The plugin should work with most permission plugins  
`patreonhalo.<yourTierName>` - Everyone with this permission will get the associated halo above there head  
`patreonhalo.command` - Players with this permission will be able to use the `/patreonhalo` command  

## Configure
The plugin adds decent configurability for multiple tiers 
```yml
update_th_tick: 2          # Delay in ticks between each halo particle spawn
Tiers:
  yourTier:
    halo_y_offset:  0.5    # Height from the players eyes
    halo_radius:    0.3    # Halo radius from player center
    particle_size:  0.65   # The redstone particle size
    particle_count: 30     # The amount of particles that should be used per halo
    color:                 # The halo color
      ==: Color
      RED: 0
      BLUE: 255
      GREEN: 229
  anotherTier:
    halo_y_offset: 0.5
    halo_radius: 0.3
    particle_size: 0.65
    particle_count: 25
    color:
      ==: Color
      RED: 0
      BLUE: 0
      GREEN: 255
```
