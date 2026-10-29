SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::GrabarItemsManualesPosicionMRF";

--
--  Proposito: Servicio para grabar el monto de los Items Manuales de Posicion MRF
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 19/12/2016
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::GrabarItemsManualesPosicionMRF" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_Fecha				DATE,
	IN p_ItemsManuales		TABLE(	"IdPosicion"	BIGINT,
									"IdMetodo"		BIGINT,
									"IdEvento"		BIGINT,
									"MontoEvento"	DECIMAL(26,8)),
	
	-- Parametros salida
	OUT p_out_RefId			BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
	    
		DECLARE v_FechaSistema	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		p_out_RefId := 0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
		
		
		--------------------
		--Validaciones
		--------------------
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		BEGIN
			-- Nuevos Items Manuales
			INSERT INTO "BSCL.Mesa.BD::MES.PosicionMRF"(
				"IdPosicion",
				"Fecha",
				"IdMatriz",
				"MontoEvento",
				"IdUsuario",
				"FechaUltimaModificacion"
				)
			SELECT
				"BSCL.Mesa.BD.Secuencias::MRF.IdPosicionMRF".NEXTVAL,
				p_Fecha,
				MEP."IdMatriz",
				ITM."MontoEvento",
				:p_IdUsuario,
				:v_FechaSistema
			FROM "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MEP
			INNER JOIN :p_ItemsManuales ITM
				ON (ITM."IdMetodo" = MEP."IdMetodo" AND ITM."IdEvento" = MEP."IdEvento")
			WHERE ITM."IdPosicion" = 0;
			
			
			-- Modificar valor de MontoEvento de Items Manuales
			UPDATE POS
			SET
				POS."MontoEvento" = ITM."MontoEvento",
				POS."IdUsuario" = :p_IdUsuario,
				POS."FechaUltimaModificacion" = :v_FechaSistema
			FROM "BSCL.Mesa.BD::MES.PosicionMRF" POS
			INNER JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MEP
				ON POS."IdMatriz" = MEP."IdMatriz"
			INNER JOIN :p_ItemsManuales ITM
				ON (POS."IdPosicion" = ITM."IdPosicion" AND POS."IdMatriz" = MEP."IdMatriz" AND POS."Fecha" = p_Fecha);
			
			p_out_RefId := 1;
			
			
			-- LOG DE AUDITORIA : COMPLETAR SI ES NECESARIO USAR LOG
			/*
			p_DetalleLog =
				SELECT
					'' AS "SubRegistro",
					'' AS "CampoRegistro",
					"ValorNuevo" AS "ValorAntiguo",
					ITM."MontoEvento" AS "ValorNuevo"
				FROM :p_ItemsManuales ITM;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => 'MODIFICACION'
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'ITEMSMANUALES'
				,p_NombreProceso 	 => 'MODIFICACION ITEMS MANUALES'
				,p_DetalleLog 		 => :p_DetalleLog
			);
			*/
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
