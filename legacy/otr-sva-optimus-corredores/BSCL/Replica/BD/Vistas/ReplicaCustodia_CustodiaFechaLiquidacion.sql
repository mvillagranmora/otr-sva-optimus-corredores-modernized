SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacion" AS

        Select WST."IdStack" AS "IDSTACK", 
                  "FechaCustodia" || ':' ||  "IdPortafolio" || ':' || "Nemotecnico"  AS TABLE_KEY,
                  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "TipoFechaConsulta"  AS "TIPOFECHACONSULTA", -- VARCHAR(3)
                  "FechaCustodia"  AS "FECHACUSTODIA", -- VARCHAR(10)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreClasificacionTipoInstrumento"  AS "NOMBRECLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "ISIN"  AS "ISIN", -- VARCHAR(40)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "EsFungible"  AS "ESFUNGIBLE", -- VARCHAR(1)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdBoveda"  AS "IDBOVEDA", -- VARCHAR(3)
                  "NumeroCuentaBoveda"  AS "NUMEROCUENTABOVEDA", -- VARCHAR(40)
                  "Relacion"  AS "RELACION", -- VARCHAR(100)
                  "DescripcionRelacion"  AS "DESCRIPCIONRELACION", -- VARCHAR(100)
                  "CantidadCustodia"  AS "CANTIDADCUSTODIA", -- DECIMAL
                  "PPPCustodia"  AS "PPPCUSTODIA", -- DECIMAL
                  "ValorPPPCustodia"  AS "VALORPPPCUSTODIA", -- DECIMAL
                  "MontoPPPCustodia"  AS "MONTOPPPCUSTODIA", -- DECIMAL
                  "PrecioMercadoCustodia"  AS "PRECIOMERCADOCUSTODIA", -- DECIMAL
                  "Rentabilidad"  AS "RENTABILIDAD", -- DECIMAL
                  "ValorMercadoCustodia"  AS "VALORMERCADOCUSTODIA", -- DECIMAL
                  "MontoMercadoCustodia"  AS "MONTOMERCADOCUSTODIA", -- DECIMAL
                  "DiferenciaMercadoCustodia"  AS "DIFERENCIAMERCADOCUSTODIA", -- DECIMAL
                  "IdMonedaReajuste"  AS "IDMONEDAREAJUSTE", -- VARCHAR(3)
                  "EsPagaderoEn"  AS "ESPAGADEROEN", -- VARCHAR(3)
                  "CantidadSimulCom"  AS "CANTIDADSIMULCOM", -- DECIMAL
                  "PPPSimulCom"  AS "PPPSIMULCOM", -- DECIMAL
                  "ValorPPPSimulCom"  AS "VALORPPPSIMULCOM", -- DECIMAL
                  "PrecioMercadoSimulCom"  AS "PRECIOMERCADOSIMULCOM", -- DECIMAL
                  "ValorMercadoSimulCom"  AS "VALORMERCADOSIMULCOM", -- DECIMAL
                  "DiferenciaMercadoSimulCom"  AS "DIFERENCIAMERCADOSIMULCOM", -- DECIMAL
                  "CantidadSimulVen"  AS "CANTIDADSIMULVEN", -- DECIMAL
                  "PPPSimulVen"  AS "PPPSIMULVEN", -- DECIMAL
                  "ValorPPPSimulVen"  AS "VALORPPPSIMULVEN", -- DECIMAL
                  "PrecioMercadoSimulVen"  AS "PRECIOMERCADOSIMULVEN", -- DECIMAL
                  "ValorMercadoSimulVen"  AS "VALORMERCADOSIMULVEN", -- DECIMAL
                  "DiferenciaMercadoSimulVen"  AS "DIFERENCIAMERCADOSIMULVEN", -- DECIMAL
                  "CantidadPrestamo"  AS "CANTIDADPRESTAMO", -- DECIMAL
                  "PPPPrestamo"  AS "PPPPRESTAMO", -- DECIMAL
                  "ValorPPPPrestamo"  AS "VALORPPPPRESTAMO", -- DECIMAL
                  "PrecioMercadoPrestamo"  AS "PRECIOMERCADOPRESTAMO", -- DECIMAL
                  "ValorMercadoPrestamo"  AS "VALORMERCADOPRESTAMO", -- DECIMAL
                  "DiferenciaMercadoPrestamo"  AS "DIFERENCIAMERCADOPRESTAMO", -- DECIMAL
                  "CantidadEmpresti"  AS "CANTIDADEMPRESTI", -- DECIMAL
                  "PPPEmpresti"  AS "PPPEMPRESTI", -- DECIMAL
                  "ValorPPPEmpresti"  AS "VALORPPPEMPRESTI", -- DECIMAL
                  "PrecioMercadoEmpresti"  AS "PRECIOMERCADOEMPRESTI", -- DECIMAL
                  "ValorMercadoEmpresti"  AS "VALORMERCADOEMPRESTI", -- DECIMAL
                  "DiferenciaMercadoEmpresti"  AS "DIFERENCIAMERCADOEMPRESTI", -- DECIMAL
                  "CantidadGarantia"  AS "CANTIDADGARANTIA", -- DECIMAL
                  "PPPGarantia"  AS "PPPGARANTIA", -- DECIMAL
                  "ValorPPPGarantia"  AS "VALORPPPGARANTIA", -- DECIMAL
                  "PrecioMercadoGarantia"  AS "PRECIOMERCADOGARANTIA", -- DECIMAL
                  "ValorMercadoGarantia"  AS "VALORMERCADOGARANTIA", -- DECIMAL
                  "DiferenciaMercadoGarantia"  AS "DIFERENCIAMERCADOGARANTIA", -- DECIMAL
                  "CantidadTransIng"  AS "CANTIDADTRANSING", -- DECIMAL
                  "CantidadTransEgr"  AS "CANTIDADTRANSEGR", -- DECIMAL
                  "TotalPosicion"  AS "TOTALPOSICION", -- DECIMAL
                  "TotalPosicionPresunta"  AS "TOTALPOSICIONPRESUNTA", -- DECIMAL
                  "CantidadPactosV"  AS "CANTIDADPACTOSV", -- DECIMAL
                  "PPPPactosV"  AS "PPPPACTOSV", -- DECIMAL
                  "ValorPPPPactosV"  AS "VALORPPPPACTOSV", -- DECIMAL
                  "PrecioMercadoPactosV"  AS "PRECIOMERCADOPACTOSV", -- DECIMAL
                  "ValorMercadoPactosV"  AS "VALORMERCADOPACTOSV", -- DECIMAL
                  "DiferenciaMercadoPactosV"  AS "DIFERENCIAMERCADOPACTOSV", -- DECIMAL
                  "CantidadPactosC"  AS "CANTIDADPACTOSC", -- DECIMAL
                  "PPPPactosC"  AS "PPPPACTOSC", -- DECIMAL
                  "ValorPPPPactosC"  AS "VALORPPPPACTOSC", -- DECIMAL
                  "PrecioMercadoPactosC"  AS "PRECIOMERCADOPACTOSC", -- DECIMAL
                  "ValorMercadoPactosC"  AS "VALORMERCADOPACTOSC", -- DECIMAL
                  "DiferenciaMercadoPactosC"  AS "DIFERENCIAMERCADOPACTOSC", -- DECIMAL
                  "IdMatrizAgrupador"  AS "IDMATRIZAGRUPADOR", -- BIGINT
                  "EsADC"  AS "ESADC", -- VARCHAR(1)
				  "FechaFuentePrecioMercadoCustodia" AS "FECHAFUENTEPRECIOMERCADOCUSTODIA",
				  "IdRegimenFondoMutuo" AS "IDREGIMENFONDOMUTUO" --NVARCHAR(40)

        from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaFechaLiquidacionAnalitica"
		--from "BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion" cus
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_CustodiaFechaLiquidacion'
        AND WST."Key" = ("FechaCustodia" || ':' || "IdPortafolio" || ':' || "Nemotecnico")
        --and 1 = 2
		--where "FechaCustodia" = '2022-05-19'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;