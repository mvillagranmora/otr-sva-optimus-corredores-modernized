SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientosInterfazAFP"; 
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientosInterfazAFP" As

	SELECT
		TO_DATE("FechaTransaccion") 								As "FechaTransaccion",
		ConsMvtoCmbte."FolioTransaccion",
		Coalesce(ConsCorrEm."CodigoCorredora", 0) 					As "Corredora",
		Mvto."ValorIndicador", 
		Case
			When ConsMvtoCmbte."AccionTipoOperacion" = 'COMPRA' 
				Then 'C'
			When ConsMvtoCmbte."AccionTipoOperacion" = 'VENTA' 
				Then 'V'
			Else
				''
		End 														As "AccionTipoOperacion",
		ConsMvtoCmbte."Cantidad"									As "Cantidad",
		ConsMvtoCmbte."Precio"										As "Precio",
		TO_TIME("FechaTransaccionHora") 							As "HoraTransaccion",
		ConsMvtoCmbte."IdMoneda"									AS "IdMoneda",
		ConsMvtoCmbte."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento",
		ConsMvtoCmbte."NombreClasificacionTipoInstrumento"			AS "NombreClasificacionTipoInstrumento",
		ConsMvtoCmbte."IdTipoInstrumento"							AS "IdTipoInstrumento",
		ConsMvtoCmbte."NombreTipoInstrumento"						AS "NombreTipoInstrumento",
		"NemotecnicoInstrumento"									AS "NemotecnicoInstrumento",
		ConsMvtoCmbte."Monto" 										As "Total",
		Case			
		  When "IdBoveda" = 'DCV'			
				Then 'D'			
			Else			
				''			
		End 														As "Boveda",
		ConsMvtoCmbte."IndicadorOrdenDirecta" As "OD", 
		Case  			
			When ConsMvtoCmbte."IdBolsa" = 'BCS' 			
				Then '1'  			
			When ConsMvtoCmbte."IdBolsa" = 'BEC' 			
				Then '2'  			
			Else 			
				''			
		End 														As "Bolsa",
		ConsMvtoCmbte."CodigoCondicionDeLiquidacion" 				As "CondicionDeLiquidacion",
		Case			
			When "CodigoOperadorCorredor" > '100' 			
				Then 'A'			
			Else 			
				'C'   			
		End 														As "TipoOperador",
		ConsMvtoCmbte."IdCliente"									AS "IdCliente",
		ConsMvtoCmbte."DocumentoIdentidad"							AS "DocumentoIdentidad",
		ConsMvtoCmbte."IdPortafolio"								AS "IdPortafolio",
		ConsMvtoCmbte."CodigoPortafolio"							AS "Portafolio",
		ConsMvtoCmbte."NombreLargoCliente"							AS "NombreCliente",
		ConsMvtoCmbte."IdTipoComprobante"							AS "IdTipoComprobante",
		ConsMvtoCmbte."NombreTipoComprobante"						AS "NombreTipoComprobante",
		ConsMvtoCmbte."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion",
		ConsMvtoCmbte."CodigoCondicionDeLiquidacion"				AS "CodigoCondicionLiquidacion",
		ConsMvtoCmbte."NombreCondicionDeLiquidacion"				AS "NombreCondicionLiquidacion",
		ConsMvtoCmbte."FechaLiquidacion"							AS "FechaLiquidacion"
	From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"				As ConsMvtoCmbte
		Inner Join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"	As Mvto
			On Mvto."IdCliente" = ConsMvtoCmbte."IdCliente"
		   AND Mvto."IdPortafolio" = ConsMvtoCmbte."IdPortafolio"
			AND Mvto."CodigoTipoIndicador" = 'FAC_COIAFP'
		Left Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" 		As ConsCorrEm
			On ConsCorrEm."IdBolsa" = 'BCS'
			And ConsCorrEm."IdEmpresa" = ConsMvtoCmbte."IdEmpresa"
		Left Join "BSCL.Orden.BD.Vistas::ConsultarTransaccion" 						As ConsTran
			On ConsTran."IdTransaccion" = ConsMvtoCmbte."IdTransaccion" ;

GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientosInterfazAFP" TO  BCS_DESARROLLADOR WITH GRANT OPTION;