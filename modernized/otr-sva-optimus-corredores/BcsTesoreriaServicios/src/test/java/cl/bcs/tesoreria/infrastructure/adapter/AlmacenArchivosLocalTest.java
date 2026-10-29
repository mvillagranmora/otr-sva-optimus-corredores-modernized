package cl.bcs.tesoreria.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Family B file-output adapter: writes below the configured base and rejects every path that escapes it. */
class AlmacenArchivosLocalTest {

    @TempDir
    Path base;

    @Test
    void escribeBajoLaBaseEnUnaCarpetaExistente() throws IOException {
        Files.createDirectories(base.resolve("CARGA"));
        new AlmacenArchivosLocal(base.toString()).escribir("CARGA/Carga_20250401.csv", "a;b\n".getBytes(StandardCharsets.UTF_8));

        assertThat(Files.readString(base.resolve("CARGA/Carga_20250401.csv"))).isEqualTo("a;b\n");
    }

    @Test
    void unaCarpetaInexistenteNoSeCrea() {
        assertThatThrownBy(() -> new AlmacenArchivosLocal(base.toString()).escribir("NO_EXISTE/a.csv", new byte[0])).isInstanceOf(java.nio.file.NoSuchFileException.class);
        assertThat(base.resolve("NO_EXISTE")).doesNotExist();
    }

    @Test
    void rechazaRutasQueEscapanDeLaBase() {
        AlmacenArchivosLocal almacen = new AlmacenArchivosLocal(base.toString());

        assertThatThrownBy(() -> almacen.escribir("../fuera.csv", new byte[0])).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> almacen.escribir("CARGA/../../fuera.csv", new byte[0])).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> almacen.escribir("..\\fuera.csv", new byte[0])).isInstanceOf(IOException.class);
        assertThat(base.getParent().resolve("fuera.csv")).doesNotExist();
    }

    @Test
    void rechazaRutasAbsolutasYVacias() {
        AlmacenArchivosLocal almacen = new AlmacenArchivosLocal(base.toString());
        String absoluta = base.resolve("x.csv").toAbsolutePath().toString();

        assertThatThrownBy(() -> almacen.escribir(absoluta, new byte[0])).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> almacen.escribir("", new byte[0])).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> almacen.escribir(null, new byte[0])).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> almacen.escribir(".", new byte[0])).isInstanceOf(IOException.class);
    }

    @Test
    void sinBaseConfiguradaFallaAlEscribirNoAlArrancar() {
        AlmacenArchivosLocal sinBase = new AlmacenArchivosLocal("");

        assertThatThrownBy(() -> sinBase.escribir("a.csv", new byte[0])).isInstanceOf(IllegalStateException.class);
    }
}
