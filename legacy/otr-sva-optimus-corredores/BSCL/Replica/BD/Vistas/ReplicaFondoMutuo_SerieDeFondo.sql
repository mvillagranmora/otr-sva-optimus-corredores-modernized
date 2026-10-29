SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SerieDeFondo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SerieDeFondo" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdSerieFondo"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdSerieFondo"  AS "IDSERIEFONDO", -- VARCHAR(40)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "IdFondo"  AS "IDFONDO", -- VARCHAR(40)
                  "NombreSerieFondo"  AS "NOMBRESERIEFONDO", -- VARCHAR(100)
                  "CodigoSerie"  AS "CODIGOSERIE", -- VARCHAR(10)
                  "Alias"  AS "ALIAS", -- VARCHAR(40)
                  "DecimalesValorCuota"  AS "DECIMALESVALORCUOTA", -- BIGINT
                  "DecimalesCuota"  AS "DECIMALESCUOTA", -- BIGINT
                  "MontoMinimo"  AS "MONTOMINIMO", -- DECIMAL
                  "MontoMaximo"  AS "MONTOMAXIMO", -- DECIMAL
                  "DiasRetencionesRescate"  AS "DIASRETENCIONESRESCATE", -- BIGINT
                  "ExcluyeGarantia"  AS "EXCLUYEGARANTIA", -- VARCHAR(1)
                  "CobraComision"  AS "COBRACOMISION", -- VARCHAR(1)
                  "Diferido"  AS "DIFERIDO", -- VARCHAR(1)
                  "InstrumentoDCV"  AS "INSTRUMENTODCV", -- VARCHAR(40)
                  "Omnibus"  AS "OMNIBUS", -- VARCHAR(1)
                  "PatrimonioMinimo"  AS "PATRIMONIOMINIMO", -- DECIMAL
                  "IdSerieFondoOrigen"  AS "IDSERIEFONDOORIGEN", -- VARCHAR(40)
                  "Estado"  AS "ESTADO", -- VARCHAR(100)
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "DiasValutaCertificacionAporte"  AS "DIASVALUTACERTIFICACIONAPORTE", -- BIGINT
                  "DiasValutaCertificacionRescate"  AS "DIASVALUTACERTIFICACIONRESCATE", -- BIGINT
                  "DiasValutaCurseAporte"  AS "DIASVALUTACURSEAPORTE", -- BIGINT
                  "DiasValutaCurseRescate"  AS "DIASVALUTACURSERESCATE", -- BIGINT
                  "DiasRetencionesAporte"  AS "DIASRETENCIONESAPORTE", -- BIGINT
                  "DiasValutaValorCuotaAporte"  AS "DIASVALUTAVALORCUOTAAPORTE", -- BIGINT
                  "DiasValutaValorCuotaRescate"  AS "DIASVALUTAVALORCUOTARESCATE", -- BIGINT
                  "CambioFondo"  AS "CAMBIOFONDO", -- VARCHAR(1)
                  "ISIN"  AS "ISIN", -- VARCHAR(40)
                  CASE WHEN YEAR("FechaValorCuota") = 1 THEN CAST('1900-01-01' AS DATE) ELSE "FechaValorCuota" END AS "FECHAVALORCUOTA", -- DATETIME
                  "ValorCuota"  AS "VALORCUOTA", -- DECIMAL
                  "NombreFamilia"  AS "NOMBREFAMILIA", -- VARCHAR(100)
                  "MedioPagoAbono"  AS "MEDIOPAGOABONO", -- VARCHAR(100)
                  "MedioPagoCargo"  AS "MEDIOPAGOCARGO" -- VARCHAR(100)

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_SerieDeFondo' AND WST."Key" = "IdSerieFondo"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SerieDeFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
