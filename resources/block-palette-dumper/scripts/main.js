// scripts/main.ts
import { world, system, ItemTypes, ItemStack, BlockTypes, BlockPermutation, BlockStates } from "@minecraft/server";
import { HttpRequest, HttpRequestMethod, http } from "@minecraft/server-net";
function mainTick() {
  if (system.currentTick % 100 == 0 /*&& world.getPlayers().length > 0*/) {
    dumpItemTags();
    dumpBlockTags();
    dumpBlockStates();
  }
  system.run(mainTick);
}
function dumpItemTags() {
  const itemTagsMap = /* @__PURE__ */ new Map();
  ItemTypes.getAll().forEach((itemType) => {
    if (itemType.id == "minecraft:air") {
      return;
    }
    const itemStack = new ItemStack(itemType, 1);
    if (itemStack == null || itemStack.getTags() == null) {
      return;
    }
    const tags = itemStack.getTags();
    tags.forEach((t) => {
      let tag = t;
      if (!tag.startsWith("minecraft:")) {
        tag = "minecraft:" + t;
      }
      if (!itemTagsMap.has(tag)) {
        itemTagsMap.set(tag, []);
      }
      itemTagsMap.get(tag)?.push(itemType.id);
    });
  });
  const sortedMapEntries = Array.from(itemTagsMap.entries()).sort(([key1], [key2]) => key1.localeCompare(key2));
  const itemTags = new Map(sortedMapEntries);
  console.log("send item tags");
  sendPostRequest("item_tags", JSON.stringify(Object.fromEntries(itemTags)));
}
function dumpBlockTags() {
  const blockTagsMap = /* @__PURE__ */ new Map();
  const dimension = world.getDimension("overworld");
  tryCreateTickingArea(dimension);
  var i = 0;
  var j = 0;
  BlockTypes.getAll().forEach((blockType) => {
    if (i > 310) {
      i = 0;
      j++;
    }
    let location = { x: j, y: i, z: 0 };
    dimension.setBlockType(location, blockType);
    const block = dimension.getBlock(location);
    if (block == null || block.getTags() == null) {
      return;
    }
    block.getTags().forEach((t) => {
      let tag = t;
      if (!tag.startsWith("minecraft:")) {
        tag = "minecraft:" + t;
      }
      if (!blockTagsMap.has(tag)) {
        blockTagsMap.set(tag, []);
      }
      blockTagsMap.get(tag)?.push(blockType.id);
    });
    i++;
  });
  const sortedMapEntries = Array.from(blockTagsMap.entries()).sort(([key1], [key2]) => key1.localeCompare(key2));
  const blockTags = new Map(sortedMapEntries);
  console.log("send block tags");
  sendPostRequest("block_tags", JSON.stringify(Object.fromEntries(blockTags)));
}
function dumpBlockStates() {
  const dimension = world.getDimension("overworld");
  tryCreateTickingArea(dimension);
  let blockStateData = {
    blocks: []
  };
  var i = 0;
  var j = 0;
  BlockTypes.getAll().forEach((blockType) => {
    if (i > 310) {
      i = 0;
      j++;
    }
    let location = { x: j, y: i, z: 0 };
    const permutation = BlockPermutation.resolve(blockType.id);
    const allStates = permutation.getAllStates();
    let statesForBlock = [];
    for (let stateName in allStates) {
      const blockState2 = BlockStates.get(stateName);
      if (blockState2 == null) {
        console.log("state for " + blockType.id + " is null: " + stateName);
        continue;
      }
      let valuesForState = [];
      const validValues = blockState2.validValues;
      for (let index = 0; index < validValues.length; index++) {
        const validValue = validValues[index];
        try {
          const tryPermutation = permutation.withState(
            stateName,
            validValue
          );
          dimension.setBlockPermutation(location, tryPermutation);
          if (validValue != tryPermutation.getState(stateName)) {
            continue;
          }
          valuesForState.push(validValue);
        } catch (error) {
          continue;
        }
      }
      statesForBlock.push({ name: stateName, values: valuesForState });
    }
    const blockState = {
      name: blockType.id,
      states: statesForBlock
    };
    blockStateData.blocks.push(blockState);
    i++;
  });
  console.log("send blocks");
  sendPostRequest("block_states", JSON.stringify(blockStateData));
}
function sendPostRequest(fileName, body) {
  let request = new HttpRequest("http://localhost:50005/" + fileName);
  request.setMethod(HttpRequestMethod.Post);
  request.setBody(body);
  http.request(request);
}
function tryCreateTickingArea(dimension) {
  const result = dimension.runCommand("tickingarea add circle 0 0 0 4 test");
  if (result.successCount > 0) {
    console.log("Created ticking area");
  }
}
system.run(mainTick);

//# sourceMappingURL=../debug/main.js.map
