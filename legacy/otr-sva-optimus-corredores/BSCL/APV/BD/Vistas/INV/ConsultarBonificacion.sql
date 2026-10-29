SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion";
 
CREATE VIEW "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion"  AS 

SELECT
	 	 BON."IdEmpresa"				   	  		
		,BON."IdBonificacion"					
		/*Registro tipo 1*/
		,BON."IdEntidadRecaudadora"				
		,BON."FechaGeneracion"					
		/*Registro tipo 2*/
		,BON."TipoRegistro"						
		,BON."AnoAhorro"						
		,BON."AnoProceso"						
		,BON."FolioEgreso"						
		,BON."MontoPagadoNeto"					
		,BON."MontoSaldoNeto"				 	
		,BON."MontoPagadoReajustado"			
		,BON."FechaPago"						
		/*Registro tipo 3*/
		,BON."IdCliente"  		
		,CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno" 
			ELSE PER."RazonSocial"
		END   AS "NombreCompletoCliente"
		,DOC."DocumentoIdentidad"
		,BON."MontoBonificacionAfiliadoNeto" 	
		,BON."PA1"								
		,BON."PA2"								
		,BON."PA3"								
		,BON."MontoAfiliadoNeto"   				
		,BON."MontoAfiliadoSaldoNeto"		    
		,BON."MontoReajustadoPagadoAfiliado"	
		,BON."IdEstado"	
		,EST."Nombre" AS "NombreEstado"
		,EST."Codigo" AS "CodigoEstado"
		,BON."CodigoArchivo"
		,PER."IdPersona"
		,BON."MontoCalculado"
		,BON."MontoCalculado" AS "MontoCalculadoOrigen"
		,COALESCE(DJD."AhorrosDeposito", 0) AS "MontoAhorroDJ"
		,COALESCE(DJD."RetirosDeposito", 0) AS "MontoRetiroDJ"
		,COALESCE(DJD."AhorrosDeposito", 0) - COALESCE(DJD."RetirosDeposito", 0) AS "MontoAhorroNetoDJ"
		,BON."MontoReajustadoPagadoAfiliado" - BON."MontoCalculado" AS "MontoBonificacionDiferencia"
		,BON."MontoTraspasoEgreso"
		,BON."MontoEgreso"
		,BON."PorcentajeTraspasoEgreso"
		,BON."PorcentajeEgreso"
		,BON."MontoTraspasoDevolucion"
		,BON."MontoEgresoDevolucion"
		,CASE WHEN COALESCE(DJD."AhorrosDeposito", 0) = 0 THEN 'Sin información en DJ'
			  WHEN COALESCE(BON."MontoTraspasoEgreso", 0) > 0 THEN 'Registra traspasos de salida'
			  WHEN COALESCE(BON."MontoEgreso", 0) > 0 THEN 'Registra egresos'
			  WHEN CIN."IdCliente" IS NOT NULL THEN 'Registra Rescates Normales a Cuenta Inversion'
			  ELSE 'OK' END AS "Comentario"
FROM "BSCL.APV.BD::INV.Bonificacion" AS BON
LEFT JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos" AS DJD
ON BON."IdCliente" = DJD."IdCliente"
AND BON."AnoAhorro" = DJD."AnoAhorro" 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
ON BON."IdCliente" = CLI."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
ON CLI."IdPersona" = PER."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
ON PER."IdPersona" = DOC."IdPersona"
AND DOC."Predeterminado" = 'S'
INNER JOIN "BSCL.APV.BD::APV.Estado" AS EST
ON BON."IdEstado" = EST."Codigo"
AND EST."NombreEntidad" = 'BONIFICACION'
LEFT JOIN (
	SELECT DISTINCT 
		CAR."IdCliente",
		YEAR(CAR."FechaOperacion") AS "AnoAhorro"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (CAR."IdPortafolio" = POR."IdPortafolio")
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = CAR."EGRFolioComprobante" 
	WHERE POR."CodigoPortafolio" = 'APV-A'
	  AND CAR."IdClasificacionTipoInstrumento" IN ('RV','FM')
	  AND SOL."IdMedioDePago" = 'CIN'
	  AND SOL."IdTipoMovimientoFondoMutuo" = 'RESNORM'
	  AND NOT EXISTS (
	  	SELECT 1 
	  	FROM "BSCL.APV.BD::TRA.OrdenTraspaso" DTR 
	  	INNER JOIN "BSCL.APV.BD::TRA.Traspaso" TRA ON TRA."IdTraspaso" = DTR."IdTraspaso" 
	  	WHERE ((DTR."IdSolicitudAporteRescate" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FMM') 
	  	   OR (DTR."IdOrden" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FAC')) 
	  	  AND CAR."IdCliente" = TRA."IdCliente" 
	  	  AND CAR."IdModulo" IN ('FMM', 'FAC')
	  )
) AS CIN ON BON."IdCliente" = CIN."IdCliente" AND BON."AnoAhorro" = CIN."AnoAhorro";
	
GRANT SELECT ON "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
