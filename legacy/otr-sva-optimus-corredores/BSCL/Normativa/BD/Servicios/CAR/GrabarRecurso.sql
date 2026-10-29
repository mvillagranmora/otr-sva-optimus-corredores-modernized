SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GrabarRecurso";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GrabarRecurso" (
	in p_IdEmpresa 	BIGINT,
	in p_IdUsuario 	BIGINT,
	in p_IdModulo 	NVARCHAR(3),
	IN p_Recursos	TABLE(
						"IdRecurso"		BIGINT,
						"IdPlantilla" 	BIGINT,
						"Editable" 		NVARCHAR(2),
						"Descripcion" 	NVARCHAR(100),
						"Grupo" 		NVARCHAR(100),
						"Nombre"	 	NVARCHAR(100),
						"Tipo" 			NVARCHAR(100),
						"Valor" 		nclob,
						"IdEstado"		NVARCHAR(3)
					) /*,
	OUT p_out_id	TABLE (
						"IdRecurso"		BIGINT,
						"Nombre"	 	NVARCHAR(100)
					) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE("Codigo" nvarchar(40), "Descripcion" nvarchar(5000)) DEFAULT EMPTY*/
)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
BEGIN
	
	 /*
	declare salida_controlada condition;
    declare exit handler for salida_controlada
    begin
        select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
        select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
    end;

    declare exit handler for sqlexception
    begin
        declare v_errorExt nvarchar(40);
        if ::sql_error_code > 10000 then
            -- Error pseudo-controlado pero desde un sub-sp.
            call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = select * from :p_out_ErroresDetalle union all
                                   select v_errorExt, ::sql_error_message from dummy;
            select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
        else
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        end if;
    end;

    p_out_AdvertenciasContador 	:= 0;
    p_out_ErroresContador 		:= 0;
	*/
    
    /* Nuevo Recurso */
    v_NuevosRecursos =
	SELECT  "BSCL.Normativa.BD.Secuencias::CAR.Global".NEXTVAL AS "IdRecurso",
			"IdPlantilla",
		    "Editable",
		    "Descripcion",
		    "Grupo",
		    "Nombre",
		    "Tipo",
		    "Valor",
		    "IdEstado"
    FROM :p_Recursos
    WHERE "IdRecurso" = 0;    

    
    INSERT INTO "BSCL.Normativa.BD::CAR.Recurso"(
	    "IdRecurso",
	    "IdPlantilla",
	    "Editable",
	    "Descripcion",
	    "Grupo",
	    "Nombre",
	    "Tipo",
	    "Valor",
	    "IdEstado"
	)
	SELECT  "IdRecurso",
			"IdPlantilla",
		    COALESCE("Editable",''),
		    COALESCE("Descripcion",''),
		    COALESCE("Grupo",''),
		    COALESCE("Nombre",''),
		    COALESCE("Tipo",''),
		    COALESCE("Valor",''),
		    COALESCE("IdEstado",'INA') 
	FROM :v_NuevosRecursos;
    
    
    
    /* Actualizacion Recurso */
	UPDATE R
	SET "IdPlantilla"	= NR."IdPlantilla",
        "Editable" 		= NR."Editable",
        "Descripcion" 	= NR."Descripcion",
        "Grupo" 		= NR."Grupo",
        "Nombre" 		= NR."Nombre",
        "Tipo" 			= NR."Tipo",
        "Valor" 		= NR."Valor"
    FROM :p_Recursos AS NR
    INNER JOIN "BSCL.Normativa.BD::CAR.Recurso" as R ON (R."IdRecurso" = NR."IdRecurso");
    
    
    p_out_id = 
    SELECT "IdRecurso","Nombre" FROM :v_NuevosRecursos UNION ALL
    SELECT "IdRecurso","Nombre" FROM :p_Recursos WHERE "IdRecurso" > 0;
    
END;
GO
COMMIT;