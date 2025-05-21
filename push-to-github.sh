#!/bin/bash

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configurações
GIT_REPO_URL="git@github.com:marcoselli/uaifood-order.git"
SSH_KEY_PATH="$HOME/.ssh/id_ed25519_tech_challenge_spassu"
EMAIL="almeidadevops042@gmail.com"
BRANCH="feature/refactoring-uaifood-order"
COMMIT_MESSAGE="refactoring uaifood-order-service complete"

# Função para log
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    return 1
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar dependências
check_dependencies() {
    log "Verificando dependências..."
    
    # Verificar git
    if ! command -v git &> /dev/null; then
        log_error "Git não está instalado"
        return 1
    fi
    
    # Verificar ssh
    if ! command -v ssh &> /dev/null; then
        log_error "SSH não está instalado"
        return 1
    fi
    
    # Verificar ssh-agent
    if ! command -v ssh-agent &> /dev/null; then
        log_error "ssh-agent não está instalado"
        return 1
    fi
}

# Verificar estrutura do projeto
check_project_structure() {
    log "Verificando estrutura do projeto..."
    
    # Verificar diretório src
    if [ ! -d "src" ]; then
        log_error "Diretório 'src' não encontrado. Execute este script no diretório raiz do projeto."
        return 1
    fi
    
    # Verificar arquivos essenciais
    for file in "build.gradle.kts" "docker-compose.yaml" "Dockerfile"; do
        if [ ! -f "$file" ]; then
            log_warning "Arquivo '$file' não encontrado"
        fi
    done
}

# Configurar SSH
setup_ssh() {
    log "Configurando SSH..."
    
    # Verificar se o diretório .ssh existe
    if [ ! -d "$HOME/.ssh" ]; then
        mkdir -p "$HOME/.ssh" || log_error "Falha ao criar diretório .ssh"
        chmod 700 "$HOME/.ssh" || log_error "Falha ao configurar permissões do diretório .ssh"
    fi
    
    # Gerar ou verificar chave SSH
    if [ ! -f "$SSH_KEY_PATH" ]; then
        log "Gerando nova chave SSH..."
        ssh-keygen -t ed25519 -C "$EMAIL" -f "$SSH_KEY_PATH" -N "" || {
            log_error "Falha ao gerar chave SSH"
            return 1
        }
    else
        log "Chave SSH existente encontrada"
    fi
    
    # Configurar permissões
    chmod 600 "$SSH_KEY_PATH" || {
        log_error "Falha ao configurar permissões da chave SSH"
        return 1
    }
    chmod 644 "${SSH_KEY_PATH}.pub" || {
        log_error "Falha ao configurar permissões da chave SSH pública"
        return 1
    }
    
    # Iniciar ssh-agent
    if ! pgrep -x "ssh-agent" > /dev/null; then
        eval "$(ssh-agent -s)" || {
            log_error "Falha ao iniciar ssh-agent"
            return 1
        }
    fi
    
    # Adicionar chave ao ssh-agent
    if ! ssh-add -l | grep -q "$(ssh-keygen -lf "$SSH_KEY_PATH" | awk '{print $2}')"; then
        ssh-add "$SSH_KEY_PATH" || {
            log_error "Falha ao adicionar chave SSH ao agente"
            return 1
        }
    fi
    
    # Mostrar chave pública
    log "Chave SSH pública para adicionar ao GitHub:"
    cat "${SSH_KEY_PATH}.pub"
    log "Adicione esta chave em https://github.com/settings/keys"
}

# Testar conexão SSH
test_ssh_connection() {
    log "Testando conexão SSH com GitHub..."
    local max_attempts=3
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if ssh -T -o BatchMode=yes -o ConnectTimeout=5 git@github.com 2>&1 | grep -q "successfully authenticated"; then
            log "Conexão SSH estabelecida com sucesso!"
            return 0
        fi
        log_warning "Tentativa $attempt de $max_attempts falhou. Tentando novamente..."
        attempt=$((attempt + 1))
        sleep 2
    done
    
    log_error "Falha na autenticação SSH com GitHub após $max_attempts tentativas"
    return 1
}

# Configurar Git
setup_git() {
    log "Configurando Git..."
    
    # Configurações globais
    git config --global user.email "$EMAIL" || {
        log_error "Falha ao configurar email do Git"
        return 1
    }
    
    git config --global core.sshCommand "ssh -i $SSH_KEY_PATH" || {
        log_error "Falha ao configurar SSH no Git"
        return 1
    }
    
    # Inicializar repositório se necessário
    if [ ! -d ".git" ]; then
        git init || {
            log_error "Falha ao inicializar repositório Git"
            return 1
        }
    fi
    
    # Configurar remote
    if git remote | grep -q "origin"; then
        git remote set-url origin "$GIT_REPO_URL" || {
            log_error "Falha ao atualizar URL do remote"
            return 1
        }
    else
        git remote add origin "$GIT_REPO_URL" || {
            log_error "Falha ao adicionar remote"
            return 1
        }
    fi
}

# Preparar e fazer commit
prepare_commit() {
    log "Preparando commit..."
    
    # Verificar status
    local status=$(git status --porcelain)
    if [ -n "$status" ]; then
        log "Arquivos modificados encontrados:"
        echo "$status"
        
        # Adicionar arquivos
        git add . || {
            log_error "Falha ao adicionar arquivos"
            return 1
        }
        
        # Verificar se há mudanças para commit
        if ! git diff --cached --quiet; then
            log "Realizando commit..."
            git commit -m "$COMMIT_MESSAGE" || {
                log_error "Falha ao realizar commit"
                return 1
            }
        else
            log_warning "Nenhuma mudança para commit"
        fi
    else
        log_warning "Nenhuma mudança detectada no repositório"
    fi
}

# Push para GitHub
push_to_github() {
    log "Realizando push para GitHub..."
    
    # Verificar se a branch existe
    if ! git rev-parse --verify "$BRANCH" &>/dev/null; then
        log "Criando branch $BRANCH..."
        git checkout -b "$BRANCH" || {
            log_error "Falha ao criar branch $BRANCH"
            return 1
        }
    else
        git checkout "$BRANCH" || {
            log_error "Falha ao mudar para branch $BRANCH"
            return 1
        }
    fi
    
    # Verificar se há commits para push
    if ! git rev-list origin/$BRANCH..HEAD &>/dev/null; then
        log_warning "Nenhum commit novo para push"
        return 0
    fi
    
    # Push com upstream
    git push -u origin "$BRANCH" || {
        log_error "Falha ao realizar push"
        return 1
    }
}

# Função principal
main() {
    log "Iniciando processo de push..."
    
    # Verificar dependências
    check_dependencies || exit 1
    
    # Verificar estrutura do projeto
    check_project_structure || exit 1
    
    # Configurar SSH
    setup_ssh || exit 1
    
    # Testar conexão SSH
    test_ssh_connection || exit 1
    
    # Configurar Git
    setup_git || exit 1
    
    # Preparar e fazer commit
    prepare_commit || exit 1
    
    # Push para GitHub
    push_to_github || exit 1
    
    log "Processo concluído com sucesso!"
}

# Executar script
main 