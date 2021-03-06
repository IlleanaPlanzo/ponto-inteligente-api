package com.illeanaplanzo.pontointeligente.api.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.Override;

import com.illeanaplanzo.pontointeligente.api.entities.Empresa;
import com.illeanaplanzo.pontointeligente.api.repositories.EmpresaRepository;
import com.illeanaplanzo.pontointeligente.api.services.EmpresaService;

@Service
public class EmpresaServiceImpl implements EmpresaService {
	
	public static final Logger log = LoggerFactory.getLogger(EmpresaServiceImpl.class);
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	@Override
	public Optional<Empresa> buscarPorCnpj(String cnpj) {
		log.info("Buscando uma empresa para a CNPJ {}", cnpj);
		return Optional.ofNullable(empresaRepository.findByCnpj(cnpj));
		
	}
	
	@Override
	public Empresa persistir(Empresa empresa) {
		log.info("Persistindo uma empresa: {}", empresa);
		return this.empresaRepository.save(empresa);
	}

}
