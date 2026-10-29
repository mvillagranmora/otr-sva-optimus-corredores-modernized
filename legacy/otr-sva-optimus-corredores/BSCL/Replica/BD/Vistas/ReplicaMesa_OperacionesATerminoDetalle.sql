SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATerminoDetalle"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATerminoDetalle" AS
	Select WST."IdStack" AS "IDSTACK", 
		TO_VARCHAR("IdReservaDetalle")  AS TABLE_KEY,
		"Action" AS ACTION,
		"IdReservaDetalle" AS "IDRESERVADETALLE",												--INT,
		"IdConceptoDetalle" AS "IDCONCEPTODETALLE",												--NVARCHAR(6),
		"CarteraNominal" AS "CARTERANOMINAL",													--NVARCHAR(1) NOT NULL,
		"FolioCarteraDetalle" AS "FOLIOCARTERADETALLE",											--INT DEFAULT 0,
		"FolioCarteraCustodiaDetalle" AS "FOLIOCARTERACUSTODIADETALLE",							--INT DEFAULT 0,
		"IdEmisorDetalle" AS "IDEMISORDETALLE",													--NVARCHAR(40),
		"IdTipoReajusteDetalle" AS "IDTIPOREAJUSTEDETALLE",										--NVARCHAR(3),
		"IdMonedaDetalle" AS "IDMONEDADETALLE",													--NVARCHAR(3),
		"IdMonedaEmisionInstrumentoDetalle" AS "IDMONEDAEMISIONINSTRUMENTODETALLE",				--NVARCHAR(3),
		"IdMonedaLiquidacionInstrumentoDetalle" AS "IDMONEDALIQUIDACIONINSTRUMENTODETALLE",		--NVARCHAR(3),
		"CantidadDetalle" AS "CANTIDADDETALLE",													--DECIMAL(26,8) DEFAULT 0,
		"IdInstrumentoDetalle" AS "IDINSTRUMENTODETALLE",										--INT DEFAULT 0, 
		"NemotecnicoDetalle" AS "NEMOTECNICODETALLE",											--NVARCHAR(40),
		"NemoSVS" AS "NEMOSVS",																	--NVARCHAR(40), 
		"IdClasificacionTipoInstrumentoDetalle" AS "IDCLASIFICACIONTIPOINSTRUMENTODETALLE",		--NVARCHAR(3), 
		"IdTipoInstrumentoDetalle" AS "IDTIPOINSTRUMENTODETALLE",								--NVARCHAR(3), 
		"PrecioCompraDetalle" AS "PRECIOCOMPRADETALLE",											--DECIMAL(26,8) DEFAULT 0,
		"PrecioMercadoDetalle" AS "PRECIOMERCADODETALLE",										--DECIMAL(26,8) DEFAULT 0,
		"TasaCompraDetalle" AS "TASACOMPRADETALLE",												--DECIMAL(26,8) DEFAULT 0,
		"TasaMercadoDetalle" AS "TASAMERCADODETALLE",											--DECIMAL(26,8) DEFAULT 0,
		"MontoCompraDetalle" AS "MONTOCOMPRADETALLE",											--DECIMAL(26,8) DEFAULT 0,
		"MontoLiquidacionDetalle" AS "MONTOLIQUIDACIONDETALLE",									--DECIMAL(26,8) DEFAULT 0,
		"EsFungibleDetalle" AS "ESFUNGIBLEDETALLE",												--NVARCHAR(1),
		"ValorRescateDetalle" AS "VALORRESCATEDETALLE",											--DECIMAL(26,8) DEFAULT 0,
		"IdOperacionDetalle" AS "IDOPERACIONDETALLE",											--INT DEFAULT 0,
		"IdBolsaDetalle" AS "IDBOLSADETALLE",													--NVARCHAR(3),
		"IdBovedaDetalle" AS "IDBOVEDADETALLE",													--NVARCHAR(3),
		"CodigoIFRSDetalle" AS "CODIGOIFRSDETALLE",												--NVARCHAR(6),
		"CodigoDescIFRSDetalle" AS "CODIGODESCIFRSDETALLE",										--NVARCHAR(100),
		"NombreBovedaDetalle" AS "NOMBREBOVEDADETALLE",											--NVARCHAR(100),
		"ClasificacionTipoInstrumentoDetalle" AS "CLASIFICACIONTIPOINSTRUMENTODETALLE",			--NVARCHAR(100),
		"MontoValorizadoReserva" AS "MONTOVALORIZADORESERVA",									--DECIMAL(26,8) DEFAULT 0,
		"MontoOperacionFinal" AS "MONTOOPERACIONFINAL"											--DECIMAL(26,8) DEFAULT 0 NOT NULL
	from "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperacionesATerminoDetalleAnalitica"
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_OperacionesATerminoDetalle' AND WST."Key" = "IdReservaDetalle"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATerminoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
