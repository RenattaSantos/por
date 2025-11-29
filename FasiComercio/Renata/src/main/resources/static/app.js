// app.js (ES Module)
const API = {
  base: "", // ex.: "http://localhost:8080"

  // Produtos (lista e busca)
  produtos: () => `${API.base}/api/produtos`,
  produtoId: (id) => `${API.base}/api/produtos/${id}`,

  // Serviços (criação/atualização)
  servicos: () => `${API.base}/api/servicos`,
  servicoId: (id) => `${API.base}/api/servicos/${id}`,
};

// === Config ===
// EAN fixo de serviço — igual ao do back
const SERVICE_EAN = "9999999999996";

// Util — evita XSS ao inserir texto
const safe = (s = "") =>
  String(s).replace(/[&<>"'`]/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;", "`": "&#96;" }[c])
  );

// Navegação lateral
const sections = [...document.querySelectorAll("[data-section]")];
document.querySelectorAll(".nav-item[data-view]").forEach((btn) => {
  btn.addEventListener("click", () => setView(btn.dataset.view));
});
function setView(view) {
  sections.forEach((sec) => (sec.hidden = sec.dataset.section !== view));
  document.querySelectorAll(".nav-item[data-view]").forEach((b) => b.removeAttribute("aria-current"));
  const current = document.querySelector(`.nav-item[data-view="${view}"]`);
  current?.setAttribute("aria-current", "page");
  document.getElementById("app").focus();
}
setView("cadastros");

// Troca Produto/Serviço (tabs)
const formProduto = document.getElementById("formProduto");
const formServico = document.getElementById("formServico");
const feedbackProd = formProduto.querySelector(".feedback");
const feedbackServ = formServico.querySelector(".feedback");
const tbody = document.getElementById("tbodyProdutos");

function setTab(kind) {
  const prod = kind === "prod";
  document.querySelectorAll(".js-tab-prod").forEach((b) => {
    b.classList.toggle("is-active", prod);
    b.setAttribute("aria-selected", String(prod));
  });
  document.querySelectorAll(".js-tab-serv").forEach((b) => {
    b.classList.toggle("is-active", !prod);
    b.setAttribute("aria-selected", String(!prod));
  });
  formProduto.hidden = !prod;
  formServico.hidden = prod;
  setMsg(feedbackProd, "");
  setMsg(feedbackServ, "");
}
document.querySelectorAll(".js-tab-prod").forEach((b) => b.addEventListener("click", () => setTab("prod")));
document.querySelectorAll(".js-tab-serv").forEach((b) => b.addEventListener("click", () => setTab("serv")));
setTab("prod");

// ------- Helpers -------
function isServico(item) {
  const ean = String(item?.codg_barras_prod ?? "");
  const byEan = ean && ean === SERVICE_EAN;
  const byStocks = item?.estoque_minimo === 0 && item?.estoque_maximo === 1 && item?.ponto_abastecimento === 1;
  return Boolean(byEan || byStocks);
}

async function parseErro(res) {
  try {
    const txt = await res.text();
    if (txt) {
      try {
        const j = JSON.parse(txt);
        const msg = j?.message || j?.error || `HTTP ${res.status}`;
        return new Error(msg);
      } catch {
        const m = txt.length > 200 ? txt.slice(0, 200) + "..." : txt;
        return new Error(m || `HTTP ${res.status}`);
      }
    }
  } catch {}
  return new Error(`HTTP ${res.status}`);
}

function setMsg(el, text, isErr = false, autoHideMs = 3000) {
  el.textContent = text;
  el.style.color = isErr ? "#b91c1c" : "#065f46";
  if (autoHideMs > 0 && text) {
    const ref = Symbol();
    el._msgRef = ref;
    setTimeout(() => {
      if (el._msgRef === ref) {
        el.textContent = "";
      }
    }, autoHideMs);
  }
}
function setValuesProduto(form, p) {
  form.querySelector("#p-nome").value = p.nomeProduto ?? "";
  form.querySelector("#p-descricao").value = p.descricao_produto ?? "";
  form.querySelector("#p-codbarras").value = p.codg_barras_prod ?? "";
  form.querySelector("#p-unmedida").value = p.id_unmedida ?? "";
  form.querySelector("#p-temp").value = p.temperatura_produto ?? "";
  form.querySelector("#p-almox").value = p.id_almoxarifado ?? "";
  form.querySelector("#p-stqmin").value = p.estoque_minimo ?? "";
  form.querySelector("#p-stqmax").value = p.estoque_maximo ?? "";
  form.querySelector("#p-ponto").value = p.ponto_abastecimento ?? "";
}
function setValuesServico(form, p) {
  form.querySelector("#s-nome").value = p.nomeProduto ?? "";
  form.querySelector("#s-descricao").value = p.descricao_produto ?? "";
}

// ------- Produtos (POST/PUT) -------
formProduto.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = formToProduto(formProduto);
  const erro = validarProduto(data);
  if (erro) return setMsg(feedbackProd, erro, true);

  try {
    const isEditing = !!formProduto.dataset.editing;
    const url = isEditing ? API.produtoId(formProduto.dataset.editing) : API.produtos();
    const method = isEditing ? "PUT" : "POST";

    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw await parseErro(res);

    setMsg(feedbackProd, isEditing ? "Produto atualizado!" : "Produto salvo com sucesso!");
    formProduto.reset();
    delete formProduto.dataset.editing;
    await carregarProdutos();
  } catch (err) {
    setMsg(feedbackProd, err.message || "Falha ao salvar produto", true);
  }
});

document.getElementById("btnRecarregar").addEventListener("click", carregarProdutos);
document.getElementById("filtroNome").addEventListener("input", filtrarTabela);

async function carregarProdutos() {
  try {
    const res = await fetch(API.produtos());
    if (!res.ok) throw await parseErro(res);
    const lista = await res.json();
    renderTabela(lista);
  } catch (err) {
    console.error("[Produtos] erro ao listar:", err);
    setMsg(feedbackProd, err.message || "Não foi possível carregar a lista", true);
  }
}
carregarProdutos();

function renderTabela(lista) {
  tbody.innerHTML = "";
  for (const p of lista) {
    const tr = document.createElement("tr");
    tr.dataset.isServico = String(isServico(p));
    tr.innerHTML = `
      <td>${safe(p.id_produto)}</td>
      <td>${safe(p.nomeProduto)}</td>
      <td>${safe(p.descricao_produto)}</td>
      <td>${safe(p.codg_barras_prod)}</td>
      <td>${safe(p.estoque_minimo)}/${safe(p.estoque_maximo)}/${safe(p.ponto_abastecimento)}</td>
      <td>${safe(p.id_unmedida)}</td>
      <td>${p.id_almoxarifado ?? "-"}</td>
    `;
    tbody.appendChild(tr);
  }
}

// Sem ação de editar via tabela
formProduto.addEventListener("reset", () => {
  delete formProduto.dataset.editing;
  setMsg(feedbackProd, "");
});

// ------- Serviço (POST/PUT) -------
formServico.addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(formServico);
  const body = {
    nomeProduto: fd.get("nomeProduto")?.toString().trim(),
    descricao_produto: fd.get("descricao_produto")?.toString().trim(),
  };
  if (!body.nomeProduto || !body.descricao_produto) {
    return setMsg(feedbackServ, "Preencha nome e descrição.", true);
  }

  try {
    const isEditing = !!formServico.dataset.editing;
    const url = isEditing ? API.servicoId(formServico.dataset.editing) : API.servicos();
    const method = isEditing ? "PUT" : "POST";

    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw await parseErro(res);

    setMsg(feedbackServ, isEditing ? "Serviço atualizado!" : "Serviço salvo com sucesso!");
    formServico.reset();
    delete formServico.dataset.editing;
    await carregarProdutos();
  } catch (err) {
    setMsg(feedbackServ, err.message || "Falha ao salvar serviço", true);
  }
});

// Utils de formulário de Produto
function formToProduto(form) {
  const fd = new FormData(form);
  const num = (v) => (v === "" || v == null ? null : Number(v));
  const int = (v) => (v === "" || v == null ? null : parseInt(v, 10));

  const data = {
    nomeProduto: fd.get("nomeProduto")?.toString().trim(),
    descricao_produto: fd.get("descricao_produto")?.toString().trim(),
    codg_barras_prod: fd.get("codg_barras_prod")?.toString().trim(),
    id_unmedida: int(fd.get("id_unmedida")),
    temperatura_produto: num(fd.get("temperatura_produto")),
    id_almoxarifado: int(fd.get("id_almoxarifado")),
    estoque_minimo: int(fd.get("estoque_minimo")),
    estoque_maximo: int(fd.get("estoque_maximo")),
    ponto_abastecimento: int(fd.get("ponto_abastecimento")),
  };

  const id = form.dataset.editing;
  if (id) data.id_produto = Number(id);
  return data;
}

function validarProduto(p) {
  if (!p.nomeProduto || !p.descricao_produto) return "Informe nome e descrição.";
  if (!p.id_unmedida) return "ID da unidade de medida é obrigatório.";
  if (!p.codg_barras_prod || !/^\d{8,14}$/.test(p.codg_barras_prod)) return "Código de barras deve ter 8–14 dígitos.";
  const min = p.estoque_minimo ?? -1,
    max = p.estoque_maximo ?? -1,
    pp = p.ponto_abastecimento ?? -1;
  if (min < 0 || max < 0 || pp < 0) return "Estoque mínimo/máximo/ponto de pedido não podem ser negativos.";
  if (max <= min) return "Estoque máximo deve ser maior que o mínimo.";
  if (!(pp > min && pp <= max)) return "Ponto de pedido deve ser > mínimo e ≤ máximo.";
  return null;
}

// Filtro cliente
function filtrarTabela(e) {
  const q = e.target.value.toLowerCase();
  for (const tr of tbody.rows) {
    const nome = tr.cells[1]?.textContent.toLowerCase() ?? "";
    tr.style.display = nome.includes(q) ? "" : "none";
  }
}

// Sair (placeholder)
document.getElementById("btnSair").addEventListener("click", () => {
  alert("Encerrando sessão (simulação).");
});
