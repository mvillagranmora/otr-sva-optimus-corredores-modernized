package cl.bcs.custodia.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaEntrada;
import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada;
import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaSalida;
import cl.bcs.utils.Log;

public class CacheValorizacion {
	
	public static Log log = new Log("CUS.CacheValorizacion"); 
	
	private static HashMap<Long, List<ValorizarCustodiaEntrada>> threadItems = new HashMap<>();
	
	public static void agregar(ValorizarCustodiaEntrada vIE) {
		Long idThread = Thread.currentThread().getId();
		List<ValorizarCustodiaEntrada> itemsValorizar = threadItems.getOrDefault(idThread, new ArrayList<>());
		itemsValorizar.add(vIE);
		threadItems.put(idThread, itemsValorizar);
	}
	
	public static List<ValorizarCustodiaSalida> valorizar(Connection con) throws Exception {
		Long idThread = Thread.currentThread().getId();
		
		List<ValorizarCustodiaEntrada> items = threadItems.getOrDefault(idThread, new ArrayList<>());
		List<ValorizarCustodiaSalida> out = new ArrayList<>();
		
		ValorizarCustodia VI = new ValorizarCustodia();
		ValorizarCustodiaEntrada VIE = new ValorizarCustodiaEntrada();
		
		for (ValorizarCustodiaEntrada item : items) {
			
			if (items.indexOf(item) == 0) {
				VIE.p_IdEmpresa = item.p_IdEmpresa;
				VIE.p_IdModulo = item.p_IdModulo;
				VIE.p_IdPais = item.p_IdPais;
				VIE.p_IdUsuario = item.p_IdUsuario;
			}
			for (ObjDatoEntrada entrada : item.p_InstrumentosAValorizar) {
				
				VIE.p_InstrumentosAValorizar.add(entrada);
			}
		}
		
		if (VIE.p_InstrumentosAValorizar.size() > 0) {
			try {
				ValorizarCustodia.ValorizarCustodiaSalida VIS = VI.logica(con, VIE );
				out.add(VIS);
			}
			catch(Exception e) {
				items.clear();
				throw e;
			}
				
		}
		items.clear();
		threadItems.remove(idThread);
		return out;
		
	}
	

}
