
public class FuncoesHash {
    private final int m;
    private final double A = 0.6180339887;

    public FuncoesHash(int m) {
        this.m = m;
    }

    // divisão: h(k) = k mod m
    public int hDiv(int k) {
        int r = k % this.m;
        if (r < 0) {
            r = r + this.m;
        }
        return r;
    }

    // multiplicação: h(k) = floor(m * frac(k * A))
    public int hMul(int k) {
        double prod = k * A;
        long parte = (long) prod;          // parte inteira
        double frac = prod - parte;       // parte fracionária
        double val = this.m * frac;
        int idx = (int) val;              // truncamento
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= this.m) {
            idx = this.m - 1;
        }
        return idx;
    }

    // dobramento
    public int hFold(int k) {
        int soma = 0;
        int resto = k;
        while (resto > 0) {
            int bloco = resto % 1000;
            soma = soma + bloco;
            resto = resto / 1000;
        }
        int r = soma % this.m;
        if (r < 0) {
            r = r + this.m;
        }
        return r;
    }
}
