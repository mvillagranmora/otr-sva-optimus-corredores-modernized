SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarAsignaciones";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarAsignaciones" as

select
"CAS"."DocumentoIdentidad" as "Rut"
,IFNULL("CAS"."IdPortafolio",0) as "Cuenta"
,IFNULL("CAS"."NombreCompleto","CAS"."RazonSocial") as "Nombre"
,IFNULL("CAS"."NombreBolsa",'') as "Bolsa"
,IFNULL("CAS"."FolioTransaccion",'') as "Folio"
,IFNULL("CAS"."Instrumento",'') as "Instrumento"
, LEFT(IFNULL("CAS"."IdTipoOperacion",'  '),1)
as "TipoOperacion"
,IFNULL("CAS"."IdOrden",0) as "NumeroOrden"
,IFNULL("CAS"."IdTransaccion",0) as "NumeroOperacion" 
,IFNULL("CAS"."CantidadAsignada",0) as "UnidadesAsignadas"
,IFNULL("CAS"."MontoAsignado",0) as "MontoTotal"    
,IFNULL("CAS"."IdMonedaOperacion",'') as "Moneda" 
,IFNULL("CAS"."FechaAsignacion",'') as "FechaAsignacion"
,LEFT(SUBSTR_AFTER((ltrim(rtrim(IFNULL("CAS"."FechaAsignacion",'')))),' '),8) as "HoraAsignacion"
,(ltrim(rtrim(substring("CAS"."DocumentoIdentidad",1,LENGTH("CAS"."DocumentoIdentidad")-2)))) as "DocumentoIdentidadRut",
"IdClasificacionTipoInstrumento" as "IdClasificacion",
"IdAsignacion" as "IdAsignacion"
 from "BSCL.Orden.BD.Vistas::ConsultarAsignacion" "CAS";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NCG380.ConsultarAsignaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
