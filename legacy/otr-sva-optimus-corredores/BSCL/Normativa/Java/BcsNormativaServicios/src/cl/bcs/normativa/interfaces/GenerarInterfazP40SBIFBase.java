package cl.bcs.normativa.interfaces;

import java.sql.SQLException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.normativa.interfaces.ClienteConfigFactory;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.custodia.ValorizacionValoresAdicionalesBatch.InValorizacionValoresAdicionales;
import cl.bcs.normativa.interfaces.pojo.ParametrosGeneracionP40;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public interface GenerarInterfazP40SBIFBase extends JavaCall{

	SalidaGenerarInterfazP40SBIF logica (Connection con, EntradaGenerarInterfazP40SBIF entrada) throws Exception;
	boolean esFinDeMes(Connection con, Date fechaOperacion) throws Exception;
	String getFechaFinDeMes(Connection con, Date fechaOperacion) throws Exception;
	String getFechaPrimerDiaMesSiguiente(Date fechaOperacion);
	JsonObject call(Connection conn, JsonObject jin) throws Exception;
}


	
