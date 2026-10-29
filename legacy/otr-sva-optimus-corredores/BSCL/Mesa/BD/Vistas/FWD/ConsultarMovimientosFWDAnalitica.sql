SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDAnalitica";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDAnalitica" AS

SELECT
	  FWD."IdEmpresa" --
	, CAST(COALESCE(Empresa."Nombre",'') AS NVARCHAR(100)) AS "NombreEmpresa" --
	, CASE WHEN COALESCE("FechaIngresoRegistro",'') = '' THEN '0001-01-01 00:00:00'
				 ELSE
				 	CASE WHEN "FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00'
				 		 ELSE LEFT(TO_CHAR(ADD_SECONDS("FechaIngresoRegistro", CONFIGURACION."Valor")),27) END
				 END 										AS "FechaIngresoRegistro" --
	, CASE WHEN FWD."IdCartera" = 0 THEN NULL ELSE FWD."IdCartera" END AS "IdCartera" --
	, FWD."FolioCartera" --
	, CASE WHEN FWD."IdOperacion" = 0 THEN NULL ELSE FWD."IdOperacion" END AS "IdOperacion" --
	, LEFT(TO_CHAR(COALESCE(FWD."FechaOperacion",'0001-01-01')),10)		AS "FechaOperacion" --
	, LEFT(TO_CHAR(COALESCE(FWD."FechaLiquidacion",'0001-01-01')),10)	AS "FechaLiquidacion" --
	, LEFT(TO_CHAR(COALESCE(
		CASE
		WHEN FWD."IdEstado"='ANT' OR (FWD."IdEstado" IN ('VCD','LIQ') AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRFechaOperacion"
		ELSE '' END,'0001-01-01')),10) AS "FechaAnticipo" --
	, coalesce(FPE."IdMedioDePago",'') 									AS "IdMedioPagoEntregamos" --
	, CAST(COALESCE(FPE."NombreMedioDePago",'') AS NVARCHAR(100))		AS "NombreMedioPagoEntregamos" --
	, COALESCE(CASE WHEN FWD."IdEstado" IN ('VCD','LIQ')
		THEN 
			CASE
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoMonedaPrincipal"
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoMonedaSecundaria"
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" < 0 
				THEN ABS(FWD."EGRMontoLiquidacion")
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" < 0 
				THEN ABS(FWD."EGRMontoLiquidacion")
			ELSE 0 END 
		ELSE FPE."Monto" END,0)				AS "MontoPagoEntregamos" --
	, LEFT(TO_CHAR(COALESCE(FPE."FechaCondicionLiquidacionGrilla",'0001-01-01')),10)	AS "FechaLiquidacionEntregamos" --
	, COALESCE(FPR."IdMedioDePago",'')									AS "IdMedioPagoRecibimos" --
	, CAST(COALESCE(FPR."NombreMedioDePago",'') AS NVARCHAR(100))	AS "NombreMedioPagoRecibimos" --
	,COALESCE(CASE WHEN FWD."IdEstado" IN ('VCD','LIQ') THEN 
		CASE
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoMonedaSecundaria"
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoMonedaPrincipal"
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" > 0 
				THEN FWD."EGRMontoLiquidacion"
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" > 0 --
				THEN FWD."EGRMontoLiquidacion"
			ELSE 0 END 
		ELSE FPR."Monto" END,0)											AS "MontoPagoRecibimos" --
	, LEFT(TO_CHAR(COALESCE(FPR."FechaCondicionLiquidacionGrilla",'0001-01-01')),10)	AS "FechaLiquidacionRecibimos" --
	, CASE WHEN FWD."IdTipoInstrumento" = '' THEN NULL ELSE FWD."IdTipoInstrumento" END AS "IdTipoInstrumento" --
	, CASE WHEN FWD."IdInstrumento" = 0 THEN NULL ELSE FWD."IdInstrumento" END AS "IdInstrumento" --
	, CAST(COALESCE(INS."RazonSocial",'') AS NVARCHAR(100))	AS "NombreInstrumento" --
	, CASE WHEN FWD."IdTipoOperacion" = '' THEN NULL ELSE FWD."IdTipoOperacion" END AS "IdTipoOperacion" --
	, CAST(COALESCE(TipoOperacion."Nombre",'') AS NVARCHAR(100))		AS "NombreTipoOperacion" --
	, CASE WHEN CLI."IdCliente" = 0 THEN NULL ELSE CLI."IdCliente" END 	AS "IdCliente" --
	, CAST(COALESCE(IDENT."DocumentoIdentidad",'') AS NVARCHAR(40)) 		AS "DocumentoIdentidadCliente"	--
	, CAST(COALESCE(NOM."NombreCompleto",'') AS NVARCHAR(302)) 			AS "NombreCliente" --
	, CASE WHEN FWD."IdMonedaPrincipal" = '' THEN NULL ELSE FWD."IdMonedaPrincipal" END AS "IdMonedaPrincipal" --
	, CAST(COALESCE(MONPRI."Nombre",'') AS NVARCHAR(100)) 				AS "NombreMonedaPrincipal" --
	, CASE WHEN FWD."IdMonedaSecundaria" = '' THEN NULL ELSE FWD."IdMonedaSecundaria" END AS "IdMonedaSecundaria"  --
	, CAST(COALESCE(MONSEC."Nombre",'')	AS NVARCHAR(100)) 				AS "NombreMonedaSecundaria" --
	, CAST(COALESCE(FWD."MontoMonedaPrincipal",0) AS DECIMAL(26,8))		AS "MontoMonedaPrincipal" --
	, CAST(COALESCE(FWD."MontoMonedaSecundaria",0) AS DECIMAL(26,8)) 	AS "MontoMonedaSecundaria" --
	, COALESCE(FWD."ValorTipoCambioCierre",0)							AS "TipoCambioCierre" --
	, COALESCE(FWD."ValorTipoCambioCosto",0) 							AS "TipoCambioCosto" --
	, COALESCE(FWD."ValorParidadCierre",0) 								AS "ParidadCierre" --
	, COALESCE(FWD."ValorParidadCosto",0) 								AS "ParidadCosto" --
	, COALESCE(FWD."INGMontoDiferido",0) 								AS "MontoEquivalente" --
	, COALESCE(FWD."INGMontoMargen",0) 									AS "Margen" --
	, COALESCE(FWD."INGMontoDiferido",0)								AS "MontoDiferido" --
	, CASE WHEN FWD."IdModalidadCumplimiento" = 0 THEN NULL ELSE FWD."IdModalidadCumplimiento" END AS "Modalidad" --
	, CAST(COALESCE(PARAM."NombreParametro",'') AS NVARCHAR(100)) 	AS "NombreModalidad" --
	, CASE WHEN FWD."IdFixing" = 0 THEN NULL ELSE FWD."IdFixing" END	AS "IdFixing" --
	, LEFT(TO_CHAR(COALESCE(
		CASE
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
			WHEN TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
			ELSE TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') END,'0001-01-01')),10)				AS "FechaFixing" --
	, CAST(COALESCE(PARAMFIXING."NombreParametro",'') AS NVARCHAR(255))	AS "NombreFixing"	--
	, COALESCE(
		case
			when fwd."PlazoEnDiasOperacion" > 0 then fwd."PlazoEnDiasOperacion"
			when fwd."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(fwd."FechaOperacion", fwd."FechaVencimientoOperacion")
			else 0
		end ,0) 							AS "Plazo" --
	, GREATEST(COALESCE(DAYS_BETWEEN (
		TO_SECONDDATE((SELECT CON."Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" CON 
					   WHERE CON."IdEmpresa" = FWD."IdEmpresa" AND CON."CodigoParametro" = 'PLA.FechaSistema'),'YYYY-MM-DD'),
		TO_SECONDDATE((CASE WHEN FWD."IdEstado" = 'ANT' OR 
								(FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRFechaOperacion"
							ELSE FWD."FechaVencimientoOperacion" END), 'YYYY-MM-DD')), 0), 0) AS "PlazoResidual"  --
	, LEFT(TO_CHAR(COALESCE(FWD."FechaVencimientoOperacion",'0001-01-01')),10)	AS "FechaVencimiento" --
	, CASE
		WHEN PARAM."ValorParametro" = 'COMPENSADA' THEN LEFT(TO_CHAR(COALESCE(FWD."FechaFixing",'0001-01-01')),10)  
		ELSE LEFT(TO_CHAR(COALESCE(FWD."FechaVencimientoOperacion",'0001-01-01')),10)   END	AS "FechaEfectiva" --
	, CASE WHEN FWD."IdAgente" = 0 THEN NULL ELSE FWD."IdAgente" END 	AS "IdAgente" --
	, CAST(COALESCE((AG."Nombres" ||' '||AG."ApellidoPaterno"),'') AS NVARCHAR(201))	AS "NombreAgente" --
	, CASE WHEN FWD."IdEstado" = '' THEN NULL ELSE FWD."IdEstado"	END	AS "IdEstado" --
	, CAST(COALESCE(EST."Nombre",'') AS NVARCHAR(100))					AS "NombreEstado" --
	, CASE WHEN FWD."IdTipoProducto" = '' THEN NULL ELSE FWD."IdTipoProducto" END AS "IdTipoProducto" --
	, TIPOPRO."Nombre" 													AS "NombreProducto" --
	, CASE WHEN FWD."IdPortafolio" = 0 THEN NULL ELSE FWD."IdPortafolio" END	AS "IdPortafolio" --
	, CAST(COALESCE(POR."CodigoPortafolio",'') AS NVARCHAR(100))		AS "CodigoPortafolio" --
	, CAST(COALESCE(POR."CodigoPortafolio",'') AS NVARCHAR(100)) 		AS "NombrePortafolio" --
	, CASE WHEN FWD."IdTipoCanalIngreso" = '' THEN NULL ELSE FWD."IdTipoCanalIngreso" END	AS "IdTipoCanalIngreso" --
	, CAST(COALESCE(TIPCAN."Nombre",'') AS NVARCHAR(100))				AS "NombreCanalIngreso" --
	, CASE WHEN FWD."IdMesa" = 0 THEN NULL ELSE FWD."IdMesa" END 		AS "IdMesa" --
	, CAST(COALESCE(MES."Nombre",'') AS NVARCHAR(30))					AS "NombreMesa" --
	, CASE WHEN FWD."IdPlaza" = 0 THEN NULL ELSE FWD."IdPlaza" END 		AS "IdPlaza" --
	, CAST(COALESCE(PLAZA."NombreCiudad",'') AS NVARCHAR(100))			AS "NombrePlaza" --
	, COALESCE(FWD."TasaCompra",0)										AS "TCForward" --
	, CASE WHEN OPE."IdUsuarioCreacion" =  0 THEN NULL ELSE OPE."IdUsuarioCreacion" END AS "IdUsuarioCreacion" --
	, CAST(COALESCE(USU."CodigoUsuario",'') AS NVARCHAR(40))			AS "CodigoUsuarioCreacion" --
	, CASE
		WHEN FWD."IdEstado" = 'LIQ' AND FWD."EGRIdMonedaLiquidacion" != '' THEN FWD."EGRIdMonedaLiquidacion"
		ELSE 'CLP' END													AS "IdMonedaLiquidacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS FWD
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE	ON (OPE."IdOperacion" = FWD."IdOperacion" AND OPE."IdTipoProducto" = FWD."IdTipoProducto")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG	ON (FWD."IdAgente" = AG."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (FWD."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS NOM ON NOM."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = FWD."IdPortafolio"
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM 
	ON (PARAM."IdParametro" = FWD."IdModalidadCumplimiento" AND
		PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PARAM."IdEmpresa" = FWD."IdEmpresa")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAMFIXING	ON (PARAMFIXING."IdParametro" = FWD."IdFixing")
LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'P' AND "IdTipoProducto" = 'MEFW'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPE
	ON (FWD."IdCartera" = FPE."IdOperacion")
LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'R' AND "IdTipoProducto" = 'MEFW'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPR
	ON (FWD."IdCartera" = FPR."IdOperacion")	
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON (FWD."IdMesa" = MES."IdMesa" AND FWD."IdEmpresa" = MES."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = FWD."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON FWD."IdEmpresa" = Empresa."IdEmpresa" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON fwd."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TIPOPRO ON TIPOPRO."IdTipoProducto" = 'MEFW'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONPRI ON (FWD."IdMonedaPrincipal" = MONPRI."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONSEC ON (FWD."IdMonedaSecundaria" = MONSEC."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS PLAZA ON (FWD."IdPlaza" = PLAZA."IdCiudad")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TIPCAN ON (FWD."IdTipoCanalIngreso" = TIPCAN."IdTipoCanalIngreso")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON (OPE."IdUsuarioCreacion" = USU."IdUsuario")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON (FWD."IdEstado" = EST."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONFIGURACION ON CONFIGURACION."CodigoParametro" = 'PLA.SegundosZonaHoraria'
WHERE FWD."IdTipoProducto" = 'MEFW';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
