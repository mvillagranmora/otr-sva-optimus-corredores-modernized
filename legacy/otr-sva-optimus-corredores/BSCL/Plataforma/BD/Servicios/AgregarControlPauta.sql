SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::AgregarControlPauta";

CREATE PROCEDURE 

--  Proposito		: Agregar registros a tabla control pauta 
--  Autor			: JG - BCS
--  Fecha Creacion	: 06/02/2017

"BSCL.Plataforma.BD.Servicios::AgregarControlPauta"( 
	IN p_IdTrabajo bigint,
	out P_OUT_ERRORESCONTADOR bigint
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	/*DECLARE EXIT Handler FOR SQLEXCEPTION
	BEGIN	
		-- manejo de error
		RESIGNAL;		
	END;*/
	BEGIN -- negocio
		P_OUT_ERRORESCONTADOR := 1;
		v_controlPauta = select 
				"BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval as "IdControlPauta",
				"IdPrograma",
				"IdTipoPrograma",
				"Ejecucion",
				"FechaSistema",
				CURRENT_UTCTIMESTAMP as "FechaCreacion",
				"IdEstado",
				"Mensaje"
			from "BSCL.Plataforma.BD::PAU.AgregarControlPauta_Entrada_Registros"
			where "IdTrabajo" = :p_IdTrabajo;
			
		insert into "BSCL.Plataforma.BD::PAU.ControlPauta"(
			"IdControlPauta",
			"IdPrograma",
			"IdTipoPrograma",
			"Ejecucion",
			"FechaSistema",
			"FechaCreacion",
			"IdEstado",
			"Mensaje")
		select 
			"IdControlPauta",
			"IdPrograma",
			"IdTipoPrograma",
			"Ejecucion",
			"FechaSistema",
			"FechaCreacion",
			"IdEstado",
			"Mensaje"
		from :v_controlPauta;
			
		P_OUT_ERRORESCONTADOR := 0;
	END;
END;
