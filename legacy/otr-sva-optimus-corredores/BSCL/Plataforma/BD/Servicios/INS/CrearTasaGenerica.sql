SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::INS.CrearTasaGenerica" ;


--  Proposito: Servicio para la modificacion de tasAS genericas
--  Autor: JLS - BCS
--  Fecha Creacion: Abril-2017

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.CrearTasaGenerica" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,

	IN p_IdAccion 						NVARCHAR(3) 	DEFAULT '' 	,	
	IN p_IdPrecioTasaTipoInstrumento	BIGINT			DEFAULT 0,	
	IN p_IdTipoinstrumento		 		NVARCHAR(6) 	DEFAULT '' 	,
	IN p_IdTipoReajuste					NVARCHAR(6) 	DEFAULT '' 	,
	IN p_PlazoDesde						BIGINT 			DEFAULT 0,	
	IN p_PlazoHasta						BIGINT 			DEFAULT 0,		
	IN p_Tasa							DECIMAL(26,8)   DEFAULT 0 ,
	IN p_Precio							DECIMAL(26,8)   DEFAULT 0 ,
	
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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	

		v_TablaCodigosMensajes = 
			SELECT 	"Codigo" AS "CodigoMensaje",
					'' AS "CodigoDato",
					'' AS "ValorDato" 
			FROM 	:p_out_ErroresDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" 
			(:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
			
		p_out_ErroresDetalle = 	
			SELECT 	"Codigo",
					"Descripcion"
			FROM 	:v_out_ErroresDetalleMensajes Msg ;
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
		--* Declare Variables
		--* ------------------------------------
		DECLARE v_FechaSistema	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE	v_Count	INT;
		
		
		--* Validaciones
		--* ------------------------------------
		SELECT	COUNT(1)
		INTO	v_Count
		FROM	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"
		WHERE	p_IdTipoinstrumento = "IdTipoInstrumento"
				AND p_IdTipoReajuste = "IdTipoReajuste"
				AND p_PlazoDesde = "PlazoDesde"
				AND p_PlazoHasta = "PlazoHasta";
		IF v_Count > 0 AND p_IdAccion = 'INS' THEN
			p_out_ErroresDetalle = 
				SELECT	'UI_REGISTRO_YA_EXISTE' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
		END IF;


		SELECT	COUNT(1)
		INTO	v_Count
		FROM	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"
		WHERE	p_IdPrecioTasaTipoInstrumento = "IdPrecioTasaTipoInstrumento";
		IF v_Count = 0 AND ( p_IdAccion = 'UPD' OR p_IdAccion = 'DEL') THEN
			p_out_ErroresDetalle = 
				SELECT	'UI_REGISTRO_NO_EXISTE' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;				
		END IF;

		/*
		IF p_IdAccion IN ('MOD', 'INS') THEN
			p_out_ErroresDetalle = 
				SELECT	'UI_PLAZO_EN_OTRO_REGISTRO' AS "Codigo", '' AS "Descripcion"
				FROM	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"
				WHERE	p_IdTipoinstrumento = "IdTipoInstrumento"
						AND p_IdTipoReajuste = "IdTipoReajuste"
						AND "PlazoDesde" <= p_PlazoDesde
						AND "PlazoHasta" >= p_PlazoHasta;
				SIGNAL SALIDA_CONTROLADA;
		END IF;
		*/
		
	
		--* Negocio
		--* ------------------------------------
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"  ( :p_IdEmpresa, v_FechaSistema );		
		

		IF p_IdAccion = 'INS' THEN
			INSERT INTO "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"
			(	"IdPrecioTasaTipoInstrumento", 
				"IdTipoInstrumento", "IdTipoReajuste",
				"Tasa", "Precio", "PlazoDesde", "PlazoHasta",
				"FechaModificacion", "IdUsuarioModificacion", "IdEstado" )
			SELECT	"BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasaTipoInstrumento".NEXTVAL,
					p_IdTipoinstrumento, p_IdTipoReajuste,
					p_Tasa, p_Precio, p_PlazoDesde, p_PlazoHasta,
					v_FechaSistema, p_IdUsUario, 'ACT'
			FROM	DUMMY;
			
		ELSE 
			IF p_IdAccion = 'UPD' THEN
				UPDATE	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"		
				SET		"Tasa" = p_Tasa, 
						"Precio" = p_Precio, 
						"PlazoDesde" = p_PlazoDesde, 
						"PlazoHasta" = p_PlazoHasta,
						"FechaModificacion" = v_FechaSistema, 
						"IdUsuarioModificacion" = p_IdUsuario
				WHERE	p_IdTipoinstrumento = "IdTipoInstrumento"
						AND p_IdTipoReajuste = "IdTipoReajuste"
						AND p_PlazoDesde = "PlazoDesde"
						AND p_PlazoHasta = "PlazoHasta";

			ELSE 
				DELETE	FROM "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"		
				WHERE	p_IdTipoinstrumento = "IdTipoInstrumento"
						AND p_IdTipoReajuste = "IdTipoReajuste"
						AND p_PlazoDesde = "PlazoDesde"
						AND p_PlazoHasta = "PlazoHasta";			
			END IF;
		END	IF;
	END;	
END;