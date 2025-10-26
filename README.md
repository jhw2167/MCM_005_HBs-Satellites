# HB's Satellites Mod

A Minecraft mod that brings futuristic satellite surveillance to your world! Monitor distant terrain, track entities, and view underground features through beautiful 3D holographic displays.

![Satellite Overview](images/satellite_overview.gif)

## Features

### Satellite System
- **Satellite Block**: Place anywhere in the world to establish surveillance
- **Controller Block**: Interface to manage your satellite network
- **Display Blocks**: Create stunning 3D holographic terrain displays
- **Force-loaded Chunks**: Ensures continuous monitoring of target areas

![Controller Block](images/controller_block.png)

### Surveillance Capabilities
- Real-time terrain visualization
- Entity tracking with color-coded pings
  - Green: Friendly entities
  - White: Neutral entities
  - Red: Hostile entities
- Underground scanning capabilities
- Adjustable viewing depth and range

![Radar Pings](images/radar_pings.gif)

## Getting Started

1. Craft and place your Satellite Block high in the sky
2. Set up your Controller Block at your base
3. Surround the Controller with Display Blocks
4. Link your Satellite to the Controller using matching wool colors
5. Use the Controller interface to:
   - Adjust viewing direction (N/S/E/W)
   - Change scanning depth
   - Modify display height

![Holographic Display](images/holo_display.gif)

## Configuration

- Customize entity tracking colors into friendly, neutral, and hostile
- Adjust satellite range and operating restrictions
- Set refresh rates for displays

## Tips & Tricks

- Place satellites high up for better coverage
- Use multiple displays for comprehensive views
- Right-click blocks in the hologram to get their exact coordinates
- Hover over areas to highlight them in the display

## Technical Requirements

- Minecraft Forge 1.20.1
- Dependencies:
  - Chisel and Bits 1.4.148+

## Planned Features

- Ore scanning capabilities
- Advanced target selection
- Moving satellite support
- Enhanced hologram visuals
- Improved multiplayer support
- Integration with:
  - Create Big Cannons
  - Orbital Railgun
  - JourneyMap

## Known Issues

- Many configs options do not impact the game yet; the ping system DOES work
- When the player rejoins the world, the satellite controller does not load the proper chunks for the satellite it was linked too
- Entity Pings do not refresh readily; and disapear often for unknown reasons
- The Satellite does not display wool color on all sides
- Satellites will not work in non-overworld dimensions
- The holo UI - hovers orange on player hover applies intermittenly, needs to be more consistent
- Right Clicking to export a block position shows the position twice; 

## License

This project is licensed under GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

