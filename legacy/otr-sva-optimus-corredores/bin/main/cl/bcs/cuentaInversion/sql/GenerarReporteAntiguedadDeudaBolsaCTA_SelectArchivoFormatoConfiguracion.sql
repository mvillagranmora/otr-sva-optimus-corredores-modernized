SELECT 
	FORM."CodigoCampo"
	,CASE 
		WHEN "CodigoCampo" = 'RADB_SECUENCIA' 					THEN 'Sec.'
		WHEN "CodigoCampo" = 'RADB_RUTCLIENTE' 					THEN 'Rut cliente'
		WHEN "CodigoCampo" = 'RADB_NOMBRECLIENTE' 				THEN 'Nombre cliente'
		WHEN "CodigoCampo" = 'RADB_CODIGOPORTAFOLIO'			THEN 'Codigo Portafolio'
		WHEN "CodigoCampo" = 'RADB_MONTOTOTALPORCOBRAR' 		THEN 'Monto total por cobrar $'
		WHEN "CodigoCampo" = 'RADB_DEUDASVIGENTES' 				THEN 'Deudas vigentes $'
		WHEN "CodigoCampo" = 'RADB_DEUDASVENCIDASDE3A10DIAS' 	THEN 'Deudas Vencidas de 3 a 10 días $'
		WHEN "CodigoCampo" = 'RADB_DEUDASVENCIDASDE11A30DIAS' 	THEN 'Deudas Vencidas de 11 a 30 días $'
		WHEN "CodigoCampo" = 'RADB_DEUDASVENCIDAMASDE30DIAS' 	THEN 'Deudas Vencidas más de 30 días $'
		WHEN "CodigoCampo" = 'RADB_OBSERVACIONES' 				THEN 'Observaciones'
		WHEN "CodigoCampo" = 'RADB_CLASIFICACIONCONTABLE' 		THEN 'Clasificación contable'
		WHEN "CodigoCampo" = 'RADB_RELACIONADO' 				THEN 'Relacionado'
		ELSE ''
	END "CampoNombreCompleto"
	,CASE WHEN :Consolidado = 'S' AND FORM."Posicion" >= 3 THEN FORM."Posicion"- 1
		ELSE FORM."Posicion"
		END "Posicion"
FROM "BSCL.Plataforma.BD::PLA.Archivo" ARCH
INNER JOIN "BSCL.Plataforma.BD::PLA.ArchivoFormato" ARCHFORM
	ON(ARCH."IdArchivo" = ARCHFORM."IdArchivo")
INNER JOIN "BSCL.Plataforma.BD::PLA.Formato" FORM
	ON(ARCHFORM."IdFormato" = FORM."IdFormato")
WHERE ARCH."CodigoArchivo" = 'REPORTE_ANTIGUEDAD_DEUDA_BOLSA_CTA' AND ARCH."IdEmpresa" = :IdEmpresa
AND FORM."CodigoCampo" != CASE WHEN :Consolidado = 'S' THEN 'RADB_CODIGOPORTAFOLIO'
ELSE ''
END 
ORDER BY FORM."Posicion" ASC;