package cl.bcs.plataforma.frm;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ValidarContenidoCampos {
	
	String idCampo();
	String idCampoMensaje();
	String prefijo();
	Class<?> tipoDato();
	
}
