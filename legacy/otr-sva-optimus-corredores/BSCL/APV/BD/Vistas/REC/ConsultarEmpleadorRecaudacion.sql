SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarEmpleadorRecaudacion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarEmpleadorRecaudacion"  AS 

SELECT 
	 "EMP"."IdRecaudacionEmpleador"
	,"EMP"."IdRecaudacion"			
	,"EMP"."IdEmpresa"
	,CASE WHEN (SELECT COUNT("IdRecaudacionCliente") FROM "BSCL.APV.BD::REC.Cliente"
		WHERE "IdRecaudacionEmpleador" = "EMP"."IdRecaudacionEmpleador") > 0 THEN 'SI' ELSE 'NO' END AS "DetalleCliente"
	,"EMP"."IdPersona"	
	,"PER"."DocumentoIdentidad"
	,"PER"."NombreCompleto"
	,"EMP"."FechaIngreso"			
	,"EMP"."PeriodoPago"			
	,"EMP"."NumeroHojas"			
	,"EMP"."TipoAfiliado"			
	,"EMP"."TipoPago"		
	,"EMP"."IdBanco"
	,"EMP"."NumeroDocumento"
	,"EMP"."Insistencias"			
	,"EMP"."MontoDepositoConvenido"
	,"EMP"."MontoDepositoAPV"
	,"EMP"."MontoDepositoConvenido" + "EMP"."MontoDepositoAPV" + "EMP"."Reajustes" + "EMP"."Intereses" AS "TotalResumen"
	,"EMP"."Reajustes"				
	,"EMP"."Intereses"				
	,"EMP"."Origen"				
	,"EMP"."Observaciones"			
	,"EMP"."IdEstado"
	,"EST"."Nombre" AS "Estado"
FROM "BSCL.APV.BD::REC.Empleador" AS "EMP"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"
ON "EMP"."IdPersona" = "PER"."IdPersona"
LEFT JOIN "BSCL.APV.BD::APV.Estado" AS "EST"
ON "EMP"."IdEstado" = "EST"."Codigo"
AND  "EST"."NombreEntidad" = 'RECAUDACION';
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarEmpleadorRecaudacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 