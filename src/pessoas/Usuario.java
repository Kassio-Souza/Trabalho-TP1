package pessoas;

import AdministracaoGestao.gestao.GestorMaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import recrutamento.excecoes.AutorizacaoException;

public class Usuario extends Pessoa {

    private static final Pattern SENHA_FORTE = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Set<String> TIPOS_VALIDOS = Set.of("ADMINISTRADOR", "GESTOR", "RECRUTADOR", "FUNCIONARIO");
    private static final List<Usuario> REGISTRO = new ArrayList<>();

    private long idUsuario;
    private String login;
    private String senha;
    private String tipo;

    public Usuario(long idUsuario, String login, String senha, String tipo, GestorMaster gestor) {
        this(idUsuario, login, senha, tipo, null, null, null, null, gestor);
    }

    public Usuario(long idUsuario,
                   String login,
                   String senha,
                   String tipo,
                   String nome,
                   String cpf,
                   String status,
                   String departamento,
                   GestorMaster gestor) {
        validarGestor(gestor);
        definirCredenciais(idUsuario, login, senha, tipo);
        if (nome != null) {
            setNome(nome);
        }
        if (cpf != null) {
            setCpf_cnpj(cpf);
        }
        if (status != null) {
            setStatus(status);
        }
        if (departamento != null) {
            setDepartamento(departamento);
        }
        if (cpf != null) {
            super.registrar();
        }
        registrarUsuario();
    }

    private void definirCredenciais(long idUsuario, String login, String senha, String tipo) {
        validarIdUnico(idUsuario);
        validarLogin(login);
        validarSenha(senha);
        validarTipo(tipo);
        this.idUsuario = idUsuario;
        this.login = login;
        this.senha = senha;
        this.tipo = tipo.toUpperCase();
    }

    private void registrarUsuario() {
        synchronized (REGISTRO) {
            REGISTRO.add(this);
        }
    }

    public static List<Usuario> listarUsuarios() {
        synchronized (REGISTRO) {
            return Collections.unmodifiableList(new ArrayList<>(REGISTRO));
        }
    }

    public static Usuario buscarPorLogin(String login) {
        synchronized (REGISTRO) {
            return REGISTRO.stream()
                    .filter(u -> u.login.equalsIgnoreCase(login))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static Usuario buscarPorId(long id) {
        synchronized (REGISTRO) {
            return REGISTRO.stream()
                    .filter(u -> u.idUsuario == id)
                    .findFirst()
                    .orElse(null);
        }
    }

    public static List<Usuario> pesquisarUsuarios(String nome, String status, String tipo) {
        synchronized (REGISTRO) {
            return REGISTRO.stream()
                    .filter(u -> nome == null || u.getNome() != null && u.getNome().equalsIgnoreCase(nome))
                    .filter(u -> status == null || u.getStatus() != null && u.getStatus().equalsIgnoreCase(status))
                    .filter(u -> tipo == null || u.tipo.equalsIgnoreCase(tipo))
                    .collect(Collectors.toList());
        }
    }

    public static void remover(Usuario usuario, GestorMaster gestor) {
        validarGestor(gestor);
        synchronized (REGISTRO) {
            REGISTRO.removeIf(u -> u.idUsuario == usuario.idUsuario);
        }
    }

    public long getIdUsuario() {
        return this.idUsuario;
    }

    public String getLogin() {
        return this.login;
    }

    public String getSenha() {
        return this.senha;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setIdUsuario(long idUsuario, GestorMaster gestor) {
        validarGestor(gestor);
        validarIdUnico(idUsuario);
        this.idUsuario = idUsuario;
    }

    public void setLogin(String login, GestorMaster gestor) {
        validarGestor(gestor);
        validarLogin(login);
        this.login = login;
    }

    public void setSenha(String senha, GestorMaster gestor) {
        validarGestor(gestor);
        validarSenha(senha);
        this.senha = senha;
    }

    public void setTipo(String tipo, GestorMaster gestor) {
        validarGestor(gestor);
        validarTipo(tipo);
        this.tipo = tipo.toUpperCase();
    }

    public boolean autenticar(String login, String senha) {
        return this.login.equalsIgnoreCase(login) && this.senha.equals(senha);
    }

    public Usuario pesquisarUsuario(String status) {
        return pesquisarUsuarios(null, status, null)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static void validarGestor(GestorMaster gestor) {
        if (gestor == null) {
            throw new AutorizacaoException("Apenas o GestorMaster pode alterar os dados do usuário.");
        }
    }

    private void validarIdUnico(long idUsuario) {
        synchronized (REGISTRO) {
            boolean existe = REGISTRO.stream().anyMatch(u -> u.idUsuario == idUsuario && u != this);
            if (existe) {
                throw new IllegalArgumentException("Já existe um usuário com o ID informado.");
            }
        }
    }

    private void validarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        synchronized (REGISTRO) {
            boolean duplicado = REGISTRO.stream().anyMatch(u -> u.login.equalsIgnoreCase(login) && u != this);
            if (duplicado) {
                throw new IllegalArgumentException("Já existe um usuário com o login informado.");
            }
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || !SENHA_FORTE.matcher(senha).matches()) {
            throw new IllegalArgumentException("A senha deve possuir ao menos 8 caracteres, incluindo letras e números.");
        }
    }

    private void validarTipo(String tipo) {
        Objects.requireNonNull(tipo, "Tipo de usuário é obrigatório.");
        if (!TIPOS_VALIDOS.contains(tipo.toUpperCase())) {
            throw new IllegalArgumentException("Tipo de usuário inválido: " + tipo);
        }
    }
}
