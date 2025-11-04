package com.senac.cafeteria.repositories;

import com.senac.cafeteria.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    // Buscar produtos por nome (ignorando maiúsculas/minúsculas)
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    
    // Buscar produto por nome exato
    Optional<Produto> findByNome(String nome);
    
    // Buscar produtos ordenados por preço (crescente)
    List<Produto> findAllByOrderByPrecoAsc();
    
    // Buscar produtos ordenados por preço (decrescente)
    List<Produto> findAllByOrderByPrecoDesc();
    
    // Buscar produtos com preço maior que
    List<Produto> findByPrecoGreaterThan(Double preco);
    
    // Buscar produtos com preço entre valores
    List<Produto> findByPrecoBetween(Double precoMin, Double precoMax);
    
    // Query personalizada com JPQL
    @Query("SELECT p FROM Produto p WHERE p.descricao LIKE %:termo%")
    List<Produto> buscarPorDescricao(@Param("termo") String termo);
}