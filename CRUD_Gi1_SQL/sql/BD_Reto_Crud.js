// Seleccionar / crear la base de datos
use users_manager

// Insertar documentos (crea la colección "users")
db.users.insertMany([
  {
    P_EMAIL: "admin@sandia.com",
    P_USERNAME: "admin",
    P_PASSWORD: "Ab123456",
    P_NAME: "Admin",
    P_LASTNAME: "Sandia",
    P_TELEPHONE: "123456789",
    A_CURRENT_ACCOUNT: "1234123412341234"
  },
  {
    P_EMAIL: "user1@sandia.com",
    P_USERNAME: "user1",
    P_PASSWORD: "Ab123456",
    P_NAME: "User 1",
    P_LASTNAME: "Sandia",
    P_TELEPHONE: "987654321",
    U_GENDER: "MALE",
    U_CARD: "4321432143214321"
  },
  {
    P_EMAIL: "user2@sandia.com",
    P_USERNAME: "user2",
    P_PASSWORD: "Ab123456",
    P_NAME: "User 2",
    P_LASTNAME: "Sandia",
    P_TELEPHONE: "987867321",
    U_GENDER: "FEMALE",
    U_CARD: "4321432337914321"
  },
  {
    P_EMAIL: "user3@sandia.com",
    P_USERNAME: "user3",
    P_PASSWORD: "Ab123456",
    P_NAME: "User 3",
    P_LASTNAME: "Sandia",
    P_TELEPHONE: "687864451",
    U_GENDER: "OTHER",
    U_CARD: "4305332143214321"
  }
])

// Creación de índices UNIQUE (obligatorio antes de usar la app)
db.users.createIndex({ P_EMAIL: 1 }, { unique: true })
db.users.createIndex({ P_USERNAME: 1 }, { unique: true })
