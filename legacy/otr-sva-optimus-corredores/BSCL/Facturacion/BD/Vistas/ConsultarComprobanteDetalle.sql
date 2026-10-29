SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle";
GO
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" As 
WITH DetalleIFRF ("IdComprobanteDetalle", "Tasa", "FechaVencimiento", "NroFila") AS (
    SELECT
        MvmtoDet."IdComprobanteDetalle",
        Mvmto."Tasa",
        Mvmto."FechaVencimiento",
        ROW_NUMBER() OVER (
            PARTITION BY MvmtoDet."IdComprobanteDetalle"
            ORDER BY Mvmto."FechaVencimiento" DESC
        ) AS "NroFila"
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS MvmtoDet 
    INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS Mvmto
        ON Mvmto."IdMovimientoComprobante" = MvmtoDet."IdMovimientoComprobante" 
    WHERE MvmtoDet."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
),
IndicadorSeparaTransaccion ("Valor", "IdPortafolio") AS (
	SELECT IND."ValorIndicador"  AS "Valor", IND."IdEntidad" AS "IdPortafolio"
	FROM "BSCL.Plataforma.BD::IND.Indicador"  AS IND 
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
		ON IND."IdTipoIndicador"  = TIND."IdTipoIndicador"
		AND IND."IdTipoEntidad" = 'PORTAFOLIO'
	WHERE TIND."CodigoTipoIndicador"  = 'FAC_SLFPTR'
		AND TIND."IdEstado" != 'INA' 
)
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
	,Case 
			When (CmbteDet."EsVentaCorta" = 'S' AND CmbteDet."IdClasificacionTipoInstrumento" = 'RV') THEN 'INTERMEDIACION VENTA CORTA RENTA VARIABLE'
			When (CmbteDet."EsVentaCorta" = 'S' AND CmbteDet."IdClasificacionTipoInstrumento" = 'RF') THEN 'INTERMEDIACION VENTA CORTA RENTA FIJA'
			Else tipooperacion."Nombre"
		End As "NombreTipoOperacion"
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
	 CASE 
	    WHEN CmbteDet."IdClasificacionTipoInstrumento" IN ('IF','RF') AND DetIFRF."Tasa" IS NOT NULL
	        THEN DetIFRF."Tasa"
	    ELSE CmbteDet."Tasa"
	 END AS "Tasa",
	 DetIFRF."FechaVencimiento",
	 CASE 
	 	WHEN Cmbte."IdTipoComprobante" = 'FOE'
		THEN  ConcepPla."Nombre"  || ' - ' ||  tipooperacion."Nombre"
    	ELSE ''     
  	END AS "Descripcion"
	,CmbteDet."MayorMenorValor"
	,CmbteDet."MontoRetencionGananciaCap"
	,COALESCE(NULLIF(DiccioPla."Abreviado", ''),CmbteDet."IdTipoInstrumento") AS "DetalleGlosaFacturacion"
	,CASE 
		WHEN CmbteDet."IdTipoInstrumento" = 'SIM' THEN tipoproducto."Nombre"
		ELSE COALESCE(NULLIF(DiccioPla."Descripcion", ''), tipoproducto."Nombre")
	END AS "DetalleGlosaTipoProducto"
	,CmbteDet."IdInstrumentoSubyacente"
	,CASE 
		WHEN tipooperacion."IdTipoOperacion" IN ('ANVIIF', 'ANVRF') THEN 'VENTA'
		WHEN tipooperacion."IdTipoOperacion" IN ('ANCIIF', 'ANCRF') THEN 'COMPRA'
		ELSE ''
	END	AS "AccionAnulacionSubyacente"
	,Inst."Reajustable" AS "InstrumentoReajustable"
	,COALESCE(IndSepTrns."Valor", '') AS "IndicadorSeparaTransacciones"
	,CmbteDet."GlosaSimplificada"

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
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" As ConsEm 
 --  ON ConsEm."IdEmisor" = Ins."IdEmisor"   
LEFT Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli 
    On ConsCli."IdCliente" = Cmbte."IdCliente"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS ConcepFac
    ON ConcepFac."IdConcepto" = CmbteDet."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConcepPla
    ON ConcepPla."IdConcepto" = ConcepFac."IdConcepto"
LEFT JOIN BSCL."BSCL.Plataforma.BD::PLA.Diccionario" AS DiccioPla 
  ON DiccioPla."CodDiccionario" = 
     CASE 
       WHEN CmbteDet."IdTipoInstrumento" = 'SIM' THEN CmbteDet."IdTipoInstrumentoSubyacente" 
	   WHEN CmbteDet."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN CmbteDet."IdClasificacionTipoInstrumento"
       ELSE CmbteDet."IdTipoInstrumento" 
     END
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Inst
	ON Inst."IdInstrumento" = CmbteDet."IdInstrumento"
LEFT JOIN DetalleIFRF AS DetIFRF
	ON DetIFRF."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle"
	AND DetIFRF."NroFila" = 1 
LEFT JOIN IndicadorSeparaTransaccion AS IndSepTrns
	ON IndSepTrns."IdPortafolio" = Cmbte."IdPortafolio" 
WHERE Upper("EstFac"."NombreEntidad") = Upper('FAC.ComprobanteDetalle');
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
