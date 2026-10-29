package cl.bcs.apv.familia_b.support;

import java.io.IOException;

import cl.bcs.apv.domain.port.out.AlmacenArchivos;

/** No family B legacy class writes a file (see CONTRATO.md section 7): any use of the port is a defect of the modern code and fails the case. */
public class AlmacenArchivosDeCaso implements AlmacenArchivos {

    @Override
    public void escribir(String rutaRelativa, byte[] contenido) throws IOException {
        throw new AssertionError("family B must not write files, but the modern code wrote " + rutaRelativa);
    }
}
