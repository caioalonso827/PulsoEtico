package com.pulsoetico.pulsoetico.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;

@Service
public class FuncionarioDetailsService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioDetailsService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return funcionarioRepository.findByEmailWithSetor(email)
                .map(FuncionarioUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado: " + email));
    }
}
