#!/bin/bash

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar se o Kafka está rodando
check_kafka() {
    echo -e "${YELLOW}Verificando se o Kafka está rodando...${NC}"
    if ! docker exec kafka_uaifood kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
        echo -e "${RED}Erro: Kafka não está rodando. Inicie os containers primeiro:${NC}"
        echo "docker-compose up -d zookeeper kafka"
        exit 1
    fi
    echo -e "${GREEN}Kafka está rodando!${NC}"
}

# Função para criar tópico se não existir
create_topic_if_not_exists() {
    local topic=$1
    echo -e "${YELLOW}Verificando tópico '$topic'...${NC}"
    
    if ! docker exec kafka_uaifood kafka-topics --bootstrap-server localhost:9092 --list | grep -q "^$topic$"; then
        echo -e "${YELLOW}Criando tópico '$topic'...${NC}"
        docker exec kafka_uaifood kafka-topics --bootstrap-server localhost:9092 --create --topic $topic --partitions 1 --replication-factor 1
        echo -e "${GREEN}Tópico '$topic' criado com sucesso!${NC}"
    else
        echo -e "${GREEN}Tópico '$topic' já existe!${NC}"
    fi
}

# Função para enviar mensagem
send_message() {
    local topic=$1
    local message=$2
    echo -e "${YELLOW}Enviando mensagem para o tópico '$topic':${NC}"
    echo "$message" | docker exec -i kafka_uaifood kafka-console-producer --bootstrap-server localhost:9092 --topic $topic
    echo -e "${GREEN}Mensagem enviada!${NC}"
}

# Função para ler mensagens
read_messages() {
    local topic=$1
    local group=$2
    echo -e "${YELLOW}Lendo mensagens do tópico '$topic' (grupo: $group):${NC}"
    docker exec -i kafka_uaifood kafka-console-consumer --bootstrap-server localhost:9092 --topic $topic --from-beginning --group $group --timeout-ms 5000
}

# Função para mostrar status do tópico
show_topic_status() {
    local topic=$1
    echo -e "${YELLOW}Status do tópico '$topic':${NC}"
    docker exec kafka_uaifood kafka-topics --bootstrap-server localhost:9092 --describe --topic $topic
}

# Função para mostrar lag do consumidor
show_consumer_lag() {
    local group=$1
    echo -e "${YELLOW}Log do grupo de consumidores '$group':${NC}"
    docker exec kafka_uaifood kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group $group
}

# Verificar Kafka
check_kafka

# Criar tópico se necessário
TOPIC="orders"
create_topic_if_not_exists $TOPIC

# Menu interativo
while true; do
    echo -e "\n${YELLOW}=== Menu de Teste do Kafka ===${NC}"
    echo "1. Enviar mensagem de teste"
    echo "2. Ler mensagens"
    echo "3. Mostrar status do tópico"
    echo "4. Mostrar log do consumidor"
    echo "5. Sair"
    echo -n "Escolha uma opção: "
    read option

    case $option in
        1)
            echo -e "\n${YELLOW}Escolha o tipo de mensagem:${NC}"
            echo "1. Criar pedido"
            echo "2. Atualizar status"
            echo "3. Mensagem customizada"
            echo -n "Opção: "
            read msg_type

            case $msg_type in
                1)
                    message='{
                        "orderId": "test-'$(date +%s)'",
                        "customerId": "customer-1",
                        "restaurantId": "restaurant-1",
                        "status": "CREATED",
                        "items": [
                            {
                                "productId": "product-1",
                                "quantity": 2,
                                "price": 15.99
                            }
                        ],
                        "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
                    }'
                    send_message $TOPIC "$message"
                    ;;
                2)
                    message='{
                        "orderId": "test-'$(date +%s)'",
                        "status": "PREPARING",
                        "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
                    }'
                    send_message $TOPIC "$message"
                    ;;
                3)
                    echo -n "Digite sua mensagem JSON: "
                    read custom_message
                    send_message $TOPIC "$custom_message"
                    ;;
                *)
                    echo -e "${RED}Opção inválida!${NC}"
                    ;;
            esac
            ;;
        2)
            echo -n "Nome do grupo de consumidores (default: test-group): "
            read group
            group=${group:-test-group}
            read_messages $TOPIC $group
            ;;
        3)
            show_topic_status $TOPIC
            ;;
        4)
            echo -n "Nome do grupo de consumidores (default: order-service): "
            read group
            group=${group:-order-service}
            show_consumer_lag $group
            ;;
        5)
            echo -e "${GREEN}Saindo...${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Opção inválida!${NC}"
            ;;
    esac
done 