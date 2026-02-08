# KMACoin: Blockchain & Cryptocurrency Demo

Educational Java implementation of a minimal **cryptocurrency**: blockchain construction, **UTXO** model, signed transactions, and balance tracking using **RSA** and **SHA-256**.

## Overview

This project implements a simplified cryptocurrency (**KMACoin**) to demonstrate core blockchain and crypto concepts. It covers key generation and addresses (RSA + SHA-256), the **UTXO** (Unspent Transaction Output) model, building and validating a chain of blocks, and constructing signed transactions that spend UTXOs and pay fees. The code can connect to a course network API to download and upload transactions and blocks, with an emphasis on educational clarity and security practices.

## Features

- **RSA key pairs** and **addresses** (public key hash, e.g. SHA-256)
- **UTXO pool** to track unspent outputs and compute balances
- **Blockchain** built from downloaded blocks and transactions
- **Signed transactions** with inputs (UTXO refs) and outputs (recipient + amount)
- **Coinbase** transactions and miner rewards
- **Transaction fees** and minimum-fee enforcement
- **Network:** download/upload transactions and blocks via API
- **Payment helper:** select UTXOs, create transaction, upload
- **Display utilities:** all blocks, all transactions, chain summary, balances

## Concepts

### Blockchain

A **blockchain** is a linked list of **blocks**, each containing a set of **transactions**. Each block stores a hash of the previous block, forming a chain. Tampering with an old block would change its hash and break the chain.

### UTXO Model

Coins are represented as **Unspent Transaction Outputs (UTXOs)**. Each UTXO has an amount, a destination address, and a reference to the transaction that created it. Spending means consuming one or more UTXOs as **inputs** and creating new UTXOs as **outputs** (payments to recipients and optionally change back to the sender).

### Transactions

A **transaction** lists inputs (references to UTXOs being spent) and outputs (recipient address + amount). The sender signs the transaction with their private key; anyone can verify the signature with the sender’s public key. The hash of the public key is the sender’s **address**.

### Blocks and Mining

**Blocks** bundle transactions (including a **coinbase** transaction that pays the miner). The block header includes the previous block hash, a hash of the transaction list, and a **nonce**. Mining is finding a nonce so the block hash meets a **difficulty** target (e.g. leading zeros), which secures the chain and controls issuance.

## Learning Outcomes

- Understanding of blockchain structure (blocks, previous-block hash, transaction list)
- UTXO model vs account-based ledgers
- How signing and address hashes provide authenticity
- Practical experience with Java Cryptography (RSA, SHA-256)
- Balance computation from the UTXO set
- Transaction construction and fee handling

## Tech Stack

- **Java** · **Cryptography** · **RSA** · **Data Structures**

## Project Structure / Deliverables

- Java source: **Currency**, **BlockChain**, **Block**, **Transaction**, **UTXO/UTXOPool**, **CryptoRSA**, **Network**, **Utility**
- Key generation and address display
- Blockchain build from network data and balance computation
- Transaction creation, signing, and upload (e.g. payment helper)
- Display utilities for blocks, transactions, chain summary, and balances

## Build & Run

```bash
# With Maven
mvn compile exec:java -Dexec.mainClass="your.main.Currency"

# Or run the payment/display utilities as defined in your project
```

## Ethics & Security

- **Educational only;** not for real money or production use.
- Private keys must stay local and never be committed or transmitted in plaintext.
- Use established crypto libraries (e.g. JCA/JCE); do not invent your own algorithms.
- Understand regulatory and ethical implications of cryptocurrency and privacy.

## License

Educational use only. See repository for details.
