package br.com.conde.mersenne;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Roda as tabelas do README.
 *
 * <pre>
 * mvn -B -ntp exec:java -Dexec.args="primos"
 * mvn -B -ntp exec:java -Dexec.args="fatores"
 * mvn -B -ntp exec:java -Dexec.args="juiz"
 * mvn -B -ntp exec:java -Dexec.args="custo"
 * mvn -B -ntp exec:java -Dexec.args="fundo"     (ate expoente 2000, job proprio)
 * </pre>
 */
public final class Medidor {

    public static final int EXPOENTE = 600;
    public static final int EXPOENTE_JUIZ = 200;
    public static final int EXPOENTE_FUNDO = 2_000;
    public static final long TETO_DE_FATOR = 2_000_000L;

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";
        switch (comando) {
            case "primos" -> primos();
            case "fatores" -> fatores();
            case "juiz" -> juiz();
            case "custo" -> custo();
            case "fundo" -> fundo();
            case "tudo" -> {
                primos();
                fatores();
                juiz();
                custo();
            }
            default -> {
                System.out.println("comando desconhecido: " + comando);
                System.exit(2);
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("-".repeat(texto.length()));
    }

    private static void primos() {
        titulo("Expoentes ate " + EXPOENTE + " em que 2^p - 1 e primo");
        List<Varredura.Achado> achados = Varredura.ate(EXPOENTE);
        System.out.printf("%-8s %-10s %s%n", "p", "digitos", "2^p - 1");
        int quantos = 0;
        for (Varredura.Achado a : achados) {
            if (!a.primo()) {
                continue;
            }
            quantos++;
            BigInteger m = Mersenne.numero(a.expoente());
            String texto = m.toString();
            if (texto.length() > 44) {
                texto = texto.substring(0, 20) + "..." + texto.substring(texto.length() - 18);
            }
            System.out.printf("%-8d %-10d %s%n", a.expoente(), a.digitos(), texto);
        }
        int expoentesPrimos = Mersenne.primosAte(EXPOENTE).length;
        System.out.printf("%n%d primos de Mersenne em %d expoentes primos testados (%.2f%%)%n",
                quantos, expoentesPrimos, 100.0 * quantos / expoentesPrimos);
    }

    private static void fatores() {
        titulo("O fator de um Mersenne composto tem a forma 2kp + 1");
        List<int[]> onde = new ArrayList<>();
        int achou = Varredura.acharFatores(EXPOENTE_JUIZ, TETO_DE_FATOR, onde);
        System.out.printf("%-8s %-8s %-14s %s%n", "p", "k", "fator 2kp+1", "fator mod 8");
        for (int i = 0; i < Math.min(16, onde.size()); i++) {
            int[] linha = onde.get(i);
            System.out.printf("%-8d %-8d %-14d %d%n",
                    linha[0], linha[1], linha[2], linha[2] % 8);
        }
        System.out.printf("%nfatores achados abaixo de %d: %d%n", TETO_DE_FATOR, achou);
        System.out.println();
        System.out.println("Quantos candidatos a forma deixa passar, contra a busca cega:");
        System.out.printf("%-8s %-16s %-16s %s%n", "p", "cega ate 10^6", "pela forma", "reducao");
        for (int p : new int[] {11, 23, 29, 37, 41, 59}) {
            long pelaForma = Mersenne.candidatosDaForma(p, 1_000_000L);
            System.out.printf("%-8d %-16d %-16d %.0fx%n",
                    p, 1_000_000L, pelaForma, 1_000_000.0 / pelaForma);
        }
    }

    private static void juiz() {
        titulo("Lucas-Lehmer contra primalidade geral, ate p = " + EXPOENTE_JUIZ);
        int quantos = Varredura.conferirContraPrimalidadeGeral(EXPOENTE_JUIZ, 40);
        System.out.printf("expoentes conferidos: %d, todos iguais%n", quantos);
        System.out.println();
        System.out.printf("expoente composto sempre da Mersenne composto: %d casos conferidos%n",
                Varredura.conferirExpoenteComposto(60));
    }

    private static void custo() {
        titulo("O que Lucas-Lehmer custa, por expoente");
        System.out.printf("%-8s %-10s %-12s %s%n", "p", "digitos", "quadrados", "milissegundos");
        for (int p : new int[] {127, 251, 521, 607, 1021, 1279}) {
            long comeco = System.nanoTime();
            boolean primo = Mersenne.lucasLehmer(p);
            double ms = (System.nanoTime() - comeco) / 1e6;
            System.out.printf("%-8d %-10d %-12d %.1f  %s%n",
                    p, Mersenne.numero(p).toString().length(),
                    Mersenne.passosDeLucasLehmer(p), ms, primo ? "PRIMO" : "composto");
        }
    }

    private static void fundo() {
        titulo("Varredura ate o expoente " + EXPOENTE_FUNDO);
        long comeco = System.nanoTime();
        List<Integer> quais = Varredura.expoentesPrimos(EXPOENTE_FUNDO);
        System.out.println("expoentes com 2^p - 1 primo:");
        System.out.println("   " + quais);
        int expoentesPrimos = Mersenne.primosAte(EXPOENTE_FUNDO).length;
        System.out.printf("%n%d primos de Mersenne em %d expoentes primos (%.3f%%)%n",
                quais.size(), expoentesPrimos, 100.0 * quais.size() / expoentesPrimos);
        BigInteger maior = Mersenne.numero(quais.get(quais.size() - 1));
        System.out.printf("o maior deles tem %d digitos%n", maior.toString().length());
        System.out.printf("%ntempo: %.1f s%n", (System.nanoTime() - comeco) / 1e9);

        List<Integer> esperado = List.of(2, 3, 5, 7, 13, 17, 19, 31, 61, 89, 107, 127, 521, 607, 1279);
        if (!quais.equals(esperado)) {
            System.out.println("MEDIDA MUDOU: a lista de expoentes nao bate com a do README");
            System.out.println("   esperado: " + esperado);
            System.exit(1);
        }
        System.out.println("a lista do README bateu");
    }
}
