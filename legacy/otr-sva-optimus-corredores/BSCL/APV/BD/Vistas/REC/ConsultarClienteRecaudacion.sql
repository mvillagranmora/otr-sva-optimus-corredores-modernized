SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarClienteRecaudacion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarClienteRecaudacion"  AS 

SELECT 
	 "CLR"."IdRecaudacionCliente"	
	,"CLR"."IdRecaudacion"
	,"CLR"."IdRecaudacionEmpleador"
	,"CLR"."IdEmpresa"				
	,"CLR"."IdCliente"	
	,"CLI"."IdPersona"
	,"CLI"."DocumentoIdentidad"
	,"CLI"."NombreCompleto"
	,"CLR"."FechaIngreso"			
	,"CLR"."TipoAfiliado"			
	,"CLR"."IdRegimenTriburario"		
	,"CLR"."MontoDepositoConvenido"
	,"CLR"."MontoDepositoAPV"		
	,"CLR"."MontoDepositoConvenido" + "CLR"."MontoDepositoAPV" AS "TotalDigitado" 
	,"CLR"."MontoDepositoConvenido" - "CLR"."MontoDepositoAPV" AS "DiferenciaDigitado" 
	,"CLR"."Origen"				
	,"CLR"."IdEstado"				
	,"EST"."Nombre" AS "Estado"
FROM "BSCL.APV.BD::REC.Cliente" AS "CLR"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI"
ON "CLR"."IdCliente" = "CLI"."IdCliente"
LEFT JOIN "BSCL.APV.BD::APV.Estado" AS "EST"
ON "CLR"."IdEstado" = "EST"."Codigo"
AND  "EST"."NombreEntidad" = 'RECAUDACION';

	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarClienteRecaudacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;