SELECT * FROM (
	SELECT
		 DISTINCT(OPE."IdOperacion") AS "IdOperacion"
		,OPE."FolioOperacion"
		,TO_NVARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
		,OPE."IdTipoOperacion"
		,OPE."NombreOperacion"
		,OPE."IdCliente"
		,COALESCE(OPE."DocumentoIdentidad",'') AS "DocumentoIdentidad"
		,OPE."RazonSocial"
		,OPE."IdPortafolio"
		,COALESCE(POR."CodigoPortafolio",'') AS "CodigoPortafolio"
		,OPE."IdClienteCorredor"
		,CRR."IdContraparte"
		,CASE WHEN CRR."Tipo"='J' THEN COALESCE(CRR."RazonSocial",'-') ELSE COALESCE(CRR."NombreCompleto",'-') END AS "RazonSocialCorredora"
		,OPE."Tasa"
		,'' AS "CatIFRS"	-- REVISAR este valor
		,OPE."IdTipoReajuste"
		,TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
		,TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento"
		,OPE."FechaLiquidacion" AS "DateLiquidacion"
		,OPE."FechaVencimiento" AS "DateVencimiento"
		,CASE WHEN TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') = '01-01-0001' THEN OPE."FechaVencimiento"
			ELSE OPE."FechaLiquidacion" END AS "FechaAnticipo"
		,CASE WHEN TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') = TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') 
			OR TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') = '01-01-0001'  THEN 'S'
			ELSE 'N' END AS "Liquidado"
		,OPE."IdAgente"
		,POR."IdEmpleadoEjecutivo" AS "IdPersonaEjecutivo"
		,COALESCE((CASE AGE."Tipo" 
				WHEN 'N' THEN AGE."Nombres" ||' '|| AGE."ApellidoPaterno" || ' ' ||AGE."ApellidoMaterno" 
				ELSE AGE."RazonSocial"
			END),TO_NVARCHAR(OPE."IdAgente")) AS "Agente"
		,OPE."IdSucursalEjecutivo"
		,COALESCE(SUC."CodigoSucursal",'')||' '||SUC."NombreSucursal" AS "Sucursal"
		,OPE."MontoOperacionInicial" AS "MontoOperacion"
		,OPE."MontoOperacionFinalAnticipo" AS "MontoCompromisoAnticipo"
		,OPE."MontoOperacionFinal" AS "MontoCompromiso"
		,OPE."MontoOperacionInicialMoneda" AS "ValorOperacion"
		,OPE."MontoOperacionFinalMonedaAnticipo" AS "ValorCompromisoAnticipo"
		,OPE."MontoOperacionFinalMoneda" AS "ValorCompromiso"
		,OPE."IdEstado"
		,OPE."IdEmpresa"
		,OPE."IdMesaOrigen"
		,OPE."NombreMesaOrigen"
		,OPE."IdTipoProducto"
		,OPE."IdMonedaLiquidacion"
		,COALESCE(OPE."DocumentoIdentidadMesa",'-')||' '||COALESCE(OPE."NombreMesaOrigen",'') AS "Mesa"
		,COALESCE(CARHIS."INGMontoCompraActualMonedaInstrumento",CAR."INGMontoCompraActualMonedaInstrumento")					AS "ValorCompraCartera"
		,COALESCE(CARHIS."INGMontoInteresCompraProximoMonedaInstrumento",CAR."INGMontoInteresCompraProximoMonedaInstrumento")	AS "ValorInteresCartera"
		,COALESCE(CARHIS."INGMontoReajusteCompraProximoMonedaInstrumento",CAR."INGMontoReajusteCompraProximoMonedaInstrumento")	AS "ValorReajusteCartera"
		,COALESCE(CARHIS."INGMontoCompraProximoMonedaInstrumento",CAR."INGMontoCompraProximoMonedaInstrumento")					AS "ValorCompraProximoCartera"
		,COALESCE(CARHIS."INGMontoATasaCompraProximo",CAR."INGMontoATasaCompraProximo")											AS "MontoCompraProximoCartera"
		,COALESCE(CARHIS."FolioCartera",CAR."FolioCartera")	AS "FolioCartera"
		,OPE."PlazoEnDias"
		,CASE WHEN IND."ValorIndicador"='S' THEN 'Relacionado' ELSE 'Sin Relacion' END AS "RelacionCorredora"
		,IND."ValorIndicador"
		,CASE WHEN OPE."ConfirmaAdemdum" = 'S' THEN 'Si' ELSE 'No' END AS "ConfirmaAdemdum"
		,OPE."IdTipoInstrumento"
		,CARHIS."FechaRespaldo" 
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 								CAR 	ON (OPE."IdOperacion" = CAR."FolioComprobante" AND CAR."IdTipoInstrumento" = 'PAC')
	LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarTablaCartera" 			CARHIS	ON (OPE."IdOperacion" = CARHIS."FolioComprobante" AND CARHIS."IdTipoInstrumento" = 'PAC')
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 							CLI		ON (OPE."IdCliente" = CLI."IdCliente" AND OPE."IdEmpresa" = CLI."IdEmpresa" AND CLI."Borrado"='N')
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 							POR		ON (OPE."IdPortafolio" = POR."IdPortafolio" AND OPE."IdCliente" = POR."IdCliente")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			CLICRR	ON (OPE."IdClienteCorredor" = CLICRR."IdCliente" AND OPE."IdEmpresa" = CLICRR."IdEmpresa")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" 		CRR		ON (CLICRR."IdPersona" = CRR."IdPersona" AND CLICRR."IdEmpresa" = CRR."IdEmpresa" AND CRR."Corredora" = 'S' AND CRR."IdEstado" = 'ACT')
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" 							AGE		ON (OPE."IdAgente" = AGE."IdPersona" AND AGE."Borrado"='N')
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" 			SUC		ON (OPE."IdSucursalEjecutivo" = SUC."IdSucursal")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"	IND 	ON (IND."IdCliente" = CLI."IdCliente" AND "IdTipoIndicador" = 56 AND "GrupoIndicador" = 'NORMATIVOS')
	WHERE OPE."IdEstado" <> 'PEN'
	AND OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC' )
	AND (OPE."FechaOperacion" 	>= CASE WHEN :FechaOpDesde 	IS NOT NULL THEN :FechaOpDesde ELSE OPE."FechaOperacion" END)
	AND (OPE."FechaOperacion" 	<= CASE WHEN :FechaOpHasta 	IS NOT NULL THEN :FechaOpHasta ELSE OPE."FechaOperacion" END)
	AND (OPE."FechaVencimiento" >= CASE WHEN :FechaVeDesde  IS NOT NULL THEN :FechaVeDesde ELSE OPE."FechaVencimiento" END)
	AND (OPE."FechaVencimiento" <= CASE WHEN :FechaVeHasta  IS NOT NULL THEN :FechaVeHasta ELSE OPE."FechaVencimiento" END)
)
WHERE ("FechaAnticipo" >= CASE WHEN :FechaAnDesde 	IS NOT NULL THEN :FechaAnDesde ELSE "FechaAnticipo" END)
AND ("FechaAnticipo" <= CASE WHEN :FechaAnHasta 	IS NOT NULL THEN :FechaAnHasta ELSE "FechaAnticipo" END)
AND (:IdEstado IS NULL OR "IdEstado" = :IdEstado);