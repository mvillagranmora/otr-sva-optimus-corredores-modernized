SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::REP.ConsultarAnticipos";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::REP.ConsultarAnticipos" AS 
SELECT	 distinct

		 CSDN."DocumentoIdentidadDefecto"
		,CSDN."NombreCompleto" as "NombreLargoCliente"
		,(CSDN."DocumentoIdentidadDefecto" || ' ' || CSDN."NombreCompleto") as "Cliente"
		,CP."CodigoPortafolio"
		,CP."Proposito"
		,(CP."CodigoPortafolio" || ' ' || CP."Proposito") as "Portafolio"
		,Sub."FolioSubyacente"					
		,Sub."IdTipoInstrumento" as "suby_IdTipoInstrumento"				
		,Sub."IdInstrumento" as "suby_IdInstrumento" 					
		,Sub."Nemotecnico" as "suby_Nemotecnico" 						
		,TipOpe."Nombre" as "NombreOperacion"
		,TipOpe."Accion" as "AccionOperacion"
		,TipOpe."IdTipoProducto" 
		,Car."IdTipoComprobante"
		,Car."FolioComprobante"
		,Car."FechaComprobante"	
		,CondLiq."NombreLargo"
		,Car."IdCartera"
		,Car."FolioCartera"
		,Car."IdEstado"
		,Car."IdEmpresa"
		,Car."IdCliente"
		,Car."IdAgente"
		,Car."IdSucursal"
		,Car."IdPortafolio"
		,Car."FolioCarteraRelacionada"
		,Car."IdClasificacionTipoInstrumento"
		,Car."IdTipoInstrumento" as "IdTipoInstrumentoOperacion"
		,Car."IdInstrumento" as "IdInstrumentoOperacion"
		,Car."Nemotecnico" as "NemotecnicoOperacion"
		,Car."IdTipoOperacion"
		,Car."IdMonedaSistema"
		,Car."IdMonedaInstrumento"
		,Car."FechaOperacion" as "FechaInversion" 
		,Car."FechaVencimientoOperacion"
		,Car."IdCondicionLiquidacion"
		,Car."FechaLiquidacion"		
		,Car."MontoAPlazo"
		,Car."PrecioAPlazo"
		,Car."PlazoEnDiasOperacion"
		,Car."INGGlosa"		   				 		
		,Car."INGFechaOperacion" 					
		,Car."INGFechaLiquidacion" 					
		,Car."INGCantidad"							
		,Car."INGValorRescate" 				
		,Car."INGTasaAgente" 	
		,Car."INGMontoAPlazo" 	
		,Car."INGPrecioAPlazo" 	

		-----------------05/07/2016 - CAMPOS AGREGADOS PARA CONSULTA DE ANTICIPOS-------------------
		,Car."EGRGlosa"
		,Car."EGRFechaOperacion"
		,Car."EGRIdTipoOperacion"
		,Car."EGRIdOperacion"
		,Car."EGRIdMovimientoCarteraEnTransito"
		,Car."EGRIdTipoCanalIngreso"
		,Car."EGRIdCondicionLiquidacion"
		,Car."EGRFechaLiquidacion"
		,Car."EGRIdTipoComprobante"
		,Car."EGRFolioComprobante"
		,Car."EGRFechaComprobante"
		,Car."EGRFolioExternoReferencia"
		,Car."EGRIdModulo"
		,Car."EGRIdConcepto"
		,Car."EGRIdUsuario"
		,Car."EGRIdSucursal"
		,Car."EGRIdAgente"
		,Car."EGRCantidad"
		,Car."EGRValorRescate"
		,Car."EGRPrecio"
		,Car."EGRTasa"
		,Car."EGRMonto"
		,Car."EGRMontoMonedaInstrumento"
		,Car."EGRSpread"
		,Car."EGRPrecioAPlazo"
		,Car."EGRMontoAPlazo"
		,Car."EGRMontoAPlazoMonedaInstrumento"
		,Car."EGRMontoMonedaPrincipal"
		,Car."EGRMontoMonedaSecundaria"
		,Car."EGRIdMonedaLiquidacion"
		,Car."EGRMontoLiquidacion"
		,Car."EGRMontoLiquidacionMonedaPrincipal"
		,Car."EGRValorParidad"
		,Car."EGRValorTipoCambioCotizador"

		---------------------------------------------------------------------------------------

  FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
 INNER JOIN "BSCL.Custodia.BD.Vistas::REP.ConsultarInstrumentosSubyacentes" as Sub ON (Sub."FolioCarteraRelacionada" = car."FolioCartera" 		)

  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = Car."IdTipoOperacion") 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Ins ON (Ins."IdInstrumento" = Sub."IdInstrumento")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" as CondLiq ON (CondLiq."IdTipoOperacion" = Car."IdTipoOperacion" AND CondLiq."IdCondicionDeLiquidacion" = Car."IdCondicionLiquidacion")
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS CSDN ON (CSDN."IdCliente" = Car."IdCliente")
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CAR."IdPortafolio")
 WHERE Car."IdClasificacionTipoInstrumento" = 'RE' and Car."IdEstado" = 'ANT'
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::REP.ConsultarAnticipos"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
