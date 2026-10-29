SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosAnularOperacionSpot";
GO
CREATE PROCEDURE
-- 
--  Proposito : Verificar que una operación Spot pueda ser anulada
--  Autor ....: Miguel Sandoval H - BCS
--  Fecha ....: Marzo 2017
--   
"BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosAnularOperacionSpot"(
	IN 	p_IdEmpresa     	BIGINT 			default 0,
    IN 	p_IdUsuario        	BIGINT,	
    IN  p_IdModulo			NVARCHAR(3),
	IN 	p_DatosOperacion    TABLE (	"NumeroOperacion" 	BIGINT,
									"IdOperacionSpot"	BIGINT,
									"FechaOperacion"    SECONDDATE,
									"IdMesa"			BIGINT
									),
	OUT p_out_SalidaDetalle TABLE (	"NumeroOperacion" 	NVARCHAR(40), 
									"Resultado" 		NVARCHAR(40), 
									"Codigo" 			NVARCHAR(40), 
									"ValorParametro" 	NVARCHAR(100), 
									"NombreParametro" 	NVARCHAR(40) 
									)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	--* Fecha de Sistema								
	DECLARE v_FechaSistema	NVARCHAR(10);
	CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);	
	
	v_Validaciones =	
		SELECT 	Ope."NumeroOperacion", 
				Ope."IdOperacionSpot", 
				1 AS "MESA_ACTIVA", 
				1 AS "MODULO_ACTIVO", 
				1 AS "FECHA_PROCESO_VALIDA", --OK
				1 AS "VALIDA_OPERADOR",
				1 AS "OPERACION_SPOT_FACTURADA"
		FROM 	:p_DatosOperacion Ope;


	--* Resultado...					
	v_TablaRequisitos =
		SELECT	Ope."NumeroOperacion", 
				CASE WHEN Val."MESA_ACTIVA" = 0 OR Mesa."IdEstadoMesa" = 'A' THEN 1 ELSE 4 END AS "IdResultado", 
				'MESA_CERRADA' AS "Codigo", 
				CAST(Ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM 	:p_DatosOperacion ope
				INNER JOIN 	:v_Validaciones Val 
							ON Ope."IdOperacionSpot" = Val."IdOperacionSpot" 
				LEFT JOIN 	"BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" Mesa 
	    					ON Mesa."IdMesa" = Ope."IdMesa"
		WHERE 	Ope."IdOperacionSpot" = Val."IdOperacionSpot"
		--* ------------------------------------------------------------------------------------------
		UNION 
		--* ------------------------------------------------------------------------------------------			
		SELECT 	Ope."NumeroOperacion", 
				CASE WHEN Val."MODULO_ACTIVO" = 0 OR (Modulo."IdEstado" = 'ACT' AND Modulo."TipoEstado" = 'A') THEN 1 ELSE 4 END AS "IdResultado", 
				'UI_MODULO_CERRADO' AS "Codigo", 
				CAST(Ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM 	:p_DatosOperacion ope
		INNER JOIN 	:v_Validaciones Val 
			ON Ope."IdOperacionSpot" = Val."IdOperacionSpot" 
		LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" Modulo 
			ON Modulo."IdModulo" = 'MSP' AND Modulo."IdEmpresa" = :p_IdEmpresa
		WHERE 	Ope."IdOperacionSpot" = Val."IdOperacionSpot"
		--* ------------------------------------------------------------------------------------------
		UNION
		--* ------------------------------------------------------------------------------------------			
		SELECT 	Ope."NumeroOperacion", 
				CASE WHEN Val."FECHA_PROCESO_VALIDA" = 0 OR v_FechaSistema = TO_CHAR(Ope."FechaOperacion",'YYYY-MM-DD') THEN 1 ELSE 4 END AS "IdResultado", 
				'FECHA_PROCESO_INVALIDA' AS "Codigo", 
				CAST(Ope."FechaOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM 	:p_DatosOperacion Ope, :v_Validaciones Val
		WHERE 	Ope."IdOperacionSpot" = Val."IdOperacionSpot"
		--* ------------------------------------------------------------------------------------------
		UNION
		--* ------------------------------------------------------------------------------------------			
		--* CUANDO LA OP. SE ESTA ANULANDO DE TESORERIA, NO DEBE VALIDAR EL USUARIO.
		SELECT 	Ope."NumeroOperacion", 
				CASE WHEN p_IdUsuario = Spt."IdUsuario" OR p_IdUsuario = usuSup."IdUsuario" THEN 1 
					 WHEN p_IdModulo = 'TES' THEN 1 
					 ELSE 4 END AS "IdResultado",
				'USUARIO_NO_AUTORIZADO_A_ANULAR_OPERACION' 	AS "Codigo",
				CAST(Ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM 	:p_DatosOperacion Ope 
				INNER JOIN 	:v_Validaciones Val 
							ON Ope."IdOperacionSpot" = Val."IdOperacionSpot" 
				INNER JOIN 	"BSCL.Mesa.BD::SPT.OperacionSpot" Spt 
							ON Ope."IdOperacionSpot" = Spt."IdOperacionSpot"
				INNER JOIN 	"BSCL.Plataforma.BD::ACC.Usuario" Usu 
							ON Spt."IdUsuario" = usu."IdUsuario" AND Usu."TipoEntidad" = 'EMP'
				INNER JOIN 	"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" Emp 
							ON Usu."IdEntidad" = Emp."IdEmpleado"
				LEFT JOIN 	"BSCL.Plataforma.BD::ACC.Usuario" UsuSup 
							ON UsuSup."IdEntidad" = Emp."IdEmpleadoSupervisor"
		--* ------------------------------------------------------------------------------------------
		UNION
		--* ------------------------------------------------------------------------------------------			
		/*SELECT	Ope."NumeroOperacion", 
				CASE WHEN Val."OPERACION_SPOT_FACTURADA" = 0 OR 
						  SPOT."IdComprobanteFacturacion" = 0 OR 
						  MOVFAC."IdEstadoFacturacion" <> 'FAC' OR 
						  (MOVFAC."IdEstadoFacturacion" = 'FAC' AND MOVNCR."IdComprobanteReferencia" IS NOT NULL) THEN 1 
					 ELSE 4 END AS "IdResultado", 
				'OPERACION_SPOT_FACTURADA' AS "Codigo", 
				CAST(Ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_Validaciones Val
			ON Ope."IdOperacionSpot" = Val."IdOperacionSpot"
		INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
			ON Ope."IdOperacionSpot" = SPOT."IdOperacionSpot"
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVFAC
			ON SPOT."IdComprobanteFacturacion" = MOVFAC."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVNCR
			ON (MOVFAC."IdComprobante" > 0 AND
				MOVFAC."IdComprobante" = MOVNCR."IdComprobanteReferencia" AND
				MOVNCR."IdTipoComprobante" IN ('NCE','NCR'))*/
		SELECT
			ope."NumeroOperacion", 
			CASE WHEN Val."OPERACION_SPOT_FACTURADA" = 0 OR SPTFAC."PuedeModificarAnular" = 1 THEN 1 ELSE 4 END AS "IdResultado", 
			SPTFAC."CodigoModificarAnular" AS "Codigo", 
			CAST(ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
			'IdOperacionSpot' AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_Validaciones Val
			ON ope."IdOperacionSpot" = Val."IdOperacionSpot"
		INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado" SPTFAC
			ON (ope."IdOperacionSpot" = SPTFAC."IdOperacionSpot" AND
				SPTFAC."IdEmpresa" = :p_IdEmpresa)
		;


	p_out_SalidaDetalle =	
		SELECT 	tb."NumeroOperacion", 
				CASE "IdResultado"
					WHEN 1 THEN 'OK'
					WHEN 2 THEN 'ADV'
					WHEN 3 THEN 'AUT'
					WHEN 4 THEN 'ERR'
				END AS "Resultado",
				tb."Codigo" ,
				tb."ValorParametro" ,
				tb."NombreParametro" 
		FROM 	:v_TablaRequisitos tb
		WHERE 	"IdResultado" != 1
		ORDER BY "NumeroOperacion";
	
END;
GO