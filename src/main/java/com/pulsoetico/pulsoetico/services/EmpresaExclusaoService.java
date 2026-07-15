package com.pulsoetico.pulsoetico.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AplicacaoFormulario;
import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Cargo;
import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Empresa;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Recomendacao;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.RespostaFormulario;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.AplicacaoFormularioRepository;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.CargoRepository;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;
import com.pulsoetico.pulsoetico.repositories.DenunciaRepository;
import com.pulsoetico.pulsoetico.repositories.EmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.RecomendacaoRepository;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;
import com.pulsoetico.pulsoetico.repositories.RespostaFormularioRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

@Service
public class EmpresaExclusaoService {

    private final RespostaFormularioRepository respostaFormularioRepository;
    private final AplicacaoFormularioRepository aplicacaoFormularioRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final DenunciaRepository denunciaRepository;
    private final CheckinHumorRepository checkinHumorRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;
    private final CargoRepository cargoRepository;
    private final SetorRepository setorRepository;
    private final EmpresaRepository empresaRepository;

    public EmpresaExclusaoService(
            RespostaFormularioRepository respostaFormularioRepository,
            AplicacaoFormularioRepository aplicacaoFormularioRepository,
            RecomendacaoRepository recomendacaoRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            DenunciaRepository denunciaRepository,
            CheckinHumorRepository checkinHumorRepository,
            RegistroPontoRepository registroPontoRepository,
            MembroEmpresaRepository membroEmpresaRepository,
            CargoRepository cargoRepository,
            SetorRepository setorRepository,
            EmpresaRepository empresaRepository
    ) {
        this.respostaFormularioRepository =
                respostaFormularioRepository;
        this.aplicacaoFormularioRepository =
                aplicacaoFormularioRepository;
        this.recomendacaoRepository =
                recomendacaoRepository;
        this.avaliacaoRiscoRepository =
                avaliacaoRiscoRepository;
        this.denunciaRepository =
                denunciaRepository;
        this.checkinHumorRepository =
                checkinHumorRepository;
        this.registroPontoRepository =
                registroPontoRepository;
        this.membroEmpresaRepository =
                membroEmpresaRepository;
        this.cargoRepository =
                cargoRepository;
        this.setorRepository =
                setorRepository;
        this.empresaRepository =
                empresaRepository;
    }

    @Transactional
    public void excluir(Empresa empresa) {
        Long empresaId = empresa.getId();

        /*
         * As respostas precisam ser apagadas antes das aplicações.
         * Os itens RespostaPergunta devem ser apagados pelo cascade
         * configurado em RespostaFormulario.
         */
        List<RespostaFormulario> respostas =
                respostaFormularioRepository
                        .findAllByAplicacao_Empresa_Id(
                                empresaId
                        );

        respostaFormularioRepository.deleteAll(respostas);
        respostaFormularioRepository.flush();

        /*
         * As aplicações precisam ser removidas antes dos membros e setores,
         * pois possuem relacionamento com ambos.
         */
        List<AplicacaoFormulario> aplicacoes =
                aplicacaoFormularioRepository
                        .findAllByEmpresaIdOrderByCriadoEmDesc(
                                empresaId
                        );

        aplicacaoFormularioRepository.deleteAll(aplicacoes);
        aplicacaoFormularioRepository.flush();

        /*
         * Recomendações apontam para avaliações de risco.
         * Portanto, recomendações devem ser removidas primeiro.
         */
        List<Recomendacao> recomendacoes =
                recomendacaoRepository
                        .findAllByAvaliacaoRisco_Setor_Empresa_Id(
                                empresaId
                        );

        recomendacaoRepository.deleteAll(recomendacoes);
        recomendacaoRepository.flush();

        List<AvaliacaoRisco> avaliacoes =
                avaliacaoRiscoRepository
                        .findAllBySetor_Empresa_Id(
                                empresaId
                        );

        avaliacaoRiscoRepository.deleteAll(avaliacoes);
        avaliacaoRiscoRepository.flush();

        /*
         * Remove denúncias e check-ins relacionados aos setores da empresa.
         */
        List<Denuncia> denuncias =
                denunciaRepository
                        .findAllBySetor_Empresa_Id(
                                empresaId
                        );

        denunciaRepository.deleteAll(denuncias);
        denunciaRepository.flush();

        List<CheckinHumor> checkins =
                checkinHumorRepository
                        .findAllBySetor_Empresa_Id(
                                empresaId
                        );

        checkinHumorRepository.deleteAll(checkins);
        checkinHumorRepository.flush();

        /*
         * RegistroPonto pode estar ligado diretamente à empresa
         * ou indiretamente por meio do setor.
         */
        List<RegistroPonto> registrosPonto =
                registroPontoRepository
                        .findAllVinculadosAEmpresa(
                                empresaId
                        );

        registroPontoRepository.deleteAll(registrosPonto);
        registroPontoRepository.flush();

        /*
         * Os membros precisam ser removidos antes dos cargos e setores.
         */
        List<MembroEmpresa> membros =
                membroEmpresaRepository
                        .findAllByEmpresaId(
                                empresaId
                        );

        membroEmpresaRepository.deleteAll(membros);
        membroEmpresaRepository.flush();

        /*
         * Ao excluir cargos como entidades, o Hibernate também remove
         * os registros da tabela cargo_permissoes.
         */
        List<Cargo> cargos =
                cargoRepository
                        .findAllByEmpresaIdOrderBySistemaDescNomeAsc(
                                empresaId
                        );

        cargoRepository.deleteAll(cargos);
        cargoRepository.flush();

        List<Setor> setores =
                setorRepository
                        .findAllByEmpresaIdOrderByNomeAsc(
                                empresaId
                        );

        setorRepository.deleteAll(setores);
        setorRepository.flush();

        /*
         * Por último, exclui a empresa.
         * As contas dos funcionários permanecem no sistema.
         */
        empresaRepository.delete(empresa);
        empresaRepository.flush();
    }
}