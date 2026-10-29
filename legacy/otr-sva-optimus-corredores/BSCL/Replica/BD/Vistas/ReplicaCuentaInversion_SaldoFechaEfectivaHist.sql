SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectivaHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectivaHist" AS

        Select WST."IdStack" AS "IDSTACK", 
			       "IdCuentaInversion" ||':'|| "FechaSaldo" AS TABLE_KEY,
			       "Action" AS ACTION,
                  "IdEjecutivo"  AS "IDEJECUTIVO", -- BIGINT
                  "NombreEjecutivo"  AS "NOMBREEJECUTIVO", -- VARCHAR(300)
                  "CodigoSucursal"  AS "CODIGOSUCURSAL", -- VARCHAR(40)
                  "NombreSucursal"  AS "NOMBRESUCURSAL", -- VARCHAR(100)
                  "IdCuentaInversion"  AS "IDCUENTAINVERSION", -- BIGINT
                  "CodigoCuentaInversion"  AS "CODIGOCUENTAINVERSION", -- VARCHAR(40)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(300)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "NombrePortafolio"  AS "NOMBREPORTAFOLIO", -- VARCHAR(100)
                  "PropositoPortafolio"  AS "PROPOSITOPORTAFOLIO", -- VARCHAR(100)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "FechaSaldo"  AS "FECHASALDO", -- VARCHAR(10)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "CodigoMoneda"  AS "CODIGOMONEDA", -- VARCHAR(40)
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "SaldoDisponible"  AS "SALDODISPONIBLE", -- DECIMAL
                  "SaldoEnGarantia"  AS "SALDOENGARANTIA", -- DECIMAL
                  "SaldoAPlazo"  AS "SALDOAPLAZO", -- DECIMAL
                  "SaldoRetiro"  AS "SALDORETIRO", -- DECIMAL
                  "SaldoRetenido" AS "SALDORETENIDO", -- DECIMAL
                  "Producto"  AS "PRODUCTO", -- VARCHAR(100)
                  "IdMatrizAgrupador"  AS "IDMATRIZAGRUPADOR", -- BIGINT
                  "EsADC"  AS "ESADC", -- VARCHAR(1)
				  "Checksum" AS "CHECKSUM" --BIGINT
        from "BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
        		ON  WST."Table" = 'CuentaInversion_SaldoFechaEfectivaHist' 
        		AND WST."Key" = "IdCuentaInversion" ||':'|| "FechaSaldo"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectivaHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
