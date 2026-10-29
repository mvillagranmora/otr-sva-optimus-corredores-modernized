package cl.bcs.plataforma.pla.utils;

public interface BarraProgreso {
	void actualizar(Long porcentaje, String estado, String mensaje) throws Exception;
    void actualizar(Long porcentaje, String estado, com.google.gson.JsonObject jsonData) throws Exception;
    void finalizar() throws Exception;
    BarraProgreso subrango(long desde, long hasta); 
}
