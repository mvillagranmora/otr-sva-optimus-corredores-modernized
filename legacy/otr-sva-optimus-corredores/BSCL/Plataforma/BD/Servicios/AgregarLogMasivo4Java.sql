SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java";

CREATE PROCEDURE
-- 
--  Proposito: SP para agregar log utilizando tablas globales temporales.
--  Autor: JG
--  Fecha Creacion: 08/03/2017

"BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java" (
	IN p_IdTrabajo BIGINT
	-- IN "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log"
	-- IN "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle"
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" AS
BEGIN 
	BEGIN -- LOGIC

		DECLARE v_contDetalle BIGINT;
		DECLARE v_FechaRegistro SECONDDATE;

		SELECT (CAST(CURRENT_UTCTIMESTAMP AS SECONDDATE)) into v_FechaRegistro from dummy;
		v_tablaTempLog = select
			   "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL as "IdLog"
			   ,"IdCabeceraLog"
			   ,"IdTipoLog" 
			   ,"IdModulo"
			   ,"IdUsuario"	
			   ,"IdEmpresa"
			   ,"FechaSistemaLog" 
			   ,"IdUsuarioAtendido"	
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
			   ,"IdPersona"
			  from "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log" where "IdTrabajo" = p_IdTrabajo; 
		
		v_tablaTempLogDetalle = select
			   "BSCL.Plataforma.BD.Secuencias::DetalleLog".NEXTVAL AS "IdDetalleLog"
			   ,L."IdLog" AS "IdLog"
			   ,LD."SubRegistro" AS "SubRegistro"
			   ,LD."CampoRegistro" AS "CampoRegistro"
			   ,LD."ValorAntiguo" AS "ValorAntiguo"
			   ,LD."ValorNuevo" AS "ValorNuevo"
			   FROM "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle" LD 
			   INNER JOIN :v_tablaTempLog L ON LD."IdCabeceraLog" = L."IdCabeceraLog" AND LD."IdTrabajo" = p_IdTrabajo;
		
	---Generar clave de autenticacion
		v_CodigoAutLog = SELECT "IdCabeceraLog", cast(HASH_SHA256(TO_BINARY(
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
			)))))))))))))))))))))))))))))))))AS VARCHAR) AS "CodigoLog" 
		FROM :v_tablaTempLog;
	
		SELECT COUNT(*) INTO v_contDetalle from :v_tablaTempLogDetalle;
	
	IF v_contDetalle > 0 THEN
		v_CodigoAutDetalle = SELECT "IdDetalleLog",cast(HASH_SHA256(TO_BINARY(
			CONCAT("IdDetalleLog"
			,CONCAT("IdLog"
			,CONCAT(COALESCE("SubRegistro",'')
			,CONCAT(COALESCE("ValorAntiguo",'')
			,CONCAT(COALESCE("ValorNuevo",''),COALESCE("CampoRegistro",''))))))
	)) AS VARCHAR) AS "CodigoDetalle"
	FROM :v_tablaTempLogDetalle;	
	END IF;
	-- Fin obtener clave de autenticacion
		
			 insert into "BSCL.Plataforma.BD::PLA.Log" (
			   "IdLog"
			   ,"IdTipoLog"
			   ,"IdModulo"
			   ,"IdUsuario"
			   ,"IdEmpresa"
			   ,"FechaLog"
			   ,"PeriodoLog"
			   ,"IdUsuarioAtendido"
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
			   ,"IdPersona"
			   ,"CodigoAutenticacion"
			   )
			 select
			   "IdLog"
			   ,"IdTipoLog" 
			   ,"IdModulo"
			   ,"IdUsuario"	
			   ,"IdEmpresa"
			   ,"FechaSistemaLog"
			   ,SUBSTRING("FechaSistemaLog", 0, 7)
			   ,"IdUsuarioAtendido"
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
			   ,:v_FechaRegistro AS "FechaRegistro"
			   ,"IdPersona"
			   ,codlog."CodigoLog"
			   from :v_tablaTempLog AS tablaTemp
			  INNER JOIN :v_CodigoAutLog AS codlog ON codlog."IdCabeceraLog"= tablatemp."IdCabeceraLog";
			 
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
			   det."IdDetalleLog"
			   ,"IdLog"
			   ,"SubRegistro"
			   ,"CampoRegistro"
			   ,"ValorAntiguo"
			   ,"ValorNuevo"
			   ,coddet."CodigoDetalle"
			   FROM :v_tablaTempLogDetalle AS det
			   INNER JOIN :v_CodigoAutDetalle AS coddet ON coddet."IdDetalleLog"= det."IdDetalleLog";
	END; -- LOGIC	
END;