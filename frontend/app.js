const API_BASE = 'http://localhost:7000/api';

// Elementos do DOM
const tabButtons = document.querySelectorAll('.tab-button');
const tabContents = document.querySelectorAll('.tab-content');
const formProduto = document.getElementById('formProduto');
const formPedido = document.getElementById('formPedido');
const formEditProduto = document.getElementById('formEditProduto');
const produtosList = document.getElementById('produtosList');
const pedidosList = document.getElementById('pedidosList');
const pedidoProdutoId = document.getElementById('pedidoProdutoId');
const modalEditProduto = document.getElementById('modalEditProduto');
const closeModal = document.querySelector('.close');

// Estado global
let produtos = [];
let pedidos = [];

// ============ INICIALIZAÇÃO ============
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    carregarProdutos();
    carregarPedidos();
    // Recarregar a cada 5 segundos
    setInterval(carregarProdutos, 5000);
    setInterval(carregarPedidos, 5000);
});

// ============ EVENT LISTENERS ============
function setupEventListeners() {
    // Abas
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.dataset.tab;
            switchTab(tabName);
        });
    });

    // Formulários
    formProduto.addEventListener('submit', criarProduto);
    formPedido.addEventListener('submit', criarPedido);
    formEditProduto.addEventListener('submit', atualizarProduto);

    // Modal
    closeModal.addEventListener('click', () => {
        modalEditProduto.classList.remove('show');
    });

    window.addEventListener('click', (event) => {
        if (event.target === modalEditProduto) {
            modalEditProduto.classList.remove('show');
        }
    });
}

function switchTab(tabName) {
    // Desativa todas as abas
    tabContents.forEach(content => content.classList.remove('active'));
    tabButtons.forEach(button => button.classList.remove('active'));

    // Ativa a aba selecionada
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
}

// ============ PRODUTOS ============
async function carregarProdutos() {
    try {
        const response = await fetch(`${API_BASE}/produtos`);
        if (!response.ok) throw new Error('Erro ao carregar produtos');
        
        produtos = await response.json();
        renderizarProdutos();
        atualizarSelectProdutos();
        mostrarAlerta('Produtos carregados com sucesso', 'info');
    } catch (error) {
        console.error('Erro:', error);
        produtosList.innerHTML = `<p class="alert alert-error">Erro ao carregar produtos: ${error.message}</p>`;
    }
}

function renderizarProdutos() {
    if (produtos.length === 0) {
        produtosList.innerHTML = '<p>Nenhum produto cadastrado.</p>';
        return;
    }

    produtosList.innerHTML = produtos.map(produto => `
        <div class="item-card">
            <h4>${produto.nome}</h4>
            <div class="info-row">
                <span class="label">Preço:</span>
                <span class="value">R$ ${parseFloat(produto.preco).toFixed(2)}</span>
            </div>
            <div class="info-row">
                <span class="label">Categoria:</span>
                <span>${produto.categoria}</span>
            </div>
            <div class="info-row">
                <span class="label">Estoque:</span>
                <span class="${produto.estoque <= 5 ? 'stock-low' : 'stock-normal'}">
                    ${produto.estoque} unidades
                </span>
            </div>
            <div class="info-row">
                <span class="label">ID:</span>
                <span class="value" style="font-size: 0.85em;">${produto.id}</span>
            </div>
            <div class="item-actions">
                <button class="btn btn-secondary" onclick="abrirEditarProduto('${produto.id}', '${produto.nome}', ${produto.preco}, '${produto.categoria}', ${produto.estoque})">
                    Editar
                </button>
                <button class="btn btn-danger" onclick="deletarProduto('${produto.id}')">
                    Deletar
                </button>
            </div>
        </div>
    `).join('');
}

async function criarProduto(e) {
    e.preventDefault();

    const nome = document.getElementById('produtoNome').value;
    const preco = parseFloat(document.getElementById('produtoPreco').value);
    const categoria = document.getElementById('produtoCategoria').value;
    const estoque = parseInt(document.getElementById('produtoEstoque').value);

    try {
        const response = await fetch(`${API_BASE}/produtos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, preco, categoria, estoque })
        });

        if (!response.ok) throw new Error('Erro ao criar produto');

        mostrarAlerta('Produto criado com sucesso!', 'success');
        formProduto.reset();
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`Erro: ${error.message}`, 'error');
    }
}

function abrirEditarProduto(id, nome, preco, categoria, estoque) {
    document.getElementById('editProdutoId').value = id;
    document.getElementById('editProdutoNome').value = nome;
    document.getElementById('editProdutoPreco').value = preco;
    document.getElementById('editProdutoCategoria').value = categoria;
    document.getElementById('editProdutoEstoque').value = estoque;
    modalEditProduto.classList.add('show');
}

async function atualizarProduto(e) {
    e.preventDefault();

    const id = document.getElementById('editProdutoId').value;
    const nome = document.getElementById('editProdutoNome').value;
    const preco = parseFloat(document.getElementById('editProdutoPreco').value);
    const categoria = document.getElementById('editProdutoCategoria').value;
    const estoque = parseInt(document.getElementById('editProdutoEstoque').value);

    try {
        const response = await fetch(`${API_BASE}/produtos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, preco, categoria, estoque })
        });

        if (!response.ok) throw new Error('Erro ao atualizar produto');

        mostrarAlerta('Produto atualizado com sucesso!', 'success');
        modalEditProduto.classList.remove('show');
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`Erro: ${error.message}`, 'error');
    }
}

