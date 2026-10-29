SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion"  AS 

	SELECT  
	 	 REC."IdRecaudacion"			
		,REC."IdEmpresa"
		,CASE WHEN (SELECT COUNT("IdRecaudacionEmpleador") FROM "BSCL.APV.BD::REC.Empleador"
		WHERE "IdRecaudacion" = REC."IdRecaudacion") > 0 THEN 'SI' ELSE 'NO' END AS "DetalleEmpleador"
		,CASE WHEN (SELECT COUNT("IdRecaudacionCliente") FROM "BSCL.APV.BD::REC.Cliente"
		WHERE "IdRecaudacion" = REC."IdRecaudacion") > 0 THEN 'SI' ELSE 'NO' END AS "DetalleCliente"
		,REC."FolioCartola"			
		,REC."LineaCartola"			
		,REC."Sucursal"				
		,REC."FechaDeposito"		
		,REC."CuentaCorriente"		
		,REC."MontoDeposito"		
		,REC."ConceptoRecaudacion"	
		,REC."Origen"				
		,REC."Observacion"			
		,REC."NumeroDocumento"		
		,REC."IdTipoDeposito"
		,MPT."Nombre"	AS "NombreTipoDeposito"
		,REC."IdBanco"		
		,BAN."NombreBanco"
		,REC."IdRegimenTributario"	
		,REC."IdCarga"
		,REC."IdEstado"
		,EST."Nombre" AS "Estado"
	FROM "BSCL.APV.BD::REC.Recaudacion" AS REC
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BAN
	ON REC."IdBanco" = BAN."IdBanco"
	LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria" AS MPT
	ON REC."IdTipoDeposito" = MPT."IdMedioDePago"
	AND "IdMoneda" = 'CLP'
	AND "IdTipoMovimiento" = 'MOVING'
	LEFT JOIN "BSCL.APV.BD::APV.Estado" AS EST
	ON REC."IdEstado" = EST."Codigo"
	AND EST."NombreEntidad" = 'RECAUDACION';
	
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

