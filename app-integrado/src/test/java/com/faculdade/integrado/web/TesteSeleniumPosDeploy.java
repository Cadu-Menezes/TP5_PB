package com.faculdade.integrado.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TesteSeleniumPosDeploy {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = obterBaseUrl();
        assumeTrue(baseUrl != null && !baseUrl.isBlank(),
                "Defina SELENIUM_BASE_URL para executar os testes Selenium pós-deploy.");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
        driver.get(baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void deveCarregarPaginaInicialComConteudoPrincipal() {
        wait.until(ExpectedConditions.titleContains("TP4 - Integrado"));

        String tituloPagina = driver.getTitle();
        assertTrue(tituloPagina.contains("TP4 - Integrado"));

        WebElement cabecalho = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header h1")));
        assertTrue(cabecalho.getText().contains("TP4 - Sistema Integrado"));
    }

    @Test
    void deveAlternarEntreAbasDeProdutosEPedidos() {
        WebElement abaPedidos = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".tab-button[data-tab='pedidos']")));
        abaPedidos.click();

        WebElement conteudoPedidos = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pedidos")));
        assertTrue(conteudoPedidos.getAttribute("class").contains("active"));

        WebElement abaProdutos = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".tab-button[data-tab='produtos']")));
        abaProdutos.click();

        WebElement conteudoProdutos = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("produtos")));
        assertTrue(conteudoProdutos.getAttribute("class").contains("active"));
    }

    @Test
    void deveAbrirEFecharModalDeCriacaoDeProduto() {
        WebElement botaoNovoProduto = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#produtos .tab-header .btn.btn-primary")));
        botaoNovoProduto.click();

        WebElement modalCriarProduto = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalCriarProduto")));
        assertTrue(modalCriarProduto.getAttribute("class").contains("show"));

        WebElement botaoFecharModal = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#modalCriarProduto .modal-header .close")));
        botaoFecharModal.click();

        wait.until(ExpectedConditions.not(
                ExpectedConditions.attributeContains(By.id("modalCriarProduto"), "class", "show")));
    }

    private String obterBaseUrl() {
        String env = System.getenv("SELENIUM_BASE_URL");
        if (env == null || env.isBlank()) {
            return null;
        }

        String normalizada = env.trim();
        return normalizada.endsWith("/") ? normalizada.substring(0, normalizada.length() - 1) : normalizada;
    }
}
