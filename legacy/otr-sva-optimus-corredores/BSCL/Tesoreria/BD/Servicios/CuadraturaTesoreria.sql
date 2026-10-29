SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CuadraturaTesoreria";
--
--  Proposito: Cuadratura Tesoreria
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 25/10/2017
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios::CuadraturaTesoreria" (
    IN p_IdEmpresa			BIGINT,
    IN p_IdUsuario			BIGINT,
    IN p_IdModulo			NVARCHAR(3),
	IN p_UsaReglasNegocio	NVARCHAR(1),
	IN p_FechaProceso		NVARCHAR(10),
	
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
	DECLARE v_out_AdvertenciasDetalle	TABLE (	"IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(1), "Descripcion" NVARCHAR(5000)) DEFAULT 
										SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "Descripcion"
										FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT 
									SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion"
									FROM DUMMY WHERE 1=2;
	
	DECLARE v_resultadoRegla NVARCHAR(40);
	DECLARE v_count BIGINT;
	DECLARE v_FechaHoraSistema SECONDDATE;
	DECLARE v_CodigoUsuario NVARCHAR(40);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		v_TablaCodigosMensajes =
			SELECT "Codigo" AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato" FROM :p_out_ErroresDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle =
			SELECT errores."IdInterno", errores."Codigo", errores."Descripcion"--msj."Descripcion"
			FROM :p_out_ErroresDetalle errores
			INNER JOIN :v_out_ErroresDetalleMensajes msj
				ON errores."Codigo" = msj."Codigo";
		
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	
	--------------------
	--Reglas Negocio
	--------------------
	CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(
		 :p_IdEmpresa
         ,:p_IdUsuario
         ,:p_IdModulo
         ,:v_out_ErroresDetalle_Basicos
         ,:v_resultadoRegla
    );
	IF :v_resultadoRegla = 'RECH' THEN
		p_out_ErroresDetalle =
			SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM :v_out_ErroresDetalle_Basicos;
		
		SIGNAL SALIDA_CONTROLADA;
	END IF;
	
	SELECT COUNT(1) INTO v_count FROM "BSCL.Plataforma.BD::PAU.Proceso"
	WHERE "CodigoProceso" = 'CUADRATURA_TESORERIA'
	  AND "IdEmpresa" = :p_IdEmpresa
	  AND "CodigoModulo" = :p_IdModulo;
	
	IF v_count = 0 THEN
		p_out_ErroresDetalle =
			SELECT 0 AS "IdInterno", 'NO_EXISTE_PROCESO' AS "Codigo", 'No existe proceso Cuadratura Cuenta Inversion' AS "Descripcion"
			FROM DUMMY;
		
		SIGNAL SALIDA_CONTROLADA;
	END IF;
	
	
	--------------------
	--Negocio Aqui
	--------------------
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa,:v_FechaHoraSistema);
	
	SELECT COALESCE(USR."CodigoUsuario",'-') INTO v_CodigoUsuario
	FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
	WHERE USR."IdUsuario" = :p_IdUsuario AND USR."IdEmpresa" = :p_IdEmpresa;
	
	
	--procesos necesarios para realizar la Cuadratura Tesoreria
	v_Procesos = 
		SELECT 'TES' AS "Modulo", 'CUADRATURA_TESORERIA' AS "Proceso" FROM DUMMY;
	
	--se borran los procesos de la Cuadratura Tesoreria generados en el dia
	DELETE FROM "BSCL.Plataforma.BD::PAU.ControlProceso"
	WHERE ("IdEmpresa", "CodigoModulo", "CodigoProceso", "FechaSistema" ) IN
		  (SELECT :p_IdEmpresa AS "IdEmpresa", PROC."Modulo", PROC."Proceso", :p_FechaProceso AS "FechaSistema"
		   FROM :v_Procesos PROC);
	
	--se crean los procesos de la Cuadratura Tesoreria
	INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso"
	SELECT
		:p_IdEmpresa,
		"BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL,
		"Modulo",
		"Proceso",
		:p_FechaProceso,
		:v_FechaHoraSistema,
		:v_CodigoUsuario
	FROM :v_Procesos PRC;
	
	
END;