async function deletarProduto(id) {
    if (!confirm('Tem certeza que deseja deletar este produto?')) return;

    try {
        const response = await fetch(`${API_BASE}/produtos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Erro ao deletar produto');

        mostrarAlerta('Produto deletado com sucesso!', 'success');
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`Erro: ${error.message}`, 'error');
    }
}

function atualizarSelectProdutos() {
    const currentValue = pedidoProdutoId.value;
    pedidoProdutoId.innerHTML = '<option value="">Selecione um produto...</option>';
    
    produtos.forEach(produto => {
        const option = document.createElement('option');
        option.value = produto.id;
        option.textContent = `${produto.nome} (R$ ${parseFloat(produto.preco).toFixed(2)}, ${produto.estoque} em estoque)`;
        pedidoProdutoId.appendChild(option);
    });

    pedidoProdutoId.value = currentValue;
}

// ============ PEDIDOS ============
async function carregarPedidos() {
    try {
        const response = await fetch(`${API_BASE}/pedidos`);
        if (!response.ok) throw new Error('Erro ao carregar pedidos');

        pedidos = await response.json();
        renderizarPedidos();
    } catch (error) {
        console.error('Erro:', error);
        pedidosList.innerHTML = `<p class="alert alert-error">Erro ao carregar pedidos: ${error.message}</p>`;
    }
}

function renderizarPedidos() {
    if (pedidos.length === 0) {
        pedidosList.innerHTML = '<p>Nenhum pedido cadastrado.</p>';
        return;
    }

    pedidosList.innerHTML = pedidos.map(pedido => `
        <div class="item-card">
            <h4>Pedido #${pedido.id.substring(0, 8)}</h4>
            <div class="info-row">
                <span class="label">Cliente:</span>
                <span>${pedido.nomeCliente}</span>
            </div>
            <div class="info-row">
                <span class="label">Status:</span>
                <span class="status-badge ${pedido.status === 'ABERTO' ? 'status-aberto' : 'status-fechado'}">
                    ${pedido.status}
                </span>
            </div>
            <div class="info-row">
                <span class="label">Data:</span>
                <span>${new Date(pedido.dataPedido).toLocaleDateString('pt-BR')}</span>
            </div>
            <div class="info-row">
                <span class="label">Observação:</span>
                <span>${pedido.observacao || '-'}</span>
            </div>
            <div class="item-actions">
                <button class="btn btn-danger" onclick="deletarPedido('${pedido.id}')">
                    Deletar
                </button>
            </div>
        </div>
    `).join('');
}

async function criarPedido(e) {
    e.preventDefault();

    const produtoId = document.getElementById('pedidoProdutoId').value;
    const quantidade = parseInt(document.getElementById('pedidoQuantidade').value);
    const nomeCliente = document.getElementById('pedidoCliente').value;
    const observacao = document.getElementById('pedidoObservacao').value;

    if (!produtoId) {
        mostrarAlerta('Selecione um produto!', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/integrado/pedidos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                produtoId,
                quantidade,
                nomeCliente,
                observacao,
                status: 'ABERTO'
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.mensagem || 'Erro ao criar pedido');
        }

        mostrarAlerta('Pedido criado com sucesso! Estoque foi decrementado.', 'success');
        formPedido.reset();
        carregarPedidos();
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`Erro: ${error.message}`, 'error');
    }
}

async function deletarPedido(id) {
    if (!confirm('Tem certeza que deseja deletar este pedido?')) return;

    try {
        const response = await fetch(`${API_BASE}/pedidos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Erro ao deletar pedido');

        mostrarAlerta('Pedido deletado com sucesso!', 'success');
        carregarPedidos();
    } catch (error) {
        mostrarAlerta(`Erro: ${error.message}`, 'error');
    }
}

// ============ UTILITÁRIOS ============
function mostrarAlerta(mensagem, tipo) {
    const alerta = document.createElement('div');
    alerta.className = `alert alert-${tipo}`;
    alerta.textContent = mensagem;
    alerta.style.position = 'fixed';
    alerta.style.top = '20px';
    alerta.style.right = '20px';
    alerta.style.zIndex = '2000';
    alerta.style.maxWidth = '400px';

    document.body.appendChild(alerta);

    setTimeout(() => {
        alerta.remove();
    }, 4000);
}
