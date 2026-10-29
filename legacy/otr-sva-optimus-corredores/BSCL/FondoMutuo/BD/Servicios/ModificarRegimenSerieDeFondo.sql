SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarRegimenSerieDeFondo";

CREATE PROCEDURE
--  Proposito: Crear Regimen Serie de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 18/01/2017
"BSCL.FondoMutuo.BD.Servicios::ModificarRegimenSerieDeFondo"(
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_RegimenSerieFondo 		TABLE	(
    								"IdInterno"				BIGINT,
    								"IdRegimenFondoMutuo"	NVARCHAR(40),
    								"IdSerieFondo"			NVARCHAR(40),
    								"IdEstado"				NVARCHAR(3)
    							),
    OUT p_out_Resultado			TABLE	(
    								"IdInterno"				BIGINT,
    								"IdRegimenFondoMutuo"	NVARCHAR(40),
    								"IdSerieFondo"			NVARCHAR(40),
    								"IdEstado"				NVARCHAR(3),
    								"IdEmpresa"				BIGINT
    							)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	v_regimenFondo = SELECT "IdInterno",
							"IdRegimenFondoMutuo",
							"IdSerieFondo",
							"IdEstado",
							:p_IdEmpresa AS "IdEmpresa"
					FROM :p_RegimenSerieFondo;
	
	UPDATE REGIMEN_SERIE
	SET "IdEstado" = P_REGIMEN_SERIE."IdEstado"
	FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS REGIMEN_SERIE
	INNER JOIN :v_regimenFondo AS P_REGIMEN_SERIE ON (REGIMEN_SERIE."IdRegimenFondoMutuo" = P_REGIMEN_SERIE."IdRegimenFondoMutuo" 
													  AND REGIMEN_SERIE."IdSerieFondo" = P_REGIMEN_SERIE."IdSerieFondo"
													  AND P_REGIMEN_SERIE."IdEmpresa" = REGIMEN_SERIE."IdEmpresa");
	
	p_out_Resultado = SELECT "IdInterno",
							 "IdRegimenFondoMutuo",
							 "IdSerieFondo",
							 "IdEstado",
							 "IdEmpresa"
					  FROM :v_regimenFondo;
	
END;