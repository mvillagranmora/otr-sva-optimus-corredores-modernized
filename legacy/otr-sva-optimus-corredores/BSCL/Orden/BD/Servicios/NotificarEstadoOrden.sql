SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::NotificarEstadoOrden";
GO
CREATE PROCEDURE
-- 
--  Proposito: Notifica al cliente el estado de su orden.
--  Autor: CHERNANDEZ
--  Fecha Creacion: 01/12/2016
--  
"BSCL.Orden.BD.Servicios::NotificarEstadoOrden" ( 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
	
	IN p_Ordenes						  table ("IdOrden" BIGINT)
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
        
        
        DECLARE   v_UrlLogoPublico   NVARCHAR(250);
        
        
        SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
        
        
        SELECT "Valor" INTO v_UrlLogoPublico  FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa  AND "CodigoParametro" = 'PLA.UrlLogoCorredoraPublic';
        
		--------------------------------------------------------------------------------------------------------------------
		--Notificar cambio de estado ordenes
		--------------------------------------------------------------------------------------------------------------------
		--Obtener los cobros de la orden
		p_Ordenes_cobros = SELECT
			"IdOrden",
			MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "Valor" END) AS "COMISION",
			MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "Valor" END) AS "IMPUESTO",
			MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "Valor" END) AS "GASTO",
			MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "Valor" END) AS "DERECHO"									
	  	FROM (
	 		SELECT co."IdOrden", co."CodigoCobroClasificacion", SUM(co."MontoCobroCalculado") AS "Valor" 
     		FROM :p_Ordenes po
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden" co ON co."IdOrden" = po."IdOrden"
			GROUP BY co."IdOrden", "CodigoCobroClasificacion"
       	) GROUP BY "IdOrden";
       	
       	--Obtener la info para notificar la orden
		v_ordenes_a_notificar = SELECT
			O."IdOrden" AS "FOLIO", 
			O."IdCliente", 
			CC."IdPersona" AS "IDCLIENTEPERSONA",
			O."Nemotecnico" AS "NEMO",		
			CC."DocumentoIdentidad" AS "RUT",
			CC."NombreCompleto" AS "CLIENTE", 
			O."IdPortafolioCliente", 
			CP."DescripcionPortafolio" AS "CUENTA", 
			O."IdEmpleadoDigitador", 
			CE."NombreCompleto" AS "AGENTE",
			CE."IdPersona" AS "IDEMPLEADOPERSONA",
			COALESCE(CPDE."Direccion",'') AS "CORREO",
			COALESCE(CPT."Numero",'') AS "TELEFONO",
			TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:v_SegundosZonaHoraria), 'DD-MM-YYYY') AS "FECHA",
			TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:v_SegundosZonaHoraria), 'HH24:MI:SS') AS "HORA",
			CTP."Nombre" AS "PRODUCTO",
			CTO."Nombre" AS "TIPO_OPERACION",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."Cantidad",'i',0) AS "CANTIDAD",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."PrecioOrden",CASE WHEN O."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN O."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END ) AS "PRECIO",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."MontoOrden",CASE WHEN O."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END,CASE WHEN O."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END ) AS "MONTO",
			O."IdMonedaOrden" AS "MONEDA",
			CES."Nombre" AS "ESTADO",
			CEMP."Nombre" AS "CORREDORA",
			CEMP."TelefonoPrincipal" AS "ATENCIONCLIENTE",
			CDL."Nombre" AS "CONDICIONLIQUIDACION",
			TPO."Nombre" AS "TIPOPRECIOORDEN",
			TO_VARCHAR(O."FechaVigencia",'DD-MM-YYYY') AS "FECHAVIGENCIA",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
				CASE 
					WHEN ins."IdTipoInstrumento" = 'SIM' THEN O."TasaSimultanea" 
					WHEN O."EsVentaCorta" = 'S' THEN O."TasaVentaCorta"
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN O."TasaRFIF"
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN O."TasaRFIF"
					ELSE 0 END
					,'d',3) AS "TASA",
			CASE 
				WHEN ins."IdTipoInstrumento" = 'SIM' THEN O."PlazoSimultanea"
				WHEN O."EsVentaCorta" = 'S' THEN O."PlazoVentaCorta"
				WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN O."PlazoIF"
				ELSE 0
				END AS "PLAZOVENCIMIENTO",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."MontoPrimaVentaCorta",CASE WHEN O."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN O."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END ) AS "MONTOPRIMA",
			O."EsVentaCorta" AS "VENTACORTA",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(RES."COMISION",'i',0) AS "COMISION",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(RES."IMPUESTO",'i',0) AS "IMPUESTO",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(RES."GASTO",'i',0) AS "GASTO",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(RES."DERECHO",'i',0) AS "DERECHO"
		FROM "BSCL.Orden.BD::ORD.Orden" O
		INNER JOIN :p_Ordenes_cobros RES ON RES."IdOrden" = O."IdOrden" AND O."IdEmpresa" = p_IdEmpresa
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = O."IdInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdPortafolio" = O."IdPortafolioCliente"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = CP."IdCliente"
		

		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CP."IdEmpleadoEjecutivo"
		
		INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL ON CDL."IdCondicionDeLiquidacion" = O."IdCondicionDeLiquidacion"
		INNER JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" TPO ON TPO."IdTipoPrecioOrden" = O."IdTipoPrecio"
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CE."IdDireccionElectronicaPrincipal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona" = CE."IdPersona" AND CPT."CodigoProposito" = 'COME'
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" CTP ON CTP."IdTipoProducto" = O."IdProducto"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" CTO ON CTO."IdTipoOperacion" = O."IdTipoOperacion"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" CES ON CES."IdEstado" = O."IdEstadoOrden"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CEMP ON CEMP."IdEmpresa" = p_IdEmpresa;
		
		p_Eventos = SELECT 
		'CAMBIO_ESTADO_ORDEN' AS "CodigoTipoEvento", 
		OAN."FOLIO" AS "IdSecuencia", 
		CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
		OAN."IDEMPLEADOPERSONA" AS "IdPersonaRemitente", 
		OAN."IDCLIENTEPERSONA" AS "IdPersonaDestinatario",
		OAN."IdPortafolioCliente" AS "IdPortafolio"
		FROM :v_ordenes_a_notificar OAN;
				
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
			"32" || "COMISION", "33" || "DERECHO", "34" || "GASTO", "35" || "IMPUESTO", "36" || :v_UrlLogoPublico
			) AS "Valor",
			        
			COALESCE(
			"1" ||'FOLIO', "2" ||'IdCliente', "3" ||'IDCLIENTEPERSONA', "4" ||'NEMO', "5" ||'DOCUMENTOIDENTIDAD', "6" ||'CLIENTE', 
			"7" ||'IDPORTAFOLIOCLIENTE', "8" ||'CUENTA', "9" ||'IEMPLEADODIGITADOR', "10" ||'AGENTE', 
			"11" ||'IDEMPLEADOPERSONA', "12" ||'CORREO', "13" ||'TELEFONO', "14" ||'FECHA', "15" ||'HORA', 
			"16" ||'PRODUCTO', "17" ||'TIPO_OPERACION', "18" ||'CANTIDAD', "19" ||'PRECIO', "20" ||'MONEDA', 
			"21" ||'ESTADO', "22" ||'CORREDORA', "23" ||'ATENCIONCLIENTE', "24" || 'MONTO', "25" || 'CONDICIONLIQUIDACION',
			"26" || 'TIPOPRECIOORDEN', "27" || 'FECHAVIGENCIA', "28" || 'TASA', "29" || 'PLAZOVENCIMIENTO', "30" || 'MONTOPRIMA',
			"31" || 'VENTACORTA', "32" || 'COMISION', "33" || 'DERECHO', "34" || 'GASTO', "35" || 'IMPUESTO', "36" || 'LOGO_PUBLICO'
			        
			) AS "Llave" 
			FROM :v_ordenes_a_notificar,"BSCL.Plataforma.BD::PLA.Identidad"
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

