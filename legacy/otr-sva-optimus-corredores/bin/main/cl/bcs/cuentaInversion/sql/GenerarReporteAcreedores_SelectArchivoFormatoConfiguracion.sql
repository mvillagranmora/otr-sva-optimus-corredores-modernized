SELECT 
	FORM."CodigoCampo"
	,CASE 
		WHEN "CodigoCampo" = 'RACREEDORES_SECUENCIA' 				THEN 'Sec.'
		WHEN "CodigoCampo" = 'RACREEDORES_RUTCLIENTE' 				THEN 'Rut cliente'
		WHEN "CodigoCampo" = 'RACREEDORES_NOMBRECLIENTE' 			THEN 'Nombre cliente'
		WHEN "CodigoCampo" = 'RACREEDORES_CODIGOPORTAFOLIO'			THEN 'Codigo Portafolio'
		WHEN "CodigoCampo" = 'RACREEDORES_MONTOTOTALPORPAGAR' 		THEN 'Monto total por pagar $'
		WHEN "CodigoCampo" = 'RACREEDORES_HABERESVIGENTES' 			THEN 'Haberes vigentes $'
		WHEN "CodigoCampo" = 'RACREEDORES_HABERESDE3A10DIAS' 		THEN 'Haberes de 3 a 10 días $'
		WHEN "CodigoCampo" = 'RACREEDORES_HABERESDE11A30DIAS' 		THEN 'Haberes de 11 a 30 días $'
		WHEN "CodigoCampo" = 'RACREEDORES_HABERESMASDE30DIAS'		THEN 'Haberes de más de 30 días $'
		WHEN "CodigoCampo" = 'RACREEDORES_OBSERVACIONES' 			THEN 'Observaciones'
		WHEN "CodigoCampo" = 'RACREEDORES_CLASIFICACIONCONTABLE'	THEN 'Clasificación contable'
		WHEN "CodigoCampo" = 'RACREEDORES_RELACIONADO' 				THEN 'Relacionado'
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
WHERE ARCH."CodigoArchivo" = 'REPORTE_ACREEDORES' AND ARCH."IdEmpresa" = :IdEmpresa
AND FORM."CodigoCampo" != CASE WHEN :Consolidado = 'S' THEN 'RACREEDORES_CODIGOPORTAFOLIO'
ELSE ''
END 
ORDER BY FORM."Posicion" ASC;