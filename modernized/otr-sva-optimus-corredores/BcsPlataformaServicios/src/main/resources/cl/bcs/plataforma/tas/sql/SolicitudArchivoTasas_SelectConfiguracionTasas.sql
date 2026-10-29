SELECT 
	CT."NombreEntidad" as "nombreEntidad",
	CASE 
		WHEN "NombreEntidad" = 'RUTAARCHIVOTASAS' AND COALESCE(CP."Valor",'') <> '' AND COALESCE(CT."Valor",'') <> '' THEN CP."Valor" || CT."Valor"
		WHEN "NombreEntidad" = 'SERVICIOCONSULTARTASAS' THEN CT."Valor"
		ELSE ''
	END AS "valor"
FROM "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" CT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CP
	ON (UCASE(CP."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND CT."IdEmpresa" = CP."IdEmpresa")
WHERE CT."IdEmpresa" = :p_IdEmpresa
  AND CT."NombreEntidad" IN ('RUTAARCHIVOTASAS','SERVICIOCONSULTARTASAS');