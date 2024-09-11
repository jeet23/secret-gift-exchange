## Secret Gift Exchange App

This is a simple application that allows a random gift exchange among members of a family.

The app uses an in-memory Database called H2, so data is not persisted across application restarts.

It also provides concurrent access to users by leveraging `@Transactional` and `PESSIMISTIC_WRITE` lock on the DB.

### Fabrikt Integration

The models and controllers are generated from the OpenAPI spec using [Fabrikt](https://github.com/cjbooms/fabrikt)
library.

The build code is generated to `build/generated` directory.

To generate the API models and controllers, run `./gradlew build`

### Run server

To run the server, `./gradlew bootRun`

### APIs and sample requests

Following APIs are supported:

- GET /members : list the family members
   ```shell
  curl 'localhost:8080/members'
   ```
- GET /members/{id} : get a single family member
  ```shell
  curl 'localhost:8080/members/1'
  ```
- POST /members :  add a family member :
    ```shell
    curl -X POST --location 'localhost:8080/members' \
    --header 'Content-Type: application/json' \
    --data '{
        "name": "Bob"
    }'
    ```
- PUT /members/{id} :  updates a family member
    ```shell
    curl -X PUT --location 'localhost:8080/members/1' \
    --header 'Content-Type: application/json' \
    --data '{
        "name": "Bob Version 2",
    }'
    ```
- DELETE /members/{id} :  delete a family member
   ```shell
    curl -X DELETE 'localhost:8080/members/1'
   ```
- POST /gift_exchange : generates a random gift exchange
  ```shell
  curl -X POST 'localhost:8080/gift_exchange'
  ```
- GET /gift_exchange : lists members along with the member id they will be gifting to
    ```shell
    curl 'localhost:8080/gift_exchange'
   ```

### Tests

Unit tests are added in `test` directory and can be run using `./gradlew test`

**NB**: For testing the randomness around the `drawNames` function, a random number generator seed module is used,
so that the results of tests are deterministic and produces same output every time.

In the actual API implementation, the `Random.default` module is used,
so there will be random gift-exchanges performed every time.
