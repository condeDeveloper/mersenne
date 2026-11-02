package br.com.conde.mersenne;

import java.math.BigInteger;

/**
 * Numeros de Mersenne e o teste de Lucas-Lehmer.
 *
 * <p>M_p = 2^p - 1. Quando p e composto, M_p tambem e, sempre: se p = a*b
 * entao 2^a - 1 divide 2^p - 1. O contrario nao vale, e e ai que mora o
 * assunto: p primo nao garante M_p primo, e separar os dois casos sem fatorar
 * um numero de centenas de digitos e o que Lucas-Lehmer faz.
 *
 * <p>O teste: comecando em S = 4, repita p-2 vezes S = S^2 - 2, tudo modulo
 * M_p. M_p e primo exatamente quando o resultado e zero. Nenhuma fatoracao,
 * nenhuma tentativa, nenhuma chance de erro — e so vale para numeros desta
 * forma.
 */
public final class Mersenne {

    private Mersenne() {
    }

    /** O numero 2^p - 1. */
    public static BigInteger numero(int p) {
        if (p < 1) {
            throw new IllegalArgumentException("expoente tem que ser positivo: " + p);
        }
        return BigInteger.ONE.shiftLeft(p).subtract(BigInteger.ONE);
    }

    /**
     * O teste de Lucas-Lehmer. So faz sentido para p primo maior que 2.
     *
     * @throws IllegalArgumentException se p nao for primo impar
     */
    public static boolean lucasLehmer(int p) {
        if (p < 3 || !primo(p)) {
            throw new IllegalArgumentException(
                    "Lucas-Lehmer so vale para p primo maior que 2, e veio " + p);
        }
        BigInteger m = numero(p);
        BigInteger s = BigInteger.valueOf(4);
        BigInteger dois = BigInteger.TWO;
        for (int i = 0; i < p - 2; i++) {
            s = s.multiply(s).subtract(dois).mod(m);
        }
        return s.signum() == 0;
    }

    /** Quantos quadrados o teste faz para um expoente. */
    public static int passosDeLucasLehmer(int p) {
        return p - 2;
    }

    /**
     * O juiz direto: primalidade geral, que nao sabe nada sobre a forma
     * 2^p - 1. Caro para numeros grandes, e por isso so e chamado onde cabe.
     */
    public static boolean primoGeral(BigInteger n, int certeza) {
        return n.isProbablePrime(certeza);
    }

    /**
     * O segundo juiz: todo fator primo de M_p com p primo tem a forma
     * 2*k*p + 1, e ainda precisa ser 1 ou 7 modulo 8. Isso reduz a busca de
     * fator a uma fracao minuscula dos candidatos.
     *
     * @return o menor fator encontrado, ou null se nenhum apareceu ate o teto
     */
    public static BigInteger fatorPelaForma(int p, long teto) {
        BigInteger m = numero(p);
        for (long k = 1; 2L * k * p + 1 <= teto; k++) {
            long candidato = 2L * k * p + 1;
            long resto8 = candidato % 8;
            if (resto8 != 1 && resto8 != 7) {
                continue;
            }
            if (!primo(candidato)) {
                continue;
            }
            BigInteger c = BigInteger.valueOf(candidato);
            if (m.mod(c).signum() == 0) {
                return c;
            }
        }
        return null;
    }

    /** Quantos candidatos a forma 2kp+1 deixa passar ate um teto. */
    public static long candidatosDaForma(int p, long teto) {
        long quantos = 0;
        for (long k = 1; 2L * k * p + 1 <= teto; k++) {
            long candidato = 2L * k * p + 1;
            long resto8 = candidato % 8;
            if (resto8 == 1 || resto8 == 7) {
                quantos++;
            }
        }
        return quantos;
    }

    public static boolean primo(long n) {
        if (n < 2) {
            return false;
        }
        if (n % 2 == 0) {
            return n == 2;
        }
        for (long d = 3; d * d <= n; d += 2) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }

    /** Os primos ate um limite, que sao os unicos expoentes que interessam. */
    public static int[] primosAte(int limite) {
        boolean[] composto = new boolean[limite + 1];
        int quantos = 0;
        for (int i = 2; i <= limite; i++) {
            if (!composto[i]) {
                quantos++;
                for (long j = (long) i * i; j <= limite; j += i) {
                    composto[(int) j] = true;
                }
            }
        }
        int[] primos = new int[quantos];
        int onde = 0;
        for (int i = 2; i <= limite; i++) {
            if (!composto[i]) {
                primos[onde++] = i;
            }
        }
        return primos;
    }
}
