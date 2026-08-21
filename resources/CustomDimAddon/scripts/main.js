// scripts/main.ts
import { system } from "@minecraft/server";
system.beforeEvents.startup.subscribe((event) => {
  event.dimensionRegistry.registerCustomDimension("test:custom_dim");
  console.log("registered custom dimension");
});
function mainTick() {
  system.run(mainTick);
}
system.run(mainTick);

//# sourceMappingURL=../debug/main.js.map
