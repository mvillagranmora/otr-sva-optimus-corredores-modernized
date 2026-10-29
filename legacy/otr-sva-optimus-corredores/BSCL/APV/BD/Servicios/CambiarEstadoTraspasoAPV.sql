SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.APV.BD.Servicios::CambiarEstadoTraspasoAPV";

CREATE PROCEDURE 
-- 
--  
-- 
"BSCL.APV.BD.Servicios::CambiarEstadoTraspasoAPV" (
    IN  p_Traspaso                  	TABLE(
    									"IdInterno"	BIGINT,
    									"IdTraspaso" BIGINT),
    IN  p_NuevoEstado				NVARCHAR(3)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	UPDATE Traspaso 
  	SET "IdEstado" = CASE WHEN p_NuevoEstado = '' THEN "IdEstado" ELSE :p_NuevoEstado END
  	FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso 
  	INNER JOIN :p_Traspaso RES on RES."IdTraspaso" = Traspaso."IdTraspaso";
END;