# Crypto Wallet Management

This Spring Boot project is designed to create and manage a **crypto wallet**. The application allows the creation of users, addition of tokens to wallets, and evaluation of wallets (without being tied to a user). 
Additionally, 
1) it includes a **timer** that automatically grabs updated prices for all available assets/tokens from CoinCap(set to run at 4 am every day).
```yaml
asset-price-updater:
  timer:
    cron: 0 0 4 * * *
```
2) there is a "initial load" request, that can also be triggered using the property

```yaml
swiss:
  asset-price-updater:
    trigger-initial-load: true 
```

## Features

- **Create/Manage Crypto Wallets**
    - Users can create crypto wallets.
    - Users can see the total of their wallets
    - Tokens can be added to wallets.
    - Wallets can be evaluated to see the total value of assets.

- **Automated Price Updates**
    - A timer fetches updated prices for assets from the CoinCap API.

- **API Endpoints**:
    - **Create User**: Endpoint to create a new user.
    - **Add Tokens to Wallet**: Endpoint to add tokens to a wallet.
    - **User can also see the value of their wallet**: Endpoint to request the total of the users wallet
    - **Evaluate Wallet**: Endpoint to evaluate the value of a wallet, not tied to any user.

## Setup

### Prerequisites

Before running the project, make sure you have the following installed:

- **Java 21**
- **Gradle**: To build and run the project.
- **PostgreSQL **
- **CoinCap API Access**
- **Docker**

### Running the Application

1. Clone the repository:

   ```bash
   git clone https://github.com/DanielaFasero/swisspost.git
   cd swisspost
   ```
2. Make sure you have docker running in your environment
```bash
docker compose up -d
```
3. Swagger specification can be found here
   [open-api.yaml](.src/test/resources/open-api.yaml)

What still needs to be done: 
1) Adding more tests, specially to the wallet evaluation endpoint 
2) Refactor:
   1) specially the wallet evaluation flow
   2) perhaps change the repos to a more reactive friendly repo, instead of using the normal JPA repos
   3) controllers that are still returning message + http status, should be updated to return just message + eventually exception
