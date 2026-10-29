SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::OPE.ConsultaOperacionesPendientes";

CREATE PROCEDURE
-- 
--  Proposito: Consultar Operaciones Pendientes.
--  Autor: BCS
--  Fecha Creacion: 29/09/2016
--   
"BSCL.Custodia.BD.Servicios::OPE.ConsultaOperacionesPendientes"(
	OUT p_out_Salida        	"BSCL.Custodia.BD.Tipos::OperacionesPendientes"
																						
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN

p_out_Salida =

		SELECT									
			ROW_NUMBER() OVER (ORDER BY  DCOMP."IdMovimientoComprobante")		AS "IdInterno"   						
			,DCOMP."IdMovimientoComprobante"									AS "FolioOperacion"					
			,''																	AS "IdTipoOperacion"					
			,TOPER."Nombre"														AS "NombreOperacion"					
			,DCOMP."IdTipoInstrumento"											AS "IdTipoInstrumento"					
			,DCOMP."NemotecnicoInstrumento"										AS "Nemo"								
			,DCOMP."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"	
			,DCOMP."CodigoCondicionDeLiquidacion"								AS "IdCondicionLiquidacion"			
--			,DCOMP."IdTipoOperacion"											AS "Accion"							
			,'N'																AS "OperadorDirecto"					
			,DCOMP."FechaMovimientoComprobante"									AS "FechaIngresoRegistro"				
			,DCOMP."FechaMovimientoComprobante"									AS "FechaVencimiento"					
			,DCOMP."TipoDocumentoIdentidad"										AS "IdTipoDocumentoIdentidad"			
			,DCOMP."DocumentoIdentidad"											AS "DocumentoIdentidad"				
			,DCOMP."IdPortafolio"												AS "IdPortafolio"						
			,DCOMP."FolioComprobante"											AS "FolioComprobante"					
			,DCOMP."FolioComprobanteReferencia"									AS "FolioComprobanteReferencia"		
			,0																	AS "FolioComprobanteRelacionado"		
			,DCOMP."IdOrden"													AS "IdOrden"							
			,DCOMP."Cantidad"													AS "Cantidad"							
			,DCOMP."Precio"														AS "Precio"							
			,DCOMP."Tasa"														AS "Tasa"								
			,DCOMP."Monto"														AS "Monto"								
			,DCOMP."MontoMovimientoComprobante"									AS "MontoComprom"						
			,DCOMP."IdEstadoComprobante"										AS "IdEstado"							
			,DCOMP."IdEmpleadoEjecutivoPortafolio"								AS "IdAgente"							
			,DCOMP."IdMoneda"													AS "IdMoneda"							
			,''																	AS "TipoGranatia"						
			,''																	AS "FondoAfp"							
			,DCOMP."IdTipoComprobante"											AS "IdTipoComprobante"					
			,DCOMP."CodigoTipoProducto"											AS "CodigoTipoProducto"				
			,PRODU."Nombre"														AS "Nombreproducto"					
			,DCOMP."IdBolsa"													AS "IdBolsa"							
			,''																	AS "DocumentoIdentidadEntidad"			
			,DCOMP."IdBoveda"													AS "IdBoveda"							
			,DCOMP."FolioTransaccion"											AS "FolioTransaccion"					
			,''																	AS "DescEstadoCclv"					
			,''																	AS "UsoGarantia"						
			,''																	AS "TipoOpePendiente"					
			,''																	AS "BlockEnvio"						
			,DCOMP."IdConceptoMovimiento"										AS "IdConcepto"						
			,CONCE."Nombre"														AS "NombreConcepto"					
			,''																	AS "IndicadorEnvio"					
			,DCOMP."IdEmisor"													AS "IdEmisor"							
			,DCOMP."EsFungible"													AS "EsFungible"						
			,''																	AS "NemoSVS"							
			,DCOMP."IdTipoReajuste"												AS "IdTipoReajuste"					
			,''																	AS "Categoria"							
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"	DCOMP
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"			TOPER 
		ON (DCOMP."IdTipoOperacion" = toper."IdTipoOperacion")						
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"				CONCE
		ON (DCOMP."IdConceptoMovimiento" = CONCE."IdConcepto")						
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"			PRODU
		ON (DCOMP."CodigoTipoProducto" = PRODU."IdTipoProducto")

	UNION ALL

	SELECT										
			ROW_NUMBER() OVER (ORDER BY  GARAN."FolioGarantia")	AS "IdInterno"   						
			,GARAN."FolioGarantia"								AS "FolioOperacion"						
			,GARAN."IdTipoOperacionOperacionOrigen"				AS "IdTipoOperacion"						
			,TOPER."Nombre"										AS "NombreOperacion"						
			,GARAN."IdTipoInstrumento"							AS "IdTipoInstrumento"						
			,GARAN."Nemotecnico"								AS "Nemo"									
			,GARAN."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,GARAN."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"				
--			,''													AS "Accion"								
			,'N'												AS "OperadorDirecto"					
			,GARAN."FechaIngresoRegistro"						AS "FechaIngresoRegistro"					
			,GARAN."FechaVencimientoOperacion"					AS "FechaVencimiento"						
			,'R'												AS "IdTipoDocumentoIdentidad"				
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"					
			,GARAN."IdPortafolio"								AS "IdPortafolio"							
			,0													AS "FolioComprobante"						
			,0													AS "FolioComprobanteReferencia"			
			,0													AS "FolioComprobanteRelacionado"			
			,0													AS "IdOrden"								
			,GARAN."Cantidad"									AS "Cantidad"								
			,GARAN."PrecioGarantia"								AS "Precio"								
			,GARAN."TasaGarantia"								AS "Tasa"									
			,GARAN."MontoGarantia"								AS "Monto"									
			,0													AS "MontoComprom"							
			,GARAN."IdEstado"									AS "IdEstado"								
			,GARAN."IdAgente"									AS "IdAgente"								
			,GARAN."IdMonedaInstrumento"						AS "IdMoneda"								
			,''													AS "TipoGranatia"							
			,''													AS "FondoAfp"								
			,''													AS "IdTipoComprobante"						
			,CASE  GARAN."IdTipoOperacionOperacionOrigen"              					
				WHEN 'INGGTA' THEN 'INSI' END					AS "CodigoTipoProducto"
			,CASE  GARAN."IdTipoOperacionOperacionOrigen"              	
				WHEN 'INGGTA' THEN 'SIMULTANEAS' END			AS "Nombreproducto"										
			,GARAN."IdBolsaOperacionOrigen"						AS "IdBolsa"												
			,'F'												AS "DocumentoIdentidadEntidad"					
			,GARAN."IdBovedaOperacionOrigen"					AS "IdBoveda"								
			,'0'												AS "FolioTransaccion"						
			,''													AS "DescEstadoCclv"						
			,''													AS "UsoGarantia"							
			,''													AS "TipoOpePendiente"						
			,''													AS "BlockEnvio"							
			,GARAN."IdConcepto"									AS "IdConcepto"							
			,Concepto."Nombre"									AS "NombreConcepto"						
			,''													AS "IndicadorEnvio"						
			,GARAN."IdEmisor"									AS "IdEmisor"								
			,GARAN."EsFungible"									AS "EsFungible"							
			,GARAN."NemoSVS"									AS "NemoSVS"								
			,GARAN."IdTipoReajuste"								AS "IdTipoReajuste"		
			,''													AS "Categoria"				
			FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia"						GARAN
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"			TOPER 
				ON (GARAN."IdTipoOperacionOperacionOrigen" = TOPER."IdTipoOperacion")
				
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = GARAN."IdEmpresa" and Cli."IdCliente" = GARAN."IdCliente")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (GARAN."IdConcepto" = Concepto."IdConcepto")
			WHERE 	GARAN."IdTipoOperacionOperacionOrigen" 	= 'INGGTA';		
	
END;
