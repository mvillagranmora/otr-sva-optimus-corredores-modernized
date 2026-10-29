SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.APV.BD.Servicios::CambiarEstadoContratoSAAAPV";

CREATE PROCEDURE 
-- 
--  
-- 
"BSCL.APV.BD.Servicios::CambiarEstadoContratoSAAAPV" (
    IN  p_Contratos                  	TABLE(
    									"IdInterno"	BIGINT,
    									"FolioContratoSAAPV" BIGINT),
    IN  p_NuevoEstado				NVARCHAR(3),
    IN  p_NuevoEstadoSolicitud		NVARCHAR(3)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	UPDATE Contrato 
  	SET 
  		"IdEstado" = CASE WHEN p_NuevoEstado = '' THEN "IdEstado" ELSE p_NuevoEstado END,
  		"IdEstadoSolicitud" = CASE WHEN p_NuevoEstadoSolicitud = '' THEN "IdEstadoSolicitud" ELSE p_NuevoEstadoSolicitud END
  	FROM "BSCL.APV.BD::SCS.ContratoSAAPV" Contrato 
  	INNER JOIN :p_Contratos RES on RES."FolioContratoSAAPV" = TO_BIGINT(Contrato."FolioContratoSAAPV")
  	WHERE Contrato."FolioContratoSAAPV" != '';
	
END;