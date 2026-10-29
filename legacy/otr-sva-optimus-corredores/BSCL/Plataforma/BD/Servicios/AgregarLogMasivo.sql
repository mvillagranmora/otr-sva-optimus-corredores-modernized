SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo"  (
-- 
--  Proposito: Agregar un log de auditoria/error/informativo al sistema
--  Autor: - BCS
--  Fecha Creacion: 22/01/2016


	IN p_Log TABLE (
				"IdCabeceraLog"				BIGINT,
				"IdTipoLog" 				NVARCHAR(40),
				"IdModulo"					NVARCHAR(3),
				"IdUsuario"					BIGINT,
				"IdEmpresa"					BIGINT,
				"FechaSistemaLog"			NVARCHAR(10),
				"IdUsuarioAtendido"			BIGINT,
				"IdPersona"					BIGINT,
				"IdCliente"					BIGINT,
				"EstadoCliente"				NVARCHAR(3),
				"NombreCliente"				NVARCHAR(100), 
				"IdTipoDocumentoIdentidad"	NVARCHAR(40), 
				"IdDocumentoIdentidad"		NVARCHAR(40), 
				"IdPortafolio"				BIGINT,
				"EstadoPortafolio"			NVARCHAR(3),
				"IdTipoRegistro"			NVARCHAR(40), 
				"IdRegistro"				NVARCHAR(40),
				"EstadoRegistro"			NVARCHAR(3),
				"IdTipoOperacion"			NVARCHAR(40), 
				"Folio"						BIGINT,
				"Cantidad"					DECIMAL(26,8),
				"PrecioTasa"				DECIMAL(26,8),
				"Monto"						DECIMAL(26,8),
				"IdMonedaOrigen"			NVARCHAR(3),
				"IdMonedaLiquidacion"		NVARCHAR(3),
				"TipoCambio"				NVARCHAR(40),
				"Nemo"						NVARCHAR(260),
				"IdProceso"					BIGINT,
				"EstadoProceso"				NVARCHAR(3),
				"NombreProceso"				NVARCHAR(100)
				),
	IN p_LogDetalle TABLE (
				"IdCabeceraLog" 			BIGINT,
				"SubRegistro" 				NVARCHAR(40), 
				"CampoRegistro" 			NVARCHAR(500), 
				"ValorAntiguo"				NVARCHAR(250), 
				"ValorNuevo" 				NVARCHAR(250)
	) DEFAULT EMPTY
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	--------------------------------------------------------- CONTROL DE ERRORES
	--- Excepcion
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL; 
	END;
	--------------------------------------------------------- PREPARAR DATOS 	
	BEGIN
	--------------------------------------------------------- Guardar LOG 
	DECLARE v_contDetalle BIGINT;
	DECLARE v_FechaRegistro SECONDDATE;
	SELECT (CAST(CURRENT_UTCTIMESTAMP AS SECONDDATE)) into v_FechaRegistro from dummy;

	SELECT COUNT(*) INTO v_contDetalle from :p_LogDetalle;
		
	v_tablaTempLog = select
	   "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL as "IdLog"
	   ,"IdCabeceraLog"
	   ,"IdTipoLog" 
	   ,"IdModulo"
	   ,"IdUsuario"	
	   ,"IdEmpresa"
	   ,"FechaSistemaLog" 
	   ,"IdUsuarioAtendido"	
	   ,"IdPersona"
	   ,"IdCliente"			   
	   ,"EstadoCliente"	
	   ,"NombreCliente"	 
	   ,"IdTipoDocumentoIdentidad"  
	   ,"IdDocumentoIdentidad"  
	   ,"IdPortafolio" 
	   ,"EstadoPortafolio" 
	   ,"IdRegistro" 
	   ,"EstadoRegistro" 
	   ,"IdTipoRegistro" 
	   ,"IdTipoOperacion"  
	   ,"Folio" 
	   ,"Cantidad" 
	   ,"PrecioTasa" 
	   ,"Monto" 
	   ,"IdMonedaOrigen" 
	   ,"IdMonedaLiquidacion"
	   ,"TipoCambio"
	   ,"Nemo" 
	   ,"IdProceso" 
	   ,"EstadoProceso" 
	   ,"NombreProceso"
	  from :p_Log; 
		 
	v_tablaTempDetLog = select
	   "BSCL.Plataforma.BD.Secuencias::DetalleLog".NEXTVAL as "IdDetLog"
	   ,"IdCabeceraLog"
	   ,"SubRegistro"
	   ,"CampoRegistro"
	   ,"ValorAntiguo",
	   "ValorNuevo"
	  from :p_LogDetalle;
			 
	---Generar codigo de autenticacion 
	v_CodigoAutLog = SELECT "IdCabeceraLog" ,"IdLog", cast(HASH_SHA256(TO_BINARY(
		 CONCAT("IdLog"
		,CONCAT("IdTipoLog"
		,CONCAT("IdModulo"
		,CONCAT("IdUsuario"
		,CONCAT("IdEmpresa"
		,CONCAT(COALESCE("FechaSistemaLog",'')
		,CONCAT(COALESCE(SUBSTRING("FechaSistemaLog", 0, 7),'')
		,CONCAT(COALESCE("IdUsuarioAtendido",0)
		,CONCAT(COALESCE("IdCliente",0)
		,CONCAT(COALESCE("EstadoCliente",'')
		,CONCAT(COALESCE("NombreCliente",'')
		,CONCAT(COALESCE("IdTipoDocumentoIdentidad",'')
		,CONCAT(COALESCE("IdDocumentoIdentidad",'')
		,CONCAT(COALESCE("IdPortafolio",0)
		,CONCAT(COALESCE("EstadoPortafolio",'')
		,CONCAT(COALESCE("IdRegistro",'')
		,CONCAT(COALESCE("EstadoRegistro",'')
		,CONCAT(COALESCE("IdTipoRegistro",'')
		,CONCAT(COALESCE("IdTipoOperacion",'')
		,CONCAT(COALESCE("Folio",0)
		,CONCAT(COALESCE("Cantidad",0)
		,CONCAT(COALESCE("PrecioTasa",0)
		,CONCAT(COALESCE("Monto",0)
		,CONCAT(COALESCE("IdProceso",0)
		,CONCAT(COALESCE("EstadoProceso",'')
		,CONCAT(COALESCE("NombreProceso",'')
		,CONCAT(COALESCE("IdMonedaOrigen",'')
		,CONCAT(COALESCE("IdMonedaLiquidacion",'')
		,CONCAT(COALESCE("TipoCambio",'')
		,CONCAT(:v_FechaRegistro
		,CONCAT(COALESCE("Nemo",''),COALESCE("IdPersona",0)
		)))))))))))))))))))))))))))))))
	))AS VARCHAR) AS "CodigoLog"
	FROM :v_tablaTempLog;
	
	
	IF v_contDetalle > 0 THEN
		v_CodigoAutDetalle = SELECT det."IdDetLog", det."IdCabeceraLog", cast(HASH_SHA256(TO_BINARY(
			 CONCAT(det."IdDetLog"
			,CONCAT(cab."IdLog"
			,CONCAT(COALESCE("SubRegistro",'')
			,CONCAT(COALESCE("ValorAntiguo",'')
			,CONCAT(COALESCE("ValorNuevo",''),COALESCE("CampoRegistro",'')
			))))))) AS VARCHAR) AS "CodigoDetalle" 
		FROM :v_tablaTempDetLog AS det
		INNER JOIN :v_tablaTempLog AS cab ON cab."IdCabeceraLog" = det."IdCabeceraLog";
	END IF;
	-- Fin obtener codigo de autenticacion	
	
			 
	insert into "BSCL.Plataforma.BD::PLA.Log" (
		   "IdLog"
		   ,"IdTipoLog"
		   ,"IdModulo"
		   ,"IdUsuario"
		   ,"IdEmpresa"
		   ,"FechaLog"
		   ,"PeriodoLog"
		   ,"IdUsuarioAtendido"
		   ,"IdPersona"
		   ,"IdCliente"
		   ,"EstadoCliente"
		   ,"NombreCliente"
		   ,"IdTipoDocumentoIdentidad"
		   ,"IdDocumentoIdentidad"
		   ,"IdPortafolio"
		   ,"EstadoPortafolio"
		   ,"IdRegistro"
		   ,"EstadoRegistro"
		   ,"IdTipoRegistro"
		   ,"IdTipoOperacion"
		   ,"Folio"
		   ,"Cantidad"
		   ,"PrecioTasa"
		   ,"Monto"
		   ,"IdMonedaOrigen"
		   ,"IdMonedaLiquidacion"
		   ,"TipoCambio"
		   ,"Nemo"
		   ,"IdProceso"
		   ,"EstadoProceso"
		   ,"NombreProceso"
		   ,"FechaRegistro"
		   ,"CodigoAutenticacion"
		   )
		 select
			tablatemp."IdLog"
		   ,"IdTipoLog" 
		   ,"IdModulo"
		   ,"IdUsuario"	
		   ,"IdEmpresa"
		   ,"FechaSistemaLog"
		   ,SUBSTRING("FechaSistemaLog", 0, 7)
		   ,"IdUsuarioAtendido"	
		   ,"IdPersona"
		   ,"IdCliente"	
		   ,"EstadoCliente"	
		   ,"NombreCliente"	 
		   ,"IdTipoDocumentoIdentidad"  
		   ,"IdDocumentoIdentidad"  
		   ,"IdPortafolio" 
		   ,"EstadoPortafolio" 
		   ,"IdRegistro" 
		   ,"EstadoRegistro" 
		   ,"IdTipoRegistro" 
		   ,"IdTipoOperacion"  
		   ,"Folio" 
		   ,"Cantidad" 
		   ,"PrecioTasa" 
		   ,"Monto" 
		   ,"IdMonedaOrigen" 
		   ,"IdMonedaLiquidacion"
		   ,"TipoCambio"
		   ,"Nemo" 
		   ,"IdProceso" 
		   ,"EstadoProceso" 
		   ,"NombreProceso"
		   ,:v_FechaRegistro as "FechaRegistro"
		   ,codlog."CodigoLog"
		  from :v_tablaTempLog AS tablatemp
		 INNER JOIN :v_CodigoAutLog AS codlog ON codlog."IdCabeceraLog"=tablatemp."IdCabeceraLog" AND codlog."IdLog"=tablatemp."IdLog"; 
			
			
		insert into "BSCL.Plataforma.BD::PLA.DetalleLog"(
		    "IdDetalleLog"
		   ,"IdLog"
		   ,"SubRegistro"
		   ,"CampoRegistro"
		   ,"ValorAntiguo"
		   ,"ValorNuevo"
		   ,"CodigoAutenticacion"
		   )
		 select 
		   "LogDetalle"."IdDetLog"
		   ,"Log"."IdLog"
		   ,"LogDetalle"."SubRegistro"
		   ,"LogDetalle"."CampoRegistro"
		   ,"LogDetalle"."ValorAntiguo"
		   ,"LogDetalle"."ValorNuevo"
		   ,"Coddet"."CodigoDetalle"
			FROM :v_tablaTempDetLog "LogDetalle" 
			INNER JOIN :v_tablaTempLog "Log" ON "LogDetalle"."IdCabeceraLog" = "Log"."IdCabeceraLog"
			INNER JOIN :v_CodigoAutDetalle "Coddet" ON "Coddet"."IdDetLog" = "LogDetalle"."IdDetLog";
	END;	
END;