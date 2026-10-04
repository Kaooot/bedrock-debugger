# Tags Generation Guide

The TagsDumper script can be used to generate vanilla item tags, block tags and a block dump. The data is sent over the
network, see DebugServerHelper and DebugHttpServer for more info.

Block Dump Generation is obsolete. Use '/dumpblockpalette' to generate the block palette. Place the block palette NBT
file in the resources folder for further dumps (e.g. creative items) and restart the debugger.

## Step 1: BDS download and preparations

* Download the BDS and move it to the data folder
* Update the server.properties and set ``content-log-console-output-enabled`` to true
* Add ``server-net`` to ``config/default/permissions.json``
* Create a test_config.json file with ``{"generate_documentation":true}`` and start the server
* Delete the generated Bedrock level

## Step 2: Generating the world

* Optional: Enable content log debugging in the client's creator settings
* Add the TagsDumper script to your ``development_behavior_packs`` folder
* Start the client and create a new flat world with beta APIs experiment and TagsDumper script enabled
* Join the world
* Move the world to the BDS worlds folder. The name must match with what's provided in the server.properties file

## Step 3: Preparing the debugger

* Set the BDS debug server path in the debugger test config - relative to the data folder

## Step 4: Start the debugger

* Start the debugger and connect to a server
* Execute '/dumpblocktags' or '/dumpitemtags'