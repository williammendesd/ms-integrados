package br.com.fiap.ms_pagamento.service;

import br.com.fiap.ms_pagamento.dto.PagamentoDTO;
import br.com.fiap.ms_pagamento.http.PedidoClient;
import br.com.fiap.ms_pagamento.model.Pagamento;
import br.com.fiap.ms_pagamento.model.Status;
import br.com.fiap.ms_pagamento.repository.PagamentoRepository;
import br.com.fiap.ms_pagamento.service.exception.DatabaseException;
import br.com.fiap.ms_pagamento.service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PagamentoService {

    @Autowired
    private PedidoClient pedidoClient;

    @Autowired
    private PagamentoRepository repository;

    @Transactional(readOnly = true)
    public List<PagamentoDTO> findAll() {
        return repository.findAll().stream()
                .map(PagamentoDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagamentoDTO findById(Long id) {
        Pagamento entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado! Id: " + id)
        );

        return new PagamentoDTO(entity);
    }

    @Transactional
    public PagamentoDTO insert(PagamentoDTO dto) {
        Pagamento entity = new Pagamento();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new PagamentoDTO(entity);
    }

    @Transactional
    public PagamentoDTO update(Long id, PagamentoDTO dto) {
        try {
            Pagamento entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new PagamentoDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado! Id: " + id);
        }
    }


    //PROPAGATION_SUPPORTS: Mantém uma transação existente,
    // executa o método sem transação caso nenhuma exista
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado! Id: " + id);
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    @Transactional
    public void confirmarPagamentoDePedido(Long id){

        Optional<Pagamento> pagamento = repository.findById(id);

        if(pagamento.isEmpty()){
            throw new ResourceNotFoundException("Recurso não encontrado! Id: " + id);
        }
        //Set status do pagamento para CONFIRMADO
        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        //chama o pedidoClient para fazer a atualização passando o ID do pedido.
        //Passamos o ID do pedido e quem tem essa informação é o pagamento.get()getPedidoId().
        // Esse get() é que possui a informação do pedido. pedidoClient foi injetado na dependência
        pedidoClient.atualizarPagamentoDoPedido(pagamento.get().getPedidoId());
    }

    public void alterarStatusDoPagamento(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);

        if(pagamento.isEmpty()){
            throw new ResourceNotFoundException("Recurso não encontrado! id: "+id);
        }
        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());
    }

    private void copyDtoToEntity(PagamentoDTO dto, Pagamento entity) {
        entity.setValor(dto.getValor());
        entity.setNome(dto.getNome());
        entity.setNumeroDoCartao(dto.getNumeroDoCartao());
        entity.setValidade(dto.getValidade());
        entity.setCodigoDeSeguranca(dto.getCodigoDeSeguranca());
        entity.setStatus(dto.getStatus());
        entity.setPedidoId(dto.getPedidoId());
        entity.setFormaDePagamentoId(dto.getFormaDePagamentoId());
    }

    //    @Transactional(readOnly = true)
//    public Page<PagamentoDTO> findAll(Pageable pageable){
//        Page<Pagamento> page = repository.findAll(pageable);
//        return page.map(PagamentoDTO::new);
//    }
}
