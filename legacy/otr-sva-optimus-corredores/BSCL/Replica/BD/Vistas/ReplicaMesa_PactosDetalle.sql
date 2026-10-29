SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_PactosDetalle"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_PactosDetalle" AS

        Select WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdReservaDetalle")  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdReservaDetalle" AS "IDRESERVADETALLE",
				"IdConceptoDetalle" AS "IDCONCEPTODETALLE",
				"CarteraNominal" AS "CARTERANOMINAL",
				"FolioCarteraDetalle" AS "FOLIOCARTERADETALLE",
				"FolioCarteraCustodiaDetalle" AS "FOLIOCARTERACUSTODIADETALLE",
				"IdEmisorDetalle" AS "IDEMISORDETALLE",
				"IdTipoReajusteDetalle" AS "IDTIPOREAJUSTEDETALLE",
				"IdMonedaDetalle" AS "IDMONEDADETALLE",
				"IdMonedaEmisionInstrumentoDetalle" AS "IDMONEDAEMISIONINSTRUMENTODETALLE",
				"IdMonedaLiquidacionInstrumentoDetalle" AS "IDMONEDALIQUIDACIONINSTRUMENTODETALLE",
				"CantidadDetalle" AS "CANTIDADDETALLE",
				"IdInstrumentoDetalle" AS "IDINSTRUMENTODETALLE",
				"NemotecnicoDetalle" AS "NEMOTECNICODETALLE",
				"IdClasificacionTipoInstrumentoDetalle" AS "IDCLASIFICACIONTIPOINSTRUMENTODETALLE",
				"IdTipoInstrumentoDetalle" AS "IDTIPOINSTRUMENTODETALLE",
				"PrecioCompraDetalle" AS "PRECIOCOMPRADETALLE",
				"PrecioMercadoDetalle" AS "PRECIOMERCADODETALLE",
				"TasaCompraDetalle" AS "TASACOMPRADETALLE",
				"TasaMercadoDetalle" AS "TASAMERCADODETALLE",
				"MontoCompraDetalle" AS "MONTOCOMPRADETALLE",
				"MontoLiquidacionDetalle" AS "MONTOLIQUIDACIONDETALLE",
				"EsFungibleDetalle" AS "ESFUNGIBLEDETALLE",
				"ValorRescateDetalle" AS "VALORRESCATEDETALLE",
				"IdOperacionDetalle" AS "IDOPERACIONDETALLE",
				"IdBolsaDetalle" AS "IDBOLSADETALLE",
				"IdBovedaDetalle" AS "IDBOVEDADETALLE",
				"CodigoIFRSDetalle" AS "CODIGOIFRSDETALLE",
				"CodigoDescIFRSDetalle" AS "CODIGODESCIFRSDETALLE",
				"NombreBovedaDetalle" AS "NOMBREBOVEDADETALLE",
				"ClasificacionTipoInstrumentoDetalle" AS "CLASIFICACIONTIPOINSTRUMENTODETALLE",
				"MontoValorizadoReserva" AS "MONTOVALORIZADORESERVA",
				"MontoOperacionFinal" AS "MONTOOPERACIONFINAL"

        from "BSCL.Mesa.BD.Vistas::MRF.ConsultarPactosDetalleAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_PactosDetalle' AND WST."Key" = "IdReservaDetalle"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_PactosDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
