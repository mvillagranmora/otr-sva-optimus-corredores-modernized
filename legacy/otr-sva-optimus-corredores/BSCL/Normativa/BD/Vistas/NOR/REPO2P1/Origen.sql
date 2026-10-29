SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Origen";
    
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Origen"(
 "ID",
 "IdEmpresa",
 "IdDocumento",
 "FuenteDatos",
    "Estado",
    "EstadoSICAM",
    "SICAM",
--camos layout    
 "TipoRegistro",
 "EventoReporte",
 "TipoOperacion",
 "FechaNegociacion",
 "HoraNegociacion",
 "FechaValutaMonedaRecibida",
 "FechaValutaMonedaEntregada",
 "Identificador",
 "RUTContraparte1",
 "LEIContraparte1",
 "NombreContraparte1",
 "RUTContraparte2",
 "LEIContraparte2",
 "NombreContraparte2",
 "PaisContraparte2",
 "RUTIntermediario",
 "PlataformaNegociacion",
 "CodigoOperacionCambio",
 "PaisMonedaExtranjera",
 "MonedaCompradaRecibida",
 "MontoMonedaCompradaRecibida",
 "MonedaVendidaEntregada",
 "MontoMonedaVendidaEntregada",
 "ConvencionTipoCambio",
 "TipoCambio",
 "AfectaDerivados",
 "IdentificadorContratoDerivados",
 "FechaSuscripcionContrato",
 "HoraSuscripcionContrato",
 "OperacionEmpresaRelacionada",
 "NumeroInscripcionObligacionExterior",
 "NombreFinancistaAporante",
 "LEIFinancistaAporante",
 "NombreReceptorInversion",
 "LEIReceptorInversion",
 "ModalidadInversionCustodio",
