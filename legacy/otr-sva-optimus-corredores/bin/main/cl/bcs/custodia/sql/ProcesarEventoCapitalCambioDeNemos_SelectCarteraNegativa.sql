SELECT
    IFNULL(SOC."IdSocioDeNegocio",0)                            AS "IdSocioNegocio",                            --  "IdSocioNegocio"
    CASE 
        WHEN SOC."IdCliente"        > 0         THEN 'CLI'
        WHEN SOC."IdProveedor"      > 0         THEN 'PRO'
        WHEN SOC."IdEmpleado"       > 0         THEN 'EMP'
        WHEN SOC."IdContraparte"    > 0         THEN 'CNT'
    END                                                         AS "TipoSocioNegocio",                          --  "TipoSocioNegocio"
    IFNULL(FT."IdPortafolio",0)                                 AS "IdPortafolio",                              --  "IdPortafolio"
    'R'                                                         AS "IdTipoDocumentoIdentidad",                  --  "IdTipoDocumentoIdentidad"
    IFNULL(CL."DocumentoIdentidad",'')                          AS "DocumentoIdentidad",                        --  "DocumentoIdentidad"
    IFNULL(CL."NombreCompleto",'')                              AS "NombreCliente",                             --  "NombreCliente"
    INS."IdClasificacionTipoInstrumento" ,         																--  "IdClasificacionTipoInstrumento"
    INS."IdTipoInstrumento",                         															--  "IdTipoInstrumento"
    FT."IdInstrumento",                             															--  "IdInstrumento"
    FT."Nemotecnico"                                         	AS "Nemo",                                      --  "Nemo"
    'CUS'                                                    	AS "TipoCartera",
    COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)          	AS "IdEmpleadoEjecutivoPortafolio",
    COALESCE(EJE."CodigoEmpleado",'')                        	AS "CodigoUsuarioAgente",                       --  "CodigoUsuarioAgente"
    COALESCE(EJE."NombreCompletoEjecutivo",'')               	AS "NombreAgente",                              --  "NombreAgente"
    COALESCE(EJE."CodigoSucursal",'')                        	AS "CodigoSucursal",                            --  "CodigoSucursal"
    CASE FT."IdBoveda"
        WHEN ''     THEN 'CIA'
        WHEN 'COR'  THEN 'DCV'
        ELSE         FT."IdBoveda"
    END                                                      	AS "IdBoveda",                                  --  "IdBoveda"
    CASE FT."IdBolsa" 
        WHEN '' THEN 'BCS'
        ELSE          IFNULL(FT."IdBolsa",'BCS')
    END                                                      	AS "IdBolsa",
    FT."IdConcepto"                                          	AS "IdConcepto",
    FT."IdCarteraNegativa"                                   	AS "IdCartera",
    FT."IdCarteraNegativa"                                   	AS "FolioCartera",
    'N'                                                      	AS "AfectoaClearing",
    -1 * FT."Cantidad"                                          AS "CantidadDerecho"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS FT
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CL ON (FT."IdCliente" = CL."IdCliente")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"  AS SOC ON (CL."IdCliente" = SOC."IdCliente")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EJE ON (FT."IdPortafolio" = EJE."IdPortafolio")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = FT."IdInstrumento" 
WHERE FT."IdEmpresa" = :p_IdEmpresa 
    AND FT."Nemotecnico" IN (:p_Nemo)
    AND FT."IdCliente" > 0
    AND IFNULL(SOC."IdSocioDeNegocio", 0) > 0
    AND INS."IdTipoInstrumento" NOT IN ('PRT', 'EMP', 'PRF', 'ERF', 'PAC', 'SIM') 
    AND FT."IdEstado" = 'VIG'
    AND (
		:p_IdInstrumento IS NULL 
		OR FT."IdInstrumento" IN (:p_IdInstrumento)
    )
GROUP by
    IFNULL(SOC."IdSocioDeNegocio",0),
    IFNULL(CL."NombreCompleto",''),
    CASE 
        WHEN SOC."IdCliente" > 0        THEN 'CLI'
        WHEN SOC."IdProveedor" > 0      THEN 'PRO'
        WHEN SOC."IdEmpleado" > 0       THEN 'EMP'
        WHEN SOC."IdContraparte" > 0    THEN 'CNT'
    END         ,               
    IFNULL(FT."IdPortafolio",0),    
    IFNULL(CL."DocumentoIdentidad",''),
    CL."NombreCompleto",
    INS."IdClasificacionTipoInstrumento",
    INS."IdTipoInstrumento",
    FT."IdInstrumento",
    FT."Nemotecnico",
    COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
    COALESCE(EJE."CodigoEmpleado",''),
    COALESCE(EJE."NombreCompletoEjecutivo",''),
    COALESCE(EJE."CodigoSucursal",''),
    CASE FT."IdBoveda"
        WHEN ''     THEN 'CIA'
        WHEN 'COR'  THEN 'DCV'
        ELSE FT."IdBoveda"
    END,
    CASE FT."IdBolsa" 
        WHEN '' THEN 'BCS'
        ELSE IFNULL(FT."IdBolsa",'BCS')
    END,
    FT."IdConcepto",
    FT."IdCarteraNegativa",
    FT."Cantidad";