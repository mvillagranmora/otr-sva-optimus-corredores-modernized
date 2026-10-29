SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAcreedores"; 
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAcreedores" 
AS
SELECT 	
	PRO."IdPrograma"
	,USU."CodigoUsuario"
	,PRO."idEstado"																					AS "IdEstadoProgramaRelacionado"
	,EST."Nombre"																						AS "EstadoProgramaRelacionado"
	,TO_NVARCHAR(PRO."fechaSistema" , 'YYYY-MM-DD') 							AS "FechaSolicitudArchivo"
	,CASE 
		WHEN PRO."idEstado" = 'TER' AND PRO."RutaSalida" LIKE_REGEXPR 'CTA/'  AND LENGTH(PRO."Comentario") > 0
			THEN REPLACE(PRO."Comentario", 'CODIGOCORREDOR_', '')||'_'||REPLACE(TO_NVARCHAR(PRO."fechaSistema", 'DD-MM-YYYY'), '-', '')||'_cuentas_por_pagar_con_separacion.xlsx'
		ELSE ''
	END AS "NombreArchivo"
	,CASE 
		WHEN LENGTH (COALESCE(PRO."Comentario", '')) <> 0 AND PRO."RutaSalida" NOT LIKE_REGEXPR 'CTA/'THEN  PRO."Comentario"
		ELSE ''
	END AS "ErrorArchivo"
	,CASE 
		WHEN PRO."idEstado" = 'TER' AND PRO."RutaSalida" LIKE_REGEXPR 'CTA/'  AND LENGTH(PRO."Comentario") > 0 
			THEN  PRO."RutaSalida"
		ELSE ''
	END AS "RutaSalidaArchivo"
	,CASE 
		WHEN PRO."RutaSalida" LIKE_REGEXPR 'CTA/' THEN 'SI'
		ELSE 'NO'
	END AS "GeneroArchivo"
FROM "BSCL.Plataforma.BD::BAT.Programa" PRO
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST
	ON PRO."idEstado" = EST."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USU
	ON USU."IdUsuario" = PRO."IdUsuario"
WHERE PRO."IdTipoPrograma" = 'GenerarReporteAcreedores'
GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAcreedores" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
