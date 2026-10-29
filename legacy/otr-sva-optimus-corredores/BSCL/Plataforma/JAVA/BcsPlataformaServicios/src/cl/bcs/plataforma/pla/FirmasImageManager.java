package cl.bcs.plataforma.pla;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FirmasImageManager implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	
	private final String SQL_SELECT_IMAGENES			  				= loadResourceToString("cl/bcs/plataforma/pla/sql/FirmasImageManager_SelectImagenes.sql");
	private final String SQL_DELETE_FIRMA				  				= loadResourceToString("cl/bcs/plataforma/pla/sql/FirmasImageManager_DeleteFirma.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class FirmasImageManager_Entrada
	{
		public Long		idUsuario;
		public Long		idEmpresa;
		public Long		idDocumento;
		public Long		idPersona;
		public String	name;
		public Long	    version;
		public String   file;
		public String	type;
		public String	ext;
		public String 	data;
		public String 	operacion;

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class FirmasImageManager_Salida
	{
		public String				P_OUT_MENSAJES;
		public String				P_OUT_IMAGEN;
		public Long 				P_OUT_ERRORESCONTADOR;

		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class FirmasSelect
	{
		public Long		IdEmpresa;
		public String	IdImagen;
		public String	NombreImagen;
		public Long		IdDocumentoIdentidad;
		public Long		IdPersona;
		public String   Tipo;
		public String   Extension;
		public String	Version;
		public byte[]   Binario;

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public FirmasImageManager_Salida logica(Connection conn, FirmasImageManager_Entrada in) throws Exception
	{	
		
		FirmasImageManager_Salida out = new FirmasImageManager_Salida();
		// CONSULTAR EXISTENCIA DE FIRMA EN LA BD
		
		if(in.operacion.equals("CREATE"))
		{
			if(ExisteFirma(in,conn,out).isEmpty()) {
				InputStream BlobStream = null;
				if(ImagenLista(in)!=null) {
					BlobStream = ImagenLista(in);
					final String insertSql = "INSERT INTO \""+Utils.getSchema()+"\".\"BSCL.SocioNegocio.BD::PER.ImagenesFirmas\"(\"IdEmpresa\", \"IdImagen\", \"NombreImagen\",\"IdDocumentoIdentidad\",\"IdPersona\", \"Tipo\", \"Extension\", \"Version\", \"Binario\") VALUES(?,CAST (SYSUUID AS VARCHAR),?,?,?,?,?,?,?)";
					try (PreparedStatement pstmt = conn.prepareStatement(insertSql)){
						pstmt.setLong(1, in.idEmpresa);
						pstmt.setString(2, in.name);
						pstmt.setLong(3, in.idDocumento);
						pstmt.setLong(4, in.idPersona);
						pstmt.setString(5, in.type);
						pstmt.setString(6, in.ext);
						pstmt.setLong(7, in.version);
						pstmt.setBinaryStream(8, BlobStream);
						pstmt.execute();
						conn.commit();
						out.P_OUT_MENSAJES="UI_FIRMACREADA";
						out.P_OUT_ERRORESCONTADOR=0L;
					}catch(SQLException ex){
						out.P_OUT_ERRORESCONTADOR=1L;
						conn.rollback();
						throw new Exception(ex);
					}
				}else{
					out.P_OUT_ERRORESCONTADOR=0L;
				}	
			}else {	
				out.P_OUT_ERRORESCONTADOR=1L;
				out.P_OUT_MENSAJES="UI_FIRMAEXISTE";
			}
			}else if(in.operacion.equals("UPDATE")){
				
				if(!ExisteFirma(in,conn,out).isEmpty()) {
					InputStream BlobStream = null;
					if(ImagenLista(in)!=null){
						BlobStream = ImagenLista(in);
						String updateSql = "UPDATE \""+Utils.getSchema()+"\".\"BSCL.SocioNegocio.BD::PER.ImagenesFirmas\" SET \"NombreImagen\" =?, \"Tipo\"=?, \"Extension\"=?, \"Version\"=?, \"Binario\"=? WHERE \"IdPersona\"=? and \"IdDocumentoIdentidad\"=?";
						
						try (PreparedStatement pstmtMod = conn.prepareStatement(updateSql)){
							pstmtMod.setString(1, in.name);
							pstmtMod.setString(2, in.type);
							pstmtMod.setString(3, in.ext);
							pstmtMod.setLong(4, in.version);
							pstmtMod.setBinaryStream(5, BlobStream);
							pstmtMod.setLong(6, in.idPersona);
							pstmtMod.setLong(7, in.idDocumento);
							pstmtMod.execute();
							conn.commit();
							out.P_OUT_ERRORESCONTADOR=0L;
							out.P_OUT_MENSAJES="UI_FIRMAMODIFICADA";
						}catch(SQLException ex){
							out.P_OUT_ERRORESCONTADOR=1L;
							conn.rollback();
							throw new Exception(ex);
						}
					}else {
						out.P_OUT_ERRORESCONTADOR=1L;
					}
				}else {
					out.P_OUT_ERRORESCONTADOR=1L;
					out.P_OUT_MENSAJES="UI_FIRMANOEXISTE";

				}
			
		}else if(in.operacion.equals("DELETE")){
			
			if(!ExisteFirma(in,conn,out).isEmpty()) {
				try {
					SQL sql = new SQL();
					FirmasSelect objDelete = new FirmasSelect();
					List<FirmasSelect> listDelete = new ArrayList<FirmasSelect>();
					objDelete.IdDocumentoIdentidad = in.idDocumento;
					objDelete.IdPersona = in.idPersona;
					listDelete.add(objDelete);
					sql.batch(conn, SQL_DELETE_FIRMA,listDelete, FirmasSelect.class);
					out.P_OUT_ERRORESCONTADOR=0L;
					out.P_OUT_MENSAJES="UI_FIRMAELIMINADA";
				} catch (Exception e) {
					out.P_OUT_ERRORESCONTADOR=1L;
				}
			}else {
				out.P_OUT_ERRORESCONTADOR=1L;
				out.P_OUT_MENSAJES="FIRMA NO EXISTE";
			}
		}else if (in.operacion.equals("SELECT")) {
			
			try {
				List<FirmasSelect> firmasObtenidas = ExisteFirma(in,conn,out);
				if(!firmasObtenidas.isEmpty()) {
					byte[] firmaObtenida = firmasObtenidas.get(0).Binario;
					String firma64 = Base64.getEncoder().encodeToString(firmaObtenida);
					String imagenbase64 = "data:"+firmasObtenidas.get(0).Tipo+";base64,"+firma64;
					out.P_OUT_IMAGEN=imagenbase64;
					out.P_OUT_ERRORESCONTADOR=0L;
					out.P_OUT_MENSAJES="UI_SELECTOK";
				}else {
					out.P_OUT_ERRORESCONTADOR=0L;
					out.P_OUT_IMAGEN="";
					out.P_OUT_MENSAJES="UI_SELECTNOOK";
				}
			} catch (Exception e) {
				out.P_OUT_ERRORESCONTADOR=1L;
				out.P_OUT_IMAGEN="";
				out.P_OUT_MENSAJES=e.toString();
			}
		}
			
		return out;
	}
	
	public InputStream ImagenLista (FirmasImageManager_Entrada in) throws IOException {
	if (in.data != null) {
		InputStream BlobStream 	= null;
		BufferedImage image2 	= null;
		byte[] imageByte2 		= Base64.getDecoder().decode(in.data.split(",")[1]);
		BlobStream = new ByteArrayInputStream(imageByte2);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		image2 = ImageIO.read(BlobStream);
		ImageIO.write(image2, in.ext, bos );
		byte [] data = bos.toByteArray();
		BlobStream = new ByteArrayInputStream(data);
		BlobStream.close();
		return BlobStream;
	}else {
		return null;
	}
	}
	
	public List<FirmasSelect> ExisteFirma (FirmasImageManager_Entrada in,Connection conn,FirmasImageManager_Salida out) throws SQLException {
	SQL sql = new SQL();
	List<FirmasSelect> firmasSelect = new ArrayList<>();
	try {
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdPersona", in.idPersona);
		params.put("IdDocumentoIdentidad", in.idDocumento);
		firmasSelect = sql.query(conn, SQL_SELECT_IMAGENES,params, FirmasSelect.class);	
		return firmasSelect;
	} catch (Exception e) {
		out.P_OUT_ERRORESCONTADOR=1L;
	}
	return firmasSelect;
	}

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		FirmasImageManager_Entrada in = gson.fromJson(jin.toString(), FirmasImageManager_Entrada.class);
		FirmasImageManager_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
   
			String jsonCL = "{\"idUsuario\":6,\"idEmpresa\":1,\"idDocumento\":574401,\"idPersona\":621869,\"name\":\"select\",\"version\":1,\"file\":\"N\",\"type\":\"select\",\"ext\":\"jpg\",\"data\":\"AAAA\",\"operacion\":\"SELECT\"}";
			
			FirmasImageManager_Entrada Objin = gson.fromJson(jsonCL, FirmasImageManager_Entrada.class);

			FirmasImageManager demo = new FirmasImageManager();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				FirmasImageManager_Salida ObjOut = demo.logica(con, Objin);
				//con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}