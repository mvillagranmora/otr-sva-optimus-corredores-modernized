SELECT 
	FORM."CodigoCampo"
	,CASE 
		WHEN "CodigoCampo" = 'SECUENCIA' 					THEN 'Sec.'
		WHEN "CodigoCampo" = 'RUTCLIENTE' 					THEN 'Rut cliente'
		WHEN "CodigoCampo" = 'NOMBRECLIENTE' 				THEN 'Nombre cliente'
		WHEN "CodigoCampo" = 'CODIGOPORTAFOLIO'				THEN 'Codigo Portafolio'
		WHEN "CodigoCampo" = 'MONTOTOTALPORCOBRAR' 			THEN 'Monto total por cobrar $'
		WHEN "CodigoCampo" = 'DEUDASVIGENTES' 				THEN 'Deudas vigentes $'
		WHEN "CodigoCampo" = 'DEUDASVENCIDASDE3A10DIAS' 	THEN 'Deudas Vencidas de 3 a 10 días $'
		WHEN "CodigoCampo" = 'DEUDASVENCIDASDE11A30DIAS' 	THEN 'Deudas Vencidas de 11 a 30 días $'
		WHEN "CodigoCampo" = 'DEUDASVENCIDAMASDE30DIAS' 	THEN 'Deudas Vencidas más de 30 días $'
		WHEN "CodigoCampo" = 'OBSERVACIONES' 				THEN 'Observaciones'
		WHEN "CodigoCampo" = 'RELACIONADO' 					THEN 'Relacionado'
		WHEN "CodigoCampo" = 'FECHADEUDA'					THEN 'Fecha Deuda'
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
WHERE ARCH."CodigoArchivo" = 'REPORTE_ANTIGUEDAD_DEUDA_CTA' AND ARCH."IdEmpresa" = :IdEmpresa
AND FORM."CodigoCampo" != CASE WHEN :Consolidado = 'S' THEN 'CODIGOPORTAFOLIO'
ELSE ''
END 
ORDER BY FORM."Posicion" ASC;