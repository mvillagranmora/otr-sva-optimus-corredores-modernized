SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::OPE.ClaveOperacionesPendientes";

-- 
-- Proposito .....: Asignar papeles a una operacion de venta a un cliente de acuerdo a 
--					su plantilla de preferencia
-- Autor .........: MSH
-- Fecha Creacion : Noviembre 2016
--      
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.ClaveOperacionesPendientes"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa	BIGINT,
    IN p_IdUsuario	BIGINT,
    IN p_IdModulo	NVARCHAR(3),
    IN p_IdPais		BIGINT,
 
	--* Variables del SP
	IN 	p_TblOperaciones	TABLE (
					   		"IdRegistro" BIGINT,	
							"IdOperacionPendiente" BIGINT,	
							"IdTipoOperacion" 	NVARCHAR(6),
							"FolioOperacion"	BIGINT,
							"Clave"				NVARCHAR(20) )
	,OUT p_out_Salida        TABLE (
					   		"IdRegistro" BIGINT,		
							"IdEstado"	 NVARCHAR(3)
							)
	--* Salidas para mensajes de advertencias y errores
	,OUT p_out_ErroresContador 	INT
	,OUT p_out_ErroresDetalle 	TABLE (	"IdAplicacion"	BIGINT,
										"Codigo" 		NVARCHAR(100),
										"Descripcion"	NVARCHAR(5000)
	) 
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN


	--* ---------------------------------
	--* DECLARE Manejo de Errores
	--* ---------------------------------
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE	v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" 
				(::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = 
				SELECT * FROM :p_out_ErroresDetalle UNION ALL  
				SELECT 0, v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;				
			SELECT 	COUNT(1) 
			INTO 	p_out_ErroresContador 
			FROM	:p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	--* --------------
	--* Proceso
	--* --------------
	BEGIN 
	
		--* ---------------------------------------------------
		--* Declaraciones Variables
		--* ---------------------------------------------------
		DECLARE v_CantidadRegistros			INT;
		DECLARE v_Nro_Errores				BIGINT;
		DECLARE	v_FechaOperacionSecondate	SECONDDATE;
		p_out_ErroresContador := 0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
			( :p_IdEmpresa, v_FechaOperacionSecondate );				
	

		--* ---------------------------------------------------
		--* Validaciones
		--* ---------------------------------------------------

		--* ---------------------------------------------------
		--* UPDATE DE REGISTROS YA EXISTENTES (YA TENIAN CLAVE)
		--* ---------------------------------------------------
		UPDATE	Operaciones
		SET		Operaciones."Clave" = Param."Clave",
				Operaciones."IdUsuario" = p_IdUsuario,
				Operaciones."FechaEstado" = v_FechaOperacionSecondate
		FROM	"BSCL.Custodia.BD::CUS.OperacionesPendientes" AS Operaciones,
				:p_TblOperaciones AS Param
		WHERE	Operaciones."IdOperacionPendiente" = Param."IdOperacionPendiente"
				AND Param."IdOperacionPendiente" > 0;

		--* ---------------------------------------------------
		--* INSERT NUEVOS REGISTROS
		--* ---------------------------------------------------
		INSERT	INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes"
				( 	"IdOperacionPendiente"
					,"IdTipoOperacion"
					,"FolioOperacion"
					,"Clave"		
					,"IdEstado"		
					,"IdUsuario" 	
					,"FechaEstado"	)
		SELECT	"BSCL.Custodia.BD.Secuencias::IdOperacionPendiente".NEXTVAL
				,"IdTipoOperacion"
				,"FolioOperacion"
				,"Clave"
				,'PEN'
				,p_IdUsuario
				,v_FechaOperacionSecondate
		FROM	:p_TblOperaciones
		WHERE	"IdOperacionPendiente" = 0;


		P_OUT_SALIDA = 
			SELECT	1 		AS "IdRegistro", 
					'PEN' 	AS "IdEstado"
			FROM 	DUMMY;		
		
	END;	
	
END;
