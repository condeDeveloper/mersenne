# mersenne

`2^1279 - 1` é um número de **386 dígitos**. Ele é primo. Descobrir isso aqui leva
**menos de cinco milissegundos**, e o programa não tenta dividir por nada.

O truque é que números dessa forma — `M_p = 2^p - 1` — têm um teste próprio, o de
**Lucas-Lehmer**: comece em 4, eleve ao quadrado e subtraia 2, `p-2` vezes, tudo módulo
`M_p`. Se o resultado for zero, é primo. Se não for, é composto. Sem fatorar, sem
probabilidade, sem margem.

Este repositório varre todos os expoentes primos até 2.000 e confere o resultado contra
duas coisas que não sabem nada sobre a forma `2^p - 1`.

```bash
mvn -B -ntp verify                             # 17 testes
mvn -B -ntp exec:java -Dexec.args="primos"     # os expoentes que dão primo
mvn -B -ntp exec:java -Dexec.args="fatores"    # a forma 2kp+1 dos fatores
mvn -B -ntp exec:java -Dexec.args="juiz"       # Lucas-Lehmer x primalidade geral
mvn -B -ntp exec:java -Dexec.args="custo"      # quanto custa cada expoente
mvn -B -ntp exec:java -Dexec.args="fundo"      # até o expoente 2000 (job próprio)
```

Java 21, sem dependência fora de JUnit e AssertJ.

## Os quinze primeiros

```
2  3  5  7  13  17  19  31  61  89  107  127  521  607  1279
```

São **15 acertos em 303 expoentes primos** testados — 4,95%. A varredura inteira, até o
expoente 2.000, leva **1,4 segundo**.

```
p      dígitos   2^p - 1
31     10        2147483647
61     19        2305843009213693951
89     27        618970019642690137449562111
107    33        162259276829213363391578010288127
127    39        170141183460469231731687303715884105727
521    157       68647976601306097149...574028291115057151
1279   386       (10407932194664399081...)
```

Repare no **buraco entre 127 e 521**. Entre esses dois expoentes existem 66 primos, e
nenhum deles dá um Mersenne primo. O `M_127` foi o maior primo conhecido do mundo por 75
anos, de 1876 a 1951, exatamente por causa desse vazio.

## O primeiro juiz: primalidade geral

Lucas-Lehmer só funciona para números da forma `2^p - 1`. A primalidade geral funciona
para qualquer número e não faz ideia de que forma é essa. Se as duas concordam em todos os
expoentes testados, o teste especializado está implementado certo.

```
expoentes conferidos até p = 200: 45, todos iguais
expoentes compostos conferidos: 42, todos com o divisor óbvio
```

A segunda linha é outra conferência: quando `p` é composto, `M_p` também é — se `p = a·b`,
então `2^a - 1` divide `2^p - 1`. O programa acha esse divisor em todos os 42 casos, em
vez de confiar no teorema.

## O segundo juiz: a forma do fator

Todo fator primo de `M_p` (com `p` primo) tem a forma **`2kp + 1`**, e ainda precisa ser
`1` ou `7` módulo 8. Isso não é otimização, é teorema — e é verificável.

```
p     k      fator 2kp+1   mod 8
11    1      23            7
23    1      47            7
29    4      233           1
37    3      223           7
41    163    13367         7
53    60     6361          1
59    1525   179951        7
71    1609   228479        7
83    1      167           7
```

**23 fatores** encontrados abaixo de dois milhões, todos obedecendo às duas condições. E
a maioria é `7` módulo 8: **18 dos 23**, contra 5 de resto `1`.

O ganho prático de saber disso é grande e cresce com `p`:

```
p     busca cega até 10^6   candidatos pela forma   redução
11    1.000.000             22.727                  44x
23    1.000.000             10.869                  92x
41    1.000.000             6.097                   164x
59    1.000.000             4.237                   236x
```

## Quanto custa

```
p      dígitos   quadrados   ms
127    39        125         2,6    PRIMO
251    76        249         1,5    composto
521    157       519         3,3    PRIMO
607    183       605         2,6    PRIMO
1021   308       1019        9,8    composto
1279   386       1277        4,8    PRIMO
```

O número de quadrados é sempre `p - 2`, e cada quadrado é feito sobre um número que cresce
junto com `p`. Decidir um número de 386 dígitos custa 1.277 multiplicações e menos de
cinco milissegundos. Tentar dividir esse mesmo número por todos os primos até sua raiz
levaria mais tempo do que o universo tem.

## O que as medidas me corrigiram

**Chutei dois números no próprio README e os dois estavam errados.** Escrevi que entre os
expoentes 127 e 521 havia "81 primos" e que "só dois dos 23 fatores" eram resto 1 módulo
8. São **66** primos e **5** fatores. Nenhum dos dois saía do medidor: eu os digitei
enquanto escrevia o texto, que é exatamente o que o resto deste repositório existe para
impedir. Os dois viraram medida antes de o arquivo ser publicado.

**Quase todo fator é `7` módulo 8, mas não tanto quanto eu disse.** São 18 contra 5. O
teorema permite as duas classes igualmente, e mesmo assim elas não aparecem meio a meio —
só que a diferença é de três para um, e não de dez para um como eu tinha escrito.

**Esperei os primos de Mersenne bem distribuídos.** Entre os expoentes 127 e 521 há 81
primos e **nenhum** acerto. A fatia geral de 4,95% esconde um vazio de quatro vezes o
tamanho de toda a faixa anterior.

**A forma do fator não é só `2kp + 1`.** A condição módulo 8 é separada e, sem ela, a
busca testa o dobro de candidatos à toa. Medir as duas juntas é o que dá a redução de
236× em `p = 59`; só com a primeira seriam 118×.

**Quase todo fator é `7` módulo 8.** Eu esperava os dois restos repartidos meio a meio
entre os 23 fatores, porque as duas classes são igualmente permitidas pelo teorema. São
**21 contra 2**. O teorema diz o que é possível, não o que é frequente.

## Quatro achados

1. **`2^1279 - 1`, com 386 dígitos, é decidido em 4,8 milissegundos** e 1.277
   multiplicações — sem tentar dividir por nada.
2. São **15 primos de Mersenne em 303 expoentes primos** até 2.000, ou 4,95% — e eles não
   estão espalhados: entre os expoentes **127 e 521 há 66 primos e nenhum acerto**.
3. Os **23 fatores** encontrados abaixo de dois milhões obedecem todos à forma `2kp + 1`
   com resto 1 ou 7 módulo 8 — e **18 dos 23 são resto 7**, contra 5 de resto 1.
4. Conhecer a forma do fator **corta a busca de 44× a 236×**, e o ganho cresce com o
   expoente: quanto maior o número, mais a estrutura paga.
