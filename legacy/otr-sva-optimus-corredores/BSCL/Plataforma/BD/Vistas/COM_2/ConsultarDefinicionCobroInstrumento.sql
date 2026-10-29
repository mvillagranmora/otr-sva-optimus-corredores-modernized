SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroInstrumento"  
AS

SELECT "IdEmpresa","CodigoDefinicionCobro",DEF."IdInstrumento","Nemotecnico","RazonSocial"
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobroInstrumento" DEF
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on DEF."IdInstrumento" = INS."IdInstrumento";



GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
