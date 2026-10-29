SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ConsultaCobrosClientes_OUT_Cobros";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ConsultaCobrosClientes_OUT_Cobros"(
	 "IdCliente"						BIGINT
	,"NombreCliente"					NVARCHAR(300)
	,"DocumentoIdentidad"				NVARCHAR(40)
	,"IdTipoCliente"					NVARCHAR(40)
	,"IdPortafolio"						BIGINT
	,"Portafolio"						NVARCHAR(300)								
	,"TipoContratoCobroPeriodico"		NVARCHAR(520)
	,"FormatoContratoCobroPeriodico"	NVARCHAR(520)
	,"NombreCompletoEjecutivo"			NVARCHAR(300)
	,"NombreSucursal"					NVARCHAR(520)
	,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
	,"IdTipoInstrumento"				NVARCHAR(3)
	,"NombreTipoInstrumento"			NVARCHAR(300)
	,"IdMonedaComprobante"				NVARCHAR(3)
	,"MontoComprobante"					DECIMAL(26,8)
	,"MontoCobroComprobante"			DECIMAL(26,8)
	,"MontoFinal"						DECIMAL(26,8)
	,"TotalComision"					DECIMAL(26,8)
	,"TotalDerecho"						DECIMAL(26,8)
	,"TotalGasto"						DECIMAL(26,8)
	,"TotalImpuesto"					DECIMAL(26,8)
	,"TotalRetencion"					DECIMAL(26,8)
) ON COMMIT DELETE ROWS;

COMMIT;