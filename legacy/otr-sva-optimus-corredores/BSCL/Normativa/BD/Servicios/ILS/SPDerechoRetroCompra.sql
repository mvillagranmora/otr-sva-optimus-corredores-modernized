SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.SPDerechoRetroCompra";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.SPDerechoRetroCompra" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	-- Parametros entrada
	IN p_FechaConsulta		NVARCHAR(10),
	-- Retorna Resultado
	OUT p_Resultado			DECIMAL(20,4),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))DEFAULT EMPTY
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
	DECLARE v_errorhashmsg TABLE ("Codigo" NVARCHAR(40), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1)
		  INTO p_out_ErroresContador
		  FROM :p_out_ErroresDetalle;

		SELECT COUNT(1)
		  INTO p_out_AdvertenciasContador
		  FROM :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE, v_errorExt);
			p_out_ErroresDetalle = SELECT *
									 FROM :p_out_ErroresDetalle
								UNION ALL
								   SELECT v_errorExt, ::SQL_ERROR_MESSAGE
								     FROM DUMMY;

			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;

	BEGIN
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(520));	
	    DECLARE v_count_registros           INT;
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
			
			SELECT count(1) INTO v_count_registros
			FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
			WHERE "IdEmpresa" = :p_IdEmpresa
			AND "FechaContable" = TO_DATE(:p_FechaConsulta,'YYYY-MM-DD');
			
			IF (:v_count_registros > 0) THEN
				SELECT COALESCE("DerechosTasaPactosMenores8Dias",0) INTO p_Resultado
				FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
				WHERE "IdEmpresa" = :p_IdEmpresa
				AND "FechaContable" = TO_DATE(:p_FechaConsulta,'YYYY-MM-DD');
			ELSE 
				SELECT 0 INTO p_Resultado from dummy;
			END IF;
		END;
	END;
END;
