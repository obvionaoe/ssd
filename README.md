# SSD PROJECT - TODO

## Notes
* Saber argumentar e explicar a escolha
* Sincronização (é preciso?)
* É preciso fazer Pub/Sub (com grpc)
* Credit bibliotecas usadas
* Meter poucos zeros e incrementar experimentalmente (Blockchain stuff)
* The architecture must be clearly presented, with the design choices being driven by the functional requirements(presented above). It also must present the assumptions made, namelymandated by theoretical and practical limitations.

## TODOS
- [x] Ler os papers
- [ ] O que são ataques eclipse ?
- [ ] DISTRIBUTED LEDGER
    - [ ] should be modular
    - [ ] must support PoW
    - [ ] using proof-of-work
    - [ ] Bonus: proof-of-stake
    - [ ] Bonus: Present an architecture (no implementation is required!) for implementing a permissioned blockchain that uses Byzantine Fault-Tolerance (BFT) [3] as its core distributed consensus (hint: start by looking at BFT-SMaRt [4])
- [ ] SECURE P2P
    - [ ] must implement Kademlia
    - [ ] Resistance to Sybil and Eclipse attacks
    - [ ] Implement trust mechanisms
- [ ] AUCTION SYSTEM (capable of supporting sellers and buyers using a single attribute auction following the English auction)
    - [ ] Transactions should be saved in the blockchain (using public key crypto)
    - [ ] A publisher/subscriber should be built on top of Kademlia to support auctions
- [ ] REPORT
