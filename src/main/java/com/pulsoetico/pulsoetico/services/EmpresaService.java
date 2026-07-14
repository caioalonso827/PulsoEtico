package com.pulsoetico.pulsoetico.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Cargo;
import com.pulsoetico.pulsoetico.models.Empresa;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.AtualizarMembroRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CargoRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CargoResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CodigoConviteResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CriarEmpresaRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.EmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.EntrarPorCodigoRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.MembroResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.SetorEmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.VinculoEmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorRequest;
import com.pulsoetico.pulsoetico.repositories.CargoRepository;
import com.pulsoetico.pulsoetico.repositories.EmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;
import com.pulsoetico.pulsoetico.security.JwtService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmpresaService {

    private final FuncionarioRepository funcionarioRepository;
    private static final String CARGO_ADMINISTRADOR = "Administrador";
    private static final String CARGO_COLABORADOR = "Colaborador";

    private static final char[] CARACTERES =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final SecureRandom random = new SecureRandom();

    private final EmpresaRepository empresaRepository;
    private final CargoRepository cargoRepository;
    private final MembroEmpresaRepository membroRepository;
    private final SetorRepository setorRepository;
    private final AutorizacaoEmpresaService autorizacao;
    private final JwtService jwtService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            CargoRepository cargoRepository,
            MembroEmpresaRepository membroRepository,
            SetorRepository setorRepository,
            JwtService jwtService,
            AutorizacaoEmpresaService autorizacao, FuncionarioRepository funcionarioRepository
    ) {
        this.empresaRepository = empresaRepository;
        this.cargoRepository = cargoRepository;
        this.membroRepository = membroRepository;
        this.setorRepository = setorRepository;
        this.autorizacao = autorizacao;
        this.funcionarioRepository = funcionarioRepository;
        this.jwtService = jwtService;
    }

@Transactional
public VinculoEmpresaResponse criar(
        CriarEmpresaRequest request,
        Funcionario criador
) {
    Empresa empresa = empresaRepository.save(
            Empresa.builder()
                    .nome(request.nome().trim())
                    .descricao(normalizar(request.descricao()))
                    .criadoPor(criador)
                    .build()
    );

    Cargo administrador = cargoRepository.save(
            Cargo.builder()
                    .empresa(empresa)
                    .nome(CARGO_ADMINISTRADOR)
                    .descricao("Acesso completo à empresa")
                    .permissoes(EnumSet.allOf(Permissoes.class))
                    .sistema(true)
                    .build()
    );

    cargoRepository.save(
            Cargo.builder()
                    .empresa(empresa)
                    .nome(CARGO_COLABORADOR)
                    .descricao("Cargo padrão para novos membros")
                    .permissoes(
                            EnumSet.of(
                                    Permissoes.RESPONDER_PESQUISAS,
                                    Permissoes.REGISTRAR_PONTO
                            )
                    )
                    .sistema(true)
                    .build()
    );

    MembroEmpresa proprietario = membroRepository.save(
            MembroEmpresa.builder()
                    .empresa(empresa)
                    .funcionario(criador)
                    .cargo(administrador)
                    .proprietario(true)
                    .build()
    );

    // Correção de bug: não mutamos mais Funcionario.papel globalmente.
    // O papel efetivo (GESTOR, nesse caso, por ser proprietário) é calculado
    // pelo JwtService a partir do próprio MembroEmpresa/Cargo dessa empresa.
    String novoToken = jwtService.gerarToken(criador, proprietario);

    return new VinculoEmpresaResponse(
            EmpresaResponse.from(
                    proprietario,
                    1,
                    0,
                    true
            ),
            novoToken
    );
}

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarMinhas(Funcionario usuario) {
        return membroRepository
                .findAllByFuncionarioIdAndAtivoTrueOrderByEntrouEmDesc(
                        usuario.getId()
                )
                .stream()
                .map(this::converterEmpresa)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscar(
            Long empresaId,
            Funcionario usuario
    ) {
        return converterEmpresa(
                autorizacao.exigirMembro(empresaId, usuario)
        );
    }


        private static final Duration TEMPO_VALIDADE_CODIGO = Duration.ofMinutes(15);

        private boolean codigoAindaValido(Empresa empresa) {
        return empresa.getCodigoConvite() != null
            && empresa.getCodigoExpiraEm() != null
            && Instant.now().isBefore(empresa.getCodigoExpiraEm());
}

        @Transactional
        public CodigoConviteResponse gerarCodigo(
                Long empresaId,
                Funcionario usuario
        ) {
        MembroEmpresa membro = autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_MEMBROS
        );

        Empresa empresa = membro.getEmpresa();

        if (codigoAindaValido(empresa)) {
                throw new IllegalStateException(
                        "Já existe um código de convite ativo para esta empresa"
                );
        }

        String codigo = gerarCodigoUnico();
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(TEMPO_VALIDADE_CODIGO);

        empresa.setCodigoConvite(codigo);
        empresa.setCodigoGeradoEm(agora);
        empresa.setCodigoExpiraEm(expiraEm);

        empresaRepository.save(empresa);

        return new CodigoConviteResponse(
                empresaId,
                codigo,
                empresa.getCodigoGeradoEm(),
                empresa.getCodigoExpiraEm()
        );
        }

    @Transactional
    public void desativarCodigo(
            Long empresaId,
            Funcionario usuario
    ) {
        MembroEmpresa membro = autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_MEMBROS
        );

        Empresa empresa = membro.getEmpresa();
        empresa.setCodigoConvite(null);
        empresa.setCodigoGeradoEm(null);
        empresa.setCodigoExpiraEm(null);

        empresaRepository.save(empresa);
    }

