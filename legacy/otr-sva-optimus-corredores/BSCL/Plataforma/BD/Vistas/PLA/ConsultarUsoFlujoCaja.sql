SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoFlujoCaja"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoFlujoCaja"  
AS
SELECT 
	UFC."Nombre" 									AS "UsoFlujoCaja",
	COALESCE(DIC."Descripcion", UFC."Nombre")		AS "DescripcionUsoFlujoCaja", 
	UFC."DesplegarEnUI", 
	UFC."IdEmpresa", 
	UFC."IdEstado",
	UFC."DesplegarEnUICliente",
	UFC."DesplegarEnUINegocio"
FROM "BSCL.Plataforma.BD::PLA.UsoFlujoCaja" UFC
LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" DIC ON DIC."CodDiccionario" = UFC."Nombre" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CON ON DIC."CodLenguaje" = CON."Valor" AND CON."IdEmpresa" = UFC."IdEmpresa" AND CON."CodigoParametro" = 'PLA.LocalizacionSistema';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoFlujoCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
