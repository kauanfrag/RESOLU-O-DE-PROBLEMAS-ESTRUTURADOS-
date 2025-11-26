/* Main: orquestra os experimentos e imprime o CSV.
   Estilo intencionalmente simples, nomes em português e comentários mínimos. */
public class Main {
    // tamanhos de tabela (fixos)
    public static final int M1 = 1009;
    public static final int M2 = 10007;
    public static final int M3 = 100003;

    // tamanhos de dataset (fixos)
    public static final int N1 = 1000;
    public static final int N2 = 10000;
    public static final int N3 = 100000;

    // seeds públicas
    public static final long SEED1 = 137L;
    public static final long SEED2 = 271828L;
    public static final long SEED3 = 314159L;

    public static final int REPETICOES = 5;

    public static final String H_DIV = "H_DIV";
    public static final String H_MUL = "H_MUL";
    public static final String H_FOLD = "H_FOLD";

    // LCG simples interno (reprodutível)
    public static class GeradorLCG {
        private long estado;
        private final long a = 1664525L;
        private final long c = 1013904223L;
        private final long m = 1L << 32;

        public GeradorLCG(long seed) {
            this.estado = seed & 0xffffffffL;
        }

        public int proximoInt9() {
            this.estado = (a * this.estado + c) % m;
            long v = this.estado % 1000000000L;
            return (int) v;
        }

        // embaralha as primeiras 'tamanho' posições do array
        public void embaralhar(int[] arr, int tamanho) {
            int i = tamanho - 1;
            while (i > 0) {
                int r = this.proximoInt9();
                int j = r % (i + 1);
                int tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i = i - 1;
            }
        }
    }

