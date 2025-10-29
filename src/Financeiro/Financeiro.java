package Financeiro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import pessoas.Pessoa;

public class Financeiro {
    private static final Map<Vinculo, RegraSalarial> REGRAS_POR_VINCULO = new EnumMap<>(Vinculo.class);

    static {
        registrarRegra(new RegraSalarial.Builder(Vinculo.CLT)
                .comValeTransporte(300.0)
                .comValeAlimentacao(300.0)
                .comAdicionalPadrao(0.0)
                .comDescontoPadrao(0.0)
                .build());
        registrarRegra(new RegraSalarial.Builder(Vinculo.ESTAGIO)
                .comValeTransporte(200.0)
                .comValeAlimentacao(0.0)
                .comAdicionalPadrao(0.0)
                .comDescontoPadrao(0.0)
                .build());
        registrarRegra(new RegraSalarial.Builder(Vinculo.PJ)
                .comValeTransporte(0.0)
                .comValeAlimentacao(0.0)
                .comAdicionalPadrao(0.0)
                .comDescontoPadrao(0.0)
                .build());
    }

    private Vinculo vinculo;
    private double salarioBase;
    private double salarioCalculado;
    private boolean temValeTransporte;
    private boolean temValeAlimentacao;
    private double adicionalSalario;
    private double descontoSalario;

    public Financeiro(Vinculo vinculo, double salarioBase) {
        this.vinculo = Objects.requireNonNull(vinculo, "vinculo");
        atualizarSalarioBase(salarioBase);
    }

    public static void registrarRegra(RegraSalarial regra) {
        Objects.requireNonNull(regra, "regra");
        REGRAS_POR_VINCULO.put(regra.getVinculo(), regra);
    }

    public static RegraSalarial consultarRegra(Vinculo vinculo) {
        return REGRAS_POR_VINCULO.get(vinculo);
    }

    public Vinculo getVinculo() {
        return vinculo;
    }

    public void setVinculo(Vinculo vinculo) {
        this.vinculo = Objects.requireNonNull(vinculo, "vinculo");
    }

    public double getSalarioBase() {
        return salarioBase;
    }

    public void atualizarSalarioBase(double salarioBase) {
        if (salarioBase < 0) {
            throw new IllegalArgumentException("O salário base não pode ser negativo.");
        }
        this.salarioBase = salarioBase;
    }

    public double getSalarioCalculado() {
        return salarioCalculado;
    }

    public void configurarBeneficios(boolean valeTransporte, boolean valeAlimentacao) {
        this.temValeTransporte = valeTransporte;
        this.temValeAlimentacao = valeAlimentacao;
    }

    public void definirAdicional(double adicional) {
        if (adicional < 0) {
            throw new IllegalArgumentException("O adicional deve ser positivo.");
        }
        this.adicionalSalario = adicional;
    }

    public void definirDesconto(double desconto) {
        if (desconto < 0) {
            throw new IllegalArgumentException("O desconto deve ser positivo.");
        }
        this.descontoSalario = desconto;
    }

    public double calcularSalario() {
        RegraSalarial regra = REGRAS_POR_VINCULO.getOrDefault(vinculo,
                new RegraSalarial.Builder(vinculo).build());
        this.salarioCalculado = regra.calcular(salarioBase,
                temValeTransporte,
                temValeAlimentacao,
                adicionalSalario,
                descontoSalario);
        return salarioCalculado;
    }
}

enum Vinculo {
    CLT,
    PJ,
    ESTAGIO,
}

enum Cargo {
    EXECUTIVO,
    ENGENHEIRO,
    ANALISTA,
    TECNICO,
    ESTAGIARIO,
}

enum Departamento {
    RH,
    ENGENHARIA,
    DIRETORIA,
}

class Funcionario {
    private final Pessoa pessoa;
    private final Financeiro financeiro;
    private final Cargo cargo;
    private final Departamento departamento;
    private boolean ativo;

    public Funcionario(Pessoa pessoa, Financeiro financeiro, Cargo cargo, Departamento departamento, boolean ativo) {
        this.pessoa = Objects.requireNonNull(pessoa, "pessoa");
        this.financeiro = Objects.requireNonNull(financeiro, "financeiro");
        this.cargo = Objects.requireNonNull(cargo, "cargo");
        this.departamento = Objects.requireNonNull(departamento, "departamento");
        this.ativo = ativo;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Financeiro getFinanceiro() {
        return financeiro;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public double calcularSalario() {
        return financeiro.calcularSalario();
    }
}

class FolhaPagamento {
    private final List<Funcionario> listaFuncionario = new ArrayList<>();
    private List<String> folhaPagamento = new ArrayList<>();
    private List<String> relatorio = new ArrayList<>();

    public void adicionarFuncionario(Funcionario funcionario) {
        Objects.requireNonNull(funcionario, "funcionario");
        listaFuncionario.add(funcionario);
    }

    public List<String> gerarFolhaPagamento(YearMonth competencia) {
        List<String> linhas = new ArrayList<>();
        linhas.add("Competencia;Nome;Cargo;Departamento;Vinculo;Salario");
        for (Funcionario funcionario : listaFuncionario) {
            if (funcionario.isAtivo()) {
                double salario = funcionario.calcularSalario();
                String linha = new StringJoiner(";")
                        .add(competencia.toString())
                        .add(funcionario.getPessoa().getNome())
                        .add(funcionario.getCargo().name())
                        .add(funcionario.getDepartamento().name())
                        .add(funcionario.getFinanceiro().getVinculo().name())
                        .add(String.format("%.2f", salario))
                        .toString();
                linhas.add(linha);
            }
        }
        folhaPagamento = Collections.unmodifiableList(linhas);
        return folhaPagamento;
    }

    public List<String> gerarRelatorioResumo() {
        Map<Vinculo, Double> totaisPorVinculo = new EnumMap<>(Vinculo.class);
        Map<Departamento, Integer> quantidadePorDepartamento = new EnumMap<>(Departamento.class);

        for (Funcionario funcionario : listaFuncionario) {
            if (!funcionario.isAtivo()) {
                continue;
            }
            double salario = funcionario.calcularSalario();
            Vinculo vinculo = funcionario.getFinanceiro().getVinculo();
            totaisPorVinculo.merge(vinculo, salario, Double::sum);

            Departamento departamento = funcionario.getDepartamento();
            quantidadePorDepartamento.merge(departamento, 1, Integer::sum);
        }

        List<String> linhas = new ArrayList<>();
        linhas.add("Resumo Financeiro");
        for (Map.Entry<Vinculo, Double> entry : totaisPorVinculo.entrySet()) {
            linhas.add(String.format("Total %s: %.2f", entry.getKey().name(), entry.getValue()));
        }
        linhas.add("Funcionários por departamento");
        for (Map.Entry<Departamento, Integer> entry : quantidadePorDepartamento.entrySet()) {
            linhas.add(String.format("%s: %d", entry.getKey().name(), entry.getValue()));
        }
        relatorio = Collections.unmodifiableList(linhas);
        return relatorio;
    }

    public Path exportarRelatorio(Path destino, String nomeArquivo) throws IOException {
        Objects.requireNonNull(destino, "destino");
        Objects.requireNonNull(nomeArquivo, "nomeArquivo");
        if (relatorio.isEmpty()) {
            throw new IllegalStateException("O relatório precisa ser gerado antes da exportação.");
        }
        Files.createDirectories(destino);
        Path arquivo = destino.resolve(nomeArquivo);
        Files.write(arquivo, relatorio);
        return arquivo;
    }

    public List<Funcionario> filtrarFuncionarios(Cargo cargo, Vinculo vinculo, Boolean ativo, Departamento departamento) {
        List<Funcionario> filtrados = new ArrayList<>();
        for (Funcionario funcionario : listaFuncionario) {
            if (cargo != null && funcionario.getCargo() != cargo) {
                continue;
            }
            if (vinculo != null && funcionario.getFinanceiro().getVinculo() != vinculo) {
                continue;
            }
            if (ativo != null && funcionario.isAtivo() != ativo) {
                continue;
            }
            if (departamento != null && funcionario.getDepartamento() != departamento) {
                continue;
            }
            filtrados.add(funcionario);
        }
        return Collections.unmodifiableList(filtrados);
    }
}

final class RegraSalarial {
    private final Vinculo vinculo;
    private final double valeTransporte;
    private final double valeAlimentacao;
    private final double adicionalPadrao;
    private final double descontoPadrao;

    private RegraSalarial(Builder builder) {
        this.vinculo = builder.vinculo;
        this.valeTransporte = builder.valeTransporte;
        this.valeAlimentacao = builder.valeAlimentacao;
        this.adicionalPadrao = builder.adicionalPadrao;
        this.descontoPadrao = builder.descontoPadrao;
    }

    public Vinculo getVinculo() {
        return vinculo;
    }

    public double calcular(double salarioBase,
                           boolean temValeTransporte,
                           boolean temValeAlimentacao,
                           double adicionalExtra,
                           double descontoExtra) {
        double salario = salarioBase;
        salario += adicionalPadrao + adicionalExtra;
        salario -= descontoPadrao + descontoExtra;

        if (temValeTransporte && valeTransporte > 0) {
            salario += valeTransporte;
        }
        if (temValeAlimentacao && valeAlimentacao > 0) {
            salario += valeAlimentacao;
        }

        if (vinculo == Vinculo.PJ) {
            salario = salarioBase + adicionalExtra;
        }

        return Math.max(salario, 0);
    }

    public static class Builder {
        private final Vinculo vinculo;
        private double valeTransporte;
        private double valeAlimentacao;
        private double adicionalPadrao;
        private double descontoPadrao;

        public Builder(Vinculo vinculo) {
            this.vinculo = Objects.requireNonNull(vinculo, "vinculo");
        }

        public Builder comValeTransporte(double valor) {
            validarValor(valor, "vale-transporte");
            this.valeTransporte = valor;
            return this;
        }

        public Builder comValeAlimentacao(double valor) {
            validarValor(valor, "vale-alimentação");
            this.valeAlimentacao = valor;
            return this;
        }

        public Builder comAdicionalPadrao(double valor) {
            validarValor(valor, "adicional padrão");
            this.adicionalPadrao = valor;
            return this;
        }

        public Builder comDescontoPadrao(double valor) {
            validarValor(valor, "desconto padrão");
            this.descontoPadrao = valor;
            return this;
        }

        public RegraSalarial build() {
            return new RegraSalarial(this);
        }

        private void validarValor(double valor, String campo) {
            if (valor < 0) {
                throw new IllegalArgumentException("O valor de " + campo + " não pode ser negativo.");
            }
        }
    }
}
