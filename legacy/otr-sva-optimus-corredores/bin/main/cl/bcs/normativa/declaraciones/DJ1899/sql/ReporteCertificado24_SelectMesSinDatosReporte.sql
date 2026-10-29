
WITH Meses AS (	
    SELECT 1 "Mes",'ENERO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 2 "Mes",'FEBRERO' 	AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 3 "Mes",'MARZO'		AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 4 "Mes",'ABRIL' 		AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 5 "Mes",'MAYO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 6 "Mes",'JUNIO' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 7 "Mes",'JULIO' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 8 "Mes",'AGOSTO' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 10 "Mes",'OCTUBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
    SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY
)

SELECT DISTINCT -- meses sin datos  
   
    c."Folio",c."IdEmpresa",c."Esquema",c."Version",c."Anio",c."Periodo",

    c."NombreEntidadRecaudadora",c."DocumentoIdentidadEntidad",c."DireccionEntidadRecaudadora",

    c."IdCliente",
    c."NombreCompletoCliente",
    c."DocumentoIdentidadCliente",
    c."DireccionCliente",
    c."RazonSocialEmpresa", 
    c."DocumentoIdentidadEmpresa", 
    c."DireccionEmpresa", 
    c."CiudadFecha", 
    m."Mes",
    m."DescripcionMes" AS "DescripcionMes",

    to_decimal(0) AS "MontoModalidadEmpleador",
    to_decimal(0) AS "MontoModalidadTrabajador",

    to_decimal(0) AS "MontoCotizacionesObligatorias",
    to_decimal(0) AS "MontoCotizacionesVoluntarias",

    to_decimal(0) AS "MontoTrabajadoresActivos",
    to_decimal(0) AS "MontoPensionados",

    to_decimal(0) AS "MontoRetencion",

    CAST(0 AS BIGINT) AS "ActivoPensionado",               
    to_decimal(0) AS "AhorrosModalidadIndirectaEmpleador", 
    to_decimal(0) AS "AhorrosModalidadDirectaTrabajador", 
    to_decimal(0) AS "CotizacionesModalidadIndirectaEmpleador", 
    to_decimal(0) AS "CotizacionesModalidadDirectaTrabajador", 
    to_decimal(0) AS "CotizacionesTrabajadorIndependiente",  
    to_decimal(0) AS "MontoRetiroPorAhorrosIncisoPrimero", 
    to_decimal(0) AS "RetencionImpuestoSobreRetiros",        
    to_decimal(0) AS "MontoRetiroPorAhorrosIncisoSegundo",   
    to_decimal(0) AS "RetiroEmpleadorActualizado",
    to_decimal(0) AS "ResumenTrabajadorActivo",
    to_decimal(0) AS "ResumenTrabajadorPensionado"            
FROM (
    SELECT DISTINCT  --datos del certificado
        c."Folio",--Certificado numero
        c."IdEmpresa",
        c."Esquema",
        c."Version",
        c."Anio",
        c."Periodo",
        -- Entidad
        e."RazonSocial" AS "NombreEntidadRecaudadora",
        e."DocumentoIdentidad" AS "DocumentoIdentidadEntidad",
        e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEntidadRecaudadora",
        --
        COALESCE(cli."IdCliente",0) AS "IdCliente",--contribuyente
        COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
        TRIM(c."RutCliente") AS "DocumentoIdentidadCliente",--rut del contribuyente
        dfi."NombreLargo" AS "DireccionCliente",--direccion contribuyente
        e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
        e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
        e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
        e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha",  --ciudad y fecha
        m."Mes",
        m."DescripcionMes" AS "DescripcionMes",
        COALESCE(CP."Certificacion",'NGE') AS "Certificacion",
        COALESCE(CP."EstadoCertificado",'NGE') AS "EstadoCertificado"
    FROM  --AQUI la tabla que filtra los ruts
    (
        SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
            FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
            WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT24'
            AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
    ) AS c
    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
    INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
    INNER JOIN 	(SELECT * FROM Meses) m ON (m."Mes" = d."Mes")
    LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1899' AND CP."Version" = :Version)
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" dfi ON (dfi."IdDireccionFisica" = cli."IdDireccionFisicaPrincipal")
    WHERE c."Version" = :Version 
    ORDER BY m."Mes" ASC
) AS c, (	
            SELECT 1 "Mes",'ENERO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 2 "Mes",'FEBRERO' 	AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 3 "Mes",'MARZO'		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 4 "Mes",'ABRIL' 		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 5 "Mes",'MAYO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 6 "Mes",'JUNIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 7 "Mes",'JULIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 8 "Mes",'AGOSTO'		AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 10 "Mes",'OCTUBRE' 	AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
            SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY
        ) AS m
WHERE NOT EXISTS (SELECT 1 FROM (
                    SELECT c."Folio",d."Mes" AS "Mes"
                        FROM 	
                        ( SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
                            FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
                            WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT24'
                            AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
                        ) AS c
                        INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
                        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
                        INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
                        WHERE c."Version" = :Version 
            ) AS r WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");
					