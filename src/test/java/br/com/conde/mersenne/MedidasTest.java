package br.com.conde.mersenne;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os numeros do README, presos. Todos sairam do {@link Medidor}. */
class MedidasTest {

    @Test
    @DisplayName("os quinze primeiros primos de Mersenne, ate o expoente 2000")
    void osQuinzePrimeiros() {
        List<Integer> quais = Varredura.expoentesPrimos(Medidor.EXPOENTE_FUNDO);
        assertThat(quais).containsExactly(
                2, 3, 5, 7, 13, 17, 19, 31, 61, 89, 107, 127, 521, 607, 1279);
        assertThat(quais).hasSize(15);
    }

    @Test
    @DisplayName("entre os expoentes 127 e 521 ha 66 primos e nenhum acerto")
    void oVazioEntre127E521() {
        int entre = 0;
        for (int p : Mersenne.primosAte(520)) {
            if (p > 127) {
                entre++;
            }
        }
        assertThat(entre).isEqualTo(66);
        for (int p : Mersenne.primosAte(520)) {
            if (p > 127) {
                assertThat(Mersenne.lucasLehmer(p)).as("p = %d", p).isFalse();
            }
        }
    }

    @Test
    @DisplayName("quinze acertos em 303 expoentes primos: 4,95%")
    void aFatiaDeAcertos() {
        int expoentesPrimos = Mersenne.primosAte(Medidor.EXPOENTE_FUNDO).length;
        assertThat(expoentesPrimos).isEqualTo(303);
        assertThat(100.0 * 15 / expoentesPrimos)
                .isCloseTo(4.950, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("M_1279 tem 386 digitos")
    void oMaiorDaVarredura() {
        assertThat(Mersenne.numero(1279).toString()).hasSize(386);
        assertThat(Mersenne.passosDeLucasLehmer(1279)).isEqualTo(1277);
        assertThat(Mersenne.lucasLehmer(1279)).isTrue();
    }

    @Test
    @DisplayName("treze primos de Mersenne ate o expoente 600")
    void ateSeiscentos() {
        assertThat(Varredura.expoentesPrimos(Medidor.EXPOENTE)).hasSize(13);
        assertThat(Mersenne.primosAte(Medidor.EXPOENTE)).hasSize(109);
    }

    @Test
    @DisplayName("Lucas-Lehmer e primalidade geral concordam em 45 expoentes")
    void juiz() {
        assertThat(Varredura.conferirContraPrimalidadeGeral(Medidor.EXPOENTE_JUIZ, 40))
                .isEqualTo(45);
    }

    @Test
    @DisplayName("42 expoentes compostos, todos com o divisor obvio")
    void expoentesCompostos() {
        assertThat(Varredura.conferirExpoenteComposto(60)).isEqualTo(42);
    }

    @Test
    @DisplayName("23 fatores achados abaixo de dois milhoes")
    void fatoresAchados() {
        List<int[]> onde = new ArrayList<>();
        int achou = Varredura.acharFatores(Medidor.EXPOENTE_JUIZ, Medidor.TETO_DE_FATOR, onde);
        assertThat(achou).isEqualTo(23);
        assertThat(onde).hasSize(23);

        // os primeiros, com o k que a forma 2kp+1 exige
        assertThat(onde.get(0)).containsExactly(11, 1, 23);
        assertThat(onde.get(1)).containsExactly(23, 1, 47);
        assertThat(onde.get(2)).containsExactly(29, 4, 233);
    }

    @Test
    @DisplayName("quase todo fator e 7 modulo 8, e nao 1")
    void osFatoresPreferemOSete() {
        List<int[]> onde = new ArrayList<>();
        Varredura.acharFatores(Medidor.EXPOENTE_JUIZ, Medidor.TETO_DE_FATOR, onde);
        long sete = onde.stream().filter(l -> l[2] % 8 == 7).count();
        long um = onde.stream().filter(l -> l[2] % 8 == 1).count();
        assertThat(sete + um).isEqualTo(onde.size());
        assertThat(sete).isEqualTo(18);
        assertThat(um).isEqualTo(5);
    }

    @Test
    @DisplayName("a forma 2kp+1 corta a busca de 44x a 236x")
    void reducaoDaBusca() {
        assertThat(1_000_000.0 / Mersenne.candidatosDaForma(11, 1_000_000L))
                .isCloseTo(44.0, org.assertj.core.data.Offset.offset(1.0));
        assertThat(1_000_000.0 / Mersenne.candidatosDaForma(59, 1_000_000L))
                .isCloseTo(236.0, org.assertj.core.data.Offset.offset(1.0));
    }
}
