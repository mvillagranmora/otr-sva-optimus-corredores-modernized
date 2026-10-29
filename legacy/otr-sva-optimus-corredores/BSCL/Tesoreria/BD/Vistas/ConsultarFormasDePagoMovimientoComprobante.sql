SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS


SELECT FP."IdInterno",
       FP."Tipo",  -- IngresoEgreso | Comprobante
       FP."IdChequera",
       FP."NumeroSerieSocio",
       FP."NumeroDocumentoSocio",
       FP."FechaDocumentoPagoSocio",
       BcoSoc."IdBanco" AS "IdBancoSocio",
       CtaCorCorredora."IdCuentaBanco" AS "IdCuentaBancoCorredora",
       CtaCorCorredora."NombreCuentaBancaria" AS "AliasBanco",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."IdMedioDePago"         ELSE 'S/I' END as "IdMedioDePago",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NombreMedioDePago"     ELSE CT."Nombre" END as "NombreMedioDePago",       
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."IdModalidadDePago"     ELSE 'S/I' END as "IdModalidadDePago",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NombreModalidadDePago" ELSE '' END as "NombreModalidadDePago",

       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN BcoCor."NombreBanco"                   ELSE '' END as "NombreBancoCorredora",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NumeroCuentaBancariaCorredora"     ELSE '' END as "NumeroCuentaBancariaCorredora",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NombreTipoCuentaBancariaCorredora" ELSE '' END as "NombreTipoCuentaBancariaCorredora",
       
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN BcoSoc."NombreBanco"                   ELSE '' END as "NombreBancoSocio",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NumeroCuentaBancariaSocio"         ELSE '' END as "NumeroCuentaBancariaSocio",
       CASE WHEN FP."IdConceptoMediosDePago" NOT IN ('MZCLMP','SINMDP') THEN FP."NombreTipoCuentaBancariaSocio"     ELSE '' END as "NombreTipoCuentaBancariaSocio"
                
