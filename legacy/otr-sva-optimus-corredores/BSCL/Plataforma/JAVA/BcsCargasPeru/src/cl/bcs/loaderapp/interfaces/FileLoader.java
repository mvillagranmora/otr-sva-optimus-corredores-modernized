package cl.bcs.loaderapp.interfaces;

import java.util.List;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;

public interface FileLoader {

	public List<Object> parse(EntradaCargaGeneral entrada);

}
