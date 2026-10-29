package cl.bcs.loaderapp.concrete.socionegocio.relacionpersonaextra;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.RelacionPersonaExtra;

public class RelacionPersonaExtraDbLoader implements DbLoader {

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral entrada, List<Object> lines) {
		for (int i = 0; i < lines.size(); i++) {
			RelacionPersonaExtra relacionExtra = (RelacionPersonaExtra) lines.get(i);
			System.out.println(relacionExtra);
		}
		return null;
	}

}