--campos de control interno especifico
 "FolioOperacion",
    "IdOperacion",
 "IdCartera",
 "FechaLiquidacion",
 "IdCliente",
 "IdTipoOperacion",
 "Nemo",
 "IdMonedaPrincipal",
 "IdMonedaSecundaria",
 "Observaciones"

 
) AS

       SELECT  
       0         AS  "ID",
       FWD."IdEmpresa"     AS  "IdEmpresa",
       0         AS  "IdDocumento",
       ''         AS  "FuenteDatos",
       'ACT'       AS "Estado",
       ''        AS "EstadoSICAM",
       ''        AS "SICAM",
       --camos layout    
       '01'       AS "TipoRegistro",
       FWD."EventoReporte"    AS  "EventoReporte",
       CASE FWD."TipoOrigen"
       WHEN 'FWD' THEN 
              
       		(CASE RTRIM(FWD."Modalidad") || '/'|| FWD."Accion" 	WHEN 'COMPENSADA/COMPRA' THEN '03' 
       		 													WHEN 'COMPENSADA/VENTA'  THEN '04' 
       		 ELSE (CASE FWD."Accion" WHEN 'COMPRA' THEN '01' ELSE '02' END)
       		END)
        
       ELSE (CASE FWD."Accion" WHEN 'COMPRA' THEN '01' ELSE '02' END)
       END
       AS  "TipoOperacion",
       FWD."FechaNegociacion"    AS "FechaNegociacion",
       FWD."HoraNegociacion"    AS "HoraNegociacion",
       CASE FWD."TipoOrigen"
       WHEN 'FWD' THEN 
              
       		(CASE RTRIM(FWD."Modalidad") || '/'|| FWD."Accion" 	WHEN 'COMPENSADA/COMPRA' THEN COALESCE(FWD."FechaRecibimos",FWD."FechaEntregamos") 	--03
       		 													WHEN 'COMPENSADA/VENTA'  THEN '0001-01-01'  										--04
       		 ELSE (COALESCE(FWD."FechaRecibimos",FWD."FechaEntregamos") )
       		END)
        
       ELSE (COALESCE(FWD."FechaRecibimos",FWD."FechaEntregamos") )
       END
       
       AS "FechaValutaMonedaRecibida",
       CASE FWD."TipoOrigen"
       WHEN 'FWD' THEN 
              
       		(CASE RTRIM(FWD."Modalidad") || '/'|| FWD."Accion" 	WHEN 'COMPENSADA/COMPRA' THEN '0001-01-01' 											--03
       		 													WHEN 'COMPENSADA/VENTA'  THEN COALESCE(FWD."FechaEntregamos",FWD."FechaRecibimos")  --04
       		 ELSE (COALESCE(FWD."FechaEntregamos",FWD."FechaRecibimos") )
       		END)
        
       ELSE (COALESCE(FWD."FechaEntregamos",FWD."FechaRecibimos") )
       END
       
       AS "FechaValutaMonedaEntregada",
       CAST(FWD."IdOperacion" as NVARCHAR(15))  AS "Identificador",
      FWD."RUTContraparte1"    AS "RUTContraparte1",
    FWD."LEIContraparte1"    AS "LEIContraparte1",
    FWD."NombreContraparte1"  AS "NombreContraparte1",  
    FWD."RUTContraparte2"   AS "RUTContraparte2",
    FWD."LEIContraparte2"   AS "LEIContraparte2",
    FWD."NombreContraparte2"  AS "NombreContraparte2",
    COALESCE(HPAIS."ValorDestino", 'CHL')   AS "PaisContraparte2",       
       ''         AS "RUTIntermediario",
    FWD."PlataformaNegociacion"     	  AS "PlataformaNegociacion",
       COALESCE("HMON1"."ValorDestino",FWD."CodigoOperacionCambio")  AS "CodigoOperacionCambio",
    FWD."PaisMonedaExtranjera"        AS "PaisMonedaExtranjera",
    CASE FWD."Accion" WHEN 'COMPRA' THEN FWD."IdMonedaPrincipal"  ELSE FWD."IdMonedaSecundaria"   END  AS "MonedaCompradaRecibida",
    CASE FWD."Accion" WHEN 'COMPRA' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria"  END  AS "MontoMonedaCompradaRecibida",
    CASE FWD."Accion" WHEN 'VENTA'  THEN FWD."IdMonedaPrincipal"  ELSE FWD."IdMonedaSecundaria"   END  AS "MonedaVendidaEntregada",
    CASE FWD."Accion" WHEN 'VENTA'  THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria"  END   AS "MontoMonedaVendidaEntregada",
    FWD."IdMonedaPrincipal"  || '/' || FWD."IdMonedaSecundaria" AS "ConvencionTipoCambio",   
    FWD."TipoCambioCierre"   AS "TipoCambio",
    FWD."AfectaDerivados"    AS "AfectaDerivados",
    CASE FWD."TipoOrigen" WHEN  'FWD' THEN CAST(FWD."IdOperacion" as NVARCHAR(15)) ELSE '' END   AS "IdentificadorContratoDerivados",
    CASE FWD."TipoOrigen" WHEN  'FWD' THEN FWD."FechaNegociacion" ELSE '0001-01-01' END   AS "FechaSuscripcionContrato",
    CASE FWD."TipoOrigen" WHEN  'FWD' THEN FWD."HoraNegociacion" ELSE '' END   AS "HoraSuscripcionContrato",
    FWD."OperacionEmpresaRelacionada"  AS "OperacionEmpresaRelacionada",
    ''         AS "NumeroInscripcionObligacionExterior",
    ''         AS "NombreFinancistaAporante",
    ''         AS "LEIFinancistaAporante",
    ''         AS "NombreReceptorInversion",
    ''         AS "LEIReceptorInversion",
    ''         AS "ModalidadInversionCustodio",
     --campos de control interno especifico
    FWD."FolioOperacion"   AS  "FolioOperacion",
       FWD."IdOperacion"    AS  "IdOperacion",
    FWD."IdCartera"     AS  "IdCartera",
    FWD."FechaLiquidacion"   AS  "FechaLiquidacion",
    FWD."IdCliente"     AS  "IdCliente",
    FWD."IdTipoOperacion"   AS  "IdTipoOperacion",
    FWD."Nemo"      AS  "Nemo",
    FWD."IdMonedaPrincipal"   AS  "IdMonedaPrincipal",
    FWD."IdMonedaSecundaria"  AS  "IdMonedaSecundaria",
	''       	AS "Observaciones"
  FROM(
     ---OPERACIONES NUEVAS 
     SELECT '1' AS "EventoReporte",OPE.* 
     FROM   "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.PreOrigen" AS OPE
     WHERE 
  
      NOT EXISTS  (SELECT 1 FROM 
          "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos" DAT WHERE  DAT."FuenteDatos"='INICIAL' AND  DAT."EstadoSICAM" IN ('ENV','RZD','REC')
          AND TO_DATE(OPE."FechaNegociacion") =TO_DATE(DAT."FechaNegociacion")
          AND OPE."RUTContraparte2" = DAT."RUTContraparte2"
          AND CAST(OPE."FolioOperacion" as NVARCHAR(15))= DAT."Identificador"
          AND OPE."IdTipoOperacion"=DAT."IdTipoOperacion"
         )
     ---OPERACIONES MODIFICADAS 
     UNION ALL
     SELECT '2' AS "EventoReporte",OPE.* 
     FROM   "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.PreOrigen" AS OPE
     WHERE 
       EXISTS  (SELECT 1 FROM 
          "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos" DAT WHERE  DAT."FuenteDatos"='INICIAL' AND  DAT."EstadoSICAM" IN ('ENV','RZD','REC')
          AND TO_DATE(OPE."FechaNegociacion") =TO_DATE(DAT."FechaNegociacion")
          AND OPE."RUTContraparte2" = DAT."RUTContraparte2"
          AND CAST(OPE."IdOperacion" as NVARCHAR(15))= DAT."Identificador"
          AND OPE."IdTipoOperacion"=DAT."IdTipoOperacion"
          AND (OPE."HoraNegociacion" <> DAT."HoraNegociacion" 
           OR 
            OPE."NombreContraparte2" <> DAT."NombreContraparte2" 
           OR
            OPE."LEIContraparte2" <> DAT."LEIContraparte2"
           OR
            OPE."TipoCambioCierre" <> DAT."TipoCambio" 
              )
         )
       
         
       )AS FWD 
     LEFT JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" AS "HMON1" 
     ON ("HMON1"."Esquema"='REPO2P1' AND "HMON1"."Tipo"='PLANILLABANCOCENTRAL' 
     AND ltrim(rtrim("HMON1"."ValorOrigen"))=ltrim(rtrim(FWD."CodigoOperacionCambio"))
     AND TO_DATE(FWD."FechaNegociacion") BETWEEN TO_DATE(HMON1."FechaDatosDesde") AND TO_DATE(HMON1."FechaDatosHasta"))  
        
     LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON PAIS."IdPais"=FWD."IdPais"   
     LEFT JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
          ON (HPAIS."Esquema" = 'REPO2P1' AND TRIM(PAIS."CodigoPais") = TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo" = 'CODPAIS' AND  TO_DATE(FWD."FechaNegociacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))
        
        
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Origen" TO bcs_desarrollador WITH GRANT OPTION;
 