package com.senac.cafeteria.repositories;

import com.senac.cafeteria.models.Pedido;
import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.models.enums.StatusPedido;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByDataCriacaoDesc(Usuario usuario);
    List<Pedido> findByStatusOrderByDataCriacaoDesc(StatusPedido status); // Mude Asc para Desc
    List<Pedido> findAllByOrderByDataCriacaoDesc();
}