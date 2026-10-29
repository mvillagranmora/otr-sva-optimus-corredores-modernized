SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearRegimenSerieDeFondo";

CREATE PROCEDURE
--  Proposito: Crear Regimen Serie de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 18/01/2017
"BSCL.FondoMutuo.BD.Servicios::CrearRegimenSerieDeFondo"(
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_RegimenSerieFondo 		TABLE	(
    								"IdInterno"				BIGINT,
    								"IdRegimenFondoMutuo"	NVARCHAR(40),
    								"IdSerieFondo"			NVARCHAR(40)
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
							'ACT' AS "IdEstado",
							:p_IdEmpresa AS "IdEmpresa"
					FROM :p_RegimenSerieFondo;
	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"(
		"IdRegimenFondoMutuo", "IdSerieFondo", "IdEstado", "IdEmpresa"
	)
	SELECT "IdRegimenFondoMutuo", "IdSerieFondo", "IdEstado", "IdEmpresa" FROM :v_regimenFondo;
	
	p_out_Resultado = SELECT "IdInterno",
							 "IdRegimenFondoMutuo",
							 "IdSerieFondo",
							 "IdEstado",
							 "IdEmpresa"
					  FROM :v_regimenFondo;
	
END;
