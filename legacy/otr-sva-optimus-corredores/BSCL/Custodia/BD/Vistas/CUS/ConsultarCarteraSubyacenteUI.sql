SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteUI";


CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteUI"   AS 
SELECT
	 suby."FolioSubyacente"
	,suby."FolioCarteraRelacionada"
	,suby."IdClasificacionTipoInstrumento"
	,suby."IdTipoInstrumento"
	,TINS."Codigo" as "CodigoTipoInstrumento"
	,TINS."Nombre" as "NombreTipoInstrumento"
	,suby."IdInstrumento"
	,suby."Nemotecnico"
	,suby."IdEmisor"
--	,suby."IdBovedaOperacionRelacionada" AS "IdBoveda" 
--	,suby."IdMonedaSistemaOperacionRelacionada" AS "IdMonedaSistema"
	,suby."IdMonedaInstrumento"
	,suby."IdTipoReajuste"
--	,suby."IdMonedaSistemaOperacionRelacionada" AS "IdMonedaMontos"  -- Podría corresponder a moneda intrumento o tipo resjuste si los montos informados son los expresados en estas monedas
	,suby."INGCantidad"	AS "Cantidad"
	,suby."INGValorRescate"	AS "ValorRescate"
	
	
	,suby."INGPrecioSubyacente" as "PrecioSubyacente"
	,suby."INGTasaSubyacente" as "TasaSubyacente"	
	,suby."INGMontoATasaSubyacenteActual" as "MontoATasaSubyacenteActual"

	,suby."INGPrecioMercadoActual" as "PrecioMercado"	
	,suby."INGTasaMercadoActual" as "TasaMercado"
	,suby."INGMontoATasaMercadoActual" as "MontoATasaMercadoActual"
	
	
	,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(suby."EsFungible") AS "EsFungible"
FROM "BSCL.Custodia.BD::CUS.Subyacente" as suby
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TINS ON TINS."IdTipoInstrumento" = suby."IdTipoInstrumento"
WITH READ ONLY;
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteUI"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;