FROM (
        SELECT IE."IdIngresoEgreso" as "IdInterno",
               'IngresoEgreso' as "Tipo",
                CASE 
                    WHEN COALESCE(FP."IdConceptoMediosDePago",'') = '' THEN 'SINMDP'  -- Sin Medio de pago
                    ELSE FP."IdConceptoMediosDePago"
                END as "IdConceptoMediosDePago",
                FP."IdMedioDePago",
                FP."NombreMedioDePago",
                FP."IdModalidadDePago",
                FP."NombreModalidadDePago",
                FP."NumeroCuentaBancariaCorredora",
                FP."NombreTipoCuentaBancariaCorredora",
                FP."IdCuentaBancariaCorredora",
                FP."NumeroCuentaBancariaSocio",
                FP."NombreTipoCuentaBancariaSocio",
                FP."NumeroSerieSocio",
                FP."NumeroDocumentoSocio",
                FP."FechaDocumentoPagoSocio",
                FP."IdBancoSocio", 
                FP."IdChequera"
          FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE
          LEFT JOIN (SELECT "IdIngresoEgreso",        
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdMedioDePago")                   ELSE '' END) AS "IdMedioDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreMedioDePago")               ELSE '' END) AS "NombreMedioDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdModalidadDePago")               ELSE '' END) AS "IdModalidadDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreModalidadDePago")           ELSE '' END) AS "NombreModalidadDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroCuentaBancariaCorredora")     ELSE '' END) AS "NumeroCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreTipoCuentaBancariaCorredora") ELSE '' END) AS "NombreTipoCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdCuentaBancariaCorredora")         ELSE 0  END) AS "IdCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdChequera")                      ELSE 0  END) AS "IdChequera",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroCuentaBancariaSocio")       ELSE '' END) AS "NumeroCuentaBancariaSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreTipoCuentaBancariaSocio")   ELSE '' END) AS "NombreTipoCuentaBancariaSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdBancoSocio")                    ELSE 0  END) AS "IdBancoSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroSerieSocio")                ELSE '' END) AS "NumeroSerieSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroDocumentoSocio")            ELSE 0  END) AS "NumeroDocumentoSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("FechaDocumentoPagoSocio")           ELSE '0001-01-01' END) AS "FechaDocumentoPagoSocio",
                            CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdMedioDePago") 
                                 WHEN COUNT(DISTINCT "IdMedioDePago") > 1 THEN 'MZCLMP'  -- Mezcla de medios de pago(+ de 1)
                            END as "IdConceptoMediosDePago"
                        FROM  "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
                       WHERE "IdEstado" NOT IN ('MOD')
                       GROUP BY "IdIngresoEgreso") as FP
                 ON FP."IdIngresoEgreso" = IE."IdIngresoEgreso"

                 
        UNION ALL             
        
        
        SELECT 
                IE."IdComprobante" as "IdInterno",
                'Comprobante' as "Tipo",
                CASE 
                    WHEN COALESCE(FP."IdConceptoMediosDePago",'') = '' THEN 'SINMDP'  -- Sin Medio de pago
                    ELSE FP."IdConceptoMediosDePago"
                END as "IdConceptoMediosDePago",
                
                FP."IdMedioDePago",
                FP."NombreMedioDePago",
                FP."IdModalidadDePago",
                FP."NombreModalidadDePago",
                FP."NumeroCuentaBancariaCorredora",
                FP."NombreTipoCuentaBancariaCorredora",
                FP."IdCuentaBancariaCorredora",
                FP."NumeroCuentaBancariaSocio",
                FP."NombreTipoCuentaBancariaSocio",
                FP."NumeroSerieSocio",
                FP."NumeroDocumentoSocio",
                FP."FechaDocumentoPagoSocio",
                FP."IdBancoSocio",
                FP."IdChequera"
          FROM "BSCL.Tesoreria.BD::TES.Comprobante" as IE
          LEFT JOIN (SELECT
                            "IdComprobante",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdMedioDePago")         ELSE '' END) AS "IdMedioDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreMedioDePago")     ELSE '' END) AS "NombreMedioDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdModalidadDePago")     ELSE '' END) AS "IdModalidadDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreModalidadDePago") ELSE '' END) AS "NombreModalidadDePago",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroCuentaBancariaCorredora")     ELSE '' END) AS "NumeroCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreTipoCuentaBancariaCorredora") ELSE '' END) AS "NombreTipoCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdCuentaBancariaCorredora")         ELSE 0  END) AS "IdCuentaBancariaCorredora",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroCuentaBancariaSocio")         ELSE '' END) AS "NumeroCuentaBancariaSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NombreTipoCuentaBancariaSocio")     ELSE '' END) AS "NombreTipoCuentaBancariaSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdChequera")                        ELSE 0  END) AS "IdChequera",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroSerieSocio")                  ELSE '' END) AS "NumeroSerieSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("NumeroDocumentoSocio")              ELSE 0  END)  AS "NumeroDocumentoSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("FechaDocumentoPagoSocio")           ELSE '' END) AS "FechaDocumentoPagoSocio",
                            (CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdBancoSocio")                      ELSE 0  END) AS "IdBancoSocio",
                            CASE WHEN COUNT(DISTINCT "IdMedioDePago") = 1 THEN MAX("IdMedioDePago") 
                                 WHEN COUNT(DISTINCT "IdMedioDePago") > 1 THEN 'MZCLMP'    -- Mezcla de medios de pago(+ de 1)
                            END as "IdConceptoMediosDePago"
                     FROM  "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
                     WHERE "IdEstado" NOT IN ('MOD')
                     GROUP BY "IdComprobante"
                     ) as FP
                  ON FP."IdComprobante" = IE."IdComprobante"
                  
                  
       ) as FP

  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as CT ON CT."IdConcepto" = FP."IdConceptoMediosDePago"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BcoSoc ON (BcoSoc."IdBanco" = FP."IdBancoSocio")
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CtaCorCorredora ON (CtaCorCorredora."IdCuentaBanco" = FP."IdCuentaBancariaCorredora")
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BcoCor ON (BcoCor."IdBanco" = CtaCorCorredora."IdBanco")
  
         
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
