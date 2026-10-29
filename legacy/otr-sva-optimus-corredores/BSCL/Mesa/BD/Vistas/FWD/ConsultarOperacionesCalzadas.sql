SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesCalzadas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesCalzadas"
AS 
SELECT RES."IdOperacion"													AS "IdOperacionOri"
	 , CUS."IdOperacion"													AS "IdOperacion"
	 , CC."FolioCartera"													AS "FolioCarteraOri"
	 , CUS."FolioCartera"													AS "FolioCartera"
	 , CUS."IdMonedaPrincipal"												AS "IdMonedaPrincipal"
	 , CUS."IdMonedaSecundaria"												AS "IdMonedaSecundaria"
	 , 0																	AS "MontoCalce" 
	 , DISPONIBLE."MontoCalce"												AS "MontoTotalCalce"
	 , CUS."Cantidad"														AS "MontoOperacion"
	 , DISPONIBLE."MontoDisponible"											AS "MontoDisponible"
	 , DAYS_BETWEEN(TO_SECONDDATE(
	 							  ( SELECT "Valor" 
									FROM "BSCL.Plataforma.BD::PLA.Configuracion"
									WHERE "IdEmpresa" = 1
									AND "CodigoParametro" = 'PLA.FechaSistema'
	 							   ), 'YYYY-MM-DD'),
					TO_SECONDDATE(CUS."FechaLiquidacion", 'YYYY-MM-DD')) 	AS "PlazoResidual"
	 , CUS."PlazoEnDiasOperacion"											AS "Plazo"
     , CUS."IdTipoInstrumento"											    AS "IdTipoInstrumento"					
     , CUS."IdInstrumento"													AS "IdInstrumento"
	 , CUS."IdTipoOperacion"												AS "IdTipoOperacion"
	 , CUS."NombreTipoOperacion"											AS "TipoOperacion"
	 , CUS."FechaLiquidacion"												AS "FechaVencimiento"
	 , CUS."IdCliente"														AS "IdCliente" 	 
	 , CUS."DocumentoIdentidad"												AS "RutCliente"
	 , CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" 
			ELSE PER."RazonSocial"
		END 						 										AS "NombreCliente"	 
	 , CUS."IdAgente" 														AS "IdAgente"  
	 , (AG."Nombres" ||' '||AG."ApellidoPaterno")							AS "NombreAgente"				 
	 , CUS."IdEstado"														AS "IdEstado"
	 , CUS."IdModalidadCumplimiento"										AS "Modalidad"
	 , CC."FechaOperacion"													AS "FechaInicio"	 
	 , CP."NombreParametro"													AS "NombreModalidad"
	 , (CUS."CodigoPortafolio" || CUS."DescripcionPortafolio")				AS "PortafolioCliente"
	 , CUS."IdPortafolio" 													AS "IdPortafolio"
	 , CUS."TipoInstrumento"												AS "TipoInstrumento"
FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CUS  
 	    ON CUS."IdOperacion" = RES."FolioCarteraOrigen"
 	   AND RES."IdTipoProducto" = CUS."IdTipoProducto"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG
	   ON CUS."IdAgente" = AG."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
		ON CUS."IdCliente" = CLI."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
		ON PER."IdPersona" = CLI."IdPersona" 
INNER JOIN (SELECT CUS."IdOperacion"
				 , SUM(RES."IngCantidad")			AS "MontoCalce" 
				 , CUS."Cantidad"	AS "MontoOperacion"
				 , ABS(SUM(RES."IngCantidad") - CUS."Cantidad") AS "MontoDisponible"
			  FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CUS  
			 		ON CUS."IdOperacion" = RES."IdOperacion"
			 	   AND RES."IdTipoProducto" = CUS."IdTipoProducto"
			  WHERE RES."IdTipoProducto" = 'MEFW' AND CUS."IdEstado"!='MOD'
			GROUP BY CUS."IdOperacion"
				   , CUS."Cantidad") AS DISPONIBLE
		ON DISPONIBLE."IdOperacion" = CUS."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CC  
 	   ON CC."IdOperacion" = RES."IdOperacion"
 	   AND RES."IdTipoProducto" = CC."IdTipoProducto"
 	   AND CC."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" AS CP
	   ON (CUS."IdModalidadCumplimiento" = CP."IdParametro" AND
		CP."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		CP."IdEmpresa" = CUS."IdEmpresa")
WHERE RES."IdTipoProducto" = 'MEFW'
AND CUS."IdEstado" IN ('VIG') -- MOD?
GROUP BY RES."IdOperacion"
	   , CUS."FolioCartera"
	   , CUS."IdMonedaPrincipal"
	   , CUS."IdMonedaSecundaria"
	   , CUS."Cantidad"
	   , DISPONIBLE."MontoCalce"
	   , DISPONIBLE."MontoDisponible"
	   , CUS."PlazoEnDiasOperacion"
	   , CUS."IdOperacion"
	   , CUS."FechaOperacion"
	   , CUS."FechaLiquidacion"
	   , CUS."IdTipoInstrumento"	
	   , CUS."IdInstrumento"	
	   , CUS."IdTipoOperacion"
	   , CUS."NombreTipoOperacion"
	   , CUS."IdCliente"
	   , CUS."DocumentoIdentidad" 
	   , PER."Nombres"
	   , PER."ApellidoPaterno"
	   , PER."RazonSocial"
	   , PER."Tipo"
	   , PER."ApellidoMaterno"
	   , CUS."IdAgente"
	   , AG."Nombres"
	   , AG."ApellidoPaterno"
	   , CUS."IdEstado"
	   , CUS."IdModalidadCumplimiento"
	   , CC."FechaOperacion"
	   , CC."FolioCartera"
	   , CP."NombreParametro"
	   ,(CUS."CodigoPortafolio" || CUS."DescripcionPortafolio")
	   , CUS."IdPortafolio"
	   , CUS."TipoInstrumento"
UNION ALL
SELECT 0								AS "IdOperacionOri"
	 , CONFWD."FolioOperacion" 			AS "IdOperacion"
	 , 0								AS "FolioCarteraOri"
	 , CONFWD."IdOperacion"				AS "FolioCartera"
	 , CONFWD."IdMonedaPrincipal" 		AS "IdMonedaPrincipal"
	 , CONFWD."IdMonedaSecundaria" 		AS "IdMonedaSecundaria"
	 , 0								AS "MontoCalce" 
	 , 0								AS "MontoTotalCalce"
	 , CONFWD."MontoMonedaPrincipal"	AS "MontoOperacion"
	 , CONFWD."MontoDisponible"			AS "MontoDisponible"
	 , CONFWD."PlazoResidual"			AS "PlazoResidual"
	 , CONFWD."Plazo"					AS "Plazo"
	 , CONFWD."IdTipoInstrumento"		AS "IdTipoInstrumento"					
     , CONFWD."IdInstrumento"			AS "IdInstrumento"
	 , CONFWD."IdTipoOperacion"			AS "IdTipoOperacion"
	 , CONFWD."TipoOperacion"			AS "TipoOperacion"
	 , CONFWD."FechaVencimiento" 		AS "FechaVencimiento"
	 , CONFWD."IdCliente" 				AS "IdCliente" 	 
	 , CONFWD."RutCliente"				AS "RutCliente"
	 , CONFWD."NombreCliente"			AS "NombreCliente"	 
	 , CONFWD."Agente" 					AS "IdAgente"  
	 , CONFWD."NombreAgente"			AS "NombreAgente"				 
	 , CONFWD."Estado" 					AS "IdEstado"
	 , CONFWD."Modalidad"				AS "Modalidad"
	 , ''								AS "FechaInicio"	 
	 , ''								AS "NombreModalidad"
	 , ''								AS "PortafolioCliente"
	 , 0								AS "IdPortafolio"
	 , ''								AS "TipoInstrumento"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS CONFWD
WHERE CONFWD."FolioOperacion" NOT IN (SELECT CUS."IdOperacion"
									  FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
									  INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CUS  
									 		  ON CUS."IdOperacion" = RES."IdOperacion"
									 	     AND RES."IdTipoProducto" = CUS."IdTipoProducto"
									  WHERE RES."IdTipoProducto" = 'MEFW'
									  GROUP BY CUS."IdOperacion"
										   , CUS."Cantidad")
AND CONFWD."Estado" IN ('VIG') -- MOD?
GROUP BY CONFWD."FolioOperacion"
	 , CONFWD."IdOperacion"
	 , CONFWD."IdMonedaPrincipal"
	 , CONFWD."IdMonedaSecundaria"
	 , CONFWD."MontoMonedaPrincipal"
	 , CONFWD."MontoDisponible"
	 , CONFWD."PlazoResidual"
	 , CONFWD."Plazo"
	 , CONFWD."IdTipoInstrumento"
     , CONFWD."IdInstrumento"
	 , CONFWD."IdTipoOperacion"
	 , CONFWD."TipoOperacion"
	 , CONFWD."FechaVencimiento"
	 , CONFWD."IdCliente"
	 , CONFWD."RutCliente"
	 , CONFWD."NombreCliente"
	 , CONFWD."Agente"
	 , CONFWD."NombreAgente"
	 , CONFWD."Estado"
	 , CONFWD."Modalidad";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesCalzadas" TO BCS_DESARROLLADOR WITH GRANT OPTION; 