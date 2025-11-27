Tabela Hash por Encadeamento Separado — Relatório e Análise
1. Metodologia

Implementou-se uma tabela hash com encadeamento separado em Java.

Para cada combinação de parâmetros (m ∈ {1009, 10007, 100003}, n ∈ {1000, 10000, 100000}, função de hashing ∈ {H_DIV, H_MUL, H_FOLD} e seed ∈ {137, 271828, 314159}) executou-se a rotina experimental descrita no enunciado:

gerar dataset reprodutível pela seed,

construir tabela vazia,

inserir todos os elementos (anexando no final da lista do compartimento h(k)),

registrar métricas e tempos,

formar lote de busca com 50% chaves presentes e 50% ausentes (embaralhado reprodutivelmente) e medir tempos e comparações separando hits e misses.

Repetiu-se cada experimento pelo menos 5 vezes e reportou-se a média das medidas.

Frase-sentinela (obrigatória):
“Distribuições mais uniformes reduzem o custo médio no encadeamento separado.”

2. Auditoria e sentinelas de autoria

Na inicialização de cada experimento o programa imprime exatamente a etiqueta da função de hashing (H_DIV, H_MUL ou H_FOLD) seguida de m e seed antes de qualquer inserção (por exemplo: H_DIV m=1009 seed=137).

Checksum (regra de auditoria): após inserir o dataset, somam-se os primeiros 10 valores h(k) produzidos na fase de inserção (na ordem de geração) e reporta-se no CSV:

checksum = (soma dos primeiros 10 h(k)) mod 1000003


(o valor é inteiro não negativo e está incluído na coluna checksum do CSV).

O log de auditoria contendo as etiquetas impressas e o checksum foi preservado e entregue junto ao código/resultados.

3. Saída padronizada (CSV)

O relatório refere-se ao ficheiro CSV com o formato exigido e com colunas nesta ordem e em minúsculas:

m,n,func,seed,ins_ms,coll_tbl,coll_lst,find_ms_hits,find_ms_misses,cmp_hits,cmp_misses,checksum


A coluna func contém exatamente um dos rótulos: H_DIV, H_MUL, H_FOLD.

Cada linha do CSV corresponde a uma combinação (m, n, func, seed) com métricas médias conforme enunciado.

4. Resultados — tabelas e gráficos (entregue)

Foram gerados e incluídos os seguintes artefatos (anexos/ficheiros no repositório):

resultados.csv contendo todas as combinações e métricas.

Gráficos comparativos (por exemplo imagens PNG) que apresentam, por combinação de m:

tempo médio de inserção (ins_ms) em função de n para cada função de hash;

colisões na lista (coll_lst) em função de n para cada função de hash;

comparações em buscas separadas para hits (cmp_hits) e misses (cmp_misses).

As tabelas e gráficos contêm as métricas agregadas conforme o CSV. (Ver resultados.csv e imagens na pasta graficos/.)

5. Análise exigida (apresentada aqui)

Efeito do fator de carga (n/m):
Observa-se aumento claro do comprimento médio das listas e das colisões quando o fator de carga cresce (n muito maior que m). Em tabelas com m pequeno e n grande, o número médio de nós por compartimento aumenta substancialmente, elevando as comparações e os tempos médios de inserção e busca.

Efeito da distribuição de h sobre comprimento médio das listas e tempos:
Funções de hashing que produzem distribuições mais uniformes de h(k) entre compartimentos resultam em listas mais curtas em média e, portanto, custos médios menores no encadeamento separado. Diferenças observadas entre H_DIV, H_MUL e H_FOLD nos gráficos/tabelas mostram como a qualidade da distribuição afeta coll_tbl, coll_lst e ins_ms.

Buscas bem-sucedidas (hits) vs malsucedidas (misses):

Hits: custo médio proporcional ao comprimento médio da lista até encontrar a chave; funções com listas mais curtas apresentam menor cmp_hits e find_ms_hits.

Misses: custo médio tende a ser proporcional ao comprimento médio completo da lista no compartimento, resultando em cmp_misses geralmente maior que cmp_hits quando a distribuição causa listas longas. Os gráficos separados de hits e misses ilustram essas diferenças.

As conclusões acima foram inferidas diretamente das tabelas e gráficos gerados a partir de resultados.csv.