@Transactional
public VinculoEmpresaResponse entrarPorCodigo(
        EntrarPorCodigoRequest request,
        Funcionario usuario
) {
    String codigo = request.codigo()
            .trim()
            .toUpperCase(Locale.ROOT);

    Empresa empresa = empresaRepository
            .findByCodigoConviteIgnoreCase(codigo)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Código inválido ou desativado"
                    )
            );

    if (empresa.getCodigoExpiraEm() == null
            || Instant.now().isAfter(empresa.getCodigoExpiraEm())) {


        empresaRepository.save(empresa);

        throw new IllegalArgumentException("Código expirado");
    }

    var vinculoExistente =
            membroRepository.findByEmpresaIdAndFuncionarioId(
                    empresa.getId(),
                    usuario.getId()
            );

    if (vinculoExistente.isPresent()
            && vinculoExistente.get().isAtivo()) {

        throw new IllegalArgumentException(
                "Você já participa desta empresa"
        );
    }

    Cargo colaborador = cargoRepository
            .findByEmpresaIdAndNomeIgnoreCase(
                    empresa.getId(),
                    CARGO_COLABORADOR
            )
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Cargo padrão não encontrado"
                    )
            );

    MembroEmpresa membro;

    if (vinculoExistente.isPresent()) {
        membro = vinculoExistente.get();
        membro.setCargo(colaborador);
        membro.setSetor(null);
        membro.setAtivo(true);
    } else {
        membro = MembroEmpresa.builder()
                .empresa(empresa)
                .funcionario(usuario)
                .cargo(colaborador)
                .proprietario(false)
                .build();
    }

    membroRepository.save(membro);

    // Correção de bug: antes gerava o token SEM passar o membro, então o
    // token nem sabia que a pessoa tinha acabado de entrar nessa empresa.
    // Também não mutamos mais Funcionario.papel globalmente.
    String novoToken = jwtService.gerarToken(usuario, membro);

    empresa.setCodigoConvite(null);
    empresa.setCodigoGeradoEm(null);
    empresa.setCodigoExpiraEm(null);
    empresaRepository.save(empresa);

    return new VinculoEmpresaResponse(
            converterEmpresa(membro),
            novoToken
    );
}

@Transactional
public VinculoEmpresaResponse sair(
        Long empresaId,
        Funcionario usuario
) {
    MembroEmpresa membro =
            autorizacao.exigirMembro(empresaId, usuario);

    if (membro.isProprietario()) {
        throw new IllegalArgumentException(
                "O proprietário não pode sair da empresa"
        );
    }

    membro.setAtivo(false);
    membroRepository.save(membro);

    // Correção de bug: não mutamos mais Funcionario.papel globalmente — isso
    // afetaria o acesso da pessoa em QUALQUER OUTRA empresa que ela participe.
    // O token pós-saída simplesmente não tem mais empresa ativa (papel USUARIO);
    // se a pessoa participar de outras empresas, o front deve chamar
    // POST /api/empresas/{id}/selecionar pra continuar usando outra.
    String novoToken = jwtService.gerarToken(usuario);

    return new VinculoEmpresaResponse(
            null,
            novoToken
    );
}