    public static void main(String[] args) {
        // cabeçalho CSV (ordem e nomes exigidos)
        System.out.println("m,n,func,seed,ins_ms,coll_tbl,coll_lst,find_ms_hits,find_ms_misses,cmp_hits,cmp_misses,checksum");

        int[] ms = new int[] { M1, M2, M3 };
        int[] ns = new int[] { N1, N2, N3 };
        long[] seeds = new long[] { SEED1, SEED2, SEED3 };
        String[] funcoes = new String[] { H_DIV, H_MUL, H_FOLD };

        int im = 0;
        while (im < 3) {
            int m = ms[im];

            int in = 0;
            while (in < 3) {
                int n = ns[in];

                int iseed = 0;
                while (iseed < 3) {
                    long seed = seeds[iseed];

                    // gerar dataset reprodutível com LCG
                    GeradorLCG ger = new GeradorLCG(seed);
                    int[] dados = new int[n];
                    int ii = 0;
                    while (ii < n) {
                        dados[ii] = ger.proximoInt9();
                        ii = ii + 1;
                    }

                    int ifunc = 0;
                    while (ifunc < 3) {
                        String funcLabel = funcoes[ifunc];

                        // acumuladores para médias
                        long somaInsMs = 0L;
                        long somaFindHitsMs = 0L;
                        long somaFindMissesMs = 0L;
                        long somaCollTbl = 0L;
                        long somaCollLst = 0L;
                        long somaCmpHits = 0L;
                        long somaCmpMisses = 0L;
                        int checksumAuditoria = 0; // será definido na primeira repetição

                        int rep = 0;
                        while (rep < REPETICOES) {
                            // warm-up curto (somente na 1ª repetição)
                            if (rep == 0) {
                                FuncoesHash warm = new FuncoesHash(101);
                                Registro[] tmp = new Registro[101];
                                int w = 0;
                                while (w < 10) {
                                    int k = (w + 31) % 1000000000;
                                    int idx = warm.hDiv(k);
                                    if (tmp[idx] == null) {
                                        tmp[idx] = new Registro(k);
                                    } else {
                                        Registro cur = tmp[idx];
                                        while (cur.proximo != null) {
                                            cur = cur.proximo;
                                        }
                                        cur.proximo = new Registro(k);
                                    }
                                    w = w + 1;
                                }
                            }

                            FuncoesHash fhash = new FuncoesHash(m);
                            Registro[] baldes = new Registro[m];

                            // auditoria exigida: imprimir etiqueta antes de inserir (stderr)
                            System.err.println(funcLabel + " m=" + m + " seed=" + seed);

                            // coleta dos primeiros 10 h(k) para checksum
                            int[] primeirosHK = new int[10];
                            int cntPrimeiros = 0;

                            long coll_tbl = 0L;
                            long coll_lst = 0L;

                            long t0 = System.nanoTime();

                            int j = 0;
                            while (j < n) {
                                int codigo = dados[j];
                                int pos;
                                if (H_DIV.equals(funcLabel)) {
                                    pos = fhash.hDiv(codigo);
                                } else {
                                    if (H_MUL.equals(funcLabel)) {
                                        pos = fhash.hMul(codigo);
                                    } else {
                                        pos = fhash.hFold(codigo);
                                    }
                                }

                                // registrar primeiros 10 h(k) (ordem de geração)
                                if (cntPrimeiros < 10) {
                                    primeirosHK[cntPrimeiros] = pos;
                                    cntPrimeiros = cntPrimeiros + 1;
                                }

                                // colisão na tabela?
                                if (baldes[pos] != null) {
                                    coll_tbl = coll_tbl + 1;
                                }

                                // inserir no final da lista
                                if (baldes[pos] == null) {
                                    baldes[pos] = new Registro(codigo);
                                } else {
                                    Registro cur = baldes[pos];
                                    int nos = 0;
                                    while (cur.proximo != null) {
                                        cur = cur.proximo;
                                        nos = nos + 1;
                                    }
                                    cur.proximo = new Registro(codigo);
                                    coll_lst = coll_lst + nos;
                                }

                                j = j + 1;
                            } // fim inserção

                            long t1 = System.nanoTime();
                            long insMs = (t1 - t0) / 1000000L;
                            somaInsMs = somaInsMs + insMs;
                            somaCollTbl = somaCollTbl + coll_tbl;
                            somaCollLst = somaCollLst + coll_lst;

                            // checksum segundo enunciado: soma dos primeiros 10 h(k)*h(k) mod 1000003
                            long somaHh = 0L;
                            int p = 0;
                            while (p < cntPrimeiros) {
                                long hk = (long) primeirosHK[p];
                                somaHh = somaHh + (hk * hk);
                                p = p + 1;
                            }
                            int checksum = (int) (somaHh % 1000003L);
                            if (checksum < 0) {
                                checksum = checksum + 1000003;
                            }
                            // definir auditoria (usar os primeiros 10 da primeira repetição)
                            if (rep == 0) {
                                checksumAuditoria = checksum;
                            }

                            // preparar lote: 50% hits (amostragem por stride) e 50% misses (garantidos ausentes)
                            int nBusca = n;
                            int metade = nBusca / 2;
                            int[] lote = new int[nBusca];

                            // hits por stride
                            int step = 1;
                            if (metade > 0) {
                                step = n / metade;
                                if (step < 1) {
                                    step = 1;
                                }
                            }
                            int ph = 0;
                            int posAmostra = 0;
                            while (ph < metade) {
                                lote[ph] = dados[posAmostra];
                                posAmostra = posAmostra + step;
                                if (posAmostra >= n) {
                                    posAmostra = posAmostra - n;
                                }
                                ph = ph + 1;
                            }

                            // misses gerados com LCG variante e verificados por varredura linear
                            GeradorLCG gerBusca = new GeradorLCG(seed + rep + 7);
                            int q = metade;
                            while (q < nBusca) {
                                int cand = gerBusca.proximoInt9();
                                boolean presente = false;
                                int z = 0;
                                while (z < n) {
                                    if (dados[z] == cand) {
                                        presente = true;
                                        break;
                                    }
                                    z = z + 1;
                                }
                                if (presente == true) {
                                    // descartar
                                } else {
                                    lote[q] = cand;
                                    q = q + 1;
                                }
                            }

                            // embaralhar lote
                            gerBusca.embaralhar(lote, nBusca);

                            // separar hits e misses consultando a tabela
                            int[] hits = new int[nBusca];
                            int[] misses = new int[nBusca];
                            int idxHits = 0;
                            int idxMisses = 0;
                            int rindex = 0;
                            while (rindex < nBusca) {
                                int cand = lote[rindex];
                                int pos;
                                if (H_DIV.equals(funcLabel)) {
                                    pos = fhash.hDiv(cand);
                                } else {
                                    if (H_MUL.equals(funcLabel)) {
                                        pos = fhash.hMul(cand);
                                    } else {
                                        pos = fhash.hFold(cand);
                                    }
                                }
                                boolean achou = false;
                                Registro cur = baldes[pos];
                                while (cur != null) {
                                    if (cur.codigo == cand) {
                                        achou = true;
                                        break;
                                    }
                                    cur = cur.proximo;
                                }
                                if (achou == true) {
                                    hits[idxHits] = cand;
                                    idxHits = idxHits + 1;
                                } else {
                                    misses[idxMisses] = cand;
                                    idxMisses = idxMisses + 1;
                                }
                                rindex = rindex + 1;
                            }

                            // medir hits (tempo e comparações)
                            long th0 = System.nanoTime();
                            long cmpHitsLocal = 0L;
                            int hv = 0;
                            while (hv < idxHits) {
                                int key = hits[hv];
                                int pos;
                                if (H_DIV.equals(funcLabel)) {
                                    pos = fhash.hDiv(key);
                                } else {
                                    if (H_MUL.equals(funcLabel)) {
                                        pos = fhash.hMul(key);
                                    } else {
                                        pos = fhash.hFold(key);
                                    }
                                }
                                Registro cur = baldes[pos];
                                while (cur != null) {
                                    cmpHitsLocal = cmpHitsLocal + 1;
                                    if (cur.codigo == key) {
                                        break;
                                    }
                                    cur = cur.proximo;
                                }
                                hv = hv + 1;
                            }
                            long th1 = System.nanoTime();
                            long hitsMs = (th1 - th0) / 1000000L;
                            somaFindHitsMs = somaFindHitsMs + hitsMs;
                            somaCmpHits = somaCmpHits + cmpHitsLocal;

                            // medir misses (tempo e comparações)
                            long tm0 = System.nanoTime();
                            long cmpMissesLocal = 0L;
                            int mv = 0;
                            while (mv < idxMisses) {
                                int key = misses[mv];
                                int pos;
                                if (H_DIV.equals(funcLabel)) {
                                    pos = fhash.hDiv(key);
                                } else {
                                    if (H_MUL.equals(funcLabel)) {
                                        pos = fhash.hMul(key);
                                    } else {
                                        pos = fhash.hFold(key);
                                    }
                                }
                                Registro cur = baldes[pos];
                                while (cur != null) {
                                    cmpMissesLocal = cmpMissesLocal + 1;
                                    if (cur.codigo == key) {
                                        break;
                                    }
                                    cur = cur.proximo;
                                }
                                mv = mv + 1;
                            }
                            long tm1 = System.nanoTime();
                            long missesMs = (tm1 - tm0) / 1000000L;
                            somaFindMissesMs = somaFindMissesMs + missesMs;
                            somaCmpMisses = somaCmpMisses + cmpMissesLocal;

                            rep = rep + 1;
                        } // fim repetições

                        // médias pedidas
                        long mediaInsMs = somaInsMs / REPETICOES;
                        long mediaHitsMs = somaFindHitsMs / REPETICOES;
                        long mediaMissesMs = somaFindMissesMs / REPETICOES;
                        long mediaCollTbl = somaCollTbl / REPETICOES;
                        long mediaCollLst = somaCollLst / REPETICOES;
                        long mediaCmpHits = somaCmpHits / REPETICOES;
                        long mediaCmpMisses = somaCmpMisses / REPETICOES;

                        // checksum legítimo: usei o calculado na primeira repetição (checksumAuditoria)
                        // observação: ele foi definido durante a 1ª repetição
                        int checksum = checksumAuditoria;

                        // imprimir CSV (ordem exata)
                        String linha = "" + m + "," + n + "," + funcLabel + "," + seed + "," +
                                mediaInsMs + "," + mediaCollTbl + "," + mediaCollLst + "," +
                                mediaHitsMs + "," + mediaMissesMs + "," +
                                mediaCmpHits + "," + mediaCmpMisses + "," + checksum;
                        System.out.println(linha);

                        ifunc = ifunc + 1;
                    } // fim funcoes

                    iseed = iseed + 1;
                } // fim seeds
                in = in + 1;
            } // fim ns
            im = im + 1;
        } // fim ms
    }
}
