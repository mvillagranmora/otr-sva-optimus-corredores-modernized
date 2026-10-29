SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.ValidarH2465";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.ValidarH2465" (
	IN p_RutCliente  NVARCHAR(40),
    IN p_RutInstitucion NVARCHAR(40),
    OUT o_Result NVARCHAR(1)
)
LANGUAGE SQLSCRIPT
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE CANTIDAD INT;

	SELECT COUNT(*) INTO CANTIDAD FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos" 
	WHERE "RutCliente" = :p_RutCliente AND "RutInstitucion" = :p_RutInstitucion;

	IF CANTIDAD > 1 THEN
		o_Result := 'S';
	ELSE 
		o_Result := 'N';
	END IF;

END;