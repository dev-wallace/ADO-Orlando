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

/*
 * Controller responsável pelas páginas e ações administrativas (produtos, pedidos, dashboard).
 * Contém handlers para CRUD de produtos, listagem/alteração de pedidos e geração de dados do dashboard.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    // Serviço para operações relacionadas a produtos (salvar, listar, atualizar, excluir)
    @Autowired
    private ProdutoService produtoService;

    // Serviço para operações relacionadas a pedidos (listar, atualizar status, excluir)
    @Autowired
    private PedidoService pedidoService;

    /*
     * Configurações do binder para conversão de tipos vindos do formulário.
     * Aqui é registrado um editor para BigDecimal e é proibida a binding direta do campo 'imagem'
     * para evitar sobrescrever o array de bytes por binding automático.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        binder.setDisallowedFields("imagem");
    }

    // ========== PRODUTOS ==========

    // Formulário para criar um novo produto
    @GetMapping("/produtos/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto()); // adiciona objeto vazio ao template
        return "admin/novo-produto";
    }

    // Salva um novo produto recebido do formulário (inclui upload de imagem)
    @PostMapping("/produtos/novo")
    public String salvarProduto(@RequestParam String nome,
                                @RequestParam String descricao,
                                @RequestParam BigDecimal preco,
                                @RequestParam("imagem") MultipartFile imagem) throws IOException {

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);

        // Delega ao serviço a persistência e tratamento da imagem
        produtoService.salvarProduto(produto, imagem);
        return "redirect:/admin/produtos";
    }

    // Lista todos os produtos mostrando imagem como Base64 para exibição no template
    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        List<Produto> produtos = produtoService.listarTodos();

        for (Produto produto : produtos) {
            if (produto.getImagem() != null) {
                String base64Image = Base64.getEncoder().encodeToString(produto.getImagem());
                produto.setImagemBase64(base64Image); // seta campo transitório para view
            }
        }

        model.addAttribute("produtos", produtos);
        return "admin/listar-produtos";
    }

    // Formulário para editar um produto existente (carrega produto por id)
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

    // Atualiza um produto a partir do formulário (pode incluir nova imagem)
    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable Long id,
                                   @ModelAttribute Produto produto,
                                   @RequestParam("imagem") MultipartFile imagem) throws IOException {
        produtoService.atualizarProduto(id, produto, imagem);
        return "redirect:/admin/produtos";
    }

    // Exclui um produto por id
    @GetMapping("/produtos/excluir/{id}")
    public String excluirProduto(@PathVariable Long id) {
        produtoService.excluirProduto(id);
        return "redirect:/admin/produtos";
    }

    // ========== PEDIDOS ==========

    /*
     * Lista pedidos filtrando por status opcional.
     * Adiciona logs simples para auxiliar debug local.
     */
    @GetMapping("/pedidos")
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

    // Visualiza detalhes de um pedido específico
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);
        return "admin/ver-pedido";
    }

    // Atualiza o status de um pedido e adiciona mensagem flash de sucesso/erro
    @GetMapping("/pedidos/{id}/status")
    public String atualizarStatus(@PathVariable Long id,
                                  @RequestParam StatusPedido status,
                                  RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ATUALIZANDO STATUS DO PEDIDO ===");
            System.out.println("Pedido ID: " + id);
            System.out.println("Novo Status: " + status);

            pedidoService.atualizarStatus(id, status);

            redirectAttributes.addFlashAttribute("sucesso", "Status do pedido #" + id + " atualizado para " + status + "!");
        } catch (Exception e) {
            System.err.println("ERRO ao atualizar status: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar status do pedido: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }

    // Exclui um pedido e informa resultado via flash attributes
    @GetMapping("/pedidos/excluir/{id}")
    public String excluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.excluirPedido(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir pedido: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }

    // ========== DASHBOARD ==========

    // Página do dashboard que agrega métricas (total produtos, pedidos, faturamento, etc.)
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

    // Soma total de todos os pedidos
    private BigDecimal calcularFaturamentoTotal(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Soma do faturamento do mês corrente
    private BigDecimal calcularFaturamentoMes(List<Pedido> pedidos) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        return pedidos.stream()
                .filter(pedido -> pedido.getDataCriacao() != null &&
                        !pedido.getDataCriacao().toLocalDate().isBefore(inicioMes))
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Quantidade de pedidos do dia atual
    private long calcularPedidosHoje(List<Pedido> pedidos) {
        LocalDate hoje = LocalDate.now();
        return pedidos.stream()
                .filter(pedido -> pedido.getDataCriacao() != null &&
                        pedido.getDataCriacao().toLocalDate().equals(hoje))
                .count();
    }

    // Soma de itens vendidos hoje (quantidades) a partir dos pedidos do dia
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
