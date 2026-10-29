SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotAnalitica"; 

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotAnalitica" AS
SELECT
 	 		 OS."IdEmpresa"
			,CASE WHEN COALESCE(OS."FechaIngresoRegistro",'') = '' THEN '0001-01-01 00:00:00'
				 ELSE
				 	CASE WHEN OS."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00'
				 		 ELSE LEFT(OS."FechaIngresoRegistro",27) END END AS "FechaIngresoRegistro"
			,CASE WHEN OS."IdOperacionSpot" = 0 THEN NULL ELSE OS."IdOperacionSpot" END AS "IdOperacion"
			,CASE WHEN OS."IdTipoOperacion" = '' THEN NULL ELSE OS."IdTipoOperacion" END AS "IdTipoOperacion"
			,CAST(COALESCE(CO."Nombre",'') AS NVARCHAR(100)) AS "NombreTipoOperacion"
			,CASE WHEN OS."IdMesa" = 0 THEN NULL ELSE OS."IdMesa" END AS "IdMesa"
			,CAST(COALESCE(MES."Nombre",'') AS NVARCHAR(30)) AS "NombreMesa"
			,LEFT(TO_CHAR(COALESCE("FechaOperacion",'0001-01-01')),10)	AS "FechaOperacion"
			,CASE WHEN CLI."IdPersona" = 0 THEN NULL ELSE CLI."IdPersona" END AS "IdPersona"
			,CASE WHEN CLI."IdCliente" = 0 THEN NULL ELSE CLI."IdCliente" END AS "IdCliente"
			,CAST(COALESCE(IDENT."DocumentoIdentidad",'') AS NVARCHAR(40)) AS "DocumentoIdentidadCliente"
			,CAST(COALESCE(CASE "PER"."Tipo" 
				WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
				ELSE "PER"."RazonSocial"
			END,'') AS NVARCHAR(302)) AS "NombreCliente"
			,CASE WHEN OS."IdPortafolio" = 0 THEN NULL ELSE OS."IdPortafolio" END AS "IdPortafolio"
			,CAST(COALESCE(CP."CodigoPortafolio",'') AS NVARCHAR(100)) AS "NombrePortafolio"
			,CAST(COALESCE(CP."CodigoPortafolio",'') AS NVARCHAR(100)) AS "CodigoPortafolio"
			,CASE WHEN "IdMonedaPrincipal" = '' THEN NULL ELSE "IdMonedaPrincipal" END AS "IdMonedaPrincipal"
			,COALESCE("MontoMonedaPrincipal",0)				AS "MontoMonedaPrincipal"
			,CASE WHEN "IdMonedaSecundaria" = '' THEN NULL ELSE "IdMonedaSecundaria" END AS "IdMonedaSecundaria"
			,COALESCE("MontoMonedaSecundaria",0)			AS "MontoMonedaSecundaria"
			,COALESCE("ParidadCosto",0)						AS "ParidadCosto"
			,COALESCE("TipoCambioCosto",0)					AS "TipoCambioCosto"
			,COALESCE("ParidadCierre",0)					AS "ParidadCierre"
			,COALESCE("TipoCambioCierre",0)					AS "TipoCambioCierre"
			,COALESCE("Margen",0)							AS "Margen"
			,CASE WHEN OS."IdTipoCanalIngreso" = 'FWD' THEN 'S' ELSE 'N' END AS "OrigenForward"
			,'SPOT'											AS "Producto"
			,CAST(PRODUCTO."Nombre" AS NVARCHAR(100)) 		AS "NombreProducto"
			,CASE WHEN OS."IdInstrumento" = 0 THEN NULL ELSE OS."IdInstrumento" END AS "IdInstrumento"
			,CAST(COALESCE(TI."RazonSocial",'') AS NVARCHAR(100)) AS "NombreInstrumento"
			,COALESCE(FPE."IdMedioDePago",'') AS "IdMedioPagoEntregamos"
			,CAST(COALESCE(FPE."NombreMedioDePago",'') AS NVARCHAR(100)) AS "NombreMedioPagoEntregamos"
			,COALESCE(FPE."Monto",0)				AS "MontoPagoEntregamos"
			,LEFT(TO_CHAR(COALESCE(FPE."FechaCondicionLiquidacionGrilla",'0001-01-01')),10) AS "FechaLiquidacionEntregamos"
			,COALESCE(FPR."IdMedioDePago",'') AS "IdMedioPagoRecibimos"
			,CAST(COALESCE(FPR."NombreMedioDePago",'') AS NVARCHAR(100)) AS "NombreMedioPagoRecibimos"
			,COALESCE(FPR."Monto",0)				AS "MontoPagoRecibimos"
			,LEFT(TO_CHAR(COALESCE(FPR."FechaCondicionLiquidacionGrilla",'0001-01-01')),10) AS "FechaLiquidacionRecibimos"
			,COALESCE("PuntaCompra",0)						AS "PuntaCompra"
			,COALESCE("PuntaVenta",0)						AS "PuntaVenta"
			,CASE WHEN "CodigoComercio" = 0 THEN NULL ELSE "CodigoComercio" END AS "CodigoComercio"
			,CAST(COALESCE(PlanillaBancoCentral."Descripcion",'') AS NVARCHAR(255)) AS "NombreCodigoComercio"
			,CASE WHEN "IdComprobanteFacturacion" = 0 THEN NULL ELSE "IdComprobanteFacturacion" END AS "IdComprobanteFacturacion"
			,COALESCE("OperacionFicticia",'')				AS "OperacionFicticia"
			,CASE WHEN "IdAgente" = 0 THEN NULL ELSE "IdAgente" END AS "IdAgente"
			,CAST(COALESCE((AG."Nombres" ||' '||AG."ApellidoPaterno"),'') AS NVARCHAR(201)) AS "NombreAgente"
			,OS."IdUsuario"
			,CAST(COALESCE("CodigoUsuario",'') AS NVARCHAR(40)) AS "CodigoUsuario"
			,CASE WHEN OS."IdTipoCanalIngreso" = '' THEN NULL ELSE OS."IdTipoCanalIngreso" END AS "IdCanalIngreso"
			,CAST(COALESCE(TIPCAN."Nombre",'')	AS NVARCHAR(100)) AS "NombreCanalIngreso"
			,CAST(COALESCE(EMP."Nombre",'') AS NVARCHAR(100)) AS "NombreEmpresa"
			,CASE WHEN OS."Estado" = '' THEN  NULL ELSE OS."Estado" END AS "IdEstado"
			,CAST(COALESCE(EST."Nombre",'') AS NVARCHAR(100)) AS "NombreEstado"
			,"IdSolicitudAporteRescate"
			,"IdOperacionSpotOrigen"
			,OS."IdPlataformaNegociacion"
			,CAST(COALESCE(NEG."Nombre",'') AS NVARCHAR(100)) AS "NombrePlataformaNegociacion"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OS
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRODUCTO ON PRODUCTO."IdTipoProducto" = 'MEME'
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US ON OS."IdUsuario" = US."IdUsuario"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI ON OS."IdInstrumento" = TI."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG ON OS."IdAgente" = AG."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON OS."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON CLI."IdPersona" = SOC."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" CO ON OS."IdTipoOperacion" = CO."IdTipoOperacion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio" = OS."IdPortafolio"
----- MSO
		LEFT JOIN (
				SELECT
					"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
					"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
					MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
				FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
				WHERE "RecibePaga" = 'P' AND "IdTipoProducto" = 'MEME' AND "Monto" > 0
				GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPE
			ON (FPE."IdOperacion" = OS."IdOperacionSpot")	
		
		LEFT JOIN (
				SELECT
					"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
					"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
					MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
				FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
				WHERE "RecibePaga" = 'R' AND "IdTipoProducto" = 'MEME' AND "Monto" > 0
				GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPR
			ON (FPR."IdOperacion" = OS."IdOperacionSpot")	
		LEFT JOIN "BSCL.Mesa.BD::MES.PlanillaBancoCentral" PlanillaBancoCentral 	ON OS."CodigoComercio" = PlanillaBancoCentral."IdPlanillaBancoCentral"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON (OS."IdMesa" = MES."IdMesa" AND OS."IdEmpresa" = MES."IdEmpresa")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TIPCAN ON (OS."IdTipoCanalIngreso" = TIPCAN."IdTipoCanalIngreso")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (OS."IdEmpresa" = EMP."IdEmpresa")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON (OS."Estado" = EST."IdEstado")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.PlataformaNegociacion" NEG ON NEG."IdPlataformaNegociacion" = OS."IdPlataformaNegociacion" ;
							
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;
