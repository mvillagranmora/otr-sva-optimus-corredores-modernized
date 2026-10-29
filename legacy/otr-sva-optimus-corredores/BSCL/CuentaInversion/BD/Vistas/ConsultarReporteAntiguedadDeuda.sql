SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAntiguedadDeuda"; 
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAntiguedadDeuda" 
AS
SELECT 	
	PRO."IdPrograma"
	,USU."CodigoUsuario"
	,PRO."idEstado"																					AS "IdEstadoProgramaRelacionado"
	,EST."Nombre"																						AS "EstadoProgramaRelacionado"
	,TO_NVARCHAR(PRO."fechaSistema" , 'YYYY-MM-DD') 							AS "FechaSolicitudArchivo"
	,CASE 
		WHEN PRO."idEstado" = 'TER' AND PRO."RutaSalida" LIKE_REGEXPR 'CTA/'  AND LENGTH(PRO."Comentario") > 0 AND PRO."IdTipoPrograma" = 'GenerarReporteAntiguedadDeudaCTA'
			THEN REPLACE(PRO."Comentario", 'CODIGOCORREDOR_', '')||'_'||REPLACE(TO_NVARCHAR(PRO."fechaSistema", 'DD-MM-YYYY'), '-', '')||'_cuentas_por_cobrar_sin_separacion.xlsx'
		WHEN PRO."idEstado" = 'TER' AND PRO."RutaSalida" LIKE_REGEXPR 'CTA/'  AND LENGTH(PRO."Comentario") > 0 AND PRO."IdTipoPrograma" = 'GenerarReporteAntiguedadDeudaBolsaCTA'
			THEN REPLACE(PRO."Comentario", 'CODIGOCORREDOR_', '')||'_'||REPLACE(TO_NVARCHAR(PRO."fechaSistema", 'DD-MM-YYYY'), '-', '')||'_cuentas_por_cobrar_con_separacion.xlsx'
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
WHERE PRO."IdTipoPrograma" IN ('GenerarReporteAntiguedadDeudaCTA','GenerarReporteAntiguedadDeudaBolsaCTA');
GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarReporteAntiguedadDeuda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 