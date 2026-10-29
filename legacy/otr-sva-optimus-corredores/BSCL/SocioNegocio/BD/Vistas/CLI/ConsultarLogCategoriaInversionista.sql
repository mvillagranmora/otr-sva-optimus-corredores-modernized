SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCategoriaInversionista"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCategoriaInversionista" AS

SELECT 
CAB."IdCliente", 
CAB."FechaRegistro",
CAB."CodigoUsuario",
DET."ValorAntiguo",
DET."ValorNuevo"
FROM 
"BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" CAB  INNER JOIN 
"BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle"  DET ON (CAB."IdLog"=DET."IdLog")
WHERE CAB."NombreModulo"='SOCIONEGOCIO'
AND CAB."IdTipoLog"='MODIFICACION'
AND DET."SubRegistro"='CLIENTE'	
AND DET."CampoRegistro"='IDCATEGORIAINVERSIONISTA' OR DET."CampoRegistro"='IdCategoriaInversionista';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCategoriaInversionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 