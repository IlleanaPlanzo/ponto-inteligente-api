package com.illeanaplanzo.pontointeligente.api.services;

import java.util.Optional;

import com.illeanaplanzo.pontointeligente.api.entities.Empresa;

public interface EmpresaService {
	
	Optional<Empresa> buscarPorCnpj(String cnpj);
	
	Empresa persistir(Empresa empresa);

}
