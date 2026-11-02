package br.com.conde.mersenne;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MersenneTest {

    @Test
    @DisplayName("2^p - 1 nos casos que se escreve de cabeca")
    void numero() {
        assertThat(Mersenne.numero(1)).isEqualTo(BigInteger.ONE);
        assertThat(Mersenne.numero(2)).isEqualTo(BigInteger.valueOf(3));
        assertThat(Mersenne.numero(3)).isEqualTo(BigInteger.valueOf(7));
        assertThat(Mersenne.numero(7)).isEqualTo(BigInteger.valueOf(127));
        assertThat(Mersenne.numero(31)).isEqualTo(BigInteger.valueOf(2_147_483_647L));
    }

    @Test
    @DisplayName("todo 2^p - 1 termina em 1 na base dois, com p uns")
    void todosUns() {
        for (int p = 1; p <= 40; p++) {
            String binario = Mersenne.numero(p).toString(2);
            assertThat(binario).as("p = %d", p).hasSize(p);
            assertThat(binario.chars().allMatch(c -> c == '1')).as("p = %d", p).isTrue();
        }
    }

    @Test
    @DisplayName("Lucas-Lehmer acerta os primeiros primos de Mersenne")
    void lucasLehmerNosConhecidos() {
        for (int p : new int[] {3, 5, 7, 13, 17, 19, 31, 61, 89, 107, 127}) {
            assertThat(Mersenne.lucasLehmer(p)).as("p = %d", p).isTrue();
        }
        for (int p : new int[] {11, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71, 73}) {
            assertThat(Mersenne.lucasLehmer(p)).as("p = %d", p).isFalse();
        }
    }

    @Test
    @DisplayName("o teste recusa expoente que nao e primo impar, em vez de dar resposta errada")
    void recusaExpoenteInvalido() {
        assertThatThrownBy(() -> Mersenne.lucasLehmer(2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Mersenne.lucasLehmer(9)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Mersenne.lucasLehmer(15)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Mersenne.numero(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("expoente composto SEMPRE da Mersenne composto")
    void expoenteCompostoNuncaDaPrimo() {
        for (int n = 4; n <= 40; n++) {
            if (Mersenne.primo(n)) {
                continue;
            }
            assertThat(Mersenne.numero(n).isProbablePrime(30)).as("n = %d", n).isFalse();
        }
    }

    @Test
    @DisplayName("todo fator achado tem a forma 2kp + 1 e e 1 ou 7 modulo 8")
    void formaDoFator() {
        for (int p : new int[] {11, 23, 29, 37, 41, 43, 47, 53, 59, 71, 73, 79, 83}) {
            BigInteger fator = Mersenne.fatorPelaForma(p, 1_000_000L);
            assertThat(fator).as("p = %d", p).isNotNull();
            long valor = fator.longValueExact();
            assertThat((valor - 1) % (2L * p)).as("p = %d, fator %d", p, valor).isZero();
            assertThat(valor % 8).as("p = %d, fator %d", p, valor).isIn(1L, 7L);
            assertThat(Mersenne.numero(p).mod(fator).signum()).as("p = %d", p).isZero();
        }
    }

    @Test
    @DisplayName("a forma corta a busca de fator em uma fatia minuscula")
    void aFormaCortaABusca() {
        for (int p : new int[] {11, 23, 59}) {
            long pelaForma = Mersenne.candidatosDaForma(p, 1_000_000L);
            assertThat(pelaForma).as("p = %d", p).isLessThan(30_000L);
            assertThat(1_000_000.0 / pelaForma).as("p = %d", p).isGreaterThan(40.0);
        }
    }

    @Test
    @DisplayName("o crivo de expoentes devolve primos de verdade")
    void crivoDeExpoentes() {
        int[] primos = Mersenne.primosAte(30);
        assertThat(primos).containsExactly(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);
        assertThat(Mersenne.primosAte(2000)).hasSize(303);
    }
}
