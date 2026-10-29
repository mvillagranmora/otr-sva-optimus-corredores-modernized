WITH clasificacionInstrumento AS ( 
	SELECT 
		INS."IdInstrumento" 
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento" 
	WHERE TIPINS."IdClasificacionTipoInstrumento" = 'RV'
)

SELECT DISTINCT
comprobante."IdComprobante",
comprobante."FolioComprobante",
comprobante."IdTipoComprobante" AS "TipoComprobante",
comprobante."IdTipoComprobanteReferencia" AS "ReferenciaComprobante",
comprobante."IdConcepto" AS "Concepto",
comprobante."DocumentoIdentidad",
consultarCliente."NombreCompleto" AS "Cliente",
portafolio."CodigoPortafolio",
comprobante."NombreEmpleadoEjecutivoPortafolio" AS "Ejecutivo",
CASE
	WHEN comprobante."IdTipoComprobante" LIKE 'N%' AND comprobante."IdTipoOperacion" NOT IN ('COMSPT','VENSPT','ANCSPT','ANVSPT') THEN medioent."MedioDePagoEntregamos"
	ELSE mediorec."MedioDePagoRecibimos"
END AS "MedioDePagoRecibimos",
CASE
	WHEN comprobante."IdTipoComprobante" LIKE 'N%' AND comprobante."IdTipoOperacion" NOT IN ('COMSPT','VENSPT','ANCSPT','ANVSPT')  THEN mediorec."MedioDePagoRecibimos"
	ELSE medioent."MedioDePagoEntregamos"
END AS "MedioDePagoEntregamos",
comprobante."FechaComprobante",
comprobante."FechaLiquidacion",
comprobante."IdMoneda" AS "Moneda",
comprobante."Monto",
monedaLocal."IdMoneda" AS "MonedaLocal",
comprobante."MontoComprobante" AS "MontoLocal",
comprobante."IdMonedaLiquidacion" AS "MonedaLiquidacion",
comprobante."MontoLiquidacion",
IFNULL(tblCobros."IdMonedaCobro",'') AS "MonedaCobro",
comprobante."MontoCobro",
IFNULL(tblCobrosCmbte."IdMonedaComprobanteCobro",'') AS "MonedaLocalCobro",
comprobante."MontoComprobanteCobro" AS "MontoLocalCobro",
IFNULL(tblCobrosLiq."IdMonedaLiquidacionCobro",'') AS "MonedaLiquidacionCobro",
comprobante."MontoLiquidacionCobro",
comprobante."MontoFinal",
comprobante."MontoLiquidacionFinal",
comprobante."MontoPagado",
comprobante."IdTipoProducto" AS "TipoProducto",
comprobante."IdTipoOperacion" AS "TipoOperacion",
comprobante."EsVentaCorta",
tblCobros."ComisionCobrada",
tblCobros."Derecho",
tblCobros."Gasto",
tblCobros."Impuesto",
tblCobros."Retencion",
comprobante."NemotecnicoInstrumento",
comprobante."Cantidad"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante
INNER JOIN clasificacionInstrumento AS clasificacion ON clasificacion."IdInstrumento" = comprobante."IdInstrumento"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoEntregamos" AS medioent ON medioent."IdComprobante"=comprobante."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoRecibimos" AS mediorec ON mediorec."IdComprobante"=comprobante."IdComprobante"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS consultarCliente ON consultarCliente."IdCliente" = comprobante."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLocal ON monedaLocal."IdMoneda" = comprobante."IdMonedaComprobante"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLiq ON monedaLiq."IdMoneda" = comprobante."IdMonedaLiquidacion"	  
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS portafolio ON portafolio."IdPortafolio" = comprobante."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" AS perfil ON perfil."IdPerfilCobros" = portafolio."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" det on det."IdPerfilCobros" = perfil."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::COM.ValorCobro" val on val."IdValorCobro" = det."IdValorCobro" 
LEFT JOIN ( 
	SELECT  "IdComprobante",
		MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "ComisionCobrada",
        MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "Derecho",
        MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "Gasto",
        MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "Impuesto",
        MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "Retencion",
        MAX("IdMonedaCobro") AS "IdMonedaCobro"
    FROM (
		SELECT  CmbteCobro."IdComprobante",
				Cobro."IdCobroClasificacion", 
				CobroClasificacion."CodigoCobroClasificacion",
				IFNULL(SUM(CmbteCobro."MontoCobro"),0) as "MontoCobro",
				CmbteCobro."IdMonedaCobro"
				FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
				LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
				LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
				GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
				ORDER BY Cobro."IdCobroClasificacion"
				)
         GROUP BY "IdComprobante"
) AS tblCobros on tblCobros."IdComprobante" = comprobante."IdComprobante"
LEFT OUTER JOIN (
    	   SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalComisCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalDereCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalGastoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalImptoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalRetenCmbte",
                    MAX("IdMonedaComprobanteCobro") AS "IdMonedaComprobanteCobro"
             FROM (
					SELECT  CmbteCobro."IdComprobante",
							Cobro."IdCobroClasificacion", 
							CobroClasificacion."CodigoCobroClasificacion",
							IFNULL(SUM(CmbteCobro."MontoComprobanteCobro"),0) as "MontoLocalCobro",
							CmbteCobro."IdMonedaComprobanteCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
					GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaComprobanteCobro"
					ORDER BY Cobro."IdCobroClasificacion"
             )
             group by "IdComprobante"
       	) AS tblCobrosCmbte on tblCobrosCmbte."IdComprobante" = comprobante."IdComprobante"
LEFT OUTER JOIN (
    	   Select "IdComprobante",
                   Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalComisLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalDereLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalGastoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalImptoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalRetenLiq",
                   MAX("IdMonedaLiquidacionCobro") AS "IdMonedaLiquidacionCobro"
           From (
					SELECT  CmbteCobro."IdComprobante",
							Cobro."IdCobroClasificacion", 
							CobroClasificacion."CodigoCobroClasificacion",
							IFNULL(SUM(CmbteCobro."MontoLiquidacionCobro"),0) as "MontoLiquidacionCobro",
							CmbteCobro."IdMonedaLiquidacionCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
					GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaLiquidacionCobro"
					ORDER BY Cobro."IdCobroClasificacion"
                  )
           Group by "IdComprobante"
       	) As tblCobrosLiq on tblCobrosLiq."IdComprobante" = comprobante."IdComprobante"
WHERE comprobante."IdEmpresa" = :IdEmpresa AND comprobante."IdEstadoComprobante" NOT IN ('ANU') 
AND comprobante."IdCliente" = CASE WHEN :IdCliente = 0 THEN comprobante."IdCliente" ELSE :IdCliente END 
AND comprobante."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN comprobante."IdPortafolio" ELSE :IdPortafolio END
AND val."IdCobro" = 'COVA'
AND det."Borrado" = 'N'
AND comprobante."FechaSistema" = :Fecha;