
SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.RegistrarProgreso";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.RegistrarProgreso" (
 	IN p_IdTrabajo BIGINT,
 	IN p_Accion NVARCHAR(200)
)
 LANGUAGE SQLSCRIPT
 SQL SECURITY INVOKER
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
	 DECLARE v_DatosTablaPaso TABLE ("IdTrabajo" BIGINT,
                              "FechaRegistro" LONGDATE,
                              "Servicio" NVARCHAR(200),
                              "Progreso" BIGINT,
                              "JsonData" TEXT,
                              "Estado" NVARCHAR(100),
                              "IdUsuario" BIGINT)
      		DEFAULT SELECT 0 AS "IdTrabajo",CURRENT_UTCTIMESTAMP AS "FechaRegistro",'' AS "Servicio", 0 AS "Progreso", '' AS "JsonData",'' AS "Estado", 0 AS "IdUsuario" FROM DUMMY WHERE 1=2;

     v_DatosTablaPaso =   SELECT 
				"IdTrabajo",
				"FechaRegistro",
				"Servicio",
				"Progreso",
				"JsonData",
				"Estado",
				"IdUsuario"
			FROM "BSCL.Plataforma.BD::PLA.BarraProgresoPaso" WHERE "IdTrabajo" = :p_IdTrabajo;
	 BEGIN AUTONOMOUS TRANSACTION
	 	
	 	
	 	IF :p_Accion = 'CREAR' THEN
	 		INSERT INTO "BSCL.Plataforma.BD::PLA.BarraProgreso"("IdTrabajo", "FechaRegistro","Servicio","Progreso","JsonData","Estado","IdUsuario")
			SELECT 
				"IdTrabajo",
				"FechaRegistro",
				"Servicio",
				"Progreso",
				"JsonData",
				"Estado",
				"IdUsuario"
			FROM :v_DatosTablaPaso WHERE "IdTrabajo" = :p_IdTrabajo;
		END IF;

		IF :p_Accion = 'ACTUALIZAR' THEN
			UPDATE c
			SET
				 c."Progreso" = d."Progreso",
				 c."JsonData" =  d."JsonData",
				 c."Estado" =  d."Estado",
				 c."FechaRegistro" = CURRENT_UTCTIMESTAMP
	        FROM "BSCL.Plataforma.BD::PLA.BarraProgreso" c
	        INNER JOIN :v_DatosTablaPaso AS d ON (d."IdTrabajo" = c."IdTrabajo")
	        WHERE d."IdTrabajo" = :p_IdTrabajo;
		END IF;

	 END;
 END;