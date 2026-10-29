SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteCierre";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteCierre"	 
AS
SELECT
	 cast(0 as BIGINT)														"NumeroCargoAbono",
	 cast(0 as BIGINT)														"IdPortafolio",
	 cast(' ' as NVARCHAR(3))												"IdMoneda",
 	 cast(' ' as NVARCHAR(6))												"IdTipoMovimiento",
 	 cast(' ' as NVARCHAR(6))												"IdConcepto",
 	 cast(' ' as NVARCHAR(260))												"Descripcion",
  	 cast(' ' as NVARCHAR(2))												"IdCondicionLiquidacion",
  	 cast(' ' as NVARCHAR(10))												"FechaCondicionLiquidacion",
	 cast(0 as DECIMAL(26,8))												"Monto",
  	 cast(' ' as NVARCHAR(4))												"IdTipoProducto",
 	 cast(' ' as NVARCHAR(6))												"IdTipoOperacion",
	 cast(0 as BIGINT)														"IdOperacion",	 
 	 cast(' ' as NVARCHAR(40))												"FechaOperacion",
 	 cast(' ' as NVARCHAR(40))												"FechaLiquidacionOperacion",
 	 cast(' ' as NVARCHAR(40))												"FechaVencimientoOperacion",
 	 cast(' ' as NVARCHAR(3))												"IdMonedaOperacion",
	 cast(0 as DECIMAL(26,8))												"MontoOperacion",
	 cast(0 as DECIMAL(26,8))												"TipoCambioOperacion",
	 cast(0 as BIGINT)														"IdContraparte",	 
 	 cast(' ' as NVARCHAR(3))												"IdTipoComprobante",
	 cast(0 as BIGINT)														"IdComprobante",	 
 	 cast(' ' as NVARCHAR(40))												"FechaComprobante",
	 cast(0 as BIGINT)														"IdComprobanteDetalle",	 
	 cast(0 as BIGINT)														"IdPersonaPagoCobro",	 
	 cast(0 as BIGINT)														"IdInstrumento",	 
	 cast(' ' as NVARCHAR(3))												"IdTipoInstrumento",
	 cast(' ' as NVARCHAR(40))												"Nemo",
	 cast(' ' as NVARCHAR(1))												"EsIndicadorBilateral",
	 cast(' ' as NVARCHAR(1))												"EsCargoAbonoPorTraspaso",
	 cast(0 as BIGINT)														"IdPortafolioTraspaso"
FROM dummy;	

											    
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteCierre" TO BCS_DESARROLLADOR WITH GRANT OPTION;
