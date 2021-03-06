package com.illeanaplanzo.pontointeligente.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.illeanaplanzo.pontointeligente.api.dtos.LancamentoDto;
import com.illeanaplanzo.pontointeligente.api.entities.Funcionario;
import com.illeanaplanzo.pontointeligente.api.entities.Lancamento;
import com.illeanaplanzo.pontointeligente.api.enums.TipoEnum;
import com.illeanaplanzo.pontointeligente.api.response.Response;
import com.illeanaplanzo.pontointeligente.api.services.FuncionarioService;
import com.illeanaplanzo.pontointeligente.api.services.LancamentoService;

import io.swagger.annotations.ApiOperation;
@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {
	
	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;
	
	public LancamentoController() {
	}
	
	/**
	 * Retorna a listagem de lancamentos de um funcionario.
	 * 
	 * @param funcionarioId
	 * @param pag
	 * @param ord
	 * @param dir
	 * @return ResponseEntity<Response<Page<LancamentoDto>>>
	 */
	@ApiOperation(value = "Busca lan??amentos de um funcion??rio por ID")
	@GetMapping(value = "/funcionario/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDto>>> listarPorFuncionarioId(
			@PathVariable("funcionarioId") Long funcionarioId,
			@RequestParam(value = "pag", defaultValue = "0") int pag,
			@RequestParam(value = "ord", defaultValue = "id") String ord,
			@RequestParam(value = "dir", defaultValue = "DESC") String dir) {
		log.info("Buscando lancamentos por ID do funcionario: {}, pagina {}", funcionarioId, pag);
		Response<Page<LancamentoDto>> response = new Response<Page<LancamentoDto>>();
		
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(
				funcionarioId, PageRequest.of(pag, this.qtdPorPagina, Direction.valueOf(dir), ord));
		Page<LancamentoDto> lancamentosDto = lancamentos.map(lancamento -> this.converterLancamentoDto(lancamento));
		
		response.setData(lancamentosDto);
		return ResponseEntity.ok(response);
		
		
	}
	
	/**
	 * Retorna um lancamento por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<LancamentoDto>>
	 */
	@ApiOperation(value = "Buscar lancamento por ID")
	@GetMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> listarPorId(@PathVariable("id") Long id) {
		log.info("Buscando lan??amento por ID: {}", id);
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) { 
			log.info("Lan??amento n??o encontrado para o ID: {}", id);
			response.getErrors().add("Lan??amento n??o encontrado para o id " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		response.setData(this.converterLancamentoDto(lancamento.get()));
		return ResponseEntity.ok(response);
	
	}
	
	/**
	 * Adiciona um novo lancamento.
	 * 
	 * @param lancamentoDto
	 * @param result
	 * @return ResponseEntity<Response<LancamentoDto>> 
	 * @throws ParseException
	 */
	@ApiOperation(value = "Cria lancamento")
	@PostMapping
	public ResponseEntity<Response<LancamentoDto>> adicionar(@Valid @RequestBody LancamentoDto lancamentoDto,
			BindingResult result) throws ParseException {
		log.info("Adicionando lan??amento: {}", lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto, result);
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro validando lan??amento: {} ", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.converterLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 
	 * @param id
	 * @param lancamentoDto
	 * @param result
	 * @return ResponseEntity<Response<LancamentoDto>>
	 * @throws ParseException
	 */
	@ApiOperation(value = "Atualiza lancamento por ID")
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> atualizar(@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
		log.info("Atualizando lan??amento {}", lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto, result);
		lancamentoDto.setId(Optional.of(id));
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro v??lido lan??amento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
			
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.converterLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
		
	}
	
	/**
	 * Remove um lancamento por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<String>>
	 */
	@ApiOperation(value = "Remove lancamento por ID")
	@DeleteMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> remover (@PathVariable("id") Long id) {
		log.info("Removendo lan??amento: {}", id);
		Response<String> response =	new Response<String>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) {
			log.info("Erro ao remover devido lan??amento. Registro n??o encontrado para o id " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		this.lancamentoService.remover(id);
		return ResponseEntity.ok(new Response<String>());
	}
	
	
	
	/**
	 * 
	 * @param lancamentoDto
	 * @param result
	 * @return lancamento
	 */
	private Lancamento converterDtoParaLancamento(@Valid LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
		Lancamento lancamento = new Lancamento();
		
		if (lancamentoDto.getId().isPresent()) {
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getId().get());
			if (lanc.isPresent()) {
				lancamento = lanc.get();
			} else {
				result.addError(new ObjectError("lancamento", "Lan??amento n??o encontrado."));
			}
		} else {
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
		}
		
		lancamento.setDescricao(lancamentoDto.getDescricao());
		lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));
		
		if (EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
		} else { 
			result.addError(new ObjectError("tipo", "Tipo inv??lido."));
		}
		
		return lancamento;
	}
	
	/**
	 * Valida um funcionario verificando se ele ?? existente e v??lido no sistema.
	 * 
	 * @param lancamentoDto
	 * @param result
	 */
	private void validarFuncionario(@Valid LancamentoDto lancamentoDto, BindingResult result) {
		if (lancamentoDto.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcion??rio n??o informado."));
			return;
		}
		
		log.info("validando funcion??rio id {}: ", lancamentoDto.getFuncionarioId());
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDto.getFuncionarioId());
		if(!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcion??rio n??o encontrado. ID inexistente."));
		}
	}

	/**
	 * Converte uma entidade lancamento em seu respectivo DTO.
	 * 
	 * @param lancamento
	 * @return LancamentoDto
	 */
	private LancamentoDto converterLancamentoDto(Lancamento lancamento) {
		LancamentoDto lancamentoDto = new LancamentoDto();
		lancamentoDto.setId(Optional.of(lancamento.getId()));
		lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDto.setTipo(lancamento.getTipo().toString());
		lancamentoDto.setDescricao(lancamento.getDescricao());
		lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());
		
		return lancamentoDto;
	}
	
	
	
}
