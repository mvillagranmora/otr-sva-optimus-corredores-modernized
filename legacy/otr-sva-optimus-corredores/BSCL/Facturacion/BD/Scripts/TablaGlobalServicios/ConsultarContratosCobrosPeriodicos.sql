SET SCHEMA "BSCL";
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" cascade;
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes"
    (  	 "IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)   		
   		,"MontoFactura"							BIGINT
   		,"MontoCobroFactura"					BIGINT
   		,"MontoNotaCredito"						BIGINT
   		,"MontoCobroNotaCredito"				BIGINT
   		,"MontoFacturaOtros"					BIGINT
   		,"MontoCobroFacturaOtros"				BIGINT
   		,"MontoNotaCreditoOtros"				BIGINT
   		,"MontoCobroNotaCreditoOtros"			BIGINT
    ) ON COMMIT DELETE ROWS;
GO    

DROP TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" cascade;
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos"
    (	 "IdContratoCobroPeriodico"				BIGINT
	   	,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"DocumentoIdentidad"					NVARCHAR(40)
   		,"NombreCliente" 						NVARCHAR(302)
   		,"IdTipoContratoCobroPeriodico"			BIGINT
   		,"NombreContrato"						NVARCHAR(302)
   		,"FormatoContratoCobroPeriodico"		BIGINT
   		,"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdConceptoCobroPeriodico"				NVARCHAR(6)
   		,"IdCobro"								NVARCHAR(4)
   		,"MontoMinimoACobrar"					BIGINT
		,"MontoMaximoACobrar"					BIGINT
		,"TipoCobroPeriodico"					NVARCHAR(1)
		,"PorRango"								NVARCHAR(1)
		,"MontoCobroPeriodico"					BIGINT
		,"PorcentajeCobroPeriodico"				DECIMAL(26,8)
		,"MontoDesde"							BIGINT
		,"MontoHasta"							BIGINT
    ) ON COMMIT DELETE ROWS;
GO    

DROP TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" cascade;
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros"
    (  	 "IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)   		
   		,"MontoOperado"							BIGINT
   		,"MontoComision"						BIGINT
   		,"MontoDerecho"							BIGINT
    ) ON COMMIT DELETE ROWS;
GO    
