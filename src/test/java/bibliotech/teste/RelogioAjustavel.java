package bibliotech.teste;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Relógio controlado pelo teste, para simular a passagem dos dias sem esperar. */
public final class RelogioAjustavel extends Clock {

    private Instant agora;

    public RelogioAjustavel(LocalDate inicio) {
        this.agora = inicio.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public void avancarDias(int dias) {
        agora = agora.plusSeconds(dias * 86_400L);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
