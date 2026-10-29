SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarFormasPagoOperacion";

CREATE VIEW  "BSCL.Mesa.BD.Vistas::ConsultarFormasPagoOperacion" AS 

SELECT 
	 COMP."IdComprobantePagoOperacion"
	,COMP."IdEmpresa"
	,COMP."IdOperacion"
	,COMP."IdIngresoEgreso"
	,COMP."IdAbono" 
	,COMP."IdCargo" 
	,COMP."AfectarTesoreria"
	,COMP."IdTipoProducto" 
	,COMP."IdFormaDePago" 
	,COMP."NombreFormaDePago"
	,COMP."IdPreferenciaPago"
	,COMP."IdMedioDePago"
	,COMP."NombreMedioDePago" AS "NombreMedioPago"
	,COMP."RecibePaga" AS "RecibePagaPortafolio"
	,'' AS "RecibePagaDesc"
	,COMP."IdModalidadPago" 
	,COMP."IdMoneda"
	,COMP."Monto" 
	,COMP."FechaIngresoComprobantePagoOperacion" 
	,COMP."FechaLiquidacionComprobantePagoOperacion" 
	,COMP."IdEstado"
	,COMP."IdCuentaBancaria"
	,COMP."IdBanco" 
	,COMP."NumeroCuentaBancaria" 
	,COMP."PorDefecto"
	,COMP."NombreBancoCuentaBancaria"
	,COMP."IdTipoOperacionBase" AS "IdTipoOperacion"
FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacionBanco" AS COMP;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarFormasPagoOperacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;

