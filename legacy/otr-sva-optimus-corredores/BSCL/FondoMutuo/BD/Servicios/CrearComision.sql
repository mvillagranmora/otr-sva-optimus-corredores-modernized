SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearComision";

CREATE PROCEDURE
--  Proposito: Crear Comision para alguna Serie
--  Autor: Gat-Blac 
--  Fecha Creacion: 19/07/2016
"BSCL.FondoMutuo.BD.Servicios::CrearComision" (
	IN p_Comision								TABLE 	(
														  	"IdInterno"				BIGINT,
														  	"IdSerieFondo" 			NVARCHAR(40),
														  	"Descripcion"			NVARCHAR(100),
														  	"TipoComision"			NVARCHAR(40),
														  	"FechaVigenciaDesde"	DATE,
														  	"FechaVigenciaHasta"	DATE,
														  	"PorcentajeLiberado"	DECIMAL(26,8),
														  	"Estado"				NVARCHAR(3),
														  	"FechaSistema"			DATE,
														  	"IdEmpresa"				BIGINT
								  						),
	IN p_TramoComision							TABLE 	(
														  	"IdInterno"				BIGINT,
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
	
	v_id_comision = SELECT "IdInterno", "BSCL.FondoMutuo.BD.Secuencias::Comision".NEXTVAL AS "IdComision"
					FROM :p_Comision;
					
	v_id_tramo = SELECT "Tramo"."IdInterno", 
						"BSCL.FondoMutuo.BD.Secuencias::TramoComision".NEXTVAL AS "IdTramo",
						"Tramo"."DiasDesde",
						"Tramo"."DiasHasta",
						"Tramo"."PorcentajeComision",
						"Tramo"."FechaSistema"
				 FROM :p_TramoComision AS "Tramo"
				 INNER JOIN :p_Comision AS "Comision" ON ("Tramo"."IdInterno" = "Comision"."IdInterno");
	
	v_comision_final = SELECT "p_Comision"."IdInterno"
								 ,"Id_Comision"."IdComision"
								 ,"p_Comision"."IdSerieFondo"
								 ,"p_Comision"."Descripcion"
								 ,"p_Comision"."TipoComision"
								 ,"p_Comision"."FechaVigenciaDesde"
								 ,"p_Comision"."FechaVigenciaHasta"
								 ,"p_Comision"."PorcentajeLiberado"
								 ,"p_Comision"."Estado"
								 ,"p_Comision"."FechaSistema"
								 ,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
								 ,"p_Comision"."IdEmpresa"
							FROM :p_Comision AS "p_Comision"
							INNER JOIN :v_id_comision AS "Id_Comision" ON ("p_Comision"."IdInterno" = "Id_Comision"."IdInterno");
	
	v_tramo_final = SELECT "Id_Tramo"."IdInterno"
							,"Id_Tramo"."IdTramo"
							,"Id_Comision"."IdComision"
							,"Id_Tramo"."DiasDesde"
							,"Id_Tramo"."DiasHasta"
							,"Id_Tramo"."PorcentajeComision"
							,'ACT' AS "Estado"
							,"Id_Tramo"."FechaSistema"
							,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
					FROM :v_id_tramo AS "Id_Tramo"
					INNER JOIN :v_comision_final AS "Id_Comision" ON ("Id_Tramo"."IdInterno" = "Id_Comision"."IdInterno");
	
	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.Comision"
	(
		"IdComision", "IdSerieFondo", "Descripcion", "TipoComision", "FechaVigenciaDesde", "FechaVigenciaHasta", "PorcentajeLiberado",
		"Estado", "FechaSistema", "FechaIngresoRegistro", "IdEmpresa"
	)
	SELECT "IdComision", "IdSerieFondo", "Descripcion", "TipoComision", "FechaVigenciaDesde", "FechaVigenciaHasta", "PorcentajeLiberado",
		"Estado", "FechaSistema", "FechaIngresoRegistro", "IdEmpresa"
	FROM :v_comision_final;
	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.TramoComision"
	(
		"IdTramo", "IdComision", "DiasDesde", "DiasHasta", "PorcentajeComision", "Estado", "FechaSistema", "FechaIngresoRegistro"
	)
	SELECT "IdTramo", "IdComision", "DiasDesde", "DiasHasta", "PorcentajeComision", "Estado", "FechaSistema", "FechaIngresoRegistro"
	FROM :v_tramo_final;
	
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
					FROM :v_tramo_final;
	
END;
	
