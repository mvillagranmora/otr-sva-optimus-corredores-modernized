SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacion";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacion" As 
SELECT	 
			comprobante."IdComprobante",
			tipocomprobante."Codigo" 															AS "IdTipoComprobante" ,
		 	tipocomprobante."Nombre" 															AS "NombreTipoComprobante" ,
		 	comprobante."FolioComprobante" ,
		 	CASE comprobante."FechaLiquidacion" 
		 		WHEN '0001-01-01' THEN '' 
				ELSE TO_NVARCHAR ( comprobante."FechaLiquidacion" ) 
			END 																				AS "FechaLiquidacionString" ,
			comprobante."FechaLiquidacion",
		 	CASE comprobante."FechaEmision" 
		 		WHEN '0001-01-01' THEN '' 
				ELSE TO_NVARCHAR ( comprobante."FechaEmision" ) 
			END 																				AS "FechaEmisionString" ,
		 	comprobante."FechaEmision" ,
		 	comprobante."FechaComprobante",
		 	TipoCondLiq."Codigo"																AS "CodigoCondicionLiquidacion",
		 	comprobante."IdCliente" ,
            comprobante."DocumentoIdentidad",
            comprobante."IdPortafolio",
		 	viewportalio."CodigoPortafolio",
 			viewportalio."Proposito",
		 	comprobante."NombreLargoCliente",
            comprobante."IdEmpleadoEjecutivoPortafolio" ,
            comprobante."NombreEmpleadoEjecutivoPortafolio" ,
            comprobante."IdSucursalEmpleadoEjecutivoPortafolio",
            comprobante."NombreSucursalEmpleadoEjecutivoPortafolio",
            comprobante."IdMoneda",
            CASE
    			WHEN tipooperacion."Accion" = 'VENTA'  THEN 
    				abs(Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoComprobanteFinal", FormCmbte."MontoDecimales", FormCmbte."MontoTrucaRedondea"), comprobante."MontoComprobanteFinal"))  
    			ELSE
    				0
    		END AS "SaldoAFavor",
    		CASE
    			WHEN tipooperacion."Accion" = 'COMPRA' THEN 
    				-abs(Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoComprobanteFinal", FormCmbte."MontoDecimales", FormCmbte."MontoTrucaRedondea"), comprobante."MontoComprobanteFinal"))
    			ELSE
    				0
			END As "SaldoEnContra",
            CASE 
            	WHEN tipooperacion."Accion" = 'COMPRA'  THEN
            		IFNULL("MontoCobro",0)
            	ELSE
            		0
			END As "TotalGastoCompra",
            CASE 
            	WHEN tipooperacion."Accion" = 'VENTA' THEN
            		-IFNULL("MontoCobro",0)
            	ELSE
            		0
			END As "TotalGastoVenta",
		 	comprobante."IdTipoOperacion" ,
		 	tipooperacion."Accion" 																As "AccionTipoOperacion",		 	
		 	tipooperacion."Codigo" 																AS "CodigoTipoOperacion",
		 	tipooperacion."Nombre" 																AS "NombreTipoOperacion",
		 	comprobante."IdTipoProducto",
		 	tipoproducto."Codigo" 																AS "CodigoTipoProducto",
		 	tipoproducto."Nombre" 																AS "NombreTipoProducto",
		 	comprobante."IdClasificacionTipoInstrumento",
		 	comprobante."IdTipoInstrumento"
	  FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = comprobante."IdTipoComprobante"
	  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as viewportalio ON viewportalio."IdPortafolio" = comprobante."IdPortafolio"	                     	  
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = comprobante."IdTipoOperacion"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = comprobante."IdTipoProducto"
      LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbte
        On FormCmbte."IdMoneda" = comprobante."IdMoneda"
       And FormCmbte."IdTipoOperacion" = ''
       And FormCmbte."IdTipoInstrumento" = ''
       And FormCmbte."IdUsoMonedaFormato" = 'GEN' 
      LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLoc
        On FormCmbteLoc."IdMoneda" = comprobante."IdMonedaComprobante"
       And FormCmbteLoc."IdTipoOperacion" = ''
       And FormCmbteLoc."IdTipoInstrumento" = ''
       And FormCmbteLoc."IdUsoMonedaFormato" = 'GEN' 
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLiq
        On FormCmbteLiq."IdMoneda" = comprobante."IdMonedaLiquidacion"
       And FormCmbteLiq."IdTipoOperacion" = ''
       And FormCmbteLiq."IdTipoInstrumento" = ''
       And FormCmbteLiq."IdUsoMonedaFormato" = 'GEN'
      LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS TipoCondLiq
      	ON TipoCondLiq."IdCondicionDeLiquidacion" = comprobante."IdCondicionLiquidacion" 
       WHERE comprobante."IdEstadoComprobante" <> 'ANU';       
       
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 