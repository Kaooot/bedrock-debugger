// scripts/main.ts
import { world, system } from "@minecraft/server";
function mainTick() {
  if (system.currentTick % 600 == 0) {
    let instance = world.getDimension("overworld").playSound(
      "test",
      { x: 0, y: 0, z: 0 },
      { volume: 1.5, pitch: 0.1 }
    );
    instance.setVolume(1);
    instance.setPitch(1);
    instance.fade(2, 1);
    instance.seekTo(3);
    instance.pause();
    instance.resume();
    instance.stop();
  }
  system.run(mainTick);
}
system.run(mainTick);

//# sourceMappingURL=../debug/main.js.map
