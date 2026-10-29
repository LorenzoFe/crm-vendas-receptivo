


CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissoes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    modulo VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissoes (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permissao_id BIGINT NOT NULL REFERENCES permissoes(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permissao_id)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255),
    departamento VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, role_id)
);


CREATE TABLE IF NOT EXISTS produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_unitario DECIMAL(12, 2) NOT NULL CHECK (preco_unitario >= 0),
    categoria VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS empresas (
    id BIGSERIAL PRIMARY KEY,
    nome_razao_social VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    segmento VARCHAR(100),
    tamanho_empresa VARCHAR(50),
    telefone VARCHAR(20),
    email VARCHAR(255),
    site VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE,
    usuario_responsavel_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contatos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),
    cargo VARCHAR(100),
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    usuario_responsavel_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS estagios_vendas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    ordem INT NOT NULL,
    probabilidade_conversao INT CHECK (probabilidade_conversao >= 0 AND probabilidade_conversao <= 100),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oportunidades (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    contato_id BIGINT REFERENCES contatos(id) ON DELETE SET NULL,
    valor_estimado DECIMAL(15, 2) CHECK (valor_estimado >= 0),
    valor_real DECIMAL(15, 2) CHECK (valor_real >= 0),
    estagio_id BIGINT NOT NULL REFERENCES estagios_vendas(id),
    usuario_vendedor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL,
    motivo_perda VARCHAR(255),
    data_fechamento_esperada DATE,
    data_fechamento_real DATE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS itens_oportunidade (
    id BIGSERIAL PRIMARY KEY,
    oportunidade_id BIGINT NOT NULL REFERENCES oportunidades(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(12, 2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal DECIMAL(15, 2) GENERATED ALWAYS AS (quantidade * preco_unitario) STORED,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS propostas (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    oportunidade_id BIGINT NOT NULL REFERENCES oportunidades(id) ON DELETE CASCADE,
    valor_total DECIMAL(15, 2) NOT NULL CHECK (valor_total >= 0),
    status VARCHAR(50) NOT NULL,
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_aceitacao DATE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS interacoes (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    empresa_id BIGINT REFERENCES empresas(id) ON DELETE CASCADE,
    contato_id BIGINT REFERENCES contatos(id) ON DELETE CASCADE,
    oportunidade_id BIGINT REFERENCES oportunidades(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    descricao TEXT NOT NULL,
    data_interacao TIMESTAMP NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tarefas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    oportunidade_id BIGINT REFERENCES oportunidades(id) ON DELETE CASCADE,
    contato_id BIGINT REFERENCES contatos(id) ON DELETE CASCADE,
    empresa_id BIGINT REFERENCES empresas(id) ON DELETE CASCADE,
    usuario_responsavel_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL,
    prioridade VARCHAR(50) NOT NULL,
    data_vencimento DATE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_conclusao TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notas (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT REFERENCES empresas(id) ON DELETE CASCADE,
    contato_id BIGINT REFERENCES contatos(id) ON DELETE CASCADE,
    oportunidade_id BIGINT REFERENCES oportunidades(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    conteudo TEXT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS interesses (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    cargo VARCHAR(100),
    empresa_nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14),
    segmento VARCHAR(100),
    tamanho_empresa VARCHAR(50),
    site VARCHAR(255),
    preferencia_contato VARCHAR(20),
    mensagem TEXT,
    status VARCHAR(20) NOT NULL,
    data_consentimento TIMESTAMP NOT NULL,
    oportunidade_id BIGINT REFERENCES oportunidades(id) ON DELETE SET NULL,
    usuario_tratamento_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    data_tratamento TIMESTAMP,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE interesses ADD COLUMN IF NOT EXISTS discador_status VARCHAR(20);
ALTER TABLE interesses ADD COLUMN IF NOT EXISTS discador_tentativas INT NOT NULL DEFAULT 0;
ALTER TABLE interesses ADD COLUMN IF NOT EXISTS discador_erro VARCHAR(500);
ALTER TABLE interesses ADD COLUMN IF NOT EXISTS discador_enviado_em TIMESTAMP;
ALTER TABLE interesses ADD COLUMN IF NOT EXISTS discador_resultado_em TIMESTAMP;

CREATE TABLE IF NOT EXISTS interesse_produtos (
    interesse_id BIGINT NOT NULL REFERENCES interesses(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT,
    PRIMARY KEY (interesse_id, produto_id)
);


CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON usuarios(ativo);

CREATE INDEX IF NOT EXISTS idx_empresas_responsavel ON empresas(usuario_responsavel_id);
CREATE INDEX IF NOT EXISTS idx_empresas_ativo ON empresas(ativo);

CREATE INDEX IF NOT EXISTS idx_contatos_empresa ON contatos(empresa_id);
CREATE INDEX IF NOT EXISTS idx_contatos_email ON contatos(email);

CREATE INDEX IF NOT EXISTS idx_oportunidades_empresa ON oportunidades(empresa_id);
CREATE INDEX IF NOT EXISTS idx_oportunidades_usuario_vendedor ON oportunidades(usuario_vendedor_id);
CREATE INDEX IF NOT EXISTS idx_oportunidades_estagio ON oportunidades(estagio_id);
CREATE INDEX IF NOT EXISTS idx_oportunidades_status ON oportunidades(status);
CREATE INDEX IF NOT EXISTS idx_oportunidades_data_fechamento ON oportunidades(data_fechamento_real);

CREATE INDEX IF NOT EXISTS idx_propostas_oportunidade ON propostas(oportunidade_id);
CREATE INDEX IF NOT EXISTS idx_propostas_numero ON propostas(numero);
CREATE INDEX IF NOT EXISTS idx_propostas_status ON propostas(status);

CREATE INDEX IF NOT EXISTS idx_interacoes_usuario ON interacoes(usuario_id);
CREATE INDEX IF NOT EXISTS idx_interacoes_oportunidade ON interacoes(oportunidade_id);
CREATE INDEX IF NOT EXISTS idx_interacoes_data ON interacoes(data_interacao DESC);

CREATE INDEX IF NOT EXISTS idx_tarefas_usuario ON tarefas(usuario_responsavel_id);
CREATE INDEX IF NOT EXISTS idx_tarefas_status ON tarefas(status);
CREATE INDEX IF NOT EXISTS idx_tarefas_oportunidade ON tarefas(oportunidade_id);

CREATE INDEX IF NOT EXISTS idx_interesses_status ON interesses(status);
CREATE INDEX IF NOT EXISTS idx_interesses_discador_status ON interesses(discador_status);
