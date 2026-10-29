SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarNomina"; 
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarNomina" AS

SELECT DISTINCT 
   NOM."IdEmpresa",
   NOM."IdEjecucion",
   TO_TIMESTAMP(LEFT(NOM."FechaEjecucion",10), 'YYYY-MM-DD') || ' 00:00:00' AS "sFechaEjecucion",
   TO_DATE(NOM."FechaEjecucion") AS "FechaEjecucion",
   NOM."IdCliente",
   NOM."IdPortafolio",
   NOM."CodigoPortafolio",
   NOM."IdPersona",
   --Datos del Envio (Nomina)
   NOM."IdDireccionElectronicaPrincipal",
   NOM."DireccionElectronica",
   NOM."DireccionFisica",
   NOM."IdEstadoNotificacion",
   NOM."EstadoNotificacion",
   NOM."IdEstadoCarta",
   NOM."EstadoCarta",
   TO_TIMESTAMP(LEFT(NOM."FechaIngreso",10), 'YYYY-MM-DD') || ' 00:00:00' AS "sFechaIngreso",
   TO_DATE(NOM."FechaIngreso") AS "FechaIngreso",
   NOM."NombreCliente"
--   COALESCE(NOM."CodigoUsuario",'') AS "CodigoUsuarioCreador"	
FROM

( SELECT NOM."IdEmpresa",
 	MAX(NOM."IdEjecucion") AS "IdEjecucion",
	   TO_DATE(NOM."FechaEjecucion") AS "FechaEjecucion",
	   NOM."IdCliente",
	   NOM."IdPortafolio",
	   NOM."CodigoPortafolio",
	   NOM."IdPersona",
	   --Datos del Envio (Nomina)
	   NOM."IdDireccionElectronicaPrincipal",
	   NOM."DireccionElectronica",
	   NOM."DireccionFisica",
	   NOM."IdEstadoNotificacion",
	   NOM."EstadoNotificacion",
	   NOM."IdEstadoCarta",
	   NOM."EstadoCarta",
	   TO_DATE(NOM."FechaIngreso") AS "FechaIngreso",
	   NOM."NombreCliente"
		FROM "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina" NOM
		GROUP BY  
		NOM."IdEmpresa",
	   TO_DATE(NOM."FechaEjecucion"),
	   NOM."IdCliente",
	   NOM."IdPortafolio",
	   NOM."CodigoPortafolio",
	   NOM."IdPersona",
	   --Datos del Envio (Nomina)
	   NOM."IdDireccionElectronicaPrincipal",
	   NOM."DireccionElectronica",
	   NOM."DireccionFisica",
	   NOM."IdEstadoNotificacion",
	   NOM."EstadoNotificacion",
	   NOM."IdEstadoCarta",
	   NOM."EstadoCarta",
	   TO_DATE(NOM."FechaIngreso"),
	   NOM."NombreCliente"


)AS NOM  ORDER BY  NOM."IdCliente",  NOM."IdPortafolio";
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarNomina" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

