const API_BASE = 'http://localhost:7000/api';

// Elementos do DOM
const tabButtons = document.querySelectorAll('.tab-button');
const tabContents = document.querySelectorAll('.tab-content');
const formProduto = document.getElementById('formProduto');
const formPedido = document.getElementById('formPedido');
const formEditPedido = document.getElementById('formEditPedido');
const formEditProduto = document.getElementById('formEditProduto');
const produtosList = document.getElementById('produtosList');
const pedidosList = document.getElementById('pedidosList');
const pedidoProdutoId = document.getElementById('pedidoProdutoId');
const filtroProdutoNome = document.getElementById('filtroProdutoNome');
const filtroProdutoCategoria = document.getElementById('filtroProdutoCategoria');
const filtroPedidoCliente = document.getElementById('filtroPedidoCliente');
const filtroPedidoStatus = document.getElementById('filtroPedidoStatus');
const produtosPrev = document.getElementById('produtosPrev');
const produtosNext = document.getElementById('produtosNext');
const pedidosPrev = document.getElementById('pedidosPrev');
const pedidosNext = document.getElementById('pedidosNext');
const produtosPageInfo = document.getElementById('produtosPageInfo');
const pedidosPageInfo = document.getElementById('pedidosPageInfo');

// Modais
const modalCriarProduto = document.getElementById('modalCriarProduto');
const modalCriarPedido = document.getElementById('modalCriarPedido');
const modalEditPedido = document.getElementById('modalEditPedido');
const modalEditProduto = document.getElementById('modalEditProduto');

// Estado global
let produtos = [];
let pedidos = [];
let paginaAtualProdutos = 1;
let paginaAtualPedidos = 1;

const ITENS_POR_PAGINA_PRODUTOS = 8;
const ITENS_POR_PAGINA_PEDIDOS = 8;

// ============ INICIALIZAÇÃO ============
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    carregarProdutos();
    carregarPedidos();
    // Recarregar a cada 10 segundos
    setInterval(carregarProdutos, 10000);
    setInterval(carregarPedidos, 10000);
});

// ============ EVENT LISTENERS ============
function setupEventListeners() {
    // Abas
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.dataset.tab;
            switchTab(tabName, button);
        });
    });

    // Formulários
    formProduto.addEventListener('submit', criarProduto);
    formPedido.addEventListener('submit', criarPedido);
    formEditPedido.addEventListener('submit', atualizarStatusPedido);
    formEditProduto.addEventListener('submit', atualizarProduto);

    // Filtros de produtos
    filtroProdutoNome?.addEventListener('input', () => {
        paginaAtualProdutos = 1;
        renderizarProdutos();
    });
    filtroProdutoCategoria?.addEventListener('change', () => {
        paginaAtualProdutos = 1;
        renderizarProdutos();
    });

    // Filtros de pedidos
    filtroPedidoCliente?.addEventListener('input', () => {
        paginaAtualPedidos = 1;
        renderizarPedidos();
    });
    filtroPedidoStatus?.addEventListener('change', () => {
        paginaAtualPedidos = 1;
        renderizarPedidos();
    });

    // Paginação
    produtosPrev?.addEventListener('click', () => {
        paginaAtualProdutos = Math.max(1, paginaAtualProdutos - 1);
        renderizarProdutos();
    });
    produtosNext?.addEventListener('click', () => {
        paginaAtualProdutos += 1;
        renderizarProdutos();
    });
    pedidosPrev?.addEventListener('click', () => {
        paginaAtualPedidos = Math.max(1, paginaAtualPedidos - 1);
        renderizarPedidos();
    });
    pedidosNext?.addEventListener('click', () => {
        paginaAtualPedidos += 1;
        renderizarPedidos();
    });

    // Fechar modais ao clicar fora
    window.addEventListener('click', (event) => {
        if (event.target === modalCriarProduto) {
            fecharModalCriarProduto();
        }
        if (event.target === modalCriarPedido) {
            fecharModalCriarPedido();
        }
        if (event.target === modalEditPedido) {
            fecharModalEditarPedido();
        }
        if (event.target === modalEditProduto) {
            fecharModalEditarProduto();
        }
    });
}

function switchTab(tabName, clickedButton) {
    // Desativa todas as abas
    tabContents.forEach(content => content.classList.remove('active'));
    tabButtons.forEach(button => button.classList.remove('active'));

    // Ativa a aba selecionada
    document.getElementById(tabName).classList.add('active');
    clickedButton.classList.add('active');
}

// ============ MODAIS - PRODUTOS ============
function abrirModalCriarProduto() {
    formProduto.reset();
    modalCriarProduto.classList.add('show');
}

function fecharModalCriarProduto() {
    modalCriarProduto.classList.remove('show');
    formProduto.reset();
}

function abrirModalEditarProduto(id, nome, preco, categoria, quantidadeEstoque, descricao) {
    document.getElementById('editProdutoId').value = id;
    document.getElementById('editProdutoNome').value = nome;
    document.getElementById('editProdutoPreco').value = preco;
    document.getElementById('editProdutoCategoria').value = categoria;
    document.getElementById('editProdutoEstoque').value = quantidadeEstoque;
    document.getElementById('editProdutoDescricao').value = descricao;
    modalEditProduto.classList.add('show');
}

function fecharModalEditarProduto() {
    modalEditProduto.classList.remove('show');
    formEditProduto.reset();
}

// ============ MODAIS - PEDIDOS ============
function abrirModalCriarPedido() {
    formPedido.reset();
    modalCriarPedido.classList.add('show');
}

function fecharModalCriarPedido() {
    modalCriarPedido.classList.remove('show');
    formPedido.reset();
}

function abrirModalEditarPedido(pedidoId) {
    const pedido = pedidos.find(p => p.id === pedidoId);
    if (!pedido) return;

    document.getElementById('editPedidoId').value = pedido.id;
    document.getElementById('editPedidoCliente').value = pedido.nomeCliente || '';
    document.getElementById('editPedidoObservacao').value = pedido.observacao || '';
    document.getElementById('editPedidoProduto').value = obterProdutoDoPedido(pedido);
    document.getElementById('editPedidoStatus').value = pedido.status || 'ABERTO';

    modalEditPedido.classList.add('show');
}

function fecharModalEditarPedido() {
    modalEditPedido.classList.remove('show');
    formEditPedido.reset();
}

// ============ PRODUTOS ============
async function carregarProdutos() {
    try {
        const response = await fetch(`${API_BASE}/produtos`);
        if (!response.ok) throw new Error('Erro ao carregar produtos');
        
        produtos = await response.json();
        renderizarProdutos();
        atualizarSelectProdutos();
        renderizarPedidos();
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        produtosList.innerHTML = `<p class="alert alert-error">❌ Erro ao carregar produtos: ${error.message}</p>`;
    }
}

function renderizarProdutos() {
    atualizarFiltroCategorias();

    const termo = normalizarTexto(filtroProdutoNome?.value || '');
    const categoriaSelecionada = (filtroProdutoCategoria?.value || '').trim();

    const filtrados = produtos.filter(produto => {
        const textoProduto = normalizarTexto(`${produto.nome} ${produto.descricao || ''} ${produto.categoria || ''}`);
        const matchTexto = !termo || textoProduto.includes(termo);
        const matchCategoria = !categoriaSelecionada || produto.categoria === categoriaSelecionada;
        return matchTexto && matchCategoria;
    });

    const totalPaginas = Math.max(1, Math.ceil(filtrados.length / ITENS_POR_PAGINA_PRODUTOS));
    paginaAtualProdutos = Math.min(paginaAtualProdutos, totalPaginas);

    const inicio = (paginaAtualProdutos - 1) * ITENS_POR_PAGINA_PRODUTOS;
    const pagina = filtrados.slice(inicio, inicio + ITENS_POR_PAGINA_PRODUTOS);

    const linhas = pagina.length > 0
        ? pagina.map(produto => `
            <tr>
                <td><strong>${escapeHtml(produto.nome)}</strong></td>
                <td>${escapeHtml(produto.descricao || '-')}</td>
                <td>R$ ${Number(produto.preco).toFixed(2)}</td>
                <td>${escapeHtml(produto.categoria || '-')}</td>
                <td><span class="${produto.quantidadeEstoque <= 5 ? 'stock-low' : 'stock-normal'}">${produto.quantidadeEstoque}</span></td>
                <td>${escapeHtml(produto.atualizadoEm || '-')}</td>
                <td>
                    <div class="table-actions">
                        <button type="button" class="btn btn-secondary btn-table btn-edit-produto" data-id="${produto.id}">Editar</button>
                        <button type="button" class="btn btn-danger btn-table btn-delete-produto" data-id="${produto.id}">Excluir</button>
                    </div>
                </td>
            </tr>
        `).join('')
        : '<tr><td colspan="7" class="table-empty">Nenhum produto encontrado para os filtros selecionados.</td></tr>';

    produtosList.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Nome</th>
                    <th>Descrição</th>
                    <th>Preço</th>
                    <th>Categoria</th>
                    <th>Estoque</th>
                    <th>Atualizado em</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>${linhas}</tbody>
        </table>
    `;

    produtosPageInfo.textContent = `Página ${paginaAtualProdutos} de ${totalPaginas}`;
    produtosPrev.disabled = paginaAtualProdutos <= 1;
    produtosNext.disabled = paginaAtualProdutos >= totalPaginas;

    produtosList.querySelectorAll('.btn-edit-produto').forEach(btn => {
        btn.addEventListener('click', () => {
            const produto = produtos.find(p => p.id === btn.dataset.id);
            if (!produto) return;
            abrirModalEditarProduto(
                produto.id,
                produto.nome,
                produto.preco,
                produto.categoria,
                produto.quantidadeEstoque,
                produto.descricao || ''
            );
        });
    });

    produtosList.querySelectorAll('.btn-delete-produto').forEach(btn => {
        btn.addEventListener('click', () => {
            deletarProduto(btn.dataset.id);
        });
    });
}

async function criarProduto(e) {
    e.preventDefault();
    const botaoSubmit = e.submitter || formProduto.querySelector('button[type="submit"]');

    const nome = document.getElementById('produtoNome').value;
    const preco = parseFloat(document.getElementById('produtoPreco').value);
    const categoria = document.getElementById('produtoCategoria').value;
    const quantidadeEstoque = parseInt(document.getElementById('produtoEstoque').value);
    const descricao = document.getElementById('produtoDescricao').value;

    try {
        if (botaoSubmit) botaoSubmit.disabled = true;

        const response = await fetch(`${API_BASE}/produtos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, preco, categoria, quantidadeEstoque, descricao })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.mensagem || data.message || 'Erro ao criar produto');
        }

        mostrarAlerta('✅ Produto criado com sucesso!', 'success');
        fecharModalCriarProduto();
        carregarProdutos();
    } catch (error) {
        console.error('Erro completo:', error);
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    } finally {
        if (botaoSubmit) botaoSubmit.disabled = false;
    }
}

async function atualizarProduto(e) {
    e.preventDefault();
    const botaoSubmit = e.submitter || formEditProduto.querySelector('button[type="submit"]');

    const id = document.getElementById('editProdutoId').value;
    const nome = document.getElementById('editProdutoNome').value;
    const preco = parseFloat(document.getElementById('editProdutoPreco').value);
    const categoria = document.getElementById('editProdutoCategoria').value;
    const quantidadeEstoque = parseInt(document.getElementById('editProdutoEstoque').value);
    const descricao = document.getElementById('editProdutoDescricao').value;

    try {
        if (botaoSubmit) botaoSubmit.disabled = true;

        const response = await fetch(`${API_BASE}/produtos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, preco, categoria, quantidadeEstoque, descricao })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.mensagem || data.message || 'Erro ao atualizar produto');
        }

        mostrarAlerta('✅ Produto atualizado com sucesso!', 'success');
        fecharModalEditarProduto();
        carregarProdutos();
    } catch (error) {
        console.error('Erro completo:', error);
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    } finally {
        if (botaoSubmit) botaoSubmit.disabled = false;
    }
}

async function deletarProduto(id) {
    if (!confirm('⚠️ Tem certeza que deseja deletar este produto?')) return;

    try {
        const response = await fetch(`${API_BASE}/produtos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Erro ao deletar produto');

        mostrarAlerta('✅ Produto deletado com sucesso!', 'success');
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    }
}

function atualizarSelectProdutos() {
    const currentValue = pedidoProdutoId.value;
    pedidoProdutoId.innerHTML = '<option value="">Selecione um produto...</option>';
    
    produtos.forEach(produto => {
        const option = document.createElement('option');
        option.value = produto.id;
        option.textContent = `${produto.nome} (R$ ${parseFloat(produto.preco).toFixed(2)}, ${produto.quantidadeEstoque} em estoque)`;
        pedidoProdutoId.appendChild(option);
    });

    pedidoProdutoId.value = currentValue;
}

function atualizarFiltroCategorias() {
    if (!filtroProdutoCategoria) return;

    const atual = filtroProdutoCategoria.value;
    const categorias = [...new Set(produtos.map(p => p.categoria).filter(Boolean))].sort();

    filtroProdutoCategoria.innerHTML = '<option value="">Todas</option>';
    categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        filtroProdutoCategoria.appendChild(option);
    });

    filtroProdutoCategoria.value = categorias.includes(atual) ? atual : '';
}

// ============ PEDIDOS ============
async function carregarPedidos() {
    try {
        const response = await fetch(`${API_BASE}/pedidos`);
        if (!response.ok) throw new Error('Erro ao carregar pedidos');

        pedidos = await response.json();
        renderizarPedidos();
    } catch (error) {
        console.error('Erro ao carregar pedidos:', error);
        pedidosList.innerHTML = `<p class="alert alert-error">❌ Erro ao carregar pedidos: ${error.message}</p>`;
    }
}