/**
 * Emite um novo token com uma empresa diferente como ativa, pra quem já é
 * membro dela (não confundir com entrarPorCodigo, que é pra ENTRAR numa
 * empresa nova). Existe porque o login só escolhe uma empresa padrão
 * automaticamente — se a pessoa participa de mais de uma, ela precisa
 * de um jeito de trocar sem digitar a senha de novo.
 */
@Transactional(readOnly = true)
public VinculoEmpresaResponse selecionar(
        Long empresaId,
        Funcionario usuario
) {
    MembroEmpresa membro = autorizacao.exigirMembro(empresaId, usuario);

    String novoToken = jwtService.gerarToken(usuario, membro);

    return new VinculoEmpresaResponse(
            converterEmpresa(membro),
            novoToken
    );
}

    @Transactional(readOnly = true)
    public List<CargoResponse> listarCargos(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirMembro(empresaId, usuario);

        return cargoRepository
                .findAllByEmpresaIdOrderBySistemaDescNomeAsc(empresaId)
                .stream()
                .map(CargoResponse::from)
                .toList();
    }

    @Transactional
    public CargoResponse criarCargo(
            Long empresaId,
            CargoRequest request,
            Funcionario usuario
    ) {
        MembroEmpresa administrador =
                autorizacao.exigirPermissao(
                        empresaId,
                        usuario,
                        Permissoes.GERENCIAR_CARGOS
                );

        if (cargoRepository.existsByEmpresaIdAndNomeIgnoreCase(
                empresaId,
                request.nome().trim()
        )) {
            throw new IllegalArgumentException(
                    "Já existe um cargo com esse nome"
            );
        }

        Cargo cargo = Cargo.builder()
                .empresa(administrador.getEmpresa())
                .nome(request.nome().trim())
                .descricao(normalizar(request.descricao()))
                .permissoes(new HashSet<>(request.permissoes()))
                .sistema(false)
                .build();

        return CargoResponse.from(cargoRepository.save(cargo));
    }

    @Transactional
    public CargoResponse atualizarCargo(
            Long empresaId,
            Long cargoId,
            CargoRequest request,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_CARGOS
        );

        Cargo cargo = buscarCargo(empresaId, cargoId);

        if (cargo.isSistema()) {
            throw new IllegalArgumentException(
                    "Cargos automáticos não podem ser alterados"
            );
        }

        cargoRepository
                .findByEmpresaIdAndNomeIgnoreCase(
                        empresaId,
                        request.nome().trim()
                )
                .filter(outro -> !outro.getId().equals(cargoId))
                .ifPresent(outro -> {
                    throw new IllegalArgumentException(
                            "Já existe um cargo com esse nome"
                    );
                });

        cargo.setNome(request.nome().trim());
        cargo.setDescricao(normalizar(request.descricao()));
        cargo.setPermissoes(
                new HashSet<>(request.permissoes())
        );

        return CargoResponse.from(cargoRepository.save(cargo));
    }

    @Transactional
    public void excluirCargo(
            Long empresaId,
            Long cargoId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_CARGOS
        );

        Cargo cargo = buscarCargo(empresaId, cargoId);

        if (cargo.isSistema()) {
            throw new IllegalArgumentException(
                    "Cargos automáticos não podem ser excluídos"
            );
        }

        if (membroRepository.countByCargoId(cargoId) > 0) {
            throw new IllegalArgumentException(
                    "Existem membros usando este cargo"
            );
        }

        cargoRepository.delete(cargo);
    }

    @Transactional(readOnly = true)
    public List<MembroResponse> listarMembros(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_MEMBROS
        );

        return membroRepository
                .findAllByEmpresaIdAndAtivoTrueOrderByFuncionarioNomeCompletoAsc(
                        empresaId
                )
                .stream()
                .map(MembroResponse::from)
                .toList();
    }

    @Transactional
    public MembroResponse atualizarMembro(
            Long empresaId,
            Long membroId,
            AtualizarMembroRequest request,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_MEMBROS
        );

        MembroEmpresa membro = buscarMembro(empresaId, membroId);

        if (membro.isProprietario()) {
            throw new IllegalArgumentException(
                    "O proprietário não pode ter seu cargo alterado"
            );
        }

        Cargo cargo = buscarCargo(empresaId, request.cargoId());

        Setor setor = request.setorId() == null
                ? null
                : buscarSetor(empresaId, request.setorId());

        membro.setCargo(cargo);
        membro.setSetor(setor);

        return MembroResponse.from(membroRepository.save(membro));
    }

    @Transactional
    public void removerMembro(
            Long empresaId,
            Long membroId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_MEMBROS
        );

        MembroEmpresa membro = buscarMembro(empresaId, membroId);

        if (membro.isProprietario()) {
            throw new IllegalArgumentException(
                    "O proprietário não pode ser removido"
            );
        }

        membro.setAtivo(false);
        membroRepository.save(membro);
    }

    @Transactional(readOnly = true)
    public List<SetorEmpresaResponse> listarSetores(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirMembro(empresaId, usuario);

        return setorRepository
                .findAllByEmpresaIdOrderByNomeAsc(empresaId)
                .stream()
                .map(SetorEmpresaResponse::from)
                .toList();
    }

    @Transactional
    public SetorEmpresaResponse criarSetor(
            Long empresaId,
            SetorRequest request,
            Funcionario usuario
    ) {
        MembroEmpresa administrador =
                autorizacao.exigirPermissao(
                        empresaId,
                        usuario,
                        Permissoes.GERENCIAR_SETORES
                );

        if (setorRepository
                .findByEmpresaIdAndNomeIgnoreCase(
                        empresaId,
                        request.nome().trim()
                )
                .isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um setor com esse nome"
            );
        }

        Setor setor = Setor.builder()
                .empresa(administrador.getEmpresa())
                .nome(request.nome().trim())
                .quantidadeColaboradores(
                        request.quantidadeColaboradores()
                )
                .build();

        return SetorEmpresaResponse.from(
                setorRepository.save(setor)
        );
    }

    @Transactional
    public SetorEmpresaResponse atualizarSetor(
            Long empresaId,
            Long setorId,
            SetorRequest request,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_SETORES
        );

        Setor setor = buscarSetor(empresaId, setorId);

        setorRepository
                .findByEmpresaIdAndNomeIgnoreCase(
                        empresaId,
                        request.nome().trim()
                )
                .filter(outro -> !outro.getId().equals(setorId))
                .ifPresent(outro -> {
                    throw new IllegalArgumentException(
                            "Já existe um setor com esse nome"
                    );
                });

        setor.setNome(request.nome().trim());
        setor.setQuantidadeColaboradores(
                request.quantidadeColaboradores()
        );

        return SetorEmpresaResponse.from(
                setorRepository.save(setor)
        );
    }

    @Transactional
    public void excluirSetor(
            Long empresaId,
            Long setorId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_SETORES
        );

        Setor setor = buscarSetor(empresaId, setorId);

        if (membroRepository.countBySetorIdAndAtivoTrue(setorId) > 0) {
            throw new IllegalArgumentException(
                    "Mova os membros antes de excluir o setor"
            );
        }

        setorRepository.delete(setor);
    }

    private EmpresaResponse converterEmpresa(MembroEmpresa membro) {
        Long empresaId = membro.getEmpresa().getId();

        return EmpresaResponse.from(
                membro,
                membroRepository.countByEmpresaIdAndAtivoTrue(
                        empresaId
                ),
                setorRepository.countByEmpresaId(empresaId),
                membro.possuiPermissao(
                        Permissoes.GERENCIAR_MEMBROS
                )
        );
    }

    private Cargo buscarCargo(Long empresaId, Long cargoId) {
        return cargoRepository
                .findByIdAndEmpresaId(cargoId, empresaId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Cargo não encontrado"
                        )
                );
    }

    private Setor buscarSetor(Long empresaId, Long setorId) {
        return setorRepository
                .findByIdAndEmpresaId(setorId, empresaId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Setor não encontrado"
                        )
                );
    }

    private MembroEmpresa buscarMembro(
            Long empresaId,
            Long membroId
    ) {
        MembroEmpresa membro = membroRepository
                .findByIdAndEmpresaId(membroId, empresaId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Membro não encontrado"
                        )
                );

        if (!membro.isAtivo()) {
            throw new EntityNotFoundException(
                    "Membro não encontrado"
            );
        }

        return membro;
    }

    private String gerarCodigoUnico() {
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            StringBuilder codigo = new StringBuilder(8);

            for (int i = 0; i < 8; i++) {
                codigo.append(
                        CARACTERES[
                                random.nextInt(CARACTERES.length)
                        ]
                );
            }

            String resultado = codigo.toString();

            if (!empresaRepository.existsByCodigoConvite(resultado)) {
                return resultado;
            }
        }

        throw new IllegalStateException(
                "Não foi possível gerar um código único"
        );
    }

    private String normalizar(String texto) {
        return texto == null || texto.isBlank()
                ? null
                : texto.trim();
    }
}