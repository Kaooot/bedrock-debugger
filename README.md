# bedrock-debugger

Copyright (c) Kaooot. All rights reserved.

Commercial use, redistribution, publication, and distribution of modified versions are not permitted without explicit
permission from the copyright holder.

Permission is granted to use and modify this software for personal use, local development, research, debugging, and
authorized testing.

Third-party dependencies remain subject to their respective licenses.

## Star the Repo ⭐

If you find this project useful, please consider giving it a star! Countless hours of development have gone into
building bedrock-debugger, and it's all publicly available — a star is a small gesture that goes a long way and is
genuinely appreciated.

## Intended Use

The anti-cheat testing and malicious client simulation features included in this project are intended solely for
authorized testing, research, development, and debugging purposes.

Do not use these features on servers or systems without explicit permission from their owner or operator. The author
does not support, encourage, or authorize the use of this project as a malicious client against third-party servers.

## Platform

This project was developed for Windows and runs exclusively on Windows. It uses https://github.com/Kaooot/Protocol

## Contributions

Contributions and pull requests are not accepted.

## Quick Start Guide

1. Place `pack.zip` in the `resource_packs` folder. The pack can be found in `resources/pack`.
2. Open [microsoft.com/link](https://microsoft.com/link) and enter the code displayed in the command line.
3. Log in to your Microsoft account.
4. If the program has not created a default account, open `data/accounts.json` and enter the refresh token displayed
   in the command line.

```json
{
  "accounts": [
    {
      "name": "NAME",
      "refreshToken": "TOKEN"
    }
  ]
}
```

* Configure the remote (target) and local server (proxied). For example, the following configuration starts a
  local proxied server on port 19122 and connects to a localhost server on port 19132:

```json
{
  "remote_address": "127.0.0.1",
  "remote_port": 19132,
  "proxy_address": "0.0.0.0",
  "proxy_port": 19122,
  "account_name": "NAME", // "default" is set automatically with the first login
  "connection_type": "DEFAULT", // DEFAULT or EXPERIENCE
  "experience_id": "EXPERIENCE ID"// if connection type is EXPERIENCE
}
```