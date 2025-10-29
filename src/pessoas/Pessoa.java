package pessoas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Pessoa {

    private static final List<Pessoa> REGISTRO = new ArrayList<>();

    private String nome;
    private String cpf_cnpj;
    private String status;
    private String departamento;

    public Pessoa() {}

    public Pessoa(String nome, String cpf_cnpj, String status, String departamento) {
        setNome(nome);
        setCpf_cnpj(cpf_cnpj);
        setStatus(status);
        setDepartamento(departamento);
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        this.nome = nome;
    }

    public void setCpf_cnpj(String cpf_cnpj) {
        if (cpf_cnpj == null || cpf_cnpj.isBlank()) {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório.");
        }
        String documento = cpf_cnpj.replaceAll("\\D", "");
        if (documento.length() == 11) {
            if (!validarCpf(documento)) {
                throw new IllegalArgumentException("CPF inválido.");
            }
        } else if (documento.length() == 14) {
            if (!validarCnpj(documento)) {
                throw new IllegalArgumentException("CNPJ inválido.");
            }
        } else {
            throw new IllegalArgumentException("Documento deve conter 11 (CPF) ou 14 (CNPJ) dígitos.");
        }
        this.cpf_cnpj = documento;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf_cnpj() {
        return cpf_cnpj;
    }

    public String getStatus() {
        return status;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void registrar() {
        Objects.requireNonNull(cpf_cnpj, "CPF/CNPJ não pode ser nulo");
        synchronized (REGISTRO) {
            REGISTRO.removeIf(p -> cpf_cnpj.equals(p.getCpf_cnpj()));
            REGISTRO.add(this);
        }
    }

    public static List<Pessoa> listar() {
        synchronized (REGISTRO) {
            return Collections.unmodifiableList(new ArrayList<>(REGISTRO));
        }
    }

    public static List<Pessoa> pesquisar(String nome, String cpfCnpj, String status, String departamento) {
        synchronized (REGISTRO) {
            return REGISTRO.stream()
                    .filter(p -> nome == null || p.getNome() != null && p.getNome().equalsIgnoreCase(nome))
                    .filter(p -> cpfCnpj == null || p.getCpf_cnpj() != null && p.getCpf_cnpj().equals(cpfCnpj))
                    .filter(p -> status == null || p.getStatus() != null && p.getStatus().equalsIgnoreCase(status))
                    .filter(p -> departamento == null || p.getDepartamento() != null && p.getDepartamento().equalsIgnoreCase(departamento))
                    .collect(Collectors.toList());
        }
    }

    public static Pessoa pesquisarPorCpf(String cpfCnpj) {
        synchronized (REGISTRO) {
            return REGISTRO.stream()
                    .filter(p -> p.getCpf_cnpj() != null && p.getCpf_cnpj().equals(cpfCnpj))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static boolean validarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        int digito1 = calcularDigitoCpf(cpf.substring(0, 9));
        int digito2 = calcularDigitoCpf(cpf.substring(0, 9) + digito1);
        return cpf.equals(cpf.substring(0, 9) + digito1 + digito2);
    }

    public static boolean validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int digito1 = calcularDigitoCnpj(cnpj.substring(0, 12));
        int digito2 = calcularDigitoCnpj(cnpj.substring(0, 12) + digito1);
        return cnpj.equals(cnpj.substring(0, 12) + digito1 + digito2);
    }

    private static int calcularDigitoCpf(String base) {
        int soma = 0;
        int peso = base.length() + 1;
        for (char c : base.toCharArray()) {
            soma += Character.getNumericValue(c) * peso--;
        }
        int mod = 11 - (soma % 11);
        return (mod > 9) ? 0 : mod;
    }

    private static int calcularDigitoCnpj(String base) {
        int[] pesos = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        int inicio = pesos.length - base.length();
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[inicio + i];
        }
        int mod = soma % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }
}