function renderizarPedidos() {
    const termo = normalizarTexto(filtroPedidoCliente?.value || '');
    const statusSelecionado = (filtroPedidoStatus?.value || '').trim();

    const filtrados = pedidos.filter(pedido => {
        const textoPedido = normalizarTexto(`${pedido.nomeCliente || ''} ${pedido.observacao || ''} ${pedido.id || ''}`);
        const matchTexto = !termo || textoPedido.includes(termo);
        const matchStatus = !statusSelecionado || pedido.status === statusSelecionado;
        return matchTexto && matchStatus;
    });

    const totalPaginas = Math.max(1, Math.ceil(filtrados.length / ITENS_POR_PAGINA_PEDIDOS));
    paginaAtualPedidos = Math.min(paginaAtualPedidos, totalPaginas);

    const inicio = (paginaAtualPedidos - 1) * ITENS_POR_PAGINA_PEDIDOS;
    const pagina = filtrados.slice(inicio, inicio + ITENS_POR_PAGINA_PEDIDOS);

    const linhas = pagina.length > 0
        ? pagina.map(pedido => `
            <tr>
                <td><strong>#${escapeHtml((pedido.id || '').substring(0, 8))}</strong></td>
                <td>${escapeHtml(pedido.nomeCliente || '-')}</td>
                <td>${escapeHtml(obterProdutoDoPedido(pedido))}</td>
                <td>
                    <span class="status-badge ${obterClasseStatusPedido(pedido.status)}">
                        ${escapeHtml(pedido.status || '-')}
                    </span>
                </td>
                <td>${escapeHtml(pedido.observacao || '-')}</td>
                <td>
                    <div class="table-actions">
                        <button type="button" class="btn btn-secondary btn-table btn-edit-pedido" data-id="${pedido.id}">Editar</button>
                        <button type="button" class="btn btn-danger btn-table btn-delete-pedido" data-id="${pedido.id}">Excluir</button>
                    </div>
                </td>
            </tr>
        `).join('')
        : '<tr><td colspan="6" class="table-empty">Nenhum pedido encontrado para os filtros selecionados.</td></tr>';

    pedidosList.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Produto</th>
                    <th>Status</th>
                    <th>Observação</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>${linhas}</tbody>
        </table>
    `;

    pedidosPageInfo.textContent = `Página ${paginaAtualPedidos} de ${totalPaginas}`;
    pedidosPrev.disabled = paginaAtualPedidos <= 1;
    pedidosNext.disabled = paginaAtualPedidos >= totalPaginas;

    pedidosList.querySelectorAll('.btn-edit-pedido').forEach(btn => {
        btn.addEventListener('click', () => {
            abrirModalEditarPedido(btn.dataset.id);
        });
    });

    pedidosList.querySelectorAll('.btn-delete-pedido').forEach(btn => {
        btn.addEventListener('click', () => {
            deletarPedido(btn.dataset.id);
        });
    });
}

async function criarPedido(e) {
    e.preventDefault();
    const botaoSubmit = e.submitter || formPedido.querySelector('button[type="submit"]');

    const produtoId = document.getElementById('pedidoProdutoId').value;
    const quantidade = parseInt(document.getElementById('pedidoQuantidade').value);
    const nomeCliente = document.getElementById('pedidoCliente').value;
    const observacao = document.getElementById('pedidoObservacao').value;

    if (!produtoId) {
        mostrarAlerta('⚠️ Selecione um produto!', 'error');
        return;
    }

    try {
        if (botaoSubmit) botaoSubmit.disabled = true;

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

        await response.json();

        mostrarAlerta('✅ Pedido criado com sucesso! Estoque foi decrementado.', 'success');
        fecharModalCriarPedido();
        carregarPedidos();
        carregarProdutos();
    } catch (error) {
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    } finally {
        if (botaoSubmit) botaoSubmit.disabled = false;
    }
}

async function deletarPedido(id) {
    if (!confirm('⚠️ Tem certeza que deseja deletar este pedido?')) return;

    try {
        const response = await fetch(`${API_BASE}/pedidos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Erro ao deletar pedido');

        mostrarAlerta('✅ Pedido deletado com sucesso!', 'success');
        carregarPedidos();
    } catch (error) {
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    }
}

async function atualizarStatusPedido(e) {
    e.preventDefault();
    const botaoSubmit = e.submitter || formEditPedido.querySelector('button[type="submit"]');

    const id = document.getElementById('editPedidoId').value;
    const nomeCliente = document.getElementById('editPedidoCliente').value;
    const observacao = document.getElementById('editPedidoObservacao').value;
    const status = document.getElementById('editPedidoStatus').value;

    try {
        if (botaoSubmit) botaoSubmit.disabled = true;

        const response = await fetch(`${API_BASE}/pedidos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nomeCliente, observacao, status })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.mensagem || data.message || 'Erro ao atualizar status do pedido');
        }

        mostrarAlerta('✅ Status do pedido atualizado com sucesso!', 'success');
        fecharModalEditarPedido();
        carregarPedidos();
    } catch (error) {
        mostrarAlerta(`❌ Erro: ${error.message}`, 'error');
    } finally {
        if (botaoSubmit) botaoSubmit.disabled = false;
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
    }, 5000);
}

function escapeHtml(texto) {
    return String(texto)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function normalizarTexto(texto) {
    return String(texto || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}

function obterClasseStatusPedido(status) {
    if (status === 'ABERTO') return 'status-aberto';
    if (status === 'EM_ANDAMENTO') return 'status-andamento';
    if (status === 'CONCLUIDO') return 'status-concluido';
    if (status === 'CANCELADO') return 'status-cancelado';
    return 'status-aberto';
}

function obterProdutoDoPedido(pedido) {
    if (!pedido || !pedido.produtoId) return '-';
    const produto = produtos.find(p => p.id === pedido.produtoId);
    return produto ? produto.nome : pedido.produtoId;
}

