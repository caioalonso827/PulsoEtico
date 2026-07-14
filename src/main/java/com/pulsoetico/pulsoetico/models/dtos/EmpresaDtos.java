package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;
import java.util.Set;

import com.pulsoetico.pulsoetico.models.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class EmpresaDtos {

    private EmpresaDtos() {
    }

    public record CriarEmpresaRequest(
            @NotBlank(message = "O nome é obrigatório")
            @Size(max = 120)
            String nome,

            @Size(max = 500)
            String descricao
    ) {
    }

    public record EntrarPorCodigoRequest(
            @NotBlank(message = "O código é obrigatório")
            @Pattern(
                    regexp = "[A-Za-z0-9]{8}",
                    message = "O código deve conter 8 caracteres"
            )
            String codigo
    ) {
    }

    public record CargoRequest(
            @NotBlank(message = "O nome do cargo é obrigatório")
            @Size(max = 80)
            String nome,

            @Size(max = 300)
            String descricao,

            @NotNull(message = "As permissões são obrigatórias")
            Set<Permissoes> permissoes
    ) {
    }

    public record AtualizarMembroRequest(
            @NotNull(message = "O cargoId é obrigatório")
            Long cargoId,

            Long setorId
    ) {
    }

    public record EmpresaResponse(
            Long id,
            String nome,
            String descricao,
            String codigoConvite,
            boolean codigoAtivo,
            boolean proprietario,
            Long cargoId,
            String cargoNome,
            Long setorId,
            String setorNome,
            Set<Permissoes> permissoes,
            long totalMembros,
            long totalSetores,
            Instant criadoEm
    ) {
        public static EmpresaResponse from(
                MembroEmpresa membro,
                long totalMembros,
                long totalSetores,
                boolean exibirCodigo
        ) {
            Empresa empresa = membro.getEmpresa();
            Setor setor = membro.getSetor();

            return new EmpresaResponse(
                    empresa.getId(),
                    empresa.getNome(),
                    empresa.getDescricao(),
                    exibirCodigo ? empresa.getCodigoConvite() : null,
                    empresa.getCodigoConvite() != null,
                    membro.isProprietario(),
                    membro.getCargo().getId(),
                    membro.getCargo().getNome(),
                    setor != null ? setor.getId() : null,
                    setor != null ? setor.getNome() : null,
                    Set.copyOf(membro.getCargo().getPermissoes()),
                    totalMembros,
                    totalSetores,
                    empresa.getCriadoEm()
            );
        }
    }

    public record CodigoConviteResponse(
            Long empresaId,
            String codigo,
            Instant geradoEm
    ) {
    }

    public record CargoResponse(
            Long id,
            String nome,
            String descricao,
            Set<Permissoes> permissoes,
            boolean sistema
    ) {
        public static CargoResponse from(Cargo cargo) {
            return new CargoResponse(
                    cargo.getId(),
                    cargo.getNome(),
                    cargo.getDescricao(),
                    Set.copyOf(cargo.getPermissoes()),
                    cargo.isSistema()
            );
        }
    }

    public record MembroResponse(
            Long id,
            Long funcionarioId,
            String nome,
            String email,
            Long cargoId,
            String cargoNome,
            Long setorId,
            String setorNome,
            boolean proprietario,
            Instant entrouEm
    ) {
        public static MembroResponse from(MembroEmpresa membro) {
            Setor setor = membro.getSetor();

            return new MembroResponse(
                    membro.getId(),
                    membro.getFuncionario().getId(),
                    membro.getFuncionario().getNomeCompleto(),
                    membro.getFuncionario().getEmail(),
                    membro.getCargo().getId(),
                    membro.getCargo().getNome(),
                    setor != null ? setor.getId() : null,
                    setor != null ? setor.getNome() : null,
                    membro.isProprietario(),
                    membro.getEntrouEm()
            );
        }
    }

    public record SetorEmpresaResponse(
            Long id,
            Long empresaId,
            String nome,
            Integer quantidadeColaboradores,
            Instant criadoEm
    ) {
        public static SetorEmpresaResponse from(Setor setor) {
            return new SetorEmpresaResponse(
                    setor.getId(),
                    setor.getEmpresa().getId(),
                    setor.getNome(),
                    setor.getQuantidadeColaboradores(),
                    setor.getCriadoEm()
            );
        }
    }
}