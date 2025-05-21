# UaiFood Order Service

Serviço responsável pelo gerenciamento de pedidos do sistema UaiFood, permitindo a criação, atualização e acompanhamento de pedidos de restaurantes.

## 🚀 Tecnologias

- Kotlin
- Spring Boot 3.2.3
- MySQL 8.0
- RabbitMQ 3.12
- Apache Kafka 7.5.3
- Docker & Docker Compose
- Gradle

## 📋 Pré-requisitos

- JDK 17
- Docker e Docker Compose
- Gradle (opcional, o projeto usa o wrapper)

## 🔧 Configuração do Ambiente

### Variáveis de Ambiente

O projeto usa as seguintes variáveis de ambiente (já configuradas no docker-compose):

```yaml
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/uaifood_orders
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SPRING_KAFKA_CONSUMER_GROUP_ID=order-service
SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET=earliest
```

## 🏃‍♂️ Executando o Projeto

### Usando Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITÓRIO]
cd uaifood-order-service
```

2. Inicie os containers:
```bash
docker-compose up -d
```

3. Verifique se os serviços estão rodando:
```bash
docker-compose ps
```

### Executando Localmente

1. Certifique-se que o MySQL, RabbitMQ e Kafka estão rodando (pode usar o docker-compose apenas para estes serviços):
```bash
# Iniciar apenas os serviços de infraestrutura
docker-compose up -d mysql rabbitmq zookeeper kafka

# Aguardar os serviços estarem prontos (pode verificar com)
docker-compose ps
```

2. Execute a aplicação em modo de desenvolvimento:
```bash
# Usando o perfil de desenvolvimento
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Ou, se preferir executar via IDE:
- Configure a variável de ambiente: `SPRING_PROFILES_ACTIVE=dev`
- Ou adicione o argumento: `--spring.profiles.active=dev`

### Configuração Local vs Docker

O projeto suporta dois perfis de execução:

1. **dev** (Desenvolvimento Local)
   - Kafka: localhost:9092
   - MySQL: localhost:3307
   - RabbitMQ: localhost:5672

2. **prod** (Docker Compose)
   - Kafka: kafka:29092
   - MySQL: mysql:3306
   - RabbitMQ: rabbitmq:5672

Para alternar entre os perfis:
- Local: Use `--spring.profiles.active=dev`
- Docker: Use `--spring.profiles.active=prod` (padrão no docker-compose)

## 📦 Portas e Endpoints

- **API**: http://localhost:8081
- **MySQL**: localhost:3307
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Kafka**: localhost:9092

### Principais Endpoints da API

- `POST /api/v1/orders` - Criar novo pedido
- `GET /api/v1/orders/{id}` - Buscar pedido por ID
- `GET /api/v1/orders` - Listar pedidos
- `PUT /api/v1/orders/{id}/status` - Atualizar status do pedido

## 🧪 Testes

Execute os testes com:
```bash
./gradlew test
```

## 📝 Logs

Os logs da aplicação podem ser visualizados através do Docker Compose:
```bash
docker-compose logs -f order-service
```

## 🔍 Monitoramento

- **Health Check**: http://localhost:8081/actuator/health
- **Metrics**: http://localhost:8081/actuator/metrics
- **Documentação Swagger**: http://localhost:8081/swagger-ui.html

## 🛠️ Desenvolvimento

### Estrutura do Projeto

```
src/main/kotlin/br/edu/uaifood/orders/
├── config/         # Configurações da aplicação
├── domain/         # Entidades e regras de negócio
├── infrastructure/ # Implementações de repositórios e serviços externos
├── application/    # Casos de uso e serviços
└── presentation/   # Controllers e DTOs
```

### Comandos Úteis

- Build do projeto:
```bash
./gradlew build
```

- Limpar build:
```bash
./gradlew clean
```

- Executar testes com cobertura:
```bash
./gradlew test jacocoTestReport
```

## 🤝 Contribuindo

1. Faça o fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 🧪 Testando o Kafka

### Usando o Console do Kafka

1. Acesse o container do Kafka:
```bash
docker exec -it kafka_uaifood bash
```

2. Listar tópicos existentes:
```bash
kafka-topics --bootstrap-server localhost:9092 --list
```

3. Criar um novo tópico (se necessário):
```bash
kafka-topics --bootstrap-server localhost:9092 --create --topic orders --partitions 1 --replication-factor 1
```

4. Produzir mensagens (em uma nova janela do terminal):
```bash
# Acesse o container
docker exec -it kafka_uaifood bash

# Inicie o console producer
kafka-console-producer --bootstrap-server localhost:9092 --topic orders

# Digite suas mensagens (uma por linha)
# Exemplo:
{"orderId": "123", "status": "CREATED"}
{"orderId": "456", "status": "PREPARING"}
# Pressione Ctrl+D para sair
```

5. Consumir mensagens (em outra janela do terminal):
```bash
# Acesse o container
docker exec -it kafka_uaifood bash

# Inicie o console consumer
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders --from-beginning --group test-group
```

### Usando Scripts de Teste

1. Crie um arquivo `test-kafka.sh` na raiz do projeto:
```bash
#!/bin/bash

# Função para enviar mensagem
send_message() {
    echo "$1" | docker exec -i kafka_uaifood kafka-console-producer --bootstrap-server localhost:9092 --topic orders
}

# Função para ler mensagens
read_messages() {
    docker exec -i kafka_uaifood kafka-console-consumer --bootstrap-server localhost:9092 --topic orders --from-beginning --group test-group --timeout-ms 5000
}

# Exemplo de uso
echo "Enviando mensagens de teste..."
send_message '{"orderId": "test-1", "status": "CREATED", "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"}'
send_message '{"orderId": "test-2", "status": "PREPARING", "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"}'

echo "Lendo mensagens..."
read_messages
```

2. Torne o script executável:
```bash
chmod +x test-kafka.sh
```

3. Execute o script:
```bash
./test-kafka.sh
```

### Monitorando o Kafka

1. Verificar o status dos tópicos:
```bash
docker exec -it kafka_uaifood kafka-topics --bootstrap-server localhost:9092 --describe --topic orders
```

2. Verificar os grupos de consumidores:
```bash
docker exec -it kafka_uaifood kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

3. Verificar o lag (mensagens não processadas) de um grupo:
```bash
docker exec -it kafka_uaifood kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group order-service
```

### Exemplos de Mensagens

Para testar o serviço de pedidos, você pode enviar mensagens no seguinte formato:

```json
// Criar pedido
{
    "orderId": "123",
    "customerId": "customer-1",
    "restaurantId": "restaurant-1",
    "status": "CREATED",
    "items": [
        {
            "productId": "product-1",
            "quantity": 2,
            "price": 15.99
        }
    ]
}

// Atualizar status
{
    "orderId": "123",
    "status": "PREPARING",
    "timestamp": "2024-05-20T20:00:00Z"
}
``` 