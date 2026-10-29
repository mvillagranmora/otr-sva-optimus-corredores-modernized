SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotOtros";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotOtros" AS
SELECT 	"IdOperacionSpot" AS "IdOperacion"
		,M."Nombre" AS "Mesa" 
		,TD."TipoDolar"
		,TC."Nombre" AS "TipoComprobante"
		,(AG."Nombres" || ' ' || AG."ApellidoPaterno") AS "NombreAgente"
		,OS."IdMonedaLocal" AS "IdMoneda"
		,CASE 
			WHEN OS."BenOur" = 'BEN' THEN 'SI'
			WHEN OS."BenOur" = 'OUR' THEN 'NO'
			ELSE '' END AS "Ben"
		,OS."EquivalenteConGasto"
		,CASE 
			WHEN OS."BenOur" = 'OUR' THEN 'SI'
			WHEN OS."BenOur" = 'BEN' THEN 'NO'
			ELSE '' END AS "Our"		
		,OS."TipoCambioCierreConGasto"
		,CASE 
			WHEN OS."OperacionFicticia" = 'S' THEN 'SI'
			WHEN OS."OperacionFicticia" = 'N' THEN 'NO'
			ELSE '' END AS "OperacionFicticia"			
		,PBC."IdPlanillaBancoCentral" || ' - ' || PBC."Descripcion" as "NombrePlanillaBancoCentral"
		,OS."Observacion"
		,CC."IdPersona"  AS "IdContacto"
		,CC."Nombre"  AS "NombreContacto"
		,CC."CorreoElectronico" AS "CorreoContacto"
		,CC."Numero"  AS "TelefonoContacto"
		,CC."Calle"
		,CC."NumeroCalle"
		,CC."NombreCiudad"
		,CC."NombrePais"
		,OS."TipoDocumento"
  FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OS
  LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" M ON M."IdMesa" = OS."IdMesa"	
  LEFT JOIN (SELECT  "IdEmpresa"
  					,"IdParametro" AS "IdTipoDolar"
  					,"ValorParametro" AS "TipoDolar"
  			  FROM "BSCL.Mesa.BD::MES.Parametro" 
  			 WHERE "NombreTipoParametro" = 'TipoDolar') AS TD ON TD."IdTipoDolar" = OS."IdTipoDolar" AND TD."IdEmpresa" = OS."IdEmpresa"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" TC ON TC."IdTipoComprobante" = OS."TipoComprobanteFacturacion"  
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG ON AG."IdPersona" = OS."IdAgente"
  LEFT JOIN "BSCL.Mesa.BD.Vistas::MES.ConsultarContactos" CC ON CC."IdCliente" = OS."IdCliente" AND CC."IdPersona" = OS."IdContacto"
  LEFT JOIN "BSCL.Mesa.BD::MES.PlanillaBancoCentral" PBC ON OS."CodigoComercio" = PBC."IdPlanillaBancoCentral"
;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotOtros" TO BCS_DESARROLLADOR WITH GRANT OPTION;