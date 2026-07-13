package com.pulsoetico.pulsoetico.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

/**
 * Cria o primeiro usuário GESTOR na inicialização, se ainda não existir nenhum.
 *
 * Credenciais do gestor semente (TROQUE a senha depois de logar pela primeira vez):
 *   email: [email protected]
 *   senha: senha123
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final FuncionarioRepository funcionarioRepository;
    private final SetorRepository setorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            FuncionarioRepository funcionarioRepository,
            SetorRepository setorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.setorRepository = setorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean jaExisteGestor = funcionarioRepository.findByEmailWithSetor  ("[email protected]").isPresent();
        if (jaExisteGestor) {
            return;
        }

        Setor setorAdmin = setorRepository.findByNome("Recursos Humanos")
                .orElseGet(() -> setorRepository.save(
                        Setor.builder().nome("Recursos Humanos").quantidadeColaboradores(12).build()));

        Funcionario gestor = Funcionario.builder()
                .nomeCompleto("Gestor Inicial")
                .email("[email protected]")
                .senha(passwordEncoder.encode("senha123"))
                .matricula("ADM-0001")
                .papel(Funcionario.Papel.GESTOR)
                .setor(setorAdmin)
                .build();

        funcionarioRepository.save(gestor);
        System.out.println(">>> Gestor semente criado: [email protected] / senha123 (TROQUE essa senha depois)");
    }
}
