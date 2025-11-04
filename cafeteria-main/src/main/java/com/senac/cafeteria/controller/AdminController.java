package com.senac.cafeteria.controller;

import com.senac.cafeteria.models.ItemPedido;
import com.senac.cafeteria.models.Pedido;
import com.senac.cafeteria.models.Produto;
import com.senac.cafeteria.models.enums.StatusPedido;
import com.senac.cafeteria.services.PedidoService;
import com.senac.cafeteria.services.ProdutoService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private PedidoService pedidoService;

    @InitBinder
  public void initBinder(WebDataBinder binder) {
    // já existente: custom editor para BigDecimal
    binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
    // impede que o DataBinder tente atribuir request param "imagem" direto no produto.imagem
    binder.setDisallowedFields("imagem");
}
    // ========== PRODUTOS ==========
    @GetMapping("/produtos/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        return "admin/novo-produto";
    }

    @PostMapping("/produtos/novo")
    public String salvarProduto(@RequestParam String nome,
                               @RequestParam String descricao,
                               @RequestParam BigDecimal preco,
                               @RequestParam("imagem") MultipartFile imagem) throws IOException {

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);

        produtoService.salvarProduto(produto, imagem);
        return "redirect:/admin/produtos";
    }

    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        List<Produto> produtos = produtoService.listarTodos();

        for (Produto produto : produtos) {
            if (produto.getImagem() != null) {
                String base64Image = Base64.getEncoder().encodeToString(produto.getImagem());
                produto.setImagemBase64(base64Image);
            }
        }

        model.addAttribute("produtos", produtos);
        return "admin/listar-produtos";
    }

    @GetMapping("/produtos/editar/{id}")
    public String editarProdutoForm(@PathVariable Long id, Model model) {
        Produto produto = produtoService.buscarPorId(id);

        if (produto.getImagem() != null) {
            String base64Image = Base64.getEncoder().encodeToString(produto.getImagem());
            produto.setImagemBase64(base64Image);
        }

        model.addAttribute("produto", produto);
        return "admin/editar-produto";
    }

    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable Long id,
                                  @ModelAttribute Produto produto,
                                  @RequestParam("imagem") MultipartFile imagem) throws IOException {
        produtoService.atualizarProduto(id, produto, imagem);
        return "redirect:/admin/produtos";
    }

    @GetMapping("/produtos/excluir/{id}")
    public String excluirProduto(@PathVariable Long id) {
        produtoService.excluirProduto(id);
        return "redirect:/admin/produtos";
    }

    // ========== PEDIDOS ==========
    @GetMapping("/listar-pedidos")
    public String listarPedidos(@RequestParam(required = false) StatusPedido status, Model model) {
        System.out.println("=== LISTAR PEDIDOS CHAMADO ===");
        System.out.println("Status filtro: " + status);
        
        List<Pedido> pedidos;
        
        if (status != null) {
            pedidos = pedidoService.listarPedidosPorStatus(status);
            model.addAttribute("filtroAtivo", status);
            System.out.println("Pedidos filtrados por " + status + ": " + pedidos.size());
        } else {
            pedidos = pedidoService.listarTodosPedidos();
            System.out.println("Todos os pedidos: " + pedidos.size());
        }
        
        // Log para debug
        pedidos.forEach(p -> System.out.println("Pedido " + p.getId() + " - Status: " + p.getStatus()));
        
        model.addAttribute("pedidos", pedidos);
        return "admin/listar-pedidos";
    }

    @GetMapping("/listar-pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);
        return "admin/ver-pedido";
    }

    @GetMapping("/listar-pedidos/{id}/status")
    public String atualizarStatus(@PathVariable Long id, 
                                 @RequestParam StatusPedido status,
                                 RedirectAttributes redirectAttributes) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Status do pedido atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar status do pedido: " + e.getMessage());
        }
        return "redirect:/admin/listar-pedidos";
    }

    @GetMapping("/listar-pedidos/excluir/{id}")
    public String excluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.excluirPedido(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir pedido: " + e.getMessage());
        }
        return "redirect:/admin/listar-pedidos";
    }

    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
       
        
        long totalProdutos = produtoService.contarProdutos();
        List<Pedido> todosPedidos = pedidoService.listarTodosPedidos();
        long totalPedidos = todosPedidos.size();
        long pedidosPendentes = pedidoService.listarPedidosPorStatus(StatusPedido.PENDENTE).size();

        System.out.println("Total produtos: " + totalProdutos);
        System.out.println("Total pedidos: " + totalPedidos);
        System.out.println("Pedidos pendentes: " + pedidosPendentes);

        BigDecimal faturamentoTotal = calcularFaturamentoTotal(todosPedidos);
        BigDecimal faturamentoMes = calcularFaturamentoMes(todosPedidos);

        List<Pedido> pedidosRecentes = todosPedidos.stream()
                .limit(10)
                .collect(Collectors.toList());


        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosPendentes", pedidosPendentes);
        model.addAttribute("faturamentoTotal", faturamentoTotal);
        model.addAttribute("faturamentoMes", faturamentoMes);
        model.addAttribute("pedidosRecentes", pedidosRecentes);
        model.addAttribute("pedidosHoje", calcularPedidosHoje(todosPedidos));
        model.addAttribute("produtosVendidosHoje", calcularProdutosVendidosHoje(todosPedidos));

        return "admin/dashboard";
    }

    // ========== MÉTODOS AUXILIARES ==========
    private BigDecimal calcularFaturamentoTotal(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

   

    private BigDecimal calcularFaturamentoMes(List<Pedido> pedidos) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        return pedidos.stream()
                .filter(pedido -> pedido.getDataCriacao() != null && 
                                 !pedido.getDataCriacao().toLocalDate().isBefore(inicioMes))
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long calcularPedidosHoje(List<Pedido> pedidos) {
        LocalDate hoje = LocalDate.now();
        return pedidos.stream()
                .filter(pedido -> pedido.getDataCriacao() != null && 
                                 pedido.getDataCriacao().toLocalDate().equals(hoje))
                .count();
    }

    private long calcularProdutosVendidosHoje(List<Pedido> pedidos) {
        LocalDate hoje = LocalDate.now();
        return pedidos.stream()
                .filter(pedido -> pedido.getDataCriacao() != null && 
                                 pedido.getDataCriacao().toLocalDate().equals(hoje))
                .flatMap(pedido -> pedido.getItens().stream())
                .mapToInt(ItemPedido::getQuantidade)
                .sum();
    }

   

}