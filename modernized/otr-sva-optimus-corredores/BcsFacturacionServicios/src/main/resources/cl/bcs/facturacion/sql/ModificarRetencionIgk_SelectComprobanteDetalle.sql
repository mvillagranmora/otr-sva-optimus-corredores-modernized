SELECT
	 CmbteDet."IdComprobanteDetalle"
	,CmbteDet."IdComprobante"
	,CmbteDet."ModPrefFact"
	,CmbteDet."IdConcepto"
	,ConcepPla."Nombre" AS "NombreConcepto"
	,CmbteDet."NroLinea"
	,CmbteDet."FechaLiquidacion"
	,TO_NVARCHAR ( TO_DATE (CmbteDet."FechaLiquidacion" ) , 'YYYY-MM-DD' ) AS "FechaLiquidacionString"
	,CmbteDet."IdInstrumento"
	,Case 
	   When CmbteDet."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then CmbteDet."NemotecnicoSubyacente" 
	 Else CmbteDet."NemotecnicoInstrumento"
	 End As "NemotecnicoInstrumento"
	,ConsCli."NombreCompleto" As "RazonSocial" 
	,CmbteDet."IdTipoInstrumento" 
	,TipoIns."Codigo" AS "CodigoTipoInstrumento"			
	,TipoIns."Nombre" AS "NombreTipoInstrumento"
	,CmbteDet."IdTipoOperacion"
	,tipooperacion."Accion" AS "AccionTipoOperacion"
	,tipooperacion."Codigo" AS "CodigoTipoOperacion"
	,tipooperacion."Nombre" AS "NombreTipoOperacion"
	,CmbteDet."IdTipoProducto"
	,tipoproducto."Codigo" AS "CodigoTipoProducto"
	,tipoproducto."Nombre" AS "NombreTipoProducto"
	,CASE WHEN Cmbte."IdClasificacionTipoInstrumento" = 'IF' THEN CmbteDet."ValorRescate" ELSE CmbteDet."Cantidad" END AS "Cantidad"
	,CmbteDet."ValorRescate"
	,CmbteDet."Precio"
	,CmbteDet."Monto"
	,CmbteDet."MontoComprobanteDetalle"
	,CmbteDet."MontoLiquidacion"
	,CmbteDet."IdBolsa"
	,CmbteDet."IdEmpleadoEjecutivoPortafolio"
	,CmbteDet."IdSucursalEmpleadoEjecutivoPortafolio"
	,CmbteDet."FechaIngresoRegistro"
	,CmbteDet."IdEstadoComprobanteDetalle"
	,"Estado"."Nombre" AS "NombreEstado"
	,Cmbte."IdMoneda"
	,Cmbte."IdMonedaComprobante"
	,Cmbte."IdMonedaLiquidacion"
	,CmbteDet."ValorTipoCambio"
	,CmbteDet."IndicadorOrdenDirecta"
	,CmbteDet."IdFormaCustodia"
	,CASE
	   WHEN CmbteDet."IdFormaCustodia" = 'NOM' THEN 'NOMINAL'
	   WHEN CmbteDet."IdFormaCustodia" = 'CUS' THEN 'CUSTODIA'
	 ELSE ''
	 END AS "NombreFormaCustodia",
	 CmbteDet."IdTransaccion",
	 CmbteDet."FolioTransaccion",
	 CmbteDet."FechaTransaccion",
	 Cmbte."FechaComprobante",
	 Cmbte."Glosa",
	 Cmbte."IdTipoComprobante",
	 CmbteDet."IdTipoInstrumentoSubyacente",
	 CmbteDet."NemotecnicoSubyacente",
	 Cmbte."Tasa",
	 CASE 
	 	WHEN Cmbte."IdTipoComprobante" = 'FOE'
		THEN  ConcepPla."Nombre"  || ' - ' ||  tipooperacion."Nombre"
    	ELSE ''     
  	END AS "Descripcion"
	,CmbteDet."MayorMenorValor"
	,CmbteDet."MontoRetencionGananciaCap"	 
	 ,Instrumento."AfectoArt107"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CmbteDet
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte 
   ON Cmbte."IdComprobante" = CmbteDet."IdComprobante"
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstFac" 
   ON "EstFac"."IdEstado" = CmbteDet."IdEstadoComprobanteDetalle"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" 
   ON "Estado"."IdEstado" = "EstFac"."IdEstado"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto 
   ON tipoproducto."IdTipoProducto" = CmbteDet."IdTipoProducto"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion 
   ON tipooperacion."IdTipoOperacion" = CmbteDet."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS tipoInstrumento 
   ON tipoInstrumento."IdTipoOperacion" = CmbteDet."IdTipoOperacion" AND tipoInstrumento."IdInstrumento" = CmbteDet."IdInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins 
   ON (Ins."IdInstrumento" = tipoInstrumento."IdInstrumento")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoIns
   ON (TipoIns."IdTipoInstrumento" = Ins."IdTipoInstrumento")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
	ON (Instrumento."IdInstrumento" = CmbteDet."IdInstrumento")  
LEFT Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli 
    On ConsCli."IdCliente" = Cmbte."IdCliente"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS ConcepFac
    ON ConcepFac."IdConcepto" = CmbteDet."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConcepPla
    ON ConcepPla."IdConcepto" = ConcepFac."IdConcepto"
WHERE Upper("EstFac"."NombreEntidad") = Upper('FAC.ComprobanteDetalle') and CmbteDet."IdComprobanteDetalle" = :IdComprobanteDetalle