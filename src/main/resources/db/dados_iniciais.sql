INSERT INTO permissoes (nome, descricao, modulo) VALUES
    ('ver_oportunidade',     'Visualizar oportunidades',     'oportunidades'),
    ('criar_oportunidade',   'Criar oportunidades',          'oportunidades'),
    ('editar_oportunidade',  'Editar oportunidades',         'oportunidades'),
    ('deletar_oportunidade', 'Deletar oportunidades',        'oportunidades'),
    ('criar_proposta',       'Criar propostas',              'propostas'),
    ('editar_proposta',      'Editar propostas',             'propostas'),
    ('deletar_proposta',     'Deletar propostas',            'propostas'),
    ('criar_empresa',        'Criar empresas e contatos',    'empresas'),
    ('editar_empresa',       'Editar empresas e contatos',   'empresas'),
    ('deletar_empresa',      'Desativar empresas e contatos','empresas'),
    ('ver_relatorio',        'Visualizar relatórios',        'relatorios'),
    ('gerenciar_usuarios',   'Gerenciar usuários',           'admin'),
    ('gerenciar_permissoes', 'Gerenciar roles e permissões', 'admin')
ON CONFLICT (nome) DO NOTHING;

INSERT INTO roles (nome, descricao) VALUES
    ('ADMIN',    'Administrador do sistema'),
    ('GERENTE',  'Gerente de vendas'),
    ('VENDEDOR', 'Vendedor')
ON CONFLICT (nome) DO NOTHING;

INSERT INTO role_permissoes (role_id, permissao_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissoes p
WHERE r.nome = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissoes (role_id, permissao_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissoes p
WHERE r.nome = 'GERENTE' AND p.modulo <> 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissoes (role_id, permissao_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissoes p
WHERE r.nome = 'VENDEDOR'
  AND p.nome IN ('ver_oportunidade', 'criar_oportunidade', 'editar_oportunidade',
                 'criar_proposta', 'editar_proposta',
                 'criar_empresa', 'editar_empresa', 'ver_relatorio')
ON CONFLICT DO NOTHING;

INSERT INTO estagios_vendas (nome, ordem, probabilidade_conversao) VALUES
    ('Prospecção',   1, 10),
    ('Qualificação', 2, 25),
    ('Proposta',     3, 50),
    ('Negociação',   4, 75),
    ('Fechado',      5, 100)
ON CONFLICT (nome) DO NOTHING;
