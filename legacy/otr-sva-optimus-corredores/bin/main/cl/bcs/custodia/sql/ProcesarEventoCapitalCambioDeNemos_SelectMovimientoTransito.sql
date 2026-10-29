SELECT
    IFNULL(SOC."IdSocioDeNegocio",0)                            as "IdSocioNegocio",                            --  "IdSocioNegocio"
    CASE 
        WHEN SOC."IdCliente"        > 0         THEN 'CLI'
        WHEN SOC."IdProveedor"      > 0         THEN 'PRO'
        WHEN SOC."IdEmpleado"       > 0         THEN 'EMP'
        WHEN SOC."IdContraparte"    > 0         THEN 'CNT'
        
    END                                                         as "TipoSocioNegocio",                          --  "TipoSocioNegocio"
    IFNULL(FT."IdPortafolio",0)                                 as "IdPortafolio",                              --  "IdPortafolio"
    'R'                                                         as "IdTipoDocumentoIdentidad",                  --  "IdTipoDocumentoIdentidad"
    IFNULL(CL."DocumentoIdentidad",'')                          as "DocumentoIdentidad",                        --  "DocumentoIdentidad"
    IFNULL(CL."NombreCompleto",'')                              as "NombreCliente",                             --  "NombreCliente"
    FT."IdClasificacionTipoInstrumento",            --  "IdClasificacionTipoInstrumento"
    FT."IdTipoInstrumento",                         --  "IdTipoInstrumento"
    FT."IdInstrumento",                             --  "IdInstrumento"
    FT."Nemotecnico"                                            as "Nemo",                                      --  "Nemo"
    'CUS'                                                       as "TipoCartera",
    COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)             as "IdEmpleadoEjecutivoPortafolio",
    COALESCE(EJE."CodigoEmpleado",'')                           as "CodigoUsuarioAgente",                       --  "CodigoUsuarioAgente"
    COALESCE(EJE."NombreCompletoEjecutivo",'')                  as "NombreAgente",                              --  "NombreAgente"
    COALESCE(EJE."CodigoSucursal",'')                           as "CodigoSucursal",                            --  "CodigoSucursal"
    CASE FT."IdBoveda"
        WHEN ''     THEN 'CIA'
        WHEN 'COR'  THEN 'DCV'
        ELSE         FT."IdBoveda"
    END                                                         as "IdBoveda",                                  --  "IdBoveda"
    CASE FT."IdBolsa" 
        WHEN '' THEN 'BCS'
        ELSE          IFNULL(FT."IdBolsa",'BCS')
    END                                                         as "IdBolsa",
    FT."IdConcepto"                                             as "IdConcepto",
    FT."IdMovimientoCarteraEnTransito"                                              AS "IdCartera",
    FT."FolioCustodia"                                           AS "FolioCartera",
    'N'                                                         as "AfectoaClearing",
    FT."Cantidad"                                            AS "CantidadDerecho"
    
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"   AS FT
    LEFT JOIN   "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"             AS CL
        ON (FT."IdCliente" = CL."IdCliente")
    LEFT JOIN   "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"      AS SOC
        ON (CL."IdCliente" = SOC."IdCliente")
    LEFT JOIN   "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EJE
        ON (FT."IdPortafolio" = EJE."IdPortafolio")
WHERE
        FT."IdEmpresa"      = :p_IdEmpresa  
    AND FT."Nemotecnico"    IN (:p_Nemo)
    AND FT."IdCliente"      > 0
    AND IFNULL(SOC."IdSocioDeNegocio",0) > 0
    --AND FT."FechaOperacion" <= :p_FechaLimite filtrar en el JAVA
    AND FT."IdTipoInstrumento"  NOT IN  ('PRT', 'EMP', 'PAC', 'SIM') 
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
    FT."IdClasificacionTipoInstrumento",
    FT."IdInstrumento",
    FT."IdTipoInstrumento",
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
    FT."IdMovimientoCarteraEnTransito",
    FT."FolioCustodia",
    FT."Cantidad";