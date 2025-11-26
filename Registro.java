/* Registro: nó simples para lista encadeada.
   Guarda a chave (codigo) e referência para próximo. */
public class Registro {
    public final int codigo;
    public Registro proximo;

    public Registro(int codigo) {
        this.codigo = codigo;
        this.proximo = null;
    }
}
