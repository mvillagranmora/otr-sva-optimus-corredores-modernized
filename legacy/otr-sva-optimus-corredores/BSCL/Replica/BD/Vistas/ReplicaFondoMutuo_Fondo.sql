SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Fondo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Fondo" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdFondo"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdFondo"  AS "IDFONDO", -- VARCHAR(40)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "IdPaisEmisor"  AS "IDPAISEMISOR", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "NombreFondo"  AS "NOMBREFONDO", -- VARCHAR(100)
                  "IdPaisOrigen"  AS "IDPAISORIGEN", -- BIGINT
                  "CodigoSVS"  AS "CODIGOSVS", -- VARCHAR(40)
                  "MoneyMarket"  AS "MONEYMARKET", -- VARCHAR(1)
                  CASE WHEN YEAR("FechaCreacion") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaCreacion" END AS "FECHACREACION", -- DATETIME
                  CASE WHEN YEAR("FechaConstitucion") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaConstitucion" END AS "FECHACONSTITUCION", -- DATETIME
                  CASE WHEN YEAR("FechaCierre") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaCierre" END AS "FECHACIERRE", -- DATETIME
                  "Vigencia"  AS "VIGENCIA", -- VARCHAR(1)
                  "FondoExtranjero"  AS "FONDOEXTRANJERO", -- VARCHAR(1)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "HoraCierreAporteComercial"  AS "HORACIERREAPORTECOMERCIAL", -- VARCHAR(8)
                  "HoraCierreRescateComercial"  AS "HORACIERRERESCATECOMERCIAL", -- VARCHAR(8)
                  "HoraCierreReinversionComercial"  AS "HORACIERREREINVERSIONCOMERCIAL", -- VARCHAR(8)
                  "HoraCierreLiquidezComercial"  AS "HORACIERRELIQUIDEZCOMERCIAL", -- VARCHAR(8)
                  "HoraCierreAporteBackoffice"  AS "HORACIERREAPORTEBACKOFFICE", -- VARCHAR(8)
                  "HoraCierreRescateBackoffice"  AS "HORACIERRERESCATEBACKOFFICE", -- VARCHAR(8)
                  "HoraCierreReinversionBackoffice"  AS "HORACIERREREINVERSIONBACKOFFICE", -- VARCHAR(8)
                  "HoraCierreLiquidezBackoffice"  AS "HORACIERRELIQUIDEZBACKOFFICE", -- VARCHAR(8)
                  "EstadoAporteComercial"  AS "ESTADOAPORTECOMERCIAL", -- VARCHAR(1)
                  "EstadoRescateComercial"  AS "ESTADORESCATECOMERCIAL", -- VARCHAR(1)
                  "EstadoReinversionComercial"  AS "ESTADOREINVERSIONCOMERCIAL", -- VARCHAR(1)
                  "EstadoLiquidezComercial"  AS "ESTADOLIQUIDEZCOMERCIAL", -- VARCHAR(1)
                  "EstadoAporteBackoffice"  AS "ESTADOAPORTEBACKOFFICE", -- VARCHAR(1)
                  "EstadoRescateBackoffice"  AS "ESTADORESCATEBACKOFFICE", -- VARCHAR(1)
                  "EstadoReinversionBackoffice"  AS "ESTADOREINVERSIONBACKOFFICE", -- VARCHAR(1)
                  "EstadoLiquidezBackoffice"  AS "ESTADOLIQUIDEZBACKOFFICE", -- VARCHAR(1)
                  "BloqueoAporte"  AS "BLOQUEOAPORTE", -- VARCHAR(1)
                  "BloqueoRescate"  AS "BLOQUEORESCATE", -- VARCHAR(1)
                  "BloqueoIndefinido"  AS "BLOQUEOINDEFINIDO", -- VARCHAR(1)
                  CASE WHEN YEAR("FechaBloqueoDesde") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaBloqueoDesde" END  AS "FECHABLOQUEODESDE", -- DATETIME
                  CASE WHEN YEAR("FechaBloqueoHasta") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaBloqueoHasta" END AS "FECHABLOQUEOHASTA", -- DATETIME
                  CASE WHEN YEAR("FechaFondo") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaFondo" END AS "FECHAFONDO", -- DATETIME
                  "DocumentoIdentidadFondo"  AS "DOCUMENTOIDENTIDADFONDO", -- VARCHAR(40)
                  "TipoFondo"  AS "TIPOFONDO", -- VARCHAR(40)
                  "CategoriaPerfilFondoMutuo"  AS "CATEGORIAPERFILFONDOMUTUO" -- VARCHAR(100)

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_Fondo' AND WST."Key" = "IdFondo"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Fondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
