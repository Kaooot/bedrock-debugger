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
3. Log in to your Microsoft account. DO NOT disclose your account details to third parties!
4. Configure the remote (target) and local server (proxied). For example, the following configuration starts a local
  proxied server on port 19122 and connects to a localhost server on port 19132:

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

> **Keep `data/accounts.json` private.** The refresh token stored there grants access to your Microsoft account — treat
> it like a password. Never share it, post it in logs or screenshots, or commit it to a repository. Anyone who obtains
> it can sign in as you.
>
> You are solely responsible for safeguarding this file and its contents. The author accepts no responsibility or
> liability for any account loss, compromise, or other damage resulting from misuse or careless handling of
> `data/accounts.json`.

## Troubleshooting

* You may need to enable loopback access when connecting to a local server,
  see https://github.com/microsoft/MinecraftCodex/blob/main/package.json#L14