package br.com.hoteliara.service;

import java.time.LocalDate;

import br.com.hoteliara.dao.PessoaDAO;
import br.com.hoteliara.exception.LoginInvalidoException;
import br.com.hoteliara.model.Cliente;
import br.com.hoteliara.model.Pessoa;
import br.com.hoteliara.util.SenhaUtil;

/** Login e cadastro. A tela só pede; quem decide e acessa o banco é este serviço. */
public class AuthService {

    private final PessoaDAO pessoaDAO = new PessoaDAO();

    /** Procura a pessoa pelo e-mail e confere a senha pelo hash (Pessoa.autenticar). */
    public Pessoa entrar(String email, String senha) throws LoginInvalidoException {
        String procurado = email == null ? "" : email.trim().toLowerCase();
        Pessoa pessoa = pessoaDAO.listar().stream()
                .filter(p -> p.getEmail().equals(procurado))
                .findFirst()
                .orElseThrow(LoginInvalidoException::new);
        if (!pessoa.autenticar(senha)) {
            throw new LoginInvalidoException();
        }
        return pessoa;
    }

    /**
     * Cria o cliente (os setters de Pessoa validam CPF, e-mail etc. e lançam
     * IllegalArgumentException) e salva no banco.
     */
    public Cliente cadastrarCliente(String nome, String cpf, String email, String senha,
                                    String telefone, LocalDate nascimento) {
        Cliente novo = new Cliente(0, nome, cpf, email, SenhaUtil.gerarHash(senha),
                telefone, nascimento);
        for (Pessoa p : pessoaDAO.listar()) {
            if (p.getEmail().equals(novo.getEmail()))
                throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
            if (p.getCpf().equals(novo.getCpf()))
                throw new IllegalArgumentException("Já existe uma conta com este CPF.");
        }
        pessoaDAO.salvar(novo);
        return novo;
    }

    /** Lê de novo o cliente do banco, com o histórico de reservas atualizado. */
    public Cliente recarregarCliente(int id) {
        Pessoa pessoa = pessoaDAO.buscarPorId(id);
        if (!(pessoa instanceof Cliente cliente))
            throw new IllegalStateException("Cliente não encontrado no banco.");
        return cliente;
    }
}
