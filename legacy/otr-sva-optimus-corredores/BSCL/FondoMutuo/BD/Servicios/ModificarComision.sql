SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarComision";

CREATE PROCEDURE
--  Proposito: Modificar Comision para alguna Serie
--  Autor: Gat-Blac 
--  Fecha Creacion: 19/07/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarComision" (
	IN p_Comision								TABLE 	(
														  	"IdInterno"				BIGINT,
														  	"IdComision"			BIGINT,
														  	"IdSerieFondo" 			NVARCHAR(40),
														  	"Descripcion"			NVARCHAR(100),
														  	"TipoComision"			NVARCHAR(40),
														  	"FechaVigenciaDesde"	DATE,
														  	"FechaVigenciaHasta"	DATE,
														  	"PorcentajeLiberado"	DECIMAL(26,8),
														  	"Estado"				NVARCHAR(3)
								  						),
	IN p_TramoComision							TABLE 	(
														  	"IdInterno"				BIGINT,
														  	"IdTramo"				BIGINT,
														  	"DiasDesde"				BIGINT,
														  	"DiasHasta"				BIGINT,
														  	"PorcentajeComision"	DECIMAL(26,8),
														  	"FechaSistema"			DATE		
												  		),
	OUT p_out_Resultado							TABLE	(
    														"IdInterno" 			BIGINT,
    														"IdComision"			BIGINT,
    														"IdSerieFondo"			NVARCHAR(40),
    														"Descripcion"			NVARCHAR(100),
    														"TipoComision"			NVARCHAR(40),
    														"FechaVigenciaDesde"	DATE,
    														"FechaVigenciaHasta"	DATE,
    														"PorcentajeLiberado"	DECIMAL(26,8),
    														"Estado"				NVARCHAR(3)
    													),
    OUT p_out_Tramos							TABLE	(
    														"IdInterno" 			BIGINT,
    														"IdTramo"				BIGINT,
    														"IdComision"			BIGINT,
    														"DiasDesde"				BIGINT,
    														"DiasHasta"				BIGINT,
    														"PorcentajeComision"	DECIMAL(26,8)		
    													)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
		
	v_comision_final = SELECT "p_Comision"."IdInterno"
								 ,"p_Comision"."IdComision"
								 ,"p_Comision"."IdSerieFondo"
								 ,"p_Comision"."Descripcion"
								 ,"p_Comision"."TipoComision"
								 ,"p_Comision"."FechaVigenciaDesde"
								 ,"p_Comision"."FechaVigenciaHasta"
								 ,"p_Comision"."PorcentajeLiberado"
								 ,"p_Comision"."Estado"
							FROM :p_Comision AS "p_Comision";
	
	v_tramos_actualizar = SELECT	"p_Tramo"."IdInterno"
									,"p_Tramo"."IdTramo"
									,"Comision"."IdComision"
									,"p_Tramo"."DiasDesde"
									,"p_Tramo"."DiasHasta"
									,"p_Tramo"."PorcentajeComision"
						   FROM :p_TramoComision AS "p_Tramo"
						   INNER JOIN :v_comision_final AS "Comision" ON ("p_Tramo"."IdInterno" = "Comision"."IdInterno")
						   WHERE "p_Tramo"."IdTramo" != 0;
	
	v_tramos_nuevos = SELECT	"p_Tramo"."IdInterno"
								,"BSCL.FondoMutuo.BD.Secuencias::TramoComision".NEXTVAL AS "IdTramo"
								,"Comision"."IdComision"
								,"p_Tramo"."DiasDesde"
								,"p_Tramo"."DiasHasta"
								,"p_Tramo"."PorcentajeComision"
								, 'ACT' AS "Estado"
								,"p_Tramo"."FechaSistema"
								,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
					   FROM :p_TramoComision AS "p_Tramo"
					   INNER JOIN :v_comision_final AS "Comision" ON ("p_Tramo"."IdInterno" = "Comision"."IdInterno")
					   WHERE "p_Tramo"."IdTramo" = 0;
	
	v_tramos_eliminar = SELECT "Comision"."IdInterno"
								,"TramoComision"."IdTramo"
						FROM "BSCL.FondoMutuo.BD::FMM.TramoComision" AS "TramoComision"
						INNER JOIN :v_comision_final AS "Comision" ON ("Comision"."IdComision" = "TramoComision"."IdComision")
						LEFT OUTER JOIN :v_tramos_actualizar AS "TramoActualizar" ON ("TramoComision"."IdTramo" = "TramoActualizar"."IdTramo")
						WHERE COALESCE("TramoActualizar"."IdTramo",'-1')='-1';
	
	UPDATE COMISION
	SET "Descripcion" 		 = ComisionFinal."Descripcion",
		"TipoComision" 		 = ComisionFinal."TipoComision",
		"FechaVigenciaDesde" = ComisionFinal."FechaVigenciaDesde",
		"FechaVigenciaHasta" = ComisionFinal."FechaVigenciaHasta",
		"PorcentajeLiberado" = ComisionFinal."PorcentajeLiberado",
		"Estado" 			 = ComisionFinal."Estado"
	FROM "BSCL.FondoMutuo.BD::FMM.Comision" AS COMISION
	INNER JOIN :v_comision_final AS ComisionFinal ON (ComisionFinal."IdComision" = COMISION."IdComision");
	
	UPDATE TRAMOS
	SET "DiasDesde" 		 = TramoFinal."DiasDesde",
		"DiasHasta" 		 = TramoFinal."DiasHasta",
		"PorcentajeComision" = TramoFinal."PorcentajeComision"
	FROM "BSCL.FondoMutuo.BD::FMM.TramoComision" AS	TRAMOS
	INNER JOIN :v_tramos_actualizar AS TramoFinal ON (TramoFinal."IdTramo" = TRAMOS."IdTramo")
	INNER JOIN :v_comision_final AS COMISION ON (COMISION."IdComision" = TRAMOS."IdComision");
	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.TramoComision"
	(
		"IdTramo", "IdComision", "DiasDesde", "DiasHasta", "PorcentajeComision", "Estado", "FechaSistema", "FechaIngresoRegistro"
	)
	SELECT "IdTramo", "IdComision", "DiasDesde", "DiasHasta", "PorcentajeComision", "Estado", "FechaSistema", "FechaIngresoRegistro"
	FROM :v_tramos_nuevos;
	
	UPDATE TRAMOS
	SET "Estado" = 'INA'
	FROM "BSCL.FondoMutuo.BD::FMM.TramoComision" AS	TRAMOS
	INNER JOIN :v_tramos_eliminar AS TramoFinal ON (TramoFinal."IdTramo" = TRAMOS."IdTramo");
	
	p_out_Resultado = SELECT	"IdInterno"
								,"IdComision"
								,"IdSerieFondo"
								,"Descripcion"
								,"TipoComision"
								,"FechaVigenciaDesde"
								,"FechaVigenciaHasta"
								,"PorcentajeLiberado"
								,"Estado"
						FROM :v_comision_final;
	
	p_out_Tramos = SELECT 	"IdInterno"
						 	,"IdTramo"
						 	,"IdComision"
						 	,"DiasDesde"
						 	,"DiasHasta"
						 	,"PorcentajeComision"
					FROM :v_tramos_actualizar
					UNION ALL
					SELECT 	"IdInterno"
						 	,"IdTramo"
						 	,"IdComision"
						 	,"DiasDesde"
						 	,"DiasHasta"
						 	,"PorcentajeComision"
					FROM :v_tramos_nuevos;
	
END;
	
