SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_MovimientoCarteraEgresos"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_MovimientoCarteraEgresos" AS

        SELECT
        	WST."IdStack" AS "IDSTACK", 
        	TO_VARCHAR("IdCartera")  AS TABLE_KEY,
        	"Action" AS ACTION,
			"IdEmpresa" AS "IDEMPRESA",
			"IdCartera" AS "IDCARTERA",
			"FechaOperacion" AS "FECHAOPERACION",
			"FechaRegistro" AS "FECHAREGISTRO",
			"TipoOperacion" AS "TIPOOPERACION",
			"IdCliente" AS "IDCLIENTE",
			"IdPortafolio" AS "IDPORTAFOLIO",
			"IdConcepto" AS "IDCONCEPTO",
			"IdClasificacionTipoInstrumento" AS "IDCLASIFICACIONTIPOINSTRUMENTO",
			"IdTipoInstrumento" AS "IDTIPOINSTRUMENTO",
			"IdInstrumento" AS "IDINSTRUMENTO",
			"Nemotecnico" AS "NEMOTECNICO",
			"ISIN" AS "ISIN",
			"IdEmisor" AS "IDEMISOR",
			"FechaVencimientoInstrumento" AS "FECHAVENCIMIENTOINSTRUMENTO",
			"FechaLiquidacion" AS "FECHALIQUIDACION",
			"IdTipoReajuste" AS "IDTIPOREAJUSTE",
			"IdMonedaInstrumento" AS "IDMONEDAINSTRUMENTO",
			"IdBoveda" AS "IDBOVEDA",
			"Cantidad" AS "CANTIDAD",
			"Precio" AS "PRECIO",
			"Tasa" AS "TASA",
			"Monto" AS "MONTO",
			"ValorRescate" AS "VALORRESCATE",
			"SaldoCartera" AS "SALDOCARTERA",
			"FolioCartera" AS "FOLIOCARTERA",
			"IdTipoComprobante" AS "IDTIPOCOMPROBANTE",
			"FolioComprobante" AS "FOLIOCOMPROBANTE",
			"FechaComprobante" AS "FECHACOMPROBANTE",
			"IdEstado" AS "IDESTADO",
			"EGRSpread" AS "EGRSPREAD",
			"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
			"RentabilidadReal" AS "RENTABILIDADREAL",
			"CostoUF" AS "COSTOUF",
			"IdTipoMovimientoFondoMutuo" AS "IDTIPOMOVIMIENTOFONDOMUTUO",
			"IdTipoOperacion" AS "IDTIPOOPERACION",
			"IdMatrizAgrupador" AS "IDMATRIZAGRUPADOR",
			"EsADC" AS "ESADC",
			"FechaFuente" AS "FECHAFUENTE",
			"ExcluyeMovimientoEnCartola" AS "EXCLUYEMOVIMIENTOENCARTOLA"

        FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_MovimientoCarteraEgresos' AND WST."Key" = "IdCartera"
        --and 1 = 2
        --where "FechaOperacion" >= '2020-05-10'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_MovimientoCarteraEgresos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
