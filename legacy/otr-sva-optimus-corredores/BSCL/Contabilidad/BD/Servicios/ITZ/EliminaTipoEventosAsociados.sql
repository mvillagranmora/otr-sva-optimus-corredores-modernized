SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.EliminaTipoEventosAsociados";
CREATE PROCEDURE 
-- 
--  Proposito: Eliminar 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 21/09/2016
--  Id Servicio = 12039
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.EliminaTipoEventosAsociados" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	-- Parametros propos del Proceso
	IN p_IdTipoEvento				NVARCHAR(40), 
	IN p_IdDefinicionAsiento		BIGINT,
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso		TABLE ("Campo" NVARCHAR(50),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	
	OUT p_out_InformacionContador	INT,
	OUT p_out_InformacionDetalle	TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,  
	OUT p_out_ErroresContador		INT,   
	OUT p_out_ErroresDetalle		TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
	-- FIN Sección Fija
)
  LANGUAGE SQLSCRIPT
  SQL SECURITY INVOKER
  DEFAULT SCHEMA "BSCL"
  --READS SQL DATA
AS
BEGIN
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY(:p_out_ResultadoProceso)) THEN
			
			v_Mensajes = SELECT 1 as "IdInterno",'EVENTO_ASOCIADO_ELIMINADO' as "Codigo",'Evento' as "CodigoDato1",:p_IdTipoEvento as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
						   FROM :p_out_ResultadoProceso;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			p_out_InformacionDetalle =
			SELECT :p_IdDefinicionAsiento as "IdDefinicionAsiento","Codigo","Descripcion" FROM :v_erroroutmsg;
			
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM :p_out_InformacionDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			
			p_out_ErroresDetalle = 
			SELECT :p_IdDefinicionAsiento as "IdDefinicionAsiento",v_errorExt as "Codigo",::SQL_ERROR_MESSAGE as "Descripcion" FROM DUMMY;

			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;

	p_out_InformacionContador 	:= 0;
	p_out_AdvertenciasContador 	:= 0;
	p_out_ErroresContador		:= 0;

	BEGIN
	
		DELETE FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento"
		WHERE "IdTipoEvento"		= :p_IdTipoEvento
		AND "IdDefinicionAsiento"	= :p_IdDefinicionAsiento;
	
		p_out_ResultadoProceso = 
		SELECT TRIM(CAST(:p_IdTipoEvento AS NVARCHAR)) || ';' || TRIM(CAST(:p_IdDefinicionAsiento as NVARCHAR)) AS "Campo",
				1 																								AS "Id",
				'BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento' 									AS "Servicio",
				'ELIMINADO' 																					AS "Resultado"
		FROM DUMMY;
		
		SIGNAL SALIDA_CONTROLADA;
	END;
END;