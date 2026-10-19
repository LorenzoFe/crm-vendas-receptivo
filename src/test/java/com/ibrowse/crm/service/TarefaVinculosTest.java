package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ibrowse.crm.security.UsuarioAutenticado;
import com.ibrowse.crm.entity.*;
import com.ibrowse.crm.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@ExtendWith(MockitoExtension.class)
class TarefaVinculosTest {

    @Mock EmpresaRepository empresas;
    @Mock ContatoRepository contatos;
    @Mock UsuarioRepository usuarios;
    @Mock EntityManager entityManager;
    VinculosAcompanhamentoService service;

    @BeforeEach
    void preparar() {
        service = new VinculosAcompanhamentoService(empresas, contatos, usuarios, entityManager);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autoriaSemAutenticacaoRetorna401() {
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.validarAutoria(3L)).getStatusCode().value());
    }

    @Test
    void autoriaDiferenteDoUsuarioAutenticadoRetorna403() {
        autenticar(3L);
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.validarAutoria(4L)).getStatusCode().value());
    }

    @Test
    void autoriaDoUsuarioAutenticadoEhAceita() {
        autenticar(3L);
        assertDoesNotThrow(() -> service.validarAutoria(3L));
    }

    @Test
    void interacaoOuNotaExigeAoMenosUmVinculo() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.resolver(null, null, null, true));
        assertEquals(422, erro.getStatusCode().value());
        verifyNoInteractions(empresas, contatos, entityManager);
    }

    @Test
    void tarefaPodeNaoTerVinculoComCliente() {
        var resultado = service.resolver(null, null, null, false);
        assertNull(resultado.empresa());
        assertNull(resultado.contato());
        assertNull(resultado.oportunidade());
    }

    @Test
    void contatoDeOutraEmpresaEhRecusado() {
        Empresa empresa = empresa(1L);
        Contato contato = contato(empresa(2L));
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(contatos.findById(9L)).thenReturn(Optional.of(contato));
        assertEquals(422, assertThrows(ResponseStatusException.class,
                () -> service.resolver(1L, 9L, null, true)).getStatusCode().value());
    }

    @Test
    void oportunidadeDeOutraEmpresaEhRecusada() {
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa(1L)));
        when(entityManager.find(Oportunidade.class, 8L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade(empresa(2L)));
        assertEquals(422, assertThrows(ResponseStatusException.class,
                () -> service.resolver(1L, null, 8L, true)).getStatusCode().value());
    }

    @Test
    void contatoEOportunidadeDeEmpresasDiferentesSemEmpresaExplicitaSaoRecusados() {
        when(contatos.findById(9L)).thenReturn(Optional.of(contato(empresa(1L))));
        when(entityManager.find(Oportunidade.class, 8L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade(empresa(2L)));
        assertEquals(422, assertThrows(ResponseStatusException.class,
                () -> service.resolver(null, 9L, 8L, true)).getStatusCode().value());
    }

    @Test
    void vinculosCompativeisSaoPreservadosComBloqueioDaOportunidade() {
        Empresa empresa = empresa(1L);
        Contato contato = contato(empresa);
        Oportunidade oportunidade = oportunidade(empresa);
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(contatos.findById(9L)).thenReturn(Optional.of(contato));
        when(entityManager.find(Oportunidade.class, 8L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);

        var resultado = service.resolver(1L, 9L, 8L, true);

        assertSame(empresa, resultado.empresa());
        assertSame(contato, resultado.contato());
        assertSame(oportunidade, resultado.oportunidade());
        verify(entityManager).find(Oportunidade.class, 8L, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void referenciasInexistentesRetornam404() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.resolver(99L, null, null, true)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.resolver(null, 99L, null, true)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.resolver(null, null, 99L, true)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.usuarioAtivo(99L)).getStatusCode().value());
    }

    @Test
    void usuarioInativoNaoPodeAssumirResponsabilidadeOuAutoria() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario));
        assertEquals(422, assertThrows(ResponseStatusException.class,
                () -> service.usuarioAtivo(3L)).getStatusCode().value());
    }

    @Test
    void usuarioAtivoEhAceito() {
        Usuario usuario = new Usuario();
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario));
        assertSame(usuario, service.usuarioAtivo(3L));
    }

    @Test
    void paginaMuitoGrandeEhRecusada() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> VinculosAcompanhamentoService.pagina(PageRequest.of(0, 101))).getStatusCode().value());
    }

    private Empresa empresa(Long id) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        return empresa;
    }

    private void autenticar(Long id) {
        var usuario = new UsuarioAutenticado(id, "Autor de teste", "autor@example.invalid", List.of(), List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.authorities()));
    }

    private Contato contato(Empresa empresa) {
        Contato contato = new Contato();
        contato.setEmpresa(empresa);
        return contato;
    }

    private Oportunidade oportunidade(Empresa empresa) {
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setEmpresa(empresa);
        return oportunidade;
    }
}
