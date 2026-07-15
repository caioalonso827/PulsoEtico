package com.pulsoetico.pulsoetico.configs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.FormularioModelo;
import com.pulsoetico.pulsoetico.models.PerguntaFormulario;
import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;
import com.pulsoetico.pulsoetico.repositories.FormularioModeloRepository;

@Component
public class FormularioSeeder implements CommandLineRunner {

    private final FormularioModeloRepository formularioRepository;

    public FormularioSeeder(
            FormularioModeloRepository formularioRepository
    ) {
        this.formularioRepository = formularioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        cadastrarAssedioDiscriminacao();
        cadastrarExaustaoTrabalho();
        cadastrarCargaRitmoTrabalho();
    }

    private void cadastrarAssedioDiscriminacao() {

        cadastrarSeNaoExistir(
                TipoFormularioPsicossocial.ASSEDIO_DISCRIMINACAO,
                "Assédio e discriminação",
                """
                Avaliação de situações de desrespeito, intimidação,
                humilhação, assédio e discriminação no ambiente de trabalho.
                As respostas devem ser utilizadas apenas para análise coletiva.
                """,
                List.of(
                        """
                        Fui alvo de humilhações, piadas ofensivas ou
                        constrangimentos no ambiente de trabalho.
                        """,

                        """
                        Presenciei colegas sendo tratados de forma
                        desrespeitosa no ambiente de trabalho.
                        """,

                        """
                        Senti medo de sofrer retaliação ao relatar
                        um comportamento inadequado.
                        """,

                        """
                        Percebi tratamento diferente por raça, gênero,
                        idade, religião, deficiência ou outra característica
                        pessoal.
                        """,

                        """
                        Recebi comentários ofensivos sobre minha aparência,
                        identidade ou vida pessoal.
                        """,

                        """
                        Senti-me intimidado por alguém que ocupava
                        uma posição de autoridade.
                        """,

                        """
                        Presenciei pessoas sendo excluídas deliberadamente
                        de atividades, reuniões ou decisões.
                        """,

                        """
                        Considerei que os canais de denúncia da empresa
                        não eram seguros ou confiáveis.
                        """
                )
        );
    }

    private void cadastrarExaustaoTrabalho() {

        cadastrarSeNaoExistir(
                TipoFormularioPsicossocial.EXAUSTAO_TRABALHO,
                "Exaustão relacionada ao trabalho",
                """
                Avaliação de sinais de cansaço, desgaste e dificuldade
                de recuperação relacionados às condições de trabalho.
                Este formulário não representa diagnóstico médico ou psicológico.
                """,
                List.of(
                        """
                        Termino minha jornada sentindo-me completamente
                        esgotado.
                        """,

                        """
                        Tenho dificuldade para recuperar minha energia
                        entre um dia de trabalho e outro.
                        """,

                        """
                        Começo minha jornada de trabalho já me sentindo
                        cansado.
                        """,

                        """
                        As preocupações com o trabalho prejudicam
                        meu descanso ou meu sono.
                        """,

                        """
                        Tenho dificuldade de concentração devido ao
                        cansaço relacionado ao trabalho.
                        """,

                        """
                        Tenho me sentido mais irritado ou impaciente
                        por causa do trabalho.
                        """,

                        """
                        Sinto que não tenho energia suficiente para
                        realizar minhas atividades.
                        """,

                        """
                        Sinto que preciso manter um esforço maior
                        do que consigo sustentar.
                        """
                )
        );
    }

    private void cadastrarCargaRitmoTrabalho() {

        cadastrarSeNaoExistir(
                TipoFormularioPsicossocial.CARGA_RITMO_TRABALHO,
                "Carga e ritmo de trabalho",
                """
                Avaliação do volume de tarefas, dos prazos, das pausas
                e do ritmo exigido durante a jornada de trabalho.
                """,
                List.of(
                        """
                        A quantidade de tarefas é maior do que consigo
                        realizar durante minha jornada.
                        """,

                        """
                        Os prazos recebidos são incompatíveis com
                        o volume de trabalho.
                        """,

                        """
                        Preciso trabalhar em ritmo excessivamente
                        acelerado.
                        """,

                        """
                        Recebo várias demandas importantes ao mesmo
                        tempo.
                        """,

                        """
                        Não consigo realizar pausas suficientes durante
                        minha jornada.
                        """,

                        """
                        Preciso fazer horas extras frequentemente para
                        concluir minhas tarefas.
                        """,

                        """
                        As prioridades mudam sem que eu tenha tempo
                        suficiente para me adaptar.
                        """,

                        """
                        A quantidade de pessoas na equipe é insuficiente
                        para o volume de trabalho.
                        """
                )
        );
    }

    private void cadastrarSeNaoExistir(
            TipoFormularioPsicossocial tipo,
            String titulo,
            String descricao,
            List<String> textosPerguntas
    ) {

        if (formularioRepository.existsByTipo(tipo)) {
            return;
        }

        FormularioModelo formulario =
                FormularioModelo.builder()
                        .tipo(tipo)
                        .titulo(titulo.trim())
                        .descricao(descricao.trim())
                        .ativo(true)
                        .perguntas(new ArrayList<>())
                        .build();

        for (int indice = 0;
             indice < textosPerguntas.size();
             indice++) {

            PerguntaFormulario pergunta =
                    PerguntaFormulario.builder()
                            .formulario(formulario)
                            .texto(
                                    textosPerguntas
                                            .get(indice)
                                            .trim()
                            )
                            .ordem(indice + 1)
                            .build();

            formulario.getPerguntas().add(pergunta);
        }

        formularioRepository.save(formulario);
    }
}