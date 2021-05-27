package com.illeanaplanzo.pontointeligente.api.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.illeanaplanzo.pontointeligente.api.entities.Empresa;

@SpringBootTest
@ActiveProfiles("test")
public class EmpresaRepositoryTest {
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	private static final String CNPJ = "514636450000100";
	
	@BeforeEach
	public void setUp() throws Exception {
		
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Empresa Teste");
		empresa.setCnpj(CNPJ);
		this.empresaRepository.save(empresa);
		
	}
	
	@AfterEach
	public final void tearDown() {
		this.empresaRepository.deleteAll();
	}
	
	@Test
	public void testBuscarPorCnpj() {
		Empresa empresa = this.empresaRepository.findByCnpj(CNPJ);
		
		Assertions.assertEquals(CNPJ, empresa.getCnpj());
	}

}
