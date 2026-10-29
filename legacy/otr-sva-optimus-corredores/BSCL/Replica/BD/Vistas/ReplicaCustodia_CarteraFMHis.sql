SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFMHis"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFMHis" AS

        SELECT 
        	WST."IdStack" AS "IDSTACK", 
        	TO_VARCHAR("IdCartera")  || ':' || TO_VARCHAR("FechaRespaldo", 'YYYY-MM-DD')  AS TABLE_KEY,
        	"Action" AS ACTION,
        	"FechaRespaldo"	AS "FECHARESPALDO",
			"IdEmpresa" AS "IDEMPRESA",
			"IdCartera" AS "IDCARTERA",
			"FolioCartera" AS "FOLIOCARTERA",
			"FechaIngresoRegistro" AS "FECHAINGRESOREGISTRO",
			"FechaModificacionRegistro" AS "FECHAMODIFICACIONREGISTRO",
			"IdCliente" AS "IDCLIENTE",
			"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
			"IdPortafolio" AS "IDPORTAFOLIO",
			"CodigoPortafolio" AS "CODIGOPORTAFOLIO",
			"IdClasificacionTipoInstrumento" AS "IDCLASIFICACIONTIPOINSTRUMENTO",
			"IdTipoInstrumento" AS "IDTIPOINSTRUMENTO",
			"TipoInstrumento" AS "TIPOINSTRUMENTO",
			"IdInstrumento" AS "IDINSTRUMENTO",
			"Nemotecnico" AS "NEMOTECNICO",
			"IdMonedaInstrumento" AS "IDMONEDAINSTRUMENTO",
			"MonedaInstrumento" AS "MONEDAINSTRUMENTO",
			"IdTipoReajuste" AS "IDTIPOREAJUSTE",
			"IdEmisor" AS "IDEMISOR",
			"FechaEmisionInstrumento" AS "FECHAEMISIONINSTRUMENTO",
			"FechaVencimientoInstrumento" AS "FECHAVENCIMIENTOINSTRUMENTO",
			"IdBoveda" AS "IDBOVEDA",
			"FechaOperacionInicial" AS "FECHAOPERACIONINICIAL",
			"CantidadInicial" AS "CANTIDADINICIAL",
			"PrecioCompraInicial" AS "PRECIOCOMPRAINICIAL",
			"MontoCompraInicial" AS "MONTOCOMPRAINICIAL",
			"MontoCompraMonedaInstrumentoInicial" AS "MONTOCOMPRAMONEDAINSTRUMENTOINICIAL",
			"IdCondicionLiquidacionInicial" AS "IDCONDICIONLIQUIDACIONINICIAL",
			"NombreCondicionLiquidacionInicial" AS "NOMBRECONDICIONLIQUIDACIONINICIAL",
			"FechaLiquidacionInicial" AS "FECHALIQUIDACIONINICIAL",
			"FechaOperacion" AS "FECHAOPERACION",
			"EGRFechaOperacion" AS "EGRFECHAOPERACION",
			"EGRFechaLiquidacion" AS "EGRFECHALIQUIDACION",
			"Cantidad" AS "CANTIDAD",
			"PrecioCompra" AS "PRECIOCOMPRA",
			"MontoCompraActualMonedaInstrumento" AS "MONTOCOMPRAACTUALMONEDAINSTRUMENTO",
			"PrecioPromedioCompra" AS "PRECIOPROMEDIOCOMPRA",
			"MontoPrecioPromedioCompra" AS "MONTOPRECIOPROMEDIOCOMPRA",
			"MontoPrecioPromedioMonedaInstrumento" AS "MONTOPRECIOPROMEDIOMONEDAINSTRUMENTO",
			"IdCondicionLiquidacion" AS "IDCONDICIONLIQUIDACION",
			"FechaLiquidacion" AS "FECHALIQUIDACION",
			"PrecioMercadoActual" AS "PRECIOMERCADOACTUAL",
			"MontoMercadoActualMonedaInstrumento" AS "MONTOMERCADOACTUALMONEDAINSTRUMENTO",
			"IdConcepto" AS "IDCONCEPTO",
			"IdTipoOperacion" AS "IDTIPOOPERACION",
			"IdTipoProductoTIPOPE" AS "IDTIPOPRODUCTOTIPOPE",
			"IdTipoComprobante" AS "IDTIPOCOMPROBANTE",
			"FolioComprobante" AS "FOLIOCOMPROBANTE",
			"FechaComprobante" AS "FECHACOMPROBANTE",
			"IdBolsa" AS "IDBOLSA",
			"IdSucursal" AS "IDSUCURSAL",
			"IdAgente" AS "IDAGENTE",
			"FolioCarteraRelacionada" AS "FOLIOCARTERARELACIONADA",
			"IdEstado" AS "IDESTADO",
			"IdTipoProducto" AS "IDTIPOPRODUCTO",
			"IdTipoCanalIngreso" AS "IDTIPOCANALINGRESO",
			"IdMonedaSistema" AS "IDMONEDASISTEMA",
			"IdUsuario" AS "IDUSUARIO",
			"TamanoContrato" AS "TAMANOCONTRATO",
			"FechaValorizacionActual" AS "FECHAVALORIZACIONACTUAL",
			"FechaValorizacionAnterior" AS "FECHAVALORIZACIONANTERIOR",
			"FechaValorizacionCartera" AS "FECHAVALORIZACIONCARTERA",
			"ClasificacionRiesgoInstrumento" AS "CLASIFICACIONRIESGOINSTRUMENTO",
			"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
			"RentabilidadReal" AS "RENTABILIDADREAL",
			"CostoUF" AS "COSTOUF",
			"IdTipoMovimientoFondoMutuo" AS "IDTIPOMOVIMIENTOFONDOMUTUO",
			"IdMatrizAgrupador" AS "IDMATRIZAGRUPADOR",
			"EsADC" AS "ESADC"

        FROM "BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
        		ON  WST."Table" = 'Custodia_CarteraFMHis' AND WST."Key" = "IdCartera"  || ':' || TO_VARCHAR("FechaRespaldo", 'YYYY-MM-DD') 
        --and 1 = 2
        --where "FechaRespaldo" = '2020-07-06'
		--ORDER BY "FechaRespaldo" 
--		WHERE "IdCartera" < 2100000
 
---		select count(1)  from Custodia_CarteraFMHis where IdCartera <= 2100000  union all-- 12400117
--select count(1)  from Custodia_CarteraFMHis WHERE IdCartera > 2100000 AND IdCartera < 3500000 union all -- 13157959
--SELECT count(1) from Custodia_CarteraFMHis WHERE IdCartera >= 3500000 AND IdCartera < 4400000 union all -- 13376301
---SELECT count(1) from Custodia_CarteraFMHis WHERE IdCartera >= 4400000-- 10951024

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFMHis" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
