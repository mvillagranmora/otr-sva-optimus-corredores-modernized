SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.EliminarDetalles";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.EliminarDetalles"
(
	-- Parametros de Entrada Comunes
	IN p_IdEmpresa 		BIGINT,
	IN p_IdUsuario 		BIGINT,
	IN p_IdModulo 		NVARCHAR(3),
	
	
	-- Parametros de Entrada Propios

	-- ID del documento.
    in p_IdDocumento bigint default null,
    
    -- Lista de los ID's de los detalles a eliminar.
	IN p_List			TABLE ("ID"		BIGINT),
	
	-- Parametros de Salida Propios
	OUT p_out_AdvertenciasContador		INT,
	OUT p_out_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),
	
	-- Salidas Errores
	
	OUT p_out_ErroresContador			INT,
	OUT p_out_ErroresDetalle			TABLE ("Codigo" nvarchar(40), "Descripcion" nvarchar(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	
AS
BEGIN
	------------ Variables Propias ----------
	DECLARE v_i INT;
	DECLARE v_arr_ids BIGINT array;
	
	 ------------ Bloque Comun de Inicializacion ----------
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::sql_error_code > 10000 THEN
			-- Error pseudo-controlado pero desde un sub-sp.
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
			p_out_ErroresDetalle = 	SELECT * FROM :p_out_ErroresDetalle UNION ALL
									SELECT v_errorExt, ::sql_error_message FROM dummy;
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		ELSE
			RESIGNAL;
		END IF;
	END;
	
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador := 0;
	p_out_AdvertenciasDetalle =  SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
	p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
	
	--Logica de Actualizacion

	IF p_IdDocumento is not null then 
		DELETE FROM "BSCL.Normativa.BD::NOR.DatosCI1720" WHERE "IdDocumento" = :p_IdDocumento;
	else
		v_arr_ids := array_agg(:p_List."ID" order by "ID" desc);
	    for v_i in 1 .. cardinality(:v_arr_ids) do
	        DELETE FROM "BSCL.Normativa.BD::NOR.DatosCI1720" where "ID" = :v_arr_ids[v_i];
	    end for;
	end if;
END;