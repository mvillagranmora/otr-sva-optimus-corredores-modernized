set schema BSCL;


--335 CLEARING
--1
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","IdEmpresa","Nombre","Nemo","Estado","TipoEnvio") 
VALUES(335,1,'28 - CLEARING FINANCIERO','CLGFIN','V','M');

--3
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('TOTAL_VENTA','Total Venta'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('TOTAL_COMPRA','Total Compra'	,'V');

--4
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(335,'TOTAL_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(335,'TOTAL_COMPRA','V');

--2
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CLEARING','CLEARING','V');


--5
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 335,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CLEARING'
				);	

--6				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('Modalidad'     	,'V','N',0,0,0,'','N','V');
		
--7				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'Folio'								,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'Bolsa'								,
				'FechaDocumento'					,
				'CompraVenta'						,
				'ClaseInstrumento'					,
				'CondicionLiquidacion'				
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CLEARING'      
				);  			
	              


--330 CarterasFW

--1
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","IdEmpresa","Nombre","Nemo","Estado","TipoEnvio") 
VALUES(330,1,'27 - CARTERAS FORWARD','FW','V','M');

--3
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_RAZONABLE_ACTIVO','Valor a Razonable Activo'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_RAZONABLE_PASIVO','Valor a Razonable Pasivo'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_RAZONABLE_NETO','Valor a Razonable Neto'	,'V');

--4
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(330,'VALOR_RAZONABLE_ACTIVO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(330,'VALOR_RAZONABLE_PASIVO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(330,'VALOR_RAZONABLE_NETO','V');  

--2
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('COMFWD','COMPRA FORWARD','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENFWD','VENTA FORWARD','V');

--5
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 330,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('COMFWD',
				 'VENFWD'
				);	

--6				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('Modalidad'     	,'V','N',0,0,0,'','N','V');
		
--7				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				,
				'Modalidad'							, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				'TipoOperacion'						, 
				'Nemo'								,
				--'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'									
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('COMFWD',
				 'VENFWD'       
				);  			
	              



			
	
	
--325
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('UTILIDADSP','UTILIDAD SPOT','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('PERDIDASPO','PERDIDA SPOT','V');




INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 325,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('UTILIDADSP',
				 'PERDIDASPO'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES(''     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('UTILIDADSP',
				 'PERDIDASPO'        
				);  			
	

--320


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('Canal'     	,'V','N',0,0,0,'','N','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('COMSPT','COMPRA SPOT','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENSPT','VENTA SPOT','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 320,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('COMSPT',
				 'VENSPT'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES(''     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('COMSPT',
				 'VENSPT'         
				);  			



--315
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('APORFM','APORTE FFMM','V');
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('RESCFM','RESCATE FFMM','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 315,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('RESCFM'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('RESCFM'          
				);  			
	
	
	
	
	
	
--310
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('APORFM','APORTE FFMM','V');
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('RESCFM','RESCATE FFMM','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 310,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('APORFM'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('APORFM'          
				);  			
	
	
			
--305

--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('APORFM','APORTE FFMM','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('RESCFM','RESCATE FFMM','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 305,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('RESCFM'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('RESCFM'          
				);  			
			
				
--300
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('APORFM','APORTE FFMM','V');
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
--VALUES('RESCFM','RESCATE FFMM','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 300,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('APORFM'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				--'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				 
				--'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				--'Concepto',
				--'NombreConcepto',
				--'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('APORFM'          
				);  				
				
				
				
--215
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CORTECUPON','CORTE CUPON','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 215,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CORTECUPON'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				--'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				--'Nemo'								,
				--'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				--'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				'Concepto',
				'NombreConcepto',
				'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('PERDIDA',
				 'GANANCIA'           
				);  				
				


--295
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('PERDIDA','PERDIDA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('GANANCIA','GANANCIA','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 295,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('PERDIDA',
				 'GANANCIA'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('ISIN'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'TipoDocumento'						,
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				--'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				--'Nemo'								,
				--'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				--'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'					,				
				--'Fecha',
				--'Portafolio',
				'Concepto',
				'NombreConcepto',
				'ISIN'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('PERDIDA',
				 'GANANCIA'           
				);  				
				

--290
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ABONO','ABONO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CARGO','CARGO','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 290,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('ABONO',
				 'CARGO'
				);	
				
--INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
--VALUES('Fecha'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				--'PortafolioCliente'					,
				'Moneda'               				, 
				--'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				--'ClaseInstrumento'     				, 
				--'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				--'Nemo'								,
				--'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				'Fecha',
				'Portafolio',
				'Concepto',
				'NombreConcepto'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('ABONO',
				 'CARGO'              
				);  				

				

				
--285
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ABNMANUAL','ABONO MANUAL','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CARMANUAL','CARGO MANUAL','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 285,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('ABNMANUAL',
				 'CARMANUAL'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('Fecha'     	,'V','N',0,0,0,'','N','V');
		
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				--'PortafolioCliente'					,
				'Moneda'               				, 
				--'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				--'EsCuentaMandante' 					,
				--'EsCuentaDepositante'				,
				--'TipoPersona'						,
				'Producto'             				, 
				--'ClaseInstrumento'     				, 
				--'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				--'Nemo'								,
				--'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				--'TipoCartera'						,
				--'ClaseEmisor'						,	
				--'Agente'               				, 
				'NombreAgente'         				 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				'Fecha',
				'Portafolio',
				'NombreConcepto',
				'Concepto',
				'NombreConcepto'
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('ABNMANUAL',
				 'CARMANUAL'              
				);  				

	
--280
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('DIVIDENDO','VENCIMIENTO SIMULTANEA COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CORTECUPON','VENCIMIENTO SIMULTANEA VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('SORTEOLETR','ANTICIPO SIMULTANEA COMPRA','V');



INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 280,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('DIVIDENDO',
				 'CORTECUPON',
				 'SORTEOLETR'	
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('EsCuentaDepositante'     	,'V','N',0,0,0,'','N','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado")   
VALUES('EsCuentaMandante'     	,'V','N',0,0,0,'','N','V');				
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				,
				'EsCuentaMandante' 					,
				'EsCuentaDepositante'				,
				'TipoPersona'						,
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,
				--'IdGarantia'						,
				--'IdCartera'							,
				--'IdEmisor'							,
				--'IdTipoReajuste'					,
				--'CodigoIFRS'						,
				'TipoCartera'						,
				--'ClaseEmisor'						,	
				--'Agente'               				, 
				'NombreAgente'         				 
				--'SucursalAgente'       				, 
				--'NombreSucursalAgente' 				, 
				--'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('DIVIDENDO',
				 'CORTECUPON',
				 'SORTEOLETR'	              
				);  				



--275
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENSIMCOMP','VENCIMIENTO SIMULTANEA COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENSIMVENT','VENCIMIENTO SIMULTANEA VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ANTSIMCOMP','ANTICIPO SIMULTANEA COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ANTSIMVENT','ANTICIPO SIMULTANEA VENTA','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 275,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VENSIMCOMP',
				 'VENSIMVENT',
				 'ANTSIMCOMP',
				 'ANTSIMVENT'	
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VENSIMCOMP',
				 'VENSIMVENT',
				 'ANTSIMCOMP',
				 'ANTSIMVENT'	              
				);  				

--270
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('SIMVENTA','SIMULTANEA VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('SIMCOMPRA','SIMULTANEA COMPRA','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 270,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('SIMVENTA',
				 'SIMCOMPRA'	
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('SIMVENTA',
				 'SIMCOMPRA'              
				);  					
			
				
--265
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENPREST','VENCIMIENTO PRESTAMO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VENEMPRE','VENCIMIENTO EMPRESTITO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ANTPREST','ANTICIPO PRESTAMO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('ANTEMPRE','ANTICIPO EMPRESTITO','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 265,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VENPREST',
				 'VENEMPRE',
				 'ANTPREST',
				 'ANTEMPRE'		
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VENPREST',
				 'VENEMPRE',
				 'ANTPREST',
				 'ANTEMPRE'	              
				);  					
								
				
				
--260
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('PRESTAMO','PRESTAMO','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('EMPRESTITO','EMPRESTITO','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 260,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('PRESTAMO',
				 'EMPRESTITO'	
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('PRESTAMO',
				 'EMPRESTITO'                
				);  					
				
--255
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VECTOIF','VENCIMIENTO IF','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('VECTORF','VENCIMIENTO RF','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 255,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VECTOIF',
				 'VECTORF'	
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				--'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VECTOIF',
				 'VECTORF'	                
				);  				

		
				
--235
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")
VALUES('CARTCOMTER','CARTERA COMPRA TERMINO','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 235,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTCOMTER'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				'TipoOperacion'						, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'						
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTCOMTER'                 
				);  				

			
--230
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")VALUES('VTATERRFIF','VENTA TERMINO RFIF RF','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 230,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VTATERRFIF'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'				
							
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('VTATERRFIF'                 
				);  				
--250
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")VALUES('CARTERARF','CARTERA RF','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 250,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTERARF'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'				
							
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTERARF'                 
				);  	
	
				
--245
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")VALUES('CARTERARV','CARTERA RV','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 245,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTERARV'
				);	
				
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				'DocumentoIdentidad'				,
				'NombreCliente'						,
				'PortafolioCliente'					,
				'Moneda'               				, 
				'EsRelacionado'        				, 
				'EsCuentaPropia'       				, 
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				--'TipoInstrumentoGarantizado'		, 
				'Nemo'								,
				'Boveda'							,	
				--'IdGarantia'						,
				'IdCartera'							,
				'IdEmisor'							,
				'IdTipoReajuste'					,
				'CodigoIFRS'						,
				'TipoCartera'						,
				'ClaseEmisor'						,	
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'CentroCostoAgente'				
							
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('CARTERARV'                 
				);  
				
--------------------
--250




INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(320,'25 - SPOT INGRESOS',1,'SPOTING','V','M');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_PRINCIPAL_SPOT_COMPRA','Valor Moneda Principal Spot Compra','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_PRINCIPAL_SPOT_VENTA','Valor Moneda Principal Spot Venta','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_SECUNDARIA_SPOT_COMPRA','Valor Moneda Secundaria Spot Compra','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_SECUNDARIA_SPOT_VENTA','Valor Moneda Secundaria Spot Venta','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_COMPRA','Valor Moneda Principal Equivalente Spot Compra','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_VENTA','Valor Moneda Principal Equivalente Spot Venta','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_SECUNDARIA_EQUIVALENTE_SPOTCOMPRA','Valor Moneda Secundaria Equivalente Spot Compra','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('MONEDA_SECUNDARIA_EQUIVALENTE_SPOT_VENTA','Valor Moneda Secundaria Equivalente Spot Venta','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_PRINCIPAL_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_PRINCIPAL_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_SECUNDARIA_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_SECUNDARIA_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_SECUNDARIA_EQUIVALENTE_SPOTCOMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(320,'MONEDA_SECUNDARIA_EQUIVALENTE_SPOT_VENTA','V');



INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(325,'26 - SPOT UTILIDAD/PERDIDA',1,'SPOTUP','V','M');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_PRINCIPAL_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_PRINCIPAL_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_SECUNDARIA_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_SECUNDARIA_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_COMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_VENTA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_SECUNDARIA_EQUIVALENTE_SPOTCOMPRA','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(325,'MONEDA_SECUNDARIA_EQUIVALENTE_SPOT_VENTA','V');


INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")VALUES('UTILIDADSP','UTILIDAD SPOT','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento","Nombre","Estado")VALUES('PERDIDASPO','PERDIDA SPOT','V');
		
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo"
SELECT 325,"IdTipoMovimiento",'V'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('UTILIDADSP'
				,'PERDIDASPO'
				);		 		
		 		
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
SELECT "IdTipoMovimiento","IdTipoDatoEvento",'V' 
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" MovimientoTipo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EventoTipoDato ON 
		EventoTipoDato."IdTipoDatoEvento" IN
				(
				
				'Producto'             				, 
				'ClaseInstrumento'     				, 
				'TipoInstrumento'					, 
				'NombreCliente'        				,
				'Portafolio'           				, 
				'EsCuentaPropia'       				, 
				'EsRelacionado'        				, 
				'Agente'               				, 
				'NombreAgente'         				, 
				'SucursalAgente'       				, 
				'NombreSucursalAgente' 				, 
				'Moneda'               				, 
				'Emisor'							,	
				'Nemotecnico'						,	
				'Boveda'							,	
				'IdCartera'							,
				'TipoReajuste'						,
				'CodigoIFRS'					
				)	
WHERE MovimientoTipo."IdTipoMovimiento" IN
				('UTILIDADSP'
				,'PERDIDASPO'                     
				);  

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(300,'21 - APORTE FONDO MUTUO CUENTA PROPIA',1,'FFMMAP','V','M');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('APORTE_FONDO_MUTUO_CUENTA_PROPIA','Aporte Fondo Mutuo Cuenta Propia','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(300,'APORTE_FONDO_MUTUO_CUENTA_PROPIA','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(305,'22 - RESCATE FONDO MUTUO CUENTA PROPIA',1,'FFMMRP','V','M');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('RESCATE_FONDO_MUTUO_CUENTA_PROPIA','Rescate Fondo Mutuo Cuenta Propia','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(305,'RESCATE_FONDO_MUTUO_CUENTA_PROPIA','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(310,'23 - APORTE FONDO MUTUO CUENTA TERCEROS',1,'FFMMAT','V','M');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('APORTE_FONDO_MUTUO_CUENTA_TERCEROS','Aporte Fondo Mutuo Cuenta Terceros','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(310,'APORTE_FONDO_MUTUO_CUENTA_TERCEROS','V');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion","Nombre","IdEmpresa","Nemo","Estado","TipoEnvio") 
VALUES(315,'24 - RESCATE FONDO MUTUO CUENTA TERCEROS',1,'FFMMRT','V','M');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") 
VALUES('RESCATE_FONDO_MUTUO_CUENTA_TERCEROS','Rescate Fondo Mutuo Cuenta Terceros','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") 
VALUES(315,'RESCATE_FONDO_MUTUO_CUENTA_TERCEROS','V');

--280 16 - EVENTOS DE CAPITAL
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTOEVENTOCAPITAL'					,'Monto Evento de Capital'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(280,'MONTOEVENTOCAPITAL'					,'V');


-- 230
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENTA_RFIF'					,'Monto Venta RF IF'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_POSITIVO_TASA_COMPRA_RFIF'	,'Spread Positivo a Tasa Compra RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_NEGATIVO_TASA_COMPRA_RFIF'	,'Spread Negativo a Tasa Compra RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_POSITIVO_TASA_MERCADO_RFIF','Spread Positivo a Tasa Mercado RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_NEGATIVO_TASA_MERCADO_RFIF','Spread Negativo a Tasa Mercado RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENTA_TASA_COMPRA','Monto Venta Tasa Compra'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENTA_TASA_MERCADO','Monto Venta Tasa Mercado'	,'V');


--230 Ventas RF IF a Terminio y Spread
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'MONTO_VENTA_RFIF'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'SPREAD_POSITIVO_TASA_COMPRA_RFIF'	,'V'); 					
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'SPREAD_NEGATIVO_TASA_COMPRA_RFIF'	,'V'); 					
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'SPREAD_POSITIVO_TASA_MERCADO_RFIF','V'); 					
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'SPREAD_NEGATIVO_TASA_MERCADO_RFIF','V'); 	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'MONTO_VENTA_TASA_COMPRA','V'); 
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(230,'MONTO_VENTA_TASA_MERCADO','V'); 
	

--220 GARANTIAS CUENTA PROPIA
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'VALOR_ACTUAL_MERCADO'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'VALOR_ANTERIOR_MERCADO'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'VALOR_ACTUAL_COMPRA'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'VALOR_ANTERIOR_COMPRA'		,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'VALOR_PROMEDIO_COMPRA'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MAYOR_VALOR_MERCADO'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MENOR_VALOR_MERCADO'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MAYOR_VALOR_PROMEDIO_COMPRA','V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MENOR_VALOR_PROMEDIO_COMPRA','V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MAYOR_VALOR_COMPRA'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(220,'MENOR_VALOR_COMPRA'		,'V');                


--220 GARANTIAS CUENTA PROPIA
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO'		,'Valor Mercado Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO'	,'Valor Mercado Anterior','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_COMPRA'		,'Valor Compra Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_COMPRA'		,'Valor Compra Anterior'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROMEDIO_COMPRA'		,'Valor Precio Promedio Compra'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_MERCADO'			,'Mayor Valor Mercado'			,'V');			
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_MERCADO'			,'Menor Valor Mercado'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_PROMEDIO_COMPRA'	,'Mayor Valor Promedio Compra'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_PROMEDIO_COMPRA'	,'Menor Valor Promedio Compra'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_COMPRA'			,'Mayor Valor Compra'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_COMPRA'			,'Menor Valor Compra'			,'V');


--285 CTA INVERSION
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(285,'ABONO'						,'V');            
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(285,'CARGO'						,'V');      

--285 CTA INVERSION
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('ABONO','Abono','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('CARGO','Cargo','V');

--275 ANTICIPO/VENCIMIENTO SIMULTANEAS
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(275,'MONTO_VENCIMIENTO_SIMULTANEA_COMPRA'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(275,'MONTO_VENCIMIENTO_SIMULTANEA_VENTA'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(275,'MONTO_ANTICIPO_SIMULTANEA_COMPRA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(275,'MONTO_ANTICIPO_SIMULTANEA_VENTA'			,'V'); 


--275 ANTICIPO/VENCIMIENTO SIMULTANEAS
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_SIMULTANEA_COMPRA'	,'Monto Vencimiento Simultanea Compra'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_SIMULTANEA_VENTA'	,'Monto Vencimiento Simultanea Venta'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_ANTICIPO_SIMULTANEA_COMPRA'		,'Monto Anticipo Simultanea Compra'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_ANTICIPO_SIMULTANEA_VENTA'		,'Monto Anticipo Simultanea Venta'		,'V');


--270 Eventos/Centralizacion de Simultaneas                                                                                                                                                                      
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ACTUAL_MERCADO_SIMULTANEA_COMPRA'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ACTUAL_MERCADO_SIMULTANEA_VENTA'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ANTERIOR_MERCADO_SIMULTANEA_COMPRA'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ANTERIOR_MERCADO_SIMULTANEA_VENTA'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ACTUAL_SIMULTANEA_COMPRA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ACTUAL_SIMULTANEA_VENTA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_PROXIMO_SIMULTANEA_COMPRA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_PROXIMO_SIMULTANEA_VENTA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ANTERIOR_SIMULTANEA_COMPRA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'VALOR_ANTERIOR_SIMULTANEA_VENTA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'INTERES_PROXIMO_SIMULTANEA_COMPRA'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'INTERES_PROXIMO_SIMULTANEA_VENTA'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'INTERES_ANTERIOR_SIMULTANEA_COMPRA'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(270,'INTERES_ANTERIOR_SIMULTANEA_VENTA'		,'V');


--270 INTERESES	SIMULTANEAS Y VALOR ACTUAL
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_SIMULTANEA_COMPRA'	,'Valor Simultanea Compra Tasa Mercado Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_SIMULTANEA_VENTA'	,'Valor Simultanea Venta Tasa Mercado Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_SIMULTANEA_COMPRA'	,'Valor Simultanea Compra Tasa Mercado Anterior','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_SIMULTANEA_VENTA'	,'Valor Simultanea Venta Tasa Mercado Anterior'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_SIMULTANEA_COMPRA'			,'Valor Simultanea Compra Actua'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_SIMULTANEA_VENTA'			,'Valor Simultanea Venta Actua'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_SIMULTANEA_COMPRA'			,'Valor Simultanea Compra Proximo'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_SIMULTANEA_VENTA'			,'Valor Simultanea Venta Proximo'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_SIMULTANEA_COMPRA'			,'Valor Simultanea Compra Anterior'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_SIMULTANEA_VENTA'			,'Valor Simultanea Venta Anterior'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_SIMULTANEA_COMPRA'		,'Interes Simultanea Compra Proximo'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_SIMULTANEA_VENTA'			,'Interes Simultanea Venta Proximo'				,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_SIMULTANEA_COMPRA'		,'Interes Simultanea Compra Anterior'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_SIMULTANEA_VENTA'		,'Interes Simultanea Venta Anterior'			,'V');


--265
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(265,'MONTO_VENCIMIENTO_PRESTAMO'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(265,'MONTO_VENCIMIENTO_EMPRESTITO'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(265,'MONTO_ANTICIPO_PRESTAMO'			,'V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(265,'MONTO_ANTICIPO_EMPRESTITO'		,'V');	

--265
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_PRESTAMO'		,'Monto Vencimiento Prestamo','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_EMPRESTITO'		,'Monto Vencimiento Emprestito','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_ANTICIPO_PRESTAMO'			,'Monto Anticipo Prestamo','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_ANTICIPO_EMPRESTITO'		,'Monto Anticipo Emprestito','V');


--260Prestamos y Emprestitos
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ACTUAL_MERCADO_PRESTAMO'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ACTUAL_MERCADO_EMPRESTITO'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ANTERIOR_MERCADO_PRESTAMO'	,'V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ANTERIOR_MERCADO_EMPRESTITO','V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ACTUAL_PRESTAMO'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ACTUAL_EMPRESTITO'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_PROXIMO_PRESTAMO'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_PROXIMO_EMPRESTITO'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ANTERIOR_PRESTAMO'			,'V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'VALOR_ANTERIOR_EMPRESTITO'		,'V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'INTERES_PROXIMO_PRESTAMO'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'INTERES_PROXIMO_EMPRESTITO'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'INTERES_ANTERIOR_PRESTAMO'		,'V');	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(260,'INTERES_ANTERIOR_EMPRESTITO'		,'V');

--260Prestamos y Emprestitos
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_PRESTAMO'	,'Valor Prestamo Tasa Mercado Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_EMPRESTITO'	,'Valor Emprestito Tasa Mercado Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_PRESTAMO'	,'Valor Prestamo Tasa Mercado Anterior'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_EMPRESTITO','Valor Emprestito Tasa Mercado Anterior','V');                                                                                                                     
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_PRESTAMO'			,'Valor Prestamo Tasa Compra Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_EMPRESTITO'			,'Valor Emprestito Tasa Compra Actual'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_PRESTAMO'			,'Valor Prestamo Tasa Compra Proximo'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_EMPRESTITO'			,'Valor Emprestito Tasa Compra Proximo'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_PRESTAMO'			,'Valor Prestamo Tasa Compra Anterior'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_EMPRESTITO'		,'Valor Emprestito Tasa Compra Anterior','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_PRESTAMO'			,'Interes Prestamo Tasa Compra Proximo'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_EMPRESTITO'		,'Interes Emprestito Tasa Compra Proximo','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_PRESTAMO'		,'Interes Prestamo Tasa Compra Anterior','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_EMPRESTITO'		,'Interes Emprestito Tasa Compra Anterior','V');

--255Eventos/Centralizacion Eventos Vencimientos RF IF                                                                                                                                                                  
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(255,'MONTO_VENCIMIENTO_IF'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(255,'MONTO_VENCIMIENTO_RF'			,'V'); 					

--255Eventos Vencimientos RF IF
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_IF'	,'Monto Vencimiento IF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENCIMIENTO_RF'	,'Monto Vencimiento RF'		,'V');

--250Eventos/Centralizacion Carteras a Termino RF                                                                                                                                                              
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_ACTUAL_MERCADO_RF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_PROXIMO_MERCADO_RF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_ANTERIOR_MERCADO_RF'	,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'INTERES_PROXIMO_MERCADO_RF'	,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'INTERES_ANTERIOR_MERCADO_RF'	,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'REAJUSTE_PROXIMO_MERCADO_RF'	,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'REAJUSTE_ANTERIOR_MERCADO_RF' ,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_ACTUAL_COMPRA_RF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_PROXIMO_COMPRA_RF'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'VALOR_ANTERIOR_COMPRA_RF'		,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'INTERES_PROXIMO_COMPRA_RF'	,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'INTERES_ANTERIOR_COMPRA_RF'	,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'REAJUSTE_PROXIMO_COMPRA_RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'REAJUSTE_ANTERIOR_COMPRA_RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'MENOR_VALOR_RF'				,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(250,'MAYOR_VALOR_RF'				,'V');

--250 -Eventos Carteras a Termino RF y spread
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_RF'		,'Valor Tasa Mercado Proximo RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_MERCADO_RF'		,'Valor Tasa Mercado Proximo RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_RF'	,'Valor Tasa Mercado Anterior RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_MERCADO_RF'	,'Interes Tasa Mercado Proximo RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_MERCADO_RF'	,'Interes Tasa Mercado Anterior RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('REAJUSTE_PROXIMO_MERCADO_RF'	,'Reajuste Tasa Mercado Proximo RF'	 ,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('REAJUSTE_ANTERIOR_MERCADO_RF'	,'Reajuste Tasa Mercado Anterior RF','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_COMPRA_RF'		,'Valor Tasa Compra Actual RF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROXIMO_COMPRA_RF'		,'Valor Tasa Compra Proximo RF'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_COMPRA_RF'		,'Valor Tasa Compra Anterior RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_PROXIMO_COMPRA_RF'	,'Interes Tasa Compra Proximo RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('INTERES_ANTERIOR_COMPRA_RF'	,'Interes Tasa Compra Anterior RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('REAJUSTE_PROXIMO_COMPRA_RF'	,'Reajuste Tasa Compra Proximo RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('REAJUSTE_ANTERIOR_COMPRA_RF'	,'Reajuste Tasa Compra Anterior RF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_RF'				,'Menor Valor RF'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_RF'				,'Mayor Valor RF'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MONTO_VENTA_RFIF'					,'Monto Venta RF IF'					,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_POSITIVO_TASA_COMPRA_RFIF'	,'Spread Positivo a Tasa Compra RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_NEGATIVO_TASA_COMPRA_RFIF'	,'Spread Negativo a Tasa Compra RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_POSITIVO_TASA_MERCADO_RFIF','Spread Positivo a Tasa Mercado RF IF'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('SPREAD_NEGATIVO_TASA_MERCADO_RFIF','Spread Negativo a Tasa Mercado RF IF'	,'V');

--245 Eventos/Centralizacion Carteras a Termino RV                                                                                                                                                              
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'VALOR_ACTUAL_MERCADO_RV'		,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'VALOR_ANTERIOR_MERCADO_RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'VALOR_ACTUAL_COMPRA_RV'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'VALOR_ANTERIOR_COMPRA_RV'		,'V');               
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'VALOR_PROMEDIO_COMPRA_RV'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MAYOR_VALOR_MERCADO_RV'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MENOR_VALOR_MERCADO_RV'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MAYOR_VALOR_PROMEDIO_COMPRA_RV','V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MENOR_VALOR_PROMEDIO_COMPRA_RV','V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MAYOR_VALOR_COMPRA_RV'		,'V');                
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado") VALUES(245,'MENOR_VALOR_COMPRA_RV'		,'V');                

--245 Eventos Carteras a Termino RV
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_MERCADO_RV'		,'Valor Mercado Actual RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_MERCADO_RV'	,'Valor Mercado Anterior RV','V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ACTUAL_COMPRA_RV'		,'Valor Compra Actual RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_ANTERIOR_COMPRA_RV'		,'Valor Compra Anterior RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('VALOR_PROMEDIO_COMPRA_RV'		,'Valor Precio Promedio Compra RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_MERCADO_RV'			,'Mayor Valor Mercado RV'			,'V');			
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_MERCADO_RV'			,'Menor Valor Mercado RV'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_PROMEDIO_COMPRA_RV'	,'Mayor Valor Promedio Compra RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_PROMEDIO_COMPRA_RV'	,'Menor Valor Promedio Compra RV'	,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MAYOR_VALOR_COMPRA_RV'			,'Mayor Valor Compra RV'			,'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento","Nombre","Estado") VALUES('MENOR_VALOR_COMPRA_RV'			,'Menor Valor Compra RV'			,'V');


----Eventos/Centralizacion Facturas RF IF x CP
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado")
select 205, "IdCobro", 'V'
from  "BSCL.Plataforma.BD::COM.Cobro"
where "IdCobro" NOT IN(select "IdTipoEvento" from "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo"
						where "IdTipoCentralizacion" = 205);

--Eventos/Centralizacion Facturas
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion","IdTipoEvento","Estado")
select 200, "IdCobro", 'V'
from  "BSCL.Plataforma.BD::COM.Cobro"
where "IdCobro" NOT IN(select "IdTipoEvento" from "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo"
						where "IdTipoCentralizacion" = 200);


