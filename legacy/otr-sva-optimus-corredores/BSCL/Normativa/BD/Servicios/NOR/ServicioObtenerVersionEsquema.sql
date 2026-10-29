SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ServicioObtenerVersionEsquema";
 
CREATE PROCEDURE 
--
--  Proposito: Servicio para obtener Version y esquema de un tipo de documento.
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 29/08/2016

"BSCL.Normativa.BD.Servicios.NOR::ServicioObtenerVersionEsquema" (
	
	--Parametros de Entrada Propios
	IN p_TipoDocumento		NVARCHAR(10),
	IN p_Fecha				NVARCHAR(10),
	
	--
	OUT	p_out_VersionEsquema TABLE (p_FechaDesde NVARCHAR(10),p_out_Esquema NVARCHAR(10),p_out_Version NVARCHAR(10),p_out_FechaDatosDesde DATE,p_out_FechaDatosHasta DATE)
	
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	p_out_VersionEsquema = SELECT * FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_TipoDocumento, TO_CHAR(:p_Fecha, 'DD-MM-YYYY'));
	
END;