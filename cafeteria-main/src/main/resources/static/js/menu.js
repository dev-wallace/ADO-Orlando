// menu.js - Funcionalidades para a página de menu

document.addEventListener('DOMContentLoaded', function() {
    // Filtro de preço
    const priceRange = document.getElementById('priceRange');
    if (priceRange) {
        priceRange.addEventListener('input', function() {
            filterProductsByPrice(this.value);
        });
    }
    
    // Busca de produtos
    const searchInput = document.querySelector('.search-box input');
    const searchButton = document.querySelector('.btn-search');
    
    if (searchInput && searchButton) {
        searchButton.addEventListener('click', function() {
            searchProducts(searchInput.value);
        });
        
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchProducts(this.value);
            }
        });
    }
    
    // Adicionar ao carrinho
    const addToCartButtons = document.querySelectorAll('.btn-add-cart');
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            addToCart(productId);
        });
    });
    
    // Filtro de categorias
    const categoryLinks = document.querySelectorAll('.category-list a');
    categoryLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remover classe active de todos os links
            categoryLinks.forEach(l => l.parentElement.classList.remove('active'));
            
            // Adicionar classe active ao link clicado
            this.parentElement.classList.add('active');
            
            const category = this.textContent;
            filterProductsByCategory(category);
        });
    });
});

// Função para filtrar produtos por preço
function filterProductsByPrice(maxPrice) {
    console.log(`Filtrando produtos até R$ ${maxPrice}`);
    // Implementar lógica de filtro real aqui
    // Esta é uma implementação simplificada
    const products = document.querySelectorAll('.product-card');
    
    products.forEach(product => {
        const priceText = product.querySelector('.product-price').textContent;
        const price = parseFloat(priceText.replace('R$ ', '').replace(',', '.'));
        
        if (price <= maxPrice) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
}

// Função para buscar produtos
function searchProducts(query) {
    console.log(`Buscando por: ${query}`);
    // Implementar lógica de busca real aqui
    const products = document.querySelectorAll('.product-card');
    
    products.forEach(product => {
        const productName = product.querySelector('h3').textContent.toLowerCase();
        const productDesc = product.querySelector('.product-desc').textContent.toLowerCase();
        
        if (productName.includes(query.toLowerCase()) || productDesc.includes(query.toLowerCase())) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
}

// Função para adicionar ao carrinho
function addToCart(productId) {
    console.log(`Adicionando produto ${productId} ao carrinho`);
    
    // Recuperar carrinho atual do localStorage ou inicializar vazio
    let cart = JSON.parse(localStorage.getItem('carrinho')) || {};
    
    // Adicionar produto ao carrinho
    if (cart[productId]) {
        cart[productId] += 1;
    } else {
        cart[productId] = 1;
    }
    
    // Salvar carrinho atualizado no localStorage
    localStorage.setItem('carrinho', JSON.stringify(cart));
    
    // Feedback visual para o usuário
    showNotification('Produto adicionado ao carrinho!');
}

// Função para filtrar por categoria
function filterProductsByCategory(category) {
    console.log(`Filtrando por categoria: ${category}`);
    // Implementação simplificada - em um caso real, isso viria do backend
    const products = document.querySelectorAll('.product-card');
    
    if (category === 'Todos os produtos') {
        products.forEach(product => {
            product.style.display = 'block';
        });
        return;
    }
    
    // Esta é uma demonstração simplificada
    // Em uma aplicação real, você teria dados de categoria nos produtos
    products.forEach((product, index) => {
        if (index % 2 === 0) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
}

// Função para mostrar notificação
function showNotification(message) {
    // Criar elemento de notificação
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.style.position = 'fixed';
    notification.style.bottom = '20px';
    notification.style.right = '20px';
    notification.style.backgroundColor = '#0e5f15ff';
    notification.style.color = 'white';
    notification.style.padding = '10px 20px';
    notification.style.borderRadius = '5px';
    notification.style.zIndex = '1000';
    notification.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
    
    // Adicionar ao documento
    document.body.appendChild(notification);
    
    // Remover após 3 segundos
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transition = 'opacity 0.5s';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 500);
    }, 3000);
}

function showNotification(message) {
  const notification = document.createElement('div');
  notification.className = 'notification';
  notification.textContent = message;

  document.body.appendChild(notification);

  setTimeout(() => {
    notification.style.opacity = '0';
    notification.style.transition = 'opacity 0.6s';
    setTimeout(() => notification.remove(), 600);
  }, 3000);
}
