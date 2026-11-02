package br.com.conde.mersenne;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Varre os expoentes primos e separa quais M_p sao primos. */
public final class Varredura {

    /** O que se descobriu sobre um expoente. */
    public record Achado(int expoente, boolean primo, int digitos, long nanos, BigInteger menorFator) {
    }

    private Varredura() {
    }

    /** Roda Lucas-Lehmer em todo expoente primo ate o limite. */
    public static List<Achado> ate(int limiteDoExpoente) {
        List<Achado> achados = new ArrayList<>();
        for (int p : Mersenne.primosAte(limiteDoExpoente)) {
            if (p == 2) {
                // M_2 = 3 e primo, mas Lucas-Lehmer nao se aplica: o teste
                // comeca em S_0 = 4 e precisa de pelo menos um passo.
                achados.add(new Achado(2, true, 1, 0, null));
                continue;
            }
            long comeco = System.nanoTime();
            boolean ehPrimo = Mersenne.lucasLehmer(p);
            long gasto = System.nanoTime() - comeco;
            achados.add(new Achado(p, ehPrimo, Mersenne.numero(p).toString().length(), gasto, null));
        }
        return achados;
    }

    /** Só os expoentes em que M_p deu primo. */
    public static List<Integer> expoentesPrimos(int limiteDoExpoente) {
        List<Integer> quais = new ArrayList<>();
        for (Achado a : ate(limiteDoExpoente)) {
            if (a.primo()) {
                quais.add(a.expoente());
            }
        }
        return quais;
    }

    /**
     * Confere Lucas-Lehmer contra a primalidade geral, que nao conhece a forma
     * 2^p - 1.
     *
     * @return quantos expoentes foram conferidos
     */
    public static int conferirContraPrimalidadeGeral(int limiteDoExpoente, int certeza) {
        int conferidos = 0;
        for (int p : Mersenne.primosAte(limiteDoExpoente)) {
            if (p == 2) {
                continue;
            }
            boolean porLucasLehmer = Mersenne.lucasLehmer(p);
            boolean porPrimalidadeGeral = Mersenne.primoGeral(Mersenne.numero(p), certeza);
            if (porLucasLehmer != porPrimalidadeGeral) {
                throw new IllegalStateException("discordaram em p = " + p
                        + ": Lucas-Lehmer diz " + porLucasLehmer
                        + ", primalidade geral diz " + porPrimalidadeGeral);
            }
            conferidos++;
        }
        return conferidos;
    }

    /**
     * Confere que todo M_p composto tem fator da forma 2kp+1, procurando um de
     * verdade dentro do teto.
     *
     * @return quantos compostos tiveram fator achado
     */
    public static int acharFatores(int limiteDoExpoente, long teto, List<int[]> ondeAchou) {
        int achou = 0;
        for (int p : Mersenne.primosAte(limiteDoExpoente)) {
            if (p == 2 || Mersenne.lucasLehmer(p)) {
                continue;
            }
            BigInteger fator = Mersenne.fatorPelaForma(p, teto);
            if (fator != null) {
                achou++;
                long k = (fator.longValueExact() - 1) / (2L * p);
                ondeAchou.add(new int[] {p, (int) k, fator.intValueExact()});
            }
        }
        return achou;
    }

    /** Confere que expoente composto sempre da Mersenne composto. */
    public static int conferirExpoenteComposto(int limite) {
        int conferidos = 0;
        for (int n = 4; n <= limite; n++) {
            if (Mersenne.primo(n)) {
                continue;
            }
            BigInteger m = Mersenne.numero(n);
            // procura o divisor pela propria decomposicao: 2^a - 1 divide 2^n - 1
            boolean achou = false;
            for (int a = 2; a < n && !achou; a++) {
                if (n % a == 0) {
                    BigInteger d = Mersenne.numero(a);
                    if (m.mod(d).signum() == 0) {
                        achou = true;
                    }
                }
            }
            if (!achou) {
                throw new IllegalStateException("expoente composto sem divisor obvio: " + n);
            }
            conferidos++;
        }
        return conferidos;
    }
}
