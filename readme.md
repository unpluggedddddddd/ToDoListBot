ToDoListBot
A Telegram bot for managing tasks! Built with Java, Spring Boot, PostgreSQL, and Docker. Add tasks, mark them done, delete, and get reminders.
Features

Add task: /add Buy milk | For dinner | 30.04.2025 18:00
List tasks: /list (filter with buttons)
Mark done: /done (pick with buttons)
Delete: /delete (pick with buttons)
Reminders: 1 hour before deadline

What You Need

Java 17
Maven
Docker
Telegram Bot Token (get from @BotFather)

Setup

Clone the repo:git clone <your-repo>
cd todolistbot


Get a bot token from @BotFather in Telegram.
Update src/main/resources/application.properties:telegram.bot.token=your_token
telegram.bot.username=@YourBotName
spring.datasource.url=jdbc:postgresql://localhost:5432/todolistbot
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration


Update docker-compose.yml:version: '3.8'
services:
  postgres:
    image: postgres:latest
    environment:
      POSTGRES_DB: todolistbot
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/todolistbot
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=password
      - TELEGRAM_BOT_TOKEN=your_token
      - TELEGRAM_BOT_USERNAME=@YourBotName
    depends_on:
      - postgres
    networks:
      - app-network
networks:
  app-network:
    driver: bridge
volumes:
  postgres_data:

Replace your_token and @YourBotName.

Run It
With Docker

Run:docker-compose up --build -d


Check containers:docker ps


See logs:docker logs todolistbot_app



With IDE

Start PostgreSQL:docker-compose up -d postgres


Open ToDoListBotApplication.java in IntelliJ, click Run.

Test It

Find your bot in Telegram (@YourBotName).
Send /start, try:
/add Test | Reminder | 25.04.2025 12:00
/list
/done, /delete

