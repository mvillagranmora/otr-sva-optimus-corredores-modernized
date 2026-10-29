SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::FAC.NotificarResumenFacturasCliente";GO
-- 
--  Proposito: Servicio para la notificacion del resumen de facturas por cliente.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 22/11/2016
--  
GO
CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::FAC.NotificarResumenFacturasCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	--Parametros de salida
	OUT p_out_Resultado				BIGINT,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		
		DECLARE p_Eventos TABLE("CodigoTipoEvento" nvarchar(40), "IdSecuencia" BIGINT , "FechaCreacionSistema" SECONDDATE, "IdPersonaRemitente" BIGINT, "IdPersonaDestinatario" BIGINT, "IdPortafolio" BIGINT);
		DECLARE p_EventoDetalle TABLE("IdSecuencia"  BIGINT,"Llave"	 NVARCHAR(40),"Valor" NVARCHAR(5000));
		DECLARE p_ArchivosAdjuntos TABLE("IdSecuencia"  BIGINT,"ArchivoAdjunto"	 BLOB,"Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520));
		DECLARE p_IdDirecciones TABLE ("IdSecuencia" BIGINT, "IdDireccion" BIGINT,"TipoDestinatario" NVARCHAR(6));
		DECLARE p_out_IdEvento TABLE ("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		
		-----------------------------------------------------
		--Logica de negocio
		-----------------------------------------------------	
		v_resumen_con_secuencia = select 
		"BSCL.Plataforma.BD.Secuencias::EventoNotificacion".NEXTVAL AS "IdSecuencia", 
		NFT."IdEmpresa",
		NFT."IdCliente",
		NFT."IdPortafolio",
		NFT."IdPersonaRemitente",
		NFT."IdPersonaDestinatario",
		NFT."CLIENTE",
		NFT."RUT",
		NFT."CUENTA",
		NFT."AGENTE",
		NFT."TELEFONO",
		NFT."CORREO",
		NFT."FECHA",
		NFT."HORA",
		NFT."CORREDORA",
		TO_NVARCHAR(NFT."TABLA") AS "TABLA",
		NFT."ATENCIONCLIENTE",
		TO_NVARCHAR(NFT."TABLACOMPRA") AS "TABLACOMPRA",
		TO_NVARCHAR(NFT."TABLAVENTA") AS "TABLAVENTA",
		LC."RutaPublica" AS "LOGOCORREDORA"
		from "BSCL.Facturacion.BD::FAC.NotificacionResumenFacturacionTemporal" AS NFT
		INNER JOIN   "BSCL.Plataforma.BD::PLA.Imagenes" AS LC  ON  "NombreImagen"='LOGO_CORREDOR' AND (LC."IdEmpresa"=:p_IdEmpresa)
		WHERE NFT."IdEmpresa" = p_IdEmpresa;
		
			
		p_Eventos = SELECT 
		'RESUMEN_FACTURACION_CLIENTE' AS "CodigoTipoEvento", 
		RCS."IdSecuencia", 
		CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
		RCS."IdPersonaRemitente" AS "IdPersonaRemitente", 
		RCS."IdPersonaDestinatario" AS "IdPersonaDestinatario",
		RCS."IdPortafolio" AS "IdPortafolio"
		FROM :v_resumen_con_secuencia RCS;
		select * from :p_Eventos;
		
		p_IdDirecciones = SELECT 
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
        LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" CIP ON CIP."IdPortafolio" = E."IdPortafolio" AND CIP."IdUso" = 'PRE' AND CIP."GrupoIndicador" = 'GRUPOFACTURACION' AND CIP."CodigoTipoIndicador" = 'FAC_DIRELE';

		p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
					
		p_EventoDetalle = SELECT "IdSecuencia", "Llave", "Valor"
		FROM (
			SELECT * ,
			        
			COALESCE(
			"1" ||"IdSecuencia", "2" ||"CLIENTE", "3" ||"RUT", "4" ||"CUENTA", "5" ||"AGENTE", "6" ||"TELEFONO",          
			"7" ||"CORREO", "8" ||"FECHA", "9" ||"HORA", "10" ||"CORREDORA", 
			"11" ||"TABLA", "12" ||"ATENCIONCLIENTE", "13" || "TABLACOMPRA", "14" || "TABLAVENTA", "15" || "LOGOCORREDORA"
			) AS "Valor",
			        
			COALESCE(
			"1" ||'IDSECUENCIA', "2" ||'CLIENTE', "3" ||'DOCUMENTOIDENTIDAD', "4" ||'PORTAFOLIO', "5" ||'AGENTE', "6" ||'TELEFONO', 
			"7" ||'CORREO', "8" ||'FECHA', "9" ||'HORA', "10" ||'CORREDORA', 
			"11" ||'TABLA', "12" ||'ATENCIONCLIENTE', "13" || 'TABLACOMPRA', "14" || 'TABLAVENTA', "15" || 'LOGOCORREDORA'
			) AS "Llave" 
			FROM :v_resumen_con_secuencia, "BSCL.Plataforma.BD::PLA.Identidad"
		)
		WHERE "Valor" IS NOT NULL;
		select * from :p_EventoDetalle;

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
 		
 		SELECT * FROM :p_out_IdEvento;
 		SELECT COUNT(*) INTO p_out_Resultado FROM :p_out_IdEvento;

 		--Limpiar los movimientos enviados a notificar por empresa.
 		DELETE FROM "BSCL.Facturacion.BD::FAC.NotificacionResumenFacturacionTemporal" WHERE "IdEmpresa" = :p_IdEmpresa;
	END;
	
END;
GO