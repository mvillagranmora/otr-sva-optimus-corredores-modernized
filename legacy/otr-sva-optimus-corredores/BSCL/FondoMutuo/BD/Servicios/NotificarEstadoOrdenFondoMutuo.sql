SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::NotificarEstadoOrdenFondoMutuo";
GO
CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::NotificarEstadoOrdenFondoMutuo" (
	IN  p_IdEmpresa							BIGINT,			
	IN  p_IdUsuario							BIGINT,			
	IN  p_IdModulo							NVARCHAR(3),
	
	IN  p_Solicitudes						TABLE(
												"IdSolicitudAporteRescate" BIGINT
											)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	BEGIN
		--------------------------------------------------------------------------------------------------------------------
		--DECLARE VARIABLES
		--------------------------------------------------------------------------------------------------------------------	
		DECLARE p_Eventos TABLE("CodigoTipoEvento" nvarchar(40), "IdSecuencia" BIGINT , "FechaCreacionSistema" SECONDDATE, "IdPersonaRemitente" BIGINT, "IdPersonaDestinatario" BIGINT, "IdPortafolio" BIGINT);
		DECLARE p_EventoDetalle TABLE("IdSecuencia"  BIGINT,"Llave"	 NVARCHAR(40),"Valor"	NVARCHAR(300));
		DECLARE p_ArchivosAdjuntos TABLE("IdSecuencia"  BIGINT,"ArchivoAdjunto"	 BLOB,"Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520));
		DECLARE p_IdDirecciones TABLE ("IdSecuencia" BIGINT, "IdDireccion" BIGINT,"TipoDestinatario" NVARCHAR(6));
		DECLARE p_out_IdEvento TABLE ("IdEvento" BIGINT,"IdSecuencia" BIGINT);
        DECLARE v_SegundosZonaHoraria       BIGINT;

        SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';

        v_solicitudes_a_notificar = SELECT 
        							SOL."IdSolicitudAporteRescate" AS "FOLIO"
        							,SOL."IdCliente"
        							,CLI."IdPersona" AS "IDCLIENTEPERSONA"
        							,SOL."IdSerieFondo" AS "NEMO"
        							,CLI."DocumentoIdentidad" AS "RUT"
        							,CLI."NombreCompleto" AS "CLIENTE"
        							,SOL."IdPortafolio" AS "IdPortafolioCliente"
        							,POR."DescripcionPortafolio" AS "CUENTA"
        							,0 AS "IdEmpleadoDigitador"
        							,'-' AS "AGENTE"
        							,0 AS "IDEMPLEADOPERSONA"
        							,CORREO."Direccion" AS "CORREO"
									,TELEFONO."Numero" AS "TELEFONO"
									,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:v_SegundosZonaHoraria), 'DD-MM-YYYY') AS "FECHA"
									,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:v_SegundosZonaHoraria), 'HH24:MI:SS') AS "HORA"
									,CTP."Nombre" AS "PRODUCTO"
									,CTO."Descripcion" AS "TIPO_OPERACION"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SOL."NumeroCuotas",'dx',SERIE."DecimalesCuota") AS "CANTIDAD"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SOL."ValorCuota",'dx',SERIE."DecimalesValorCuota") AS "PRECIO"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SOL."Monto",CASE WHEN SOL."IdMoneda"='CLP' THEN 'i' ELSE 'd' END,CASE WHEN SOL."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "MONTO"
									,SOL."IdMoneda" AS "MONEDA"
									,EST."Nombre" AS "ESTADO"
									,CEMP."Nombre" AS "CORREDORA"
									,CEMP."TelefonoPrincipal" AS "ATENCIONCLIENTE"
									,'T0' AS "CONDICIONLIQUIDACION"
									,'M' AS "TIPOPRECIOORDEN"
									,TO_VARCHAR(SOL."FechaCertificacion",'DD-MM-YYYY') AS "FECHAVIGENCIA"
									,0 "TASA"
									,0 "PLAZOVENCIMIENTO"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(0,CASE WHEN SOL."IdMoneda"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN SOL."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "MONTOPRIMA"
									,'N' AS "VENTACORTA"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SOL."MontoComision",'i',0) AS "COMISION"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(0,'i',0) AS "IMPUESTO"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(0,'i',0) AS "GASTO"
									,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(0,'i',0) AS "DERECHO"
									FROM :p_Solicitudes P_SOL
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON (SOL."IdSolicitudAporteRescate" = P_SOL."IdSolicitudAporteRescate")
									INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = SOL."IdPortafolio"
									INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = SOL."IdCliente"
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON (SOL."IdSerieFondo" = SERIE."IdSerieFondo")
									LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CORREO ON (CORREO."IdDireccionElectronica" = CLI."IdDireccionElectronicaPrincipal")
									LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELEFONO ON (TELEFONO."IdTelefono" = CLI."IdTelefonoPrincipal")
									INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" EST ON EST."IdEstado" = SOL."Estado"
									INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" CTP ON CTP."IdTipoProducto" = 'INFM'
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" CTO ON CTO."IdTipoMovimientoFondoMutuo" = SOL."IdTipoMovimientoFondoMutuo"
									INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CEMP ON CEMP."IdEmpresa" = p_IdEmpresa;
        
									
		p_Eventos = SELECT 
		'CERTIFICACION_FFMM' AS "CodigoTipoEvento", 
		OAN."FOLIO" AS "IdSecuencia", 
		CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
		OAN."IDEMPLEADOPERSONA" AS "IdPersonaRemitente", 
		OAN."IDCLIENTEPERSONA" AS "IdPersonaDestinatario",
		OAN."IdPortafolioCliente" AS "IdPortafolio"
		FROM :v_solicitudes_a_notificar OAN;
		
		p_IdDirecciones = 
		SELECT 
		E."IdSecuencia",
		CASE 	
			WHEN COALESCE(CIPO."IdDireccionNotificacion", '')='' OR CIPO."IdDireccionNotificacion"='N' OR CIPO."IdDireccionNotificacion"='false' THEN CASE CIP."ValorIndicador" WHEN '' THEN 0 ELSE CIP."ValorIndicador" END 
			ELSE CIPO."IdDireccionNotificacion" END AS "IdDireccion",
		'TO' AS "TipoDestinatario"
		FROM :p_Eventos E
		INNER JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" TE ON TE."CodigoTipoEvento" = E."CodigoTipoEvento"
		INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" CED ON CED."CodigoTipoEvento" = TE."CodigoTipoEvento"
		INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CED."IdConfiguracionEvento" =   CE."IdConfiguracionEvento" AND CE."FormaDespacho" IN('T','C')
		LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" CIPO ON CIPO."IdPortafolio" = E."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" CIP ON CIP."IdPortafolio" = E."IdPortafolio" AND CIP."IdUso" = 'PRE' AND CIP."GrupoIndicador" = 'GRUPONOTIFICACIONES' AND CIP."CodigoTipoIndicador" = 'NOT_EMAIL';
		
		
		p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
					
		p_EventoDetalle = SELECT "FOLIO" AS "IdSecuencia", "Llave", "Valor"
		FROM (
			SELECT * ,

			COALESCE(
			"1" ||"FOLIO", "2" ||"IdCliente", "3" ||"IDCLIENTEPERSONA", "4" ||"NEMO", "5" ||"RUT", "6" ||"CLIENTE",          
			"7" ||"IdPortafolioCliente", "8" ||"CUENTA", "9" ||"IdEmpleadoDigitador", "10" ||"AGENTE", 
			"11" ||"IDEMPLEADOPERSONA", "12" ||"CORREO", "13" ||"TELEFONO", "14" ||"FECHA", "15" ||"HORA", 
			"16" ||"PRODUCTO", "17" ||"TIPO_OPERACION", "18" ||"CANTIDAD", "19" ||"PRECIO", "20" ||"MONEDA", "21" ||"ESTADO", 
			"22" ||"CORREDORA", "23" ||"ATENCIONCLIENTE", "24" || "MONTO", "25" || "CONDICIONLIQUIDACION", "26" || "TIPOPRECIOORDEN",
			"27" || "FECHAVIGENCIA", "28" || "TASA", "29" || "PLAZOVENCIMIENTO", "30" || "MONTOPRIMA", "31" || "VENTACORTA",
			"32" || "COMISION", "33" || "DERECHO", "34" || "GASTO", "35" || "IMPUESTO"
			) AS "Valor",
			        
			COALESCE(
			"1" ||'FOLIO', "2" ||'IdCliente', "3" ||'IDCLIENTEPERSONA', "4" ||'NEMO', "5" ||'RUT', "6" ||'CLIENTE', 
			"7" ||'IdPortafolioCliente', "8" ||'CUENTA', "9" ||'IdEmpleadoDigitador', "10" ||'AGENTE', 
			"11" ||'IDEMPLEADOPERSONA', "12" ||'CORREO', "13" ||'TELEFONO', "14" ||'FECHA', "15" ||'HORA', 
			"16" ||'PRODUCTO', "17" ||'TIPO_OPERACION', "18" ||'CANTIDAD', "19" ||'PRECIO', "20" ||'MONEDA', 
			"21" ||'ESTADO', "22" ||'CORREDORA', "23" ||'ATENCIONCLIENTE', "24" || 'MONTO', "25" || 'CONDICIONLIQUIDACION',
			"26" || 'TIPOPRECIOORDEN', "27" || 'FECHAVIGENCIA', "28" || 'TASA', "29" || 'PLAZOVENCIMIENTO', "30" || 'MONTOPRIMA',
			"31" || 'VENTACORTA', "32" || 'COMISION', "33" || 'DERECHO', "34" || 'GASTO', "35" || 'IMPUESTO'
			        
			) AS "Llave" 
			FROM :v_solicitudes_a_notificar,"BSCL.Plataforma.BD::PLA.Identidad"
		)
		WHERE "Valor" IS NOT NULL;
		
		--Guardar eventos

		CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
 			p_IdEmpresa => p_IdEmpresa,
 			p_IdUsuario => p_IdUsuario,
 			p_IdModulo => p_IdModulo,
 			p_Eventos => :p_Eventos,
 			p_Valores => :p_EventoDetalle,
 			p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
 			p_IdDirecciones => :p_IdDirecciones,
 			p_out_IdEvento => p_out_IdEvento
 		);
   END;
END;