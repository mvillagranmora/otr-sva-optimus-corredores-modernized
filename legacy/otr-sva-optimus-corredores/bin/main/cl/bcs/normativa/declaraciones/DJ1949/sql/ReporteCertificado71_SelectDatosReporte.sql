   SELECT
			
	c."Folio",--Certificado numero
    c."IdEmpresa",
    c."Esquema",
    c."Version",
    c."Anio",
    TO_NVARCHAR(TO_INT(c."Periodo") + 1) AS "Periodo",
    ---
    d."NroCertificadoF5",
    d."FactorF7",
    d."NroDividendoF2",
    d."MontoF6",
    d."MontoActualizadoF8",
    --Montos Retiros Reajustados
    d."ConCreditoIDPCDesed2017C6F9",
    d."ConCreditoIDPCHasta2017C7F10",
    d."ConDerechoIDPCVoluntarioC8F11",
    d."SinDerechoCreditoC9F12",
    --Rentas con tributación cumplida
    d."MtoDivReaREXTribCumpRentasRap_C10F13",
    d."MtoDivReaREXTribCumpOtrasRentas_C11F14",
    d."MtoDivReaREXTribCumpExcDist_C12F15",
    d."MtoDivReaREXTribCumpUtiISFUT_C13F16",
    d."MtoDivReaREXTribCumpRentasISFUT_C14F17",
    --Rentas Excentas
    d."MtoDivReaREXRentExIGCAfecIAart11_C15F18",
    d."RentasExcentasIGCC16F19",
    d."MtoDivReaREXNoConstRenta_C17F20",
    --Resto
    d."CIGCDesde01012017RentAfecNoRestSDD_C18F21",
    d."CIGCDesde01012017RentAfecNoRestCDD_C19F22",
    d."CIGCDesde01012017RentAfecNoRestSDD_C20F23",
    d."CIGCDesde01012017RentAfecNoRestCDD_C21F24",
    d."CIGCDesde01012017RentAfecRestSDD_C22F25",
    d."CIGCDesde01012017RentAfecRestCDD_C23F26",
    d."CIGCDesde01012017RentExenRestSDD_C24F27",
    d."CIGCDesde01012017RentExenRestCDD_C25F28",
    d."CIGCDesde01012017CredIPE_C26F29",
    d."CIGCHasta31122016RentAfecSDD_C27F30",
    d."CIGCHasta31122016RentAfecCDD_C28F31",
    d."CIGCHasta31122016RentExenSDD_C29F32",
    d."CIGCHasta31122016RentExenCDD_C30F33",
    d."CIGCHasta31122016CredIPE_C31F34",
    d."CIGCImptoTasaAdicional_C32F35",
    d."DevCapArt17N7LIR_C33F36",
    d."TasaEfectCredFUT",
    d."TasaEfectCredFUNT",
    --
    TO_CHAR(TO_DATE(d."FechaDividendo",'YYYY-MM-DD'),'DD-MM-YYYY') AS "FechaDividendo",
    d."RutSocPagadora",
    CASE WHEN LTRIM(RTRIM(COALESCE(soc."RazonSocial",''))) = '' THEN COALESCE(soc."NombreCompleto",'') ELSE COALESCE(soc."RazonSocial",'') END AS "RazonSocialSocPagadora",
    d."TipoSociedad",
    d."RutReceptor",
    --Institución Autorizada
    e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
    e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
    e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
    --La Administradora de Fondos de Pensiones...
    e."RazonSocial" AS "NombreEntidadRecaudadora",
    --trabajador
    c."IdCliente",--contribuyente
    COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
    COALESCE(cli."DocumentoIdentidad",'0') AS "DocumentoIdentidadCliente",--rut del contribuyente
    --
    e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha" --ciudad y fecha
	
	FROM  -- AQUI la tabla que filtra los ruts
	
	(
		SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
		 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
		 WHERE CER."IdEmpresa" = :IdEmpresa AND CER."Version" = :Version AND CER."IdTipoCertificado"='CERT71'
		 AND CER."IdCertificadoCliente" IN (:IdCertificadoCliente)
		
	) AS c
	INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" soc ON (TRIM(soc."DocumentoIdentidad") = TRIM(d."RutSocPagadora"))
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
WHERE c."Version" = :Version